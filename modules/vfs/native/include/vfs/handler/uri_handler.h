/*
 * Tencent is pleased to support the open source community by making
 * Hippy available.
 *
 * Copyright (C) 2022 THL A29 Limited, a Tencent company.
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

#include <functional>
#include <memory>
#include <string>
#include <unordered_map>

#include "footstone/string_view.h"
#include "vfs/request_job.h"
#include "vfs/job_response.h"

namespace hippy {
inline namespace vfs {

class UriHandler {
 public:
  using bytes = std::string;
  using string_view = footstone::string_view;
  using RetCode = hippy::JobResponse::RetCode;

  UriHandler() = default;
  virtual ~UriHandler() = default;

  virtual void RequestUntrustedContent(
      std::shared_ptr<RequestJob> request,
      std::shared_ptr<JobResponse> response,
      std::function<std::shared_ptr<UriHandler>()> next) = 0;
  virtual void RequestUntrustedContent(
      std::shared_ptr<RequestJob> request,
      std::function<void(std::shared_ptr<JobResponse>)> cb,
      std::function<std::shared_ptr<UriHandler>()> next) = 0;
};

}
}

