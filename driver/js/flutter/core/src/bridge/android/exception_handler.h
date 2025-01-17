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

#include <iostream>
#include <sstream>

#include "core/napi/v8/js_native_api_v8.h"
#include "core/runtime/v8/runtime.h"
#include "voltron_bridge.h"

namespace voltron {

class ExceptionHandler {
 public:
  using unicode_string_view = tdf::base::unicode_string_view;
  using StringViewUtils = hippy::base::StringViewUtils;

  ExceptionHandler() = default;
  ~ExceptionHandler() = default;
  static void ReportJsException(const std::shared_ptr<Runtime> &runtime,
                                const unicode_string_view &desc,
                                const unicode_string_view &stack);

  static void HandleUncaughtJsError(v8::Local<v8::Message> message, v8::Local<v8::Value> error);
};
}
