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

#import "HippySmartViewPagerViewManager.h"
#import "HippySmartViewPagerView.h"

@implementation HippySmartViewPagerViewManager

HIPPY_EXPORT_VIEW_PROPERTY(scrollEventThrottle, NSTimeInterval)
HIPPY_EXPORT_VIEW_PROPERTY(initialListReady, HippyDirectEventBlock);
HIPPY_EXPORT_VIEW_PROPERTY(onScrollBeginDrag, HippyDirectEventBlock)
HIPPY_EXPORT_VIEW_PROPERTY(onPageSelected, HippyDirectEventBlock)
HIPPY_EXPORT_VIEW_PROPERTY(onScroll, HippyDirectEventBlock)
HIPPY_EXPORT_VIEW_PROPERTY(onPageScrollStateChanged, HippyDirectEventBlock)

HIPPY_EXPORT_VIEW_PROPERTY(pageGap, CGFloat)
HIPPY_EXPORT_VIEW_PROPERTY(previousMargin, CGFloat)
HIPPY_EXPORT_VIEW_PROPERTY(nextMargin, CGFloat)
HIPPY_EXPORT_VIEW_PROPERTY(autoplayTimeInterval, CGFloat)
HIPPY_EXPORT_VIEW_PROPERTY(initialPage, NSInteger)
HIPPY_EXPORT_VIEW_PROPERTY(circular, BOOL)
HIPPY_EXPORT_VIEW_PROPERTY(autoplay, BOOL)

- (UIView *)view {
    return [[HippySmartViewPagerView alloc] init];
}

// clang-format off
RENDER_COMPONENT_EXPORT_METHOD(setPage:(nonnull NSNumber *)hippyTag
        pageNumber:(NSNumber *)pageNumber) {
    [self.renderContext addUIBlock:^(__unused id<HippyRenderContext> renderContext, NSDictionary<NSNumber *, UIView *> *viewRegistry){
        UIView *view = viewRegistry[hippyTag];

        if (view == nil || ![view isKindOfClass:[HippySmartViewPagerView class]]) {
//            HippyLogError(@"tried to setPage: on an error viewPager %@ "
//                        "with tag #%@", view, hippyTag);
        }
        NSInteger pageNumberInteger = pageNumber.integerValue;
        [(HippySmartViewPagerView *)view setPage:pageNumberInteger animated:YES];
    }];
}
// clang-format on

// clang-format off
RENDER_COMPONENT_EXPORT_METHOD(setPageWithoutAnimation:(nonnull NSNumber *)hippyTag
        pageNumber:(NSNumber *)pageNumber) {
    [self.renderContext addUIBlock:^(__unused id<HippyRenderContext> renderContext, NSDictionary<NSNumber *, UIView *> *viewRegistry){
        UIView *view = viewRegistry[hippyTag];
        if (view == nil || ![view isKindOfClass:[HippySmartViewPagerView class]]) {
//            HippyLogError(@"tried to setPage: on an error viewPager %@ "
//                        "with tag #%@", view, hippyTag);
        }
        NSInteger pageNumberInteger = pageNumber.integerValue;
        [(HippySmartViewPagerView *)view setPage:pageNumberInteger animated:NO];
    }];
}
// clang-format on

// clang-format off
RENDER_COMPONENT_EXPORT_METHOD(getPageIndex:(nonnull NSNumber *)hippyTag
                    callback:(RenderUIResponseSenderBlock)callback) {
    [self.renderContext addUIBlock:^(__unused id<HippyRenderContext> renderContext, NSDictionary<NSNumber *,__kindof UIView *> *viewRegistry) {
        HippySmartViewPagerView *view = viewRegistry[hippyTag];
        NSInteger currrentPage = [view getCurrentPage];
        callback(@(currrentPage));
    }];
}
// clang-format on

@end

