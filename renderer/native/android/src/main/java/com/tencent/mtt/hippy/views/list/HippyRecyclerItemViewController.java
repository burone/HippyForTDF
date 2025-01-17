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

package com.tencent.mtt.hippy.views.list;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tencent.mtt.hippy.HippyRootView;
import com.tencent.mtt.hippy.annotation.HippyController;
import com.tencent.mtt.hippy.common.HippyMap;
import com.tencent.mtt.hippy.uimanager.ControllerManager;
import com.tencent.mtt.hippy.uimanager.HippyViewController;
import com.tencent.mtt.hippy.uimanager.ListItemRenderNode;
import com.tencent.mtt.hippy.uimanager.ListViewRenderNode;
import com.tencent.mtt.hippy.uimanager.RecyclerItemRenderNode;
import com.tencent.mtt.hippy.uimanager.RenderNode;
import java.util.Map;

@HippyController(name = HippyRecyclerItemViewController.CLASS_NAME, isLazyLoad = true)
public class HippyRecyclerItemViewController extends HippyViewController<HippyListItemView> {

    public static final String CLASS_NAME = "ListViewItem";

    @Override
    protected View createViewImpl(Context context) {
        return new HippyListItemView(context);
    }

    @Override
    public RenderNode createRenderNode(int id, @Nullable Map<String, Object> props, @NonNull String className,
            @NonNull ViewGroup hippyRootView, ControllerManager controllerManager, boolean isLazyLoad) {
        return new RecyclerItemRenderNode(id, props, className, hippyRootView, controllerManager, isLazyLoad);
    }
}
