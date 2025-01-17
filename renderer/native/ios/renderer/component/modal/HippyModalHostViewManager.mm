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

#import "HippyModalHostViewManager.h"
#import "HippyModalHostViewController.h"
#import "HippyShadowView.h"
#import "HippyUtils.h"
#import "HippyModalTransitioningDelegate.h"

@interface HippyModalHostShadowView : HippyShadowView

@end

@implementation HippyModalHostShadowView

- (void)setDomManager:(const std::weak_ptr<hippy::DomManager>)domManager {
    [super setDomManager:domManager];
    CGRect frame = { .origin = CGPointZero, .size = HippyScreenSize() };
    [self setLayoutFrame:frame];
}

@end

@implementation HippyModalHostViewManager

HIPPY_EXPORT_VIEW_PROPERTY(animationType, NSString)
HIPPY_EXPORT_VIEW_PROPERTY(transparent, BOOL)
HIPPY_EXPORT_VIEW_PROPERTY(darkStatusBarText, BOOL)
HIPPY_EXPORT_VIEW_PROPERTY(onShow, HippyDirectEventBlock)
HIPPY_EXPORT_VIEW_PROPERTY(onRequestClose, HippyDirectEventBlock)
HIPPY_EXPORT_VIEW_PROPERTY(supportedOrientations, NSArray)
HIPPY_EXPORT_VIEW_PROPERTY(onOrientationChange, HippyDirectEventBlock)
HIPPY_EXPORT_VIEW_PROPERTY(primaryKey, NSString)
HIPPY_EXPORT_VIEW_PROPERTY(hideStatusBar, NSNumber)

- (UIView *)view {
    HippyModalHostView *view = [[HippyModalHostView alloc] init];
    view.delegate = self.transitioningDelegate;
    if (!_hostViews) {
        _hostViews = [NSHashTable weakObjectsHashTable];
    }
    [_hostViews addObject:view];
    return view;
}

- (id<HippyModalHostViewInteractor, UIViewControllerTransitioningDelegate>)transitioningDelegate {
    if (!_transitioningDelegate) {
        _transitioningDelegate = [HippyModalTransitioningDelegate new];
    }
    return _transitioningDelegate;
}

- (HippyShadowView *)shadowView {
    return [HippyModalHostShadowView new];
}

@end
