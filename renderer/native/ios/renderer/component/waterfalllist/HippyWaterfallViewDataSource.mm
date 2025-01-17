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

#import "HippyWaterfallViewDataSource.h"
#import "HippyShadowView.h"
#import <UIKit/NSIndexPath+UIKitAdditions.h>

@interface HippyWaterfallViewDataSource () {
    BOOL _containBannerView;
    NSArray<HippyShadowView *> *_cellShadowViews;
    HippyShadowView *_bannerView;
}

@end

@implementation HippyWaterfallViewDataSource

- (instancetype)init {
    self = [super init];
    if (self) {
    }
    return self;
}

- (void)setDataSource:(NSArray<HippyShadowView *> *)dataSource {
    [self setDataSource:dataSource containBannerView:NO];
}

- (void)setDataSource:(NSArray<HippyShadowView *> *)dataSource containBannerView:(BOOL)containBannerView {
    _containBannerView = containBannerView;
    if ([dataSource count] > 0) {
        if (containBannerView) {
            _bannerView = [dataSource firstObject];
        }
        NSArray<HippyShadowView *> *candidateCellShadowViews = [dataSource subarrayWithRange:NSMakeRange(1, [dataSource count] - 1)];
        NSString *viewName = self.itemViewName;
        static dispatch_once_t onceToken;
        static NSPredicate *prediate = nil;
        dispatch_once(&onceToken, ^{
            prediate = [NSPredicate predicateWithBlock:^BOOL(id  _Nullable evaluatedObject, NSDictionary<NSString *,id> * _Nullable bindings) {
                HippyShadowView *shadowView = (HippyShadowView *)evaluatedObject;
                if ([shadowView.viewName isEqualToString:viewName]) {
                    return YES;
                }
                return NO;
            }];
        });
        _cellShadowViews = [candidateCellShadowViews filteredArrayUsingPredicate:prediate];
    }
}

-(HippyShadowView *)bannerView {
    return _bannerView;
}

- (HippyShadowView *)cellForIndexPath:(NSIndexPath *)indexPath {
    if (_containBannerView && 0 == [indexPath section]) {
        return _bannerView;
    }
    else {
        return [_cellShadowViews objectAtIndex:[indexPath row]];
    }
}

- (HippyShadowView *)headerForSection:(NSInteger)section {
    return nil;
}

- (NSInteger)numberOfSection {
    return _containBannerView ? 2  : 1;
}

- (NSInteger)numberOfCellForSection:(NSInteger)section {
    if (_containBannerView) {
        return 0 == section ? 1 : [_cellShadowViews count];
    }
    else {
        return [_cellShadowViews count];
    }
}

- (NSIndexPath *)indexPathOfCell:(HippyShadowView *)cell {
    NSInteger row = 0;
    NSInteger section = 0;
    if (_containBannerView) {
        if (_bannerView != cell) {
            section = 1;
            row =  [_cellShadowViews indexOfObject:cell];
        }
    }
    else {
        row =  [_cellShadowViews indexOfObject:cell];
    }
    return [NSIndexPath indexPathForRow:row inSection:section];
}

- (NSIndexPath *)indexPathForFlatIndex:(NSInteger)index {
    NSInteger row = 0;
    NSInteger section = 0;
    if (_containBannerView) {
        if (0 != index) {
            section = 1;
            index -= 1;
        }
    }
    else {
        row = index;
    }
    return [NSIndexPath indexPathForRow:row inSection:section];
}

- (NSInteger)flatIndexForIndexPath:(NSIndexPath *)indexPath {
    NSInteger row = [indexPath row];
    NSInteger section = [indexPath section];
    NSInteger index = 0;
    if (_containBannerView) {
        if (0 != section) {
            index = row + 1;
        }
    }
    else {
        index = row;
    }
    return index;
}

@end
