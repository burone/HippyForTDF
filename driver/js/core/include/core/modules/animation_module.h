/*
 *
 * Tencent is pleased to support the open source community by making
 * Hippy available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#pragma once

#include "core/napi/js_native_api_types.h"
#include "dom/animation/cubic_bezier_animation.h"
#include "dom/animation/animation_set.h"

namespace hippy {

std::shared_ptr<hippy::napi::InstanceDefine<CubicBezierAnimation>>
RegisterAnimation(const std::weak_ptr<Scope>& weak_scope);

std::shared_ptr<hippy::napi::InstanceDefine<AnimationSet>>
RegisterAnimationSet(const std::weak_ptr<Scope>& weak_scope);

}

