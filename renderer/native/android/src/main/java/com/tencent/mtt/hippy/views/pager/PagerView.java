package com.tencent.mtt.hippy.views.pager;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.HippyRecyclerExtension;
import androidx.recyclerview.widget.HippyRecyclerPool;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mtt.hippy.uimanager.HippyViewBase;
import com.tencent.mtt.hippy.uimanager.NativeGestureDispatcher;
import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerView;
import com.tencent.mtt.hippy.views.hippylist.RecyclerViewEventHelper;

/**
 * HippyRecyclerListAdapter#getChildNode(int)里默认recyclerview是被一个viewGroup包着的，
 * 所以这里不得已包一个父亲
 */
public class PagerView extends FrameLayout implements HippyViewBase {

    private final HippyRecyclerView<?> recyclerView;
    private final PagerOperator operator;
    private final MagicPagerSnapHelper snapHelper;

    private PagerRecyclerAdapter adapter;
    private Runnable pendingInitTask;
    private NativeGestureDispatcher nativeGestureDispatcher;

    public PagerView(Context context) {
        super(context);
        HippyRecyclerView<?> rv = new HippyRecyclerView<>(context);
        recyclerView = rv;
        snapHelper = new MagicPagerSnapHelper();
        snapHelper.attachToRecyclerView(rv);
        operator = new PagerOperator(rv);
        addView(rv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        HippyRecyclerExtension cacheExtension = new HippyRecyclerExtension(rv, rv.getNodePositionHelper());
        rv.setViewCacheExtension(cacheExtension);
        HippyRecyclerPool pool = new HippyRecyclerPool(this, cacheExtension, rv.getNodePositionHelper());
        pool.setViewAboundListener(rv);
        rv.setRecycledViewPool(pool);
    }

    public void init(final boolean circular, final int initPos) {
        final HippyRecyclerView<?> rv = recyclerView;
        rv.setClipToPadding(false);
        rv.setItemAnimator(null);
        rv.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
        adapter = circular ? new PagerCircularRecyclerAdapter(rv) : new PagerRecyclerAdapter(rv);
        rv.setAdapter(adapter);
        operator.setPageChangeListener(new PagerChangeMonitor(this));
        // 初始position的计算需要用到RenderNodeCount，故延到onBatchComplete执行
        pendingInitTask = new Runnable() {
            @Override
            public void run() {
                int initPosition = Math.max(0, initPos);
                if (circular) {
                    initPosition = PagerOperator.getCircularRuntimeIndexNearMiddleIfBoundary(
                    adapter, 0, initPosition, adapter.getRenderNodeCount());
                }
                rv.scrollToPosition(initPosition);
            }
        };
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        operator.stopCarousel();
    }

    public void setAutoPlay(boolean auto) {
        operator.setAutoPlay(auto);
    }

    public void setInterval(int interval) {
        operator.setInterval(interval);
    }

    @Override
    public NativeGestureDispatcher getGestureDispatcher() {
        return nativeGestureDispatcher;
    }

    @Override
    public void setGestureDispatcher(NativeGestureDispatcher dispatcher) {
        nativeGestureDispatcher = dispatcher;
    }

    public HippyRecyclerView getRecyclerView() {
        return recyclerView;
    }

    public PagerOperator getOperator() {
        return operator;
    }

    @Override
    public int computeVerticalScrollOffset() {
        return recyclerView.computeVerticalScrollOffset();
    }

    public int getChildCountWithCaches() {
        return recyclerView.getChildCountWithCaches();
    }

    public View getChildAtWithCaches(int index) {
        return recyclerView.getChildAtWithCaches(index);
    }



    public void onBatchStart() {
        recyclerView.onBatchStart();
    }

    public void onBatchComplete() {
        Runnable pendingTask = pendingInitTask;
        if (pendingTask != null) {
            pendingTask.run();
            pendingInitTask = null;
        }
        recyclerView.onBatchComplete();
        recyclerView.setListData();
        operator.checkCarousel();
    }

}
