package com.tencent.mtt.hippy.views.pager;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tencent.mtt.hippy.views.hippylist.HippyRecyclerView;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author MikaelHuang
 */
class PagerOperator {

    private final HippyRecyclerView recyclerView;
    private final Carousel carousel;
    private boolean carouselConfigChanged = true;
    private OnPageChangeListener pageChangeListener;

    PagerOperator(HippyRecyclerView rv) {
        recyclerView = rv;
        carousel = new Carousel(this);
        rv.addOnScrollListener(scrollListener);
    }

    private final RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        private int mCurrentFirstVisiblePosition;
        private int mCurrentLastVisiblePosition;
        private int mCurrentFirstFullyVisiblePosition;
        private int mCurrentLastFullyVisiblePosition;
        private int mTotalItemCount;

        private int currentPageIndex = -1;

        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            final LinearLayoutManager layoutManager = getLayoutManager();
            final int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            final int lastVisiblePosition = layoutManager.findLastVisibleItemPosition();
            final int firstFullyVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
            final int lastFullyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
            final int totalItemCount = layoutManager.getItemCount();

            if (firstVisiblePosition < 0 || lastVisiblePosition < 0) {
                return;
            }
            if (firstVisiblePosition == mCurrentFirstVisiblePosition
                && lastVisiblePosition == mCurrentLastVisiblePosition
                && firstFullyVisibleItemPosition == mCurrentFirstFullyVisiblePosition
                && lastFullyVisibleItemPosition == mCurrentLastFullyVisiblePosition
                && totalItemCount == mTotalItemCount) {
                return;
            }

            mCurrentFirstVisiblePosition = firstVisiblePosition;
            mCurrentLastVisiblePosition = lastVisiblePosition;
            mCurrentFirstFullyVisiblePosition = firstFullyVisibleItemPosition;
            mCurrentLastFullyVisiblePosition = lastFullyVisibleItemPosition;
            mTotalItemCount = totalItemCount;

            onViewportChanged(firstVisiblePosition, lastVisiblePosition);
        }

        private void onViewportChanged(int firstVisiblePosition, int lastVisiblePosition) {
            // 在RV的区域判断逻辑中：
            // 1.RV的可视内容区域为 [RV.left + paddingLeft, RV.right - paddingRight]
            // 2.itemView的横向区域为 [v.left - v.decLeft - v.marginLeft, v.right + v.decRight + v.marginRight]
            // 由此可得：
            // 1. 虽然通过padding+clipToPadding(false)可使前后item在视觉上可见，但逻辑上并不可见
            // 2. 结合结论1和Viewpager特性，对于ViewPager模式，稳定状态下屏幕上只会存在一个可见item
            // 3. itemDecoration存在时，始终不存在FullyVisible的item
            // 4. 首个可见item同时也是最后一个可见item时，该item即可认为是被选择的item
            int selectedIndex = lastVisiblePosition == firstVisiblePosition ? firstVisiblePosition : -1;
            if (currentPageIndex == selectedIndex || selectedIndex < 0) {
                return;
            }
            currentPageIndex = selectedIndex;
            onPageSelected(selectedIndex);
            checkBoundariesOnViewportChanged(firstVisiblePosition);
        }

        private void onPageSelected(int selectedRuntimeIndex) {
            int userPosition = getAdapter().getNormalizedPosition(selectedRuntimeIndex);
            if (pageChangeListener != null) {
                pageChangeListener.onPageSelected(userPosition);
            }
        }

    };

    private PagerRecyclerAdapter getAdapter() {
        return (PagerRecyclerAdapter) recyclerView.getAdapter();
    }

    private LinearLayoutManager getLayoutManager() {
        return (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    void setPageChangeListener(OnPageChangeListener listener) {
        pageChangeListener = listener;
    }

    void setAutoPlay(boolean auto) {
        carouselConfigChanged |= carousel.setAutoPlay(auto);
    }

    void setInterval(int interval) {
        carouselConfigChanged |= carousel.setInterval(interval);
    }

    void checkCarousel() {
        if (!carouselConfigChanged) {
            return;
        }
        carouselConfigChanged = false;
        if (carousel.isAutoPlay()) {
            startCarousel();
        } else {
            stopCarousel();
        }
    }

    void startCarousel() {
        carousel.start();
    }

    void stopCarousel() {
        carousel.stop();
    }


    @UiThread
    void checkBoundariesOnViewportChanged(int runtimeFirstVisiblePos) {
        PagerRecyclerAdapter adapter = getAdapter();
        if (adapter == null || !adapter.canCircular()) {
            // checking is only needed in circular model
            return;
        }
        if (runtimeFirstVisiblePos < 0) {
            return;
        }
        int userFirstVisiblePos = adapter.getNormalizedPosition(runtimeFirstVisiblePos);
        int userPageCount = adapter.getNormalizedItemCount();
        int checkedRuntimePos = getCircularRuntimeIndexNearMiddleIfBoundary(
                adapter, runtimeFirstVisiblePos, userFirstVisiblePos, userPageCount);
        if (runtimeFirstVisiblePos != checkedRuntimePos) {
            recyclerView.scrollToPosition(checkedRuntimePos);
        }
    }

    @UiThread
    void requestScrollToPage(int userExpectPos, boolean smooth, boolean inDirecNext) {
        PagerRecyclerAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        final int userPageCount = adapter.getNormalizedItemCount();
        if (userExpectPos < 0 || userExpectPos >= userPageCount) {
            return;
        }
        int runtimePos = userExpectPos;
        if (adapter.canCircular()) {
            int runtimeCurrentPos = getRuntimeCurrentPosition();
            int userCurrentPos = adapter.getNormalizedPosition(runtimeCurrentPos);
            int distance = userExpectPos - userCurrentPos;
            runtimePos = runtimeCurrentPos + distance;
            if (inDirecNext) {
                // 在'往下一页'的方向上执行页面切换
                runtimePos += distance < 0 ? userPageCount : 0;
            }
            // checking boundary
            int checkedRuntimePos = getCircularRuntimeIndexNearMiddleIfBoundary(
                    adapter, runtimePos, userExpectPos, userPageCount);
            if (runtimePos != checkedRuntimePos) {
                // we got a middle-near runtime position
                runtimePos = checkedRuntimePos;
                smooth = false;
            }
        }
        if (smooth) {
            recyclerView.smoothScrollToPosition(runtimePos);
        } else {
            recyclerView.scrollToIndex(-1, runtimePos, false, 0);
        }
    }

    @UiThread
    void requestScrollToPage(int userExpectPos, boolean smooth) {
        requestScrollToPage(userExpectPos, smooth, false);
    }

    @UiThread
    void requestScrollToNextPage(boolean smooth) {
        PagerRecyclerAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }
        final int userPageCount = adapter.getNormalizedItemCount();
        final int userCurrentPos = getCurrentPosition();
        if (userPageCount <= 0 || userCurrentPos < 0) {
            return;
        }
        int userExpectPos = (userCurrentPos + 1) % userPageCount;
        requestScrollToPage(userExpectPos, smooth, true);
    }

    int getCurrentPosition() {
        int runtimePos = getRuntimeCurrentPosition();
        if (runtimePos < 0) {
            return -1;
        }
        return getAdapter().getNormalizedPosition(runtimePos);
    }

    private int getRuntimeCurrentPosition() {
        LinearLayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return -1;
        }
        return layoutManager.findFirstVisibleItemPosition();
    }

    /**
     * circular模式下调用。当runtimePos触达边界条件时，将转换成接近中间的pos进行返回；否则直接返回入参值
     */
    static int getCircularRuntimeIndexNearMiddleIfBoundary(PagerRecyclerAdapter adapter, int runtimePos,
                                                            int userExpectPos, int userPageCount) {
        // in circular model, runtimePageCount is Integer.MAX_VALUE
        final int runtimePageCount = adapter.getItemCount();
        // 由于存在两边露出多个前向/后向页的场景，这里简单选100作为边界
        if (runtimePos < 100 || runtimePos > runtimePageCount - 100) {
            // near the boundaries, set to the position near the middle
            final int jumpToMiddle = runtimePageCount / 2;
            final int offsetFirstItem = userPageCount == 0 ? 0 : jumpToMiddle % userPageCount;
            return Math.max(0, userExpectPos) + jumpToMiddle - offsetFirstItem;
        }
        return runtimePos;
    }

}
