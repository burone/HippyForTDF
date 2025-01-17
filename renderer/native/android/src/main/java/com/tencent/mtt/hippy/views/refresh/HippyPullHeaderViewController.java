/* Tencent is pleased to support the open source community by making Hippy available.
 * Copyright (C) 2018 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tencent.mtt.hippy.views.refresh;

import android.content.Context;
import android.view.View;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tencent.mtt.hippy.annotation.HippyController;
import com.tencent.mtt.hippy.common.HippyArray;
import com.tencent.mtt.hippy.uimanager.ControllerManager;
import com.tencent.mtt.hippy.uimanager.HippyViewController;
import com.tencent.mtt.hippy.uimanager.PullHeaderRenderNode;
import com.tencent.mtt.hippy.uimanager.RenderNode;
import com.tencent.mtt.hippy.utils.LogUtils;
import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerView;
import com.tencent.mtt.hippy.views.hippylist.PullHeaderRefreshHelper;
import com.tencent.mtt.hippy.views.list.HippyListView;

import java.util.List;
import java.util.Map;

@HippyController(name = HippyPullHeaderViewController.CLASS_NAME, isLazyLoad = true, useSystemStandardType = true)
public class HippyPullHeaderViewController extends HippyViewController<HippyPullHeaderView> {

    private static final String TAG = "HippyPullHeaderViewController";
    public static final String CLASS_NAME = "PullHeaderView";
    private static final String COLLAPSE_PULL_HEADER = "collapsePullHeader";
    private static final String EXPAND_PULL_HEADER = "expandPullHeader";

    @Override
    protected View createViewImpl(Context context) {
        return new HippyPullHeaderView(context);
    }

    @Override
    public RenderNode createRenderNode(int id, @Nullable Map<String, Object> props,
            @NonNull String className, @NonNull ViewGroup hippyRootView,
            @NonNull ControllerManager controllerManager, boolean isLazyLoad) {
        return new PullHeaderRenderNode(id, props, className, hippyRootView, controllerManager,
                isLazyLoad);
    }

    @Override
    public void onViewDestroy(HippyPullHeaderView pullHeaderView) {
        pullHeaderView.onDestroy();
    }

    private void execListViewFunction(@NonNull HippyListView listView,
            @NonNull String functionName) {
        switch (functionName) {
            case COLLAPSE_PULL_HEADER: {
                listView.onHeaderRefreshFinish();
                break;
            }
            case EXPAND_PULL_HEADER: {
                listView.onHeaderRefresh();
                break;
            }
            default: {
                LogUtils.w(TAG, "Unknown function name: " + functionName);
            }
        }
    }

    private void execRecyclerViewFunction(@NonNull HippyRecyclerView recyclerView,
            @NonNull String functionName) {
        switch (functionName) {
            case COLLAPSE_PULL_HEADER: {
                recyclerView.getAdapter().onHeaderRefreshCompleted();
                break;
            }
            case EXPAND_PULL_HEADER: {
                recyclerView.getAdapter().enableHeaderRefresh();
                break;
            }
            default: {
                LogUtils.w(TAG, "Unknown function name: " + functionName);
            }
        }
    }

    @Override
    public void dispatchFunction(@NonNull HippyPullHeaderView pullHeaderView,
            @NonNull String functionName, @NonNull HippyArray params) {
        dispatchFunction(pullHeaderView, functionName, params.getInternalArray());
    }

    @Override
    public void dispatchFunction(@NonNull HippyPullHeaderView pullHeaderView,
            @NonNull String functionName, @NonNull List params) {
        super.dispatchFunction(pullHeaderView, functionName, params);
        View parent = pullHeaderView.getRecyclerView();
        if (parent instanceof HippyListView) {
            execListViewFunction((HippyListView) parent, functionName);
        } else if (parent instanceof HippyRecyclerView) {
            execRecyclerViewFunction((HippyRecyclerView) parent, functionName);
        }
    }
}
