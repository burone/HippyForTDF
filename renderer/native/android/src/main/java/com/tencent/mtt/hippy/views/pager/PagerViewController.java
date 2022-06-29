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

package com.tencent.mtt.hippy.views.pager;

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mtt.hippy.annotation.HippyController;
import com.tencent.mtt.hippy.annotation.HippyControllerProps;
import com.tencent.mtt.hippy.common.HippyArray;
import com.tencent.mtt.hippy.uimanager.ControllerManager;
import com.tencent.mtt.hippy.uimanager.HippyViewController;
import com.tencent.mtt.hippy.uimanager.ListViewRenderNode;
import com.tencent.mtt.hippy.uimanager.RenderNode;
import com.tencent.mtt.hippy.utils.PixelUtil;
import com.tencent.renderer.utils.ArrayUtils;
import com.tencent.renderer.utils.MapUtils;

import java.util.List;
import java.util.Map;

/**
 * Created  on 2020/12/22.
 */

@HippyController(name = PagerViewController.CLASS_NAME, useSystemStandardType = true)
public class PagerViewController<PRV extends PagerView> extends HippyViewController<PRV> {

    public static final String CLASS_NAME = "SmartViewPager";
    public static final String GET_PAGE_INDEX = "getPageIndex";
    public static final String SET_PAGE_WITHOUT_ANIM = "setPageWithoutAnimation";
    public static final String SET_PAGE = "setPage";

    public PagerViewController() {
    }

    @Override
    public int getChildCount(PRV viewGroup) {
        return viewGroup.getChildCountWithCaches();
    }

    @Override
    public View getChildAt(PRV viewGroup, int index) {
        return viewGroup.getChildAtWithCaches(index);
    }

    /**
     * view 被Hippy的RenderNode 删除了，这样会导致View的child完全是空的，这个view是不能再被recyclerView复用了
     * 否则如果被复用，在adapter的onBindViewHolder的时候，view的实际子view和renderNode的数据不匹配，diff会出现异常
     * 导致item白条，显示不出来，所以被删除的view，需要把viewHolder.setIsRecyclable(false)，刷新list后，这个view就
     * 不会进入缓存。
     */
    @Override
    protected void deleteChild(ViewGroup parentView, View childView) {
        super.deleteChild(parentView, childView);
        ((PRV) parentView).getRecyclerView().disableRecycle(childView);
    }

    @Override
    public void onBatchStart(PRV view) {
        super.onBatchStart(view);
        view.onBatchStart();
    }

    @Override
    public void onBatchComplete(PRV view) {
        super.onBatchComplete(view);
        view.onBatchComplete();
    }

    @Override
    protected View createViewImpl(Context context) {
        return createViewImpl(context, null);
    }

    @Override
    protected View createViewImpl(Context context, @Nullable Map<String, Object> props) {
        boolean circular = false;
        int initPos = 0;
        if (props != null) {
            if (props.containsKey("circular")) {
                circular = MapUtils.getBooleanValue(props, "circular");
            }
            if (props.containsKey("initialPage")) {
                initPos = MapUtils.getIntValue(props, "initialPage");
            }
        }
        PagerView pagerView = new PagerView(context);
        pagerView.init(circular, initPos);
        return pagerView;
    }

    @Override
    public RenderNode createRenderNode(int id, @Nullable Map<String, Object> props, @NonNull String className,
            @NonNull ViewGroup hippyRootView, ControllerManager controllerManager, boolean isLazyLoad) {
        return new ListViewRenderNode(id, props, className, hippyRootView, controllerManager, isLazyLoad);
    }

    @HippyControllerProps(name = "previousMargin", defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setPreviousMargin(final PRV view, double prevMargin) {
        RecyclerView rv = view.getRecyclerView();
        rv.setPadding((int) PixelUtil.dp2px(prevMargin), 0 , rv.getPaddingRight(), 0);
    }

    @HippyControllerProps(name = "nextMargin", defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setNextMargin(final PRV view, double nextMargin) {
        RecyclerView rv = view.getRecyclerView();
        rv.setPadding(rv.getPaddingLeft(), 0 , (int) PixelUtil.dp2px(nextMargin), 0);
    }

    @HippyControllerProps(name = "pageGap", defaultType = HippyControllerProps.NUMBER, defaultNumber = 0)
    public void setPageGap(final PRV view, final double pageGape) {
        RecyclerView rv = view.getRecyclerView();
        if (rv.getItemDecorationCount() > 0) {
            rv.removeItemDecorationAt(0);
        }
        rv.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.right = (int) PixelUtil.dp2px(pageGape);
            }
        });
    }

    @HippyControllerProps(name = "autoplay", defaultType = HippyControllerProps.BOOLEAN, defaultBoolean = false)
    public void setAutoPlay(PRV view, boolean autoPlay) {
        view.setAutoPlay(autoPlay);
    }

    @HippyControllerProps(name = "autoplayTimeInterval", defaultType = HippyControllerProps.NUMBER, defaultNumber = 5000)
    public void setInterval(PRV view, float interval) {
        view.setInterval((int) interval);
    }

    @Override
    public void onAfterUpdateProps(PRV view) {
        super.onAfterUpdateProps(view);
        view.getRecyclerView().onAfterUpdateProps();
    }

    @Override
    public void dispatchFunction(PRV view, @NonNull String functionName,
                                 @NonNull HippyArray params) {
        dispatchFunction(view, functionName, params.getInternalArray());
    }

    @Override
    public void dispatchFunction(PRV view, @NonNull String functionName,
                                 @NonNull List params) {
        super.dispatchFunction(view, functionName, params);
        switch (functionName) {
            case GET_PAGE_INDEX: {
                // 该接口目前不会调上来，直接在c层返回index
                int pageIndex = view.getOperator().getCurrentPosition();
                break;
            }
            case SET_PAGE: {
                int index = ArrayUtils.getIntValue(params, 0);
                view.getOperator().requestScrollToPage(index, true);
                break;
            }
            case SET_PAGE_WITHOUT_ANIM: {
                int index = ArrayUtils.getIntValue(params, 0);
                view.getOperator().requestScrollToPage(index, false);
                break;
            }
            default:
                break;
        }
    }

}
