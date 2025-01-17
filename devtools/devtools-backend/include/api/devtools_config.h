/*
 * Tencent is pleased to support the open source community by making
 * Hippy available.
 *
 * Copyright (C) 2017-2019 THL A29 Limited, a Tencent company.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma once
#include <memory>
#include <string>

namespace hippy::devtools {

/**
 * @brief framework type
 */
enum class Framework {
  kHippy,
  kVl,
  kTdf
};

/**
 * @brief tunnel channel type
 */
enum Tunnel {
  kWebSocket,  // Websocket channel, supporting WLAN / public network, or Android wired
  kTcp,  // Wired TCP channel. Compared with WS, you can use chrome to debug JSC (console, source, memory) for IOS
  kInterface  // CDP protocol interface calling mode, and the business party builds a debugging channel
};

/**
 * devtools config sets
 */
struct DevtoolsConfig {
  Framework framework = Framework::kHippy;

  Tunnel tunnel = Tunnel::kTcp;

  std::string ws_url;
};
}  // namespace hippy::devtools
