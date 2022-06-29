package com.tencent.mtt.hippy.views.pager;

import android.view.View;

import com.tencent.renderer.utils.EventUtils;

import java.util.HashMap;
import java.util.Map;

class PagerChangeMonitor implements OnPageChangeListener {

    private static final String PAGE_ITEM_POSITION = "position";

    private final View targetView;

    PagerChangeMonitor(PagerView view) {
        targetView = view;
    }

    @Override
    public void onPageSelected(int position) {
        Map<String, Object> params = new HashMap<>();
        params.put(PAGE_ITEM_POSITION, position);
        EventUtils.send(targetView, EventUtils.EVENT_VIEW_PAGE_SELECTED, params);
    }
}
