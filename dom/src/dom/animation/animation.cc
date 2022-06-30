#include "dom/animation/animation.h"

#include "core/base/base_time.h"
#include "base/logging.h"
#include "dom/animation/animation_manager.h"
#include "dom/dom_manager.h"

namespace hippy {
inline namespace animation {

constexpr int32_t kLoopCnt = -1;

std::atomic<uint32_t> Animation::animation_id_{1};

Animation::Animation(int32_t cnt,
                     uint64_t delay,
                     uint64_t last_begin_time,
                     uint64_t duration,
                     uint64_t exec_time,
                     double start_value,
                     AnimationStartCb on_start,
                     AnimationEndCb on_end,
                     AnimationCancelCb on_cancel,
                     AnimationRepeatCb on_repeat,
                     uint32_t parent_id,
                     std::shared_ptr<std::vector<std::shared_ptr<Animation>>> children,
                     Status status,
                     std::weak_ptr<hippy::AnimationManager> animation_manager)
    : id_(animation_id_.fetch_add(1)),
      cnt_(cnt),
      delay_(delay),
      last_begin_time_(last_begin_time),
      duration_(duration),
      exec_time_(exec_time),
      start_value_(start_value),
      on_start_(on_start),
      on_end_(on_end),
      on_cancel_(on_cancel),
      on_repeat_(on_repeat),
      parent_id_(parent_id),
      children_(children),
      status_(status),
      animation_manager_(animation_manager) {}

Animation::Animation(int32_t cnt, uint64_t delay, uint64_t duration, double start_value) :
    Animation(
        cnt,
        delay,
        hippy::base::MonotonicallyIncreasingTime(),
        duration,
        0,
        start_value,
        nullptr,
        nullptr,
        nullptr,
        nullptr,
        kInvalidAnimationParentId,
        nullptr,
        Status::kCreated,
        {}) {}

Animation::Animation(int32_t cnt) : Animation(cnt,
                                              0,
                                              hippy::base::MonotonicallyIncreasingTime(),
                                              0,
                                              0,
                                              0,
                                              nullptr,
                                              nullptr,
                                              nullptr,
                                              nullptr,
                                              kInvalidAnimationParentId,
                                              std::make_shared<
                                                  std::vector<std::shared_ptr<Animation>>>(),
                                              Status::kCreated,
                                              {}) {
}

Animation::Animation() : Animation(0, 0, 0, 0) {}

Animation::~Animation() {}

double Animation::Calculate(uint64_t time) {
  return start_value_;
}

void Animation::AddEventListener(const std::string& event, AnimationCb cb) {
  if (event == kAnimationStartKey) {
    on_start_ = std::move(cb);
  } else if (event == kAnimationEndKey) {
    on_end_ = std::move(cb);
  } else if (event == kAnimationCancelKey) {
    on_cancel_ = std::move(cb);
  } else if (event == kAnimationRepeatKey) {
    on_repeat_ = std::move(cb);
  } else {
    TDF_BASE_DLOG(WARNING) << "event error, event = " << event;
  }
}

void Animation::RemoveEventListener(const std::string& event) {
  if (event == kAnimationStartKey) {
    on_start_ = nullptr;
  } else if (event == kAnimationEndKey) {
    on_end_ = nullptr;
  } else if (event == kAnimationCancelKey) {
    on_cancel_ = nullptr;
  } else if (event == kAnimationRepeatKey) {
    on_repeat_ = nullptr;
  } else {
    TDF_BASE_DLOG(WARNING) << "event error, event = " << event;
  }
}

void Animation::Start() {
  auto animation_manager = animation_manager_.lock();
  auto animation = animation_manager->GetAnimation(id_);
  if (!animation) {
    return;
  }
  auto status = animation->GetStatus();
  switch (status) {
    case Animation::Status::kCreated: {
      animation->SetStatus(Animation::Status::kStart);
      break;
    }
    case Animation::Status::kStart:
    case Animation::Status::kRunning:
    case Animation::Status::kPause:
    case Animation::Status::kResume:
    case Animation::Status::kEnd:
    case Animation::Status::kDestroy:
    default: {
      return;
    }
  }
  auto now = hippy::base::MonotonicallyIncreasingTime();
  last_begin_time_ = now;
  if (delay_ == 0) {
    if (HasChildren()) {
      for (auto& child: *children_) {
        child->SetExecTime(delay_);
      }
    }
    animation_manager->AddActiveAnimation(animation);
    if (on_start_) {
      on_start_();
    }
  } else {
    std::weak_ptr<Animation> weak_animation = animation;
    std::weak_ptr<AnimationManager> weak_animation_manager = animation_manager;
    auto dom_manager = animation_manager->GetDomManager().lock();
    if (!dom_manager) {
      return;
    }
    std::vector<std::function<void()>> ops = {[weak_animation, weak_animation_manager] {
      auto animation = weak_animation.lock();
      if (!animation) {
        return;
      }
      auto animation_manager = weak_animation_manager.lock();
      if (!animation_manager) {
        return;
      }
      auto dom_manager = animation_manager->GetDomManager().lock();
      if (!dom_manager) {
        return;
      }

      auto now = hippy::base::MonotonicallyIncreasingTime();
      auto delay = animation->GetDelay();
      animation->SetExecTime(delay); // reduce latency impact of task_runner
      animation->SetLastBeginTime(now);
      animation_manager->RemoveDelayedAnimationRecord(animation->GetId());
      if (animation->HasChildren()) {
        for (auto& child: *animation->GetChildren()) {
          child->SetExecTime(delay);
        }
      }
      animation_manager->AddActiveAnimation(animation);
      auto on_start = animation->GetAnimationStartCb();
      if (on_start) {
        on_start();
      }
    }};
    auto task = dom_manager->PostDelayedTask(Scene(std::move(ops)), delay_);
    animation_manager->AddDelayedAnimationRecord(id_, task);
  }
}

void Animation::Run(uint64_t now, AnimationOnRun on_run) {
  switch (status_) {
    case Animation::Status::kResume: {
      status_ = Animation::Status::kRunning;
      break;
    }
    case Animation::Status::kStart: {
      status_ = Animation::Status::kRunning;
      if (on_start_) {
        on_start_();
      }
      break;
    }
    case Animation::Status::kRunning: {
      break;
    }
    case Animation::Status::kCreated:
    case Animation::Status::kPause:
    case Animation::Status::kEnd:
    case Animation::Status::kDestroy:
    default:TDF_BASE_UNREACHABLE();
  }

  if (HasChildren()) {
    for (auto& child: *children_) {
      child->SetStatus(Animation::Status::kRunning);
      child->SetLastBeginTime(last_begin_time_);
      auto exec_time = child->GetExecTime();
      auto delay = child->GetDelay();
      auto duration = child->GetDuration();
      if (exec_time >= delay && exec_time < delay + duration) {
        if (!child->HasChildren()) {
          if (on_run) {
            on_run(child->Calculate(now));
          }
        }
      } else if (exec_time < delay) {
        child->SetExecTime(exec_time_);
      }
    }
    exec_time_ += now - (last_begin_time_);
    last_begin_time_ = now;
  } else {
    if (on_run) {
      on_run(Calculate(now));
    }
  }

  if (exec_time_ >= delay_ + duration_) {
    status_ = Animation::Status::kEnd;
    auto animation_manager = animation_manager_.lock();
    if (animation_manager) {
      animation_manager->RemoveActiveAnimation(id_);
    }
    if (on_end_) {
      on_end_();
    }
    if (cnt_ > 0 || cnt_ == hippy::kLoopCnt) {
      Repeat(now);
    }
  }
}

void Animation::Destroy() {
  auto animation_manager = animation_manager_.lock();
  if (!animation_manager) {
    return;
  }
  auto dom_manager = animation_manager->GetDomManager().lock();
  if (!dom_manager) {
    return;
  }
  auto animation = animation_manager->GetAnimation(id_);
  if (!animation) {
    return;
  }
  auto status = animation->GetStatus();
  switch (status) {
    case Animation::Status::kCreated:
    case Animation::Status::kStart:
    case Animation::Status::kRunning:
    case Animation::Status::kPause:
    case Animation::Status::kResume:
    case Animation::Status::kEnd:animation->SetStatus(Animation::Status::kDestroy);
      break;
    case Animation::Status::kDestroy:
    default:return;
  }
  animation_manager->RemoveActiveAnimation(id_);
  animation_manager->RemoveAnimation(animation);
  if (status == Animation::Status::kRunning) {
    auto on_cancel = animation->GetAnimationCancelCb();
    if (on_cancel) {
      auto task_runner = dom_manager->GetDelegateTaskRunner().lock();
      if (task_runner) {
        auto task = std::make_shared<CommonTask>();
        task->func_ = [on_cancel = std::move(on_cancel)]() {
          on_cancel();
        };
        task_runner->PostTask(std::move(task));
      }
    }
  }
}

void Animation::Pause() {
  auto animation_manager = animation_manager_.lock();
  if (!animation_manager) {
    return;
  }
  auto animation = animation_manager->GetAnimation(id_);
  if (!animation) {
    return;
  }
  auto status = animation->GetStatus();
  switch (status) {
    case Animation::Status::kStart:
    case Animation::Status::kRunning:
    case Animation::Status::kResume:animation->SetStatus(Animation::Status::kPause);
      break;
    case Animation::Status::kCreated:
    case Animation::Status::kPause:
    case Animation::Status::kEnd:
    case Animation::Status::kDestroy:
    default:return;
  }
  animation_manager->RemoveActiveAnimation(id_);
  animation_manager->CancelDelayedAnimation(id_);
  animation_manager->RemoveDelayedAnimationRecord(id_);
  auto now = hippy::base::MonotonicallyIncreasingTime();
  exec_time_ += (now - last_begin_time_);
  last_begin_time_ = now;
}

void Animation::Resume() {
  auto animation_manager = animation_manager_.lock();
  if (!animation_manager) {
    return;
  }
  auto dom_manager = animation_manager->GetDomManager().lock();
  if (!dom_manager) {
    return;
  }
  auto animation = animation_manager->GetAnimation(id_);
  if (!animation) {
    return;
  }
  auto status = animation->GetStatus();
  switch (status) {
    case Animation::Status::kPause:animation->SetStatus(Animation::Status::kResume);
      break;
    case Animation::Status::kCreated:
    case Animation::Status::kStart:
    case Animation::Status::kRunning:
    case Animation::Status::kResume:
    case Animation::Status::kEnd:
    case Animation::Status::kDestroy:
    default:return;
  }
  auto exec_time = animation->GetExecTime();
  auto delay = animation->GetDelay();
  auto duration = animation->GetDuration();
  if (exec_time < delay) {
    auto interval = delay - exec_time;
    std::weak_ptr<Animation> weak_animation = animation;
    std::weak_ptr<AnimationManager> weak_animation_manager = animation_manager;
    std::vector<std::function<void()>> ops = {[weak_animation, weak_animation_manager] {
      auto animation = weak_animation.lock();
      if (!animation) {
        return;
      }
      auto animation_manager = weak_animation_manager.lock();
      if (!animation_manager) {
        return;
      }
      animation_manager->RemoveDelayedAnimationRecord(animation->GetId());
      auto now = hippy::base::MonotonicallyIncreasingTime();
      auto delay = animation->GetDelay();
      animation->SetExecTime(delay);
      animation->SetLastBeginTime(now);
      animation_manager->AddActiveAnimation(animation);
    }};
    auto task = dom_manager->PostDelayedTask(Scene(std::move(ops)), interval);
    animation_manager->AddDelayedAnimationRecord(id_, task);
  } else if (exec_time >= delay && exec_time < delay + duration) {
    auto now = hippy::base::MonotonicallyIncreasingTime();
    last_begin_time_ = now;
    animation_manager->AddActiveAnimation(animation);
  }
}

void Animation::Repeat(uint64_t now) {
  last_begin_time_ = now;
  exec_time_ = 0;
  status_ = Animation::Status::kCreated;
  if (cnt_ > 1) {
    cnt_ -= 1;
    if (on_repeat_) {
      on_repeat_();
    }
  } else if (cnt_ == kLoopCnt) {
    if (on_repeat_) {
      on_repeat_();
    }
  } else { // animation is done
    cnt_ -= 1;
    return;
  }
  auto animation_manager = animation_manager_.lock();
  if (!animation_manager) {
    return;
  }
  auto self = animation_manager->GetAnimation(id_);
  if (delay_ == 0) {
    self->SetExecTime(0);
    self->SetLastBeginTime(now);
    if (self->HasChildren()) {
      for (auto& child: *self->GetChildren()) {
        child->SetExecTime(self->GetDelay());
      }
    }
    animation_manager->AddActiveAnimation(self);
  } else {
    auto dom_manager = animation_manager->GetDomManager().lock();
    if (!dom_manager) {
      return;
    }
    std::weak_ptr<Animation> weak_animation = self;
    auto weak_dom_manager = dom_manager;
    std::weak_ptr<AnimationManager> weak_animation_manager = animation_manager;
    std::vector<std::function<void()>> ops = {[weak_animation, weak_animation_manager] {
      auto animation = weak_animation.lock();
      if (!animation) {
        return;
      }
      auto animation_manager = weak_animation_manager.lock();
      if (!animation_manager) {
        return;
      }
      auto now = hippy::base::MonotonicallyIncreasingTime();
      auto delay = animation->GetDelay();
      animation->SetExecTime(delay);
      animation->SetLastBeginTime(now);
      animation_manager->RemoveDelayedAnimationRecord(animation->GetId());
      if (animation->HasChildren()) {
        for (auto& child: *animation->GetChildren()) {
          child->SetExecTime(delay);
        }
      }
      animation_manager->AddActiveAnimation(animation);
    }};
    auto task = dom_manager->PostDelayedTask(Scene(std::move(ops)), delay_);
    animation_manager->AddDelayedAnimationRecord(id_, task);
    status_ = Animation::Status::kStart;
  }
}

}
}
