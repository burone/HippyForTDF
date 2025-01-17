#include "dom/layer_optimized_render_manager.h"

#include <unordered_set>

#include "dom/node_props.h"

namespace hippy {
inline namespace dom {

LayerOptimizedRenderManager::LayerOptimizedRenderManager(
        std::shared_ptr<RenderManager> render_manager)
        : render_manager_(std::move(render_manager)) {}

void LayerOptimizedRenderManager::CreateRenderNode(std::vector<std::shared_ptr<DomNode>>&& nodes) {
  std::vector<std::shared_ptr<DomNode>> nodes_to_create;
  for (const auto& node : nodes) {
    node->SetLayoutOnly(ComputeLayoutOnly(node));
    if (!CanBeEliminated(node)) {
      UpdateRenderInfo(node);
      nodes_to_create.push_back(node);
    }
  }

  if (!nodes_to_create.empty()) {
    render_manager_->CreateRenderNode(std::move(nodes_to_create));
  }
}

void LayerOptimizedRenderManager::UpdateRenderNode(std::vector<std::shared_ptr<DomNode>>&& nodes) {
  std::vector<std::shared_ptr<DomNode>> nodes_to_create;
  std::vector<std::shared_ptr<DomNode>> nodes_to_update;
  for (const auto& node : nodes) {
    bool could_be_eliminated = CanBeEliminated(node);
    node->SetLayoutOnly(ComputeLayoutOnly(node));
    if (!CanBeEliminated(node)) {
      if (could_be_eliminated) {
        UpdateRenderInfo(node);
        nodes_to_create.push_back(node);
      } else {
        nodes_to_update.push_back(node);
      }
    }
  }

  if (!nodes_to_create.empty()) {
    // step1: create child
    render_manager_->CreateRenderNode(std::vector<std::shared_ptr<DomNode>>(nodes_to_create));
    for (const auto& node : nodes_to_create) {
      // step2: move child
      std::vector<std::shared_ptr<DomNode>> moved_children;
      FindValidChildren(node, moved_children);
      if (!moved_children.empty()) {
        std::vector<int32_t> moved_ids;
        moved_ids.reserve(moved_children.size());
        for (const auto& moved_node : moved_children) {
          moved_ids.push_back(hippy::base::checked_numeric_cast<uint32_t, int32_t>(moved_node->GetId()));
        }
        MoveRenderNode(std::move(moved_ids),
                       hippy::base::checked_numeric_cast<uint32_t, int32_t>(node->GetRenderInfo().pid),
                       hippy::base::checked_numeric_cast<uint32_t, int32_t>(node->GetRenderInfo().id));
      }
    }
  }

  if (!nodes_to_update.empty()) {
    render_manager_->UpdateRenderNode(std::move(nodes_to_update));
  }
}

void LayerOptimizedRenderManager::MoveRenderNode(std::vector<std::shared_ptr<DomNode>> &&nodes) {}

void LayerOptimizedRenderManager::DeleteRenderNode(std::vector<std::shared_ptr<DomNode>>&& nodes) {
  std::vector<std::shared_ptr<DomNode>> nodes_to_delete;
  for (const auto& node : nodes) {
    if (!CanBeEliminated(node)) {
      nodes_to_delete.push_back(node);
    } else {
      FindValidChildren(node, nodes_to_delete);
    }
  }
  if (!nodes_to_delete.empty()) {
    render_manager_->DeleteRenderNode(std::move(nodes_to_delete));
  }
}

void LayerOptimizedRenderManager::UpdateLayout(const std::vector<std::shared_ptr<DomNode>>& nodes) {
  std::vector<std::shared_ptr<DomNode>> nodes_to_update;
  for (const auto& node : nodes) {
    if (!CanBeEliminated(node)) {
      nodes_to_update.push_back(node);
    }
  }
  render_manager_->UpdateLayout(nodes_to_update);
}

void LayerOptimizedRenderManager::MoveRenderNode(std::vector<int32_t>&& moved_ids,
                                                 int32_t from_pid,
                                                 int32_t to_pid) {
  render_manager_->MoveRenderNode(std::move(moved_ids), from_pid, to_pid);
}

void LayerOptimizedRenderManager::EndBatch() {
  render_manager_->EndBatch();
}

void LayerOptimizedRenderManager::BeforeLayout() {
  render_manager_->BeforeLayout();
}

void LayerOptimizedRenderManager::AfterLayout() {
  render_manager_->AfterLayout();
}

void LayerOptimizedRenderManager::AddEventListener(std::weak_ptr<DomNode> dom_node,
                                                   const std::string &name) {
  render_manager_->AddEventListener(dom_node, name);
}

void LayerOptimizedRenderManager::RemoveEventListener(std::weak_ptr<DomNode> dom_node,
                                                      const std::string &name) {
  render_manager_->RemoveEventListener(dom_node, name);
}

void LayerOptimizedRenderManager::CallFunction(
        std::weak_ptr<DomNode> dom_node, const std::string &name,
        const DomArgument &param,
        uint32_t cb_id) {
  render_manager_->CallFunction(dom_node, name, param, cb_id);
}

bool LayerOptimizedRenderManager::ComputeLayoutOnly(const std::shared_ptr<DomNode>& node) const {
  return node->GetViewName() == kTagNameView
         && CheckStyleJustLayout(node)
         && !node->HasEventListeners();
}

bool LayerOptimizedRenderManager::CheckStyleJustLayout(const std::shared_ptr<DomNode>& node) const {
  const auto &style_map = node->GetStyleMap();
  for (const auto &entry : *style_map) {
    const auto &key = entry.first;
    const auto &value = entry.second;

    if (IsJustLayoutProp(key.c_str())) {
      continue;
    }

    if (key == kOpacity) {
      if (value->IsNull() || (value->IsNumber() && value->ToDoubleChecked() == 1)) {
        continue;
      }
    } else if (key == kBorderRadius) {
      const auto &background_color = style_map->find(kBackgroundColor);
      if (background_color != style_map->end() &&
          (*background_color).second->IsNumber() &&
          (*background_color).second->ToDoubleChecked() != 0) {
        return false;
      }
      const auto &border_width = style_map->find(kBorderWidth);
      if (border_width != style_map->end() &&
          (*border_width).second->IsNumber() &&
          (*border_width).second->ToDoubleChecked() != 0) {
        return false;
      }
    } else if (key == kBorderLeftColor) {
      if (value->IsNumber() && value->ToDoubleChecked() == 0) {
        continue;
      }
    } else if (key == kBorderRightColor) {
      if (value->IsNumber() && value->ToDoubleChecked() == 0) {
        continue;
      }
    } else if (key == kBorderTopColor) {
      if (value->IsNumber() && value->ToDoubleChecked() == 0) {
        continue;
      }
    } else if (key == kBorderBottomColor) {
      if (value->IsNumber() && value->ToDoubleChecked() == 0) {
        continue;
      }
    } else if (key == kBorderWidth) {
      if (value->IsNull() || (value->IsNumber() && value->ToDoubleChecked() == 0)) {
        continue;
      }
    } else if (key == kBorderLeftWidth) {
      if (value->IsNull() || (value->IsNumber() && value->ToDoubleChecked() == 0)) {
        continue;
      }
    } else if (key == kBorderTopWidth) {
      if (value->IsNull() || (value->IsNumber() && value->ToDoubleChecked() == 0)) {
        continue;
      }
    } else if (key == kBorderRightWidth) {
      if (value->IsNull() || (value->IsNumber() && value->ToDoubleChecked() == 0)) {
        continue;
      }
    } else if (key == kBorderBottomWidth) {
      if (value->IsNull() || (value->IsNumber() && value->ToDoubleChecked() == 0)) {
        continue;
      }
    }
    return false;
  }
  return true;
}

static constexpr std::array<const char*, 31> kJustLayoutProps = {
        kAilgnSelf, kAlignItems, kFlex, kFlexDirection, kFlexWrap, kJustifyContent,
        // position
        kPosition, kRight, kTop, kBottom, kLeft,
        // dimensions
        kWidth, kHeight, kMinWidth, kMaxWidth, kMinHeight, kMaxHeight,
        // margins
        kMargin, kMarginVertical, kMarginHorizontal,
        kMarginLeft, kMarginRight, kMarginTop, kMarginBottom,
        // paddings
        kPadding, kPaddingVertical, kPaddingHorizontal,
        kPaddingLeft, kPaddingRight, kPaddingTop, kPaddingBottom};

bool LayerOptimizedRenderManager::IsJustLayoutProp(const char *prop_name) const {
  return std::any_of(kJustLayoutProps.begin(), kJustLayoutProps.end(),
                     [prop_name](const char *prop) { return strcmp(prop, prop_name) == 0; });
}

bool LayerOptimizedRenderManager::CanBeEliminated(const std::shared_ptr<DomNode>& node) {
  return node->IsLayoutOnly() || node->IsVirtual();
}

void LayerOptimizedRenderManager::UpdateRenderInfo(const std::shared_ptr<DomNode>& node) {
  DomNode::RenderInfo render_info = node->GetRenderInfo();
  auto render_parent = GetRenderParent(node);
  if (render_parent) {
    int32_t index = CalculateRenderNodeIndex(render_parent, node);
    render_info.pid = render_parent->GetId();
    render_info.index = index;
  }
  node->SetRenderInfo(render_info);
}

std::shared_ptr<DomNode> LayerOptimizedRenderManager::GetRenderParent(
        const std::shared_ptr<DomNode> &node) {
  auto parent = node->GetParent();
  while (parent && CanBeEliminated(parent)) {
    parent = parent->GetParent();
  }
  return parent;
}

int32_t LayerOptimizedRenderManager::CalculateRenderNodeIndex(
        const std::shared_ptr<DomNode> &parent,
        const std::shared_ptr<DomNode> &node) {
  assert(parent != nullptr);
  return CalculateRenderNodeIndex(parent, node, 0).second;
}

std::pair<bool, int32_t>
LayerOptimizedRenderManager::CalculateRenderNodeIndex(const std::shared_ptr<DomNode>& parent,
                                                      const std::shared_ptr<DomNode> &node,
                                                      int32_t index) {
  for (size_t i = 0; i < parent->GetChildCount(); i++) {
    std::shared_ptr<DomNode> child_node = parent->GetChildAt(i);
    if (child_node == node) {
      return std::make_pair(true, index);
    }

    if (CanBeEliminated(child_node)) {
      auto view_index = CalculateRenderNodeIndex(child_node, node, index);
      if (view_index.first) {
        return view_index;
      } else {
        index = view_index.second;
      }
    } else {
      index++;
    }
  }
  return std::make_pair(false, index);
}

void LayerOptimizedRenderManager::FindValidChildren(const std::shared_ptr<DomNode>& node,
                                                    std::vector<std::shared_ptr<DomNode>>& valid_children_nodes) {
  for (size_t i = 0; i < node->GetChildCount(); i++) {
    auto child_node = node->GetChildAt(i);
    if (CanBeEliminated(child_node)) {
      FindValidChildren(child_node, valid_children_nodes);
    } else {
      valid_children_nodes.push_back(child_node);
    }
  }
}

}  // namespace dom
}  // namespace hippy
