/*!
 * iOS SDK
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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "HippyNavigatorViewManager.h"

@interface HippyNavigatorViewManager ()

@end

@implementation HippyNavigatorViewManager

- (UIView *)view {
    HippyNavigatorHostView *hostView = [[HippyNavigatorHostView alloc] initWithProps:self.props];
    hostView.delegate = self;
    return hostView;
}

// clang-format off
RENDER_COMPONENT_EXPORT_METHOD(push:(NSNumber *__nonnull)hippyTag parms:(NSDictionary *__nonnull)params) {
    [self.renderContext addUIBlock:^(id<HippyRenderContext> renderContext, NSDictionary<NSNumber *,__kindof UIView *> *viewRegistry) {
        HippyNavigatorHostView *navigatorHostView = viewRegistry[hippyTag];
        [navigatorHostView push:params];
    }];
}
// clang-format on

// clang-format off
RENDER_COMPONENT_EXPORT_METHOD(pop:(NSNumber *__nonnull)hippyTag parms:(NSDictionary *__nonnull)params) {
    [self.renderContext addUIBlock:^(id<HippyRenderContext> renderContext, NSDictionary<NSNumber *,__kindof UIView *> *viewRegistry) {
        HippyNavigatorHostView *navigatorHostView = viewRegistry[hippyTag];
        [navigatorHostView pop:params];
    }];
}
// clang-format on
@end
