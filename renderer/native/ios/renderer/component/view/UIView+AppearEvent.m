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

#import "UIView+AppearEvent.h"
#import "objc/runtime.h"

@implementation UIView (AppearEvent)

#define LifeCycleEvent(setter, getter)                                                      \
    - (void)setter:(HippyDirectEventBlock)getter {                                          \
        objc_setAssociatedObject(self, @selector(getter), getter, OBJC_ASSOCIATION_COPY);   \
    }                                                                                       \
                                                                                            \
    - (HippyDirectEventBlock)getter {                                                       \
        return objc_getAssociatedObject(self, @selector(getter));                           \
    }

LifeCycleEvent(setOnAppear, onAppear)
LifeCycleEvent(setOnDisappear, onDisappear)
LifeCycleEvent(setOnWillAppear, onWillAppear)
LifeCycleEvent(setOnWillDisappear, onWillDisappear)
LifeCycleEvent(setOnDidMount, onDidMount)
LifeCycleEvent(setOnDidUnmount, onDidUnmount)

- (void)viewAppearEvent {
    if (self.onAppear) {
        self.onAppear(@{});
    }
}

- (void)viewDisappearEvent {
    if (self.onDisappear) {
        self.onDisappear(@{});
    }
}

- (void)viewWillAppearEvent {
    if (self.onWillAppear) {
        self.onWillAppear(@{});
    }
}

- (void)viewWillDisappearEvent {
    if (self.onWillDisappear) {
        self.onWillDisappear(@{});
    }
}

- (void)viewDidMountEvent {
    if (self.onDidMount) {
        self.onDidMount(@{});
    }
}
- (void)viewDidUnmoundEvent {
    if (self.onDidUnmount) {
        self.onDidUnmount(@{});
    }
}
@end
