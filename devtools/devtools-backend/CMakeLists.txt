#[[
  Tencent is pleased to support the open source community by making
  Hippy available.

  Copyright (C) 2017-2019 THL A29 Limited, a Tencent company.
  All rights reserved.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
]]

cmake_minimum_required(VERSION 3.14)

project("devtools_backend")

get_filename_component(PROJECT_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../.." REALPATH)

include("${PROJECT_ROOT_DIR}/buildconfig/cmake/InfraPackagesModule.cmake")
include("${PROJECT_ROOT_DIR}/buildconfig/cmake/compiler_toolchain.cmake")

set(CMAKE_VERBOSE_MAKEFILE on)
set(CMAKE_CXX_STANDARD 17)


# region asio
InfraPackage_Add(asio
  REMOTE "devtools/backend/third_party/asio/1.22.1/asio.tar.gz"
  LOCAL "third_party/asio"
)
# no rtti
add_definitions("-DASIO_NO_TYPEID")

# no exception
add_definitions("-DASIO_NO_EXCEPTIONS")
add_definitions("-DASIO_DISABLE_ALIGNOF")

include_directories(${asio_SOURCE_DIR}/asio/include)
# endregion

# region websocketpp
InfraPackage_Add(websocketpp
  REMOTE "devtools/backend/third_party/websocketpp/develop/git-repo.tgz"
  LOCAL "third_party/websocketpp"
)

# no exception
add_definitions("-D_WEBSOCKETPP_NO_EXCEPTIONS_")

include_directories(${websocketpp_SOURCE_DIR})
# endregion

# region nlohmann_json
InfraPackage_Add(json
  REMOTE "devtools/backend/third_party/json/3.10.5/json.tar.xz"
  LOCAL "third_party/json"
)

# no exception
add_definitions("-DJSON_NOEXCEPTION")
# endregion

set(SOURCE_SET
        src/api/adapter/data/domain_metas.cc
        src/api/adapter/data/dom_node_location.cc
        src/api/adapter/data/dom_node_metas.cc
        src/api/adapter/data/frame_timing_metas.cc
        src/api/adapter/data/memory_metas.cc
        src/api/adapter/data/render_diagnostic_metas.cc
        src/api/adapter/data/render_node_metas.cc
        src/api/adapter/data/trace_event_metas.cc
        src/api/devtools_backend_service.cc
        src/api/notification/data/devtools_http_loading_finished.cc
        src/api/notification/data/devtools_http_request.cc
        src/api/notification/data/devtools_http_response.cc
        src/api/notification/default/default_log_notification.cc
        src/api/notification/default/default_network_notification.cc
        src/api/notification/default/default_runtime_notification.cc
        src/module/domain/base_domain.cc
        src/module/domain/css_domain.cc
        src/module/domain/dom_domain.cc
        src/module/domain/network_domain.cc
        src/module/domain/page_domain.cc
        src/module/domain/tdf_common_protocol_domain.cc
        src/module/domain/tdf_inspector_domain.cc
        src/module/domain/tdf_memory_domain.cc
        src/module/domain/tdf_performance_domain.cc
        src/module/domain/tdf_runtime_domain.cc
        src/module/domain_dispatch.cc
        src/module/model/css_model.cc
        src/module/model/dom_model.cc
        src/module/model/frame_poll_model.cc
        src/module/model/screen_shot_model.cc
        src/module/model/screen_shot_response.cc
        src/module/request/css_edit_style_texts_request.cc
        src/module/request/css_node_data_request.cc
        src/module/request/dom_node_data_request.cc
        src/module/request/dom_node_for_location_request.cc
        src/module/request/network_response_body_request.cc
        src/module/request/screen_shot_request.cc
        src/module/request/selected_render_object_request.cc
        src/tunnel/net_channel.cc
        src/tunnel/tcp/frame_codec.cc
        src/tunnel/tcp/tcp_channel.cc
        src/tunnel/tunnel_service.cc
        src/tunnel/ws/web_socket_channel.cc
        # TODO: remove `devtools_base` from src when `base` library readly
        src/devtools_base/common/base64.cc
        src/devtools_base/common/log_settings.cc
        src/devtools_base/common/log_settings_state.cc
        src/devtools_base/common/platform/adr/CMakeLists.txt
        src/devtools_base/common/platform/adr/logging.cc
        src/devtools_base/common/platform/adr/thread.cc
        src/devtools_base/common/platform/ios/CMakeLists.txt
        src/devtools_base/common/platform/ios/logging.cc
        src/devtools_base/common/platform/ios/thread.cc
        src/devtools_base/common/platform/linux/CMakeLists.txt
        src/devtools_base/common/platform/linux/logging.cc
        src/devtools_base/common/platform/linux/thread.cc
        src/devtools_base/common/platform/mac/CMakeLists.txt
        src/devtools_base/common/platform/mac/logging.cc
        src/devtools_base/common/platform/mac/thread.cc
        src/devtools_base/common/platform/win/CMakeLists.txt
        src/devtools_base/common/platform/win/logging.cc
        src/devtools_base/common/platform/win/thread.cc
        src/devtools_base/common/task.cc
        src/devtools_base/common/task_runner.cc
        src/devtools_base/common/unicode_string_view.cc
        src/devtools_base/common/worker.cc
        src/devtools_base/common/worker_pool.cc
        src/devtools_base/logging.cc
        src/devtools_base/socket.cc
        src/devtools_base/tdf_base_util.cc
        src/devtools_base/tdf_string_util.cc)

if(IOS)
  add_definitions("-DIOS")
endif()

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)
add_library(${PROJECT_NAME} ${SOURCE_SET})
target_compile_options(${PROJECT_NAME} PRIVATE ${COMPILE_OPTIONS})
target_link_libraries(${PROJECT_NAME} PUBLIC nlohmann_json::nlohmann_json)
