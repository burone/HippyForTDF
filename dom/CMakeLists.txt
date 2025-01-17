#
# Tencent is pleased to support the open source community by making
# Hippy available.
#
# Copyright (C) 2022 THL A29 Limited, a Tencent company.
# All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.14)

project("dom")

set(LAYOUT_ENGINE "Yoga")

get_filename_component(PROJECT_ROOT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/.." REALPATH)

include("${PROJECT_ROOT_DIR}/buildconfig/cmake/InfraPackagesModule.cmake")
include("${PROJECT_ROOT_DIR}/buildconfig/cmake/compiler_toolchain.cmake")

set(CMAKE_CXX_STANDARD 17)

# region layout engine
if (NOT DEFINED LAYOUT_ENGINE)
  set(LAYOUT_ENGINE "Taitank")
endif()

message("LAYOUT_ENGINE: ${LAYOUT_ENGINE}")
if ("${LAYOUT_ENGINE}" STREQUAL "Yoga")
  InfraPackage_Add(yoga
    REMOTE "dom/third_party/yoga/1.19.0/git-repo.tgz"
    LOCAL "third_party/yoga"
  )
  include_directories(${yoga_SOURCE_DIR}/yoga)
  set(DEPS yogacore)
elseif ("${LAYOUT_ENGINE}" STREQUAL "Taitank")
  add_definitions("-DUSE_TAITANK")
  get_filename_component(TAITANK_LAYOUT_DIR "${PROJECT_ROOT_DIR}/layout" REALPATH)
  add_subdirectory(${TAITANK_LAYOUT_DIR} dom/third_party/taitank)
  include_directories(${TAITANK_LAYOUT_DIR})
  set(DEPS layout)
else()
  message(FATAL_ERROR "Layout Engine ${LAYOUT_ENGINE} is not supported")
endif()
# endregion

# region core
get_filename_component(CORE_DIR "${PROJECT_ROOT_DIR}/driver/js/core/" REALPATH)
include_directories(${CORE_DIR}/include)
include_directories(${CORE_DIR}/third_party/base/include)
# endregion

# region source set
set(SOURCE_SET
  src/dom/animation/animation.cc
  src/dom/animation/animation_manager.cc
  src/dom/animation/animation_math.cc
  src/dom/animation/animation_set.cc
  src/dom/animation/cubic_bezier_animation.cc
  src/dom/deserializer.cc
  src/dom/diff_utils.cc
  src/dom/dom_argument.cc
  src/dom/dom_event.cc
  src/dom/dom_listener.cc
  src/dom/dom_manager.cc
  src/dom/dom_node.cc
  src/dom/dom_value.cc
  src/dom/layer_optimized_render_manager.cc
  src/dom/layout_node.cc
  src/dom/root_node.cc
  src/dom/scene.cc
  src/dom/scene_builder.cc
  src/dom/serializer.cc)
if (${LAYOUT_ENGINE} STREQUAL "Yoga")
  list(APPEND SOURCE_SET src/dom/yoga_layout_node.cc)
elseif(${LAYOUT_ENGINE} STREQUAL "Taitank")
  list(APPEND SOURCE_SET src/dom/taitank_layout_node.cc)
endif()
message("SOURCE_SET: ${SOURCE_SET}")
# endregion

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)
add_library(${PROJECT_NAME} STATIC ${SOURCE_SET})
target_compile_options(${PROJECT_NAME} PRIVATE ${COMPILE_OPTIONS})
target_link_libraries(${PROJECT_NAME} ${DEPS})
