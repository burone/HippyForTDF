package com.tencent.mtt.hippy.views.pager;

import android.os.Handler;
import android.os.Looper;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author MikaelHuang
 *
 * 用来控制Pager进行自动轮播
 */
class Carousel {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final PagerOperator pagerOperator;
    private boolean autoPlay;
    private int interval;

    Carousel(PagerOperator operator) {
        this.pagerOperator = operator;
    }

    boolean setAutoPlay(boolean autoPlay) {
        if (this.autoPlay != autoPlay) {
            this.autoPlay = autoPlay;
            return true;
        }
        return false;
    }

    boolean setInterval(int interval) {
        if (this.interval != interval) {
            this.interval = interval;
            return true;
        }
        return false;
    }

    boolean isAutoPlay() {
        return autoPlay;
    }

    public void start() {
        scheduleNextPageSwitch();
    }

    public void stop() {
        handler.removeCallbacks(pageSwitchTask);
    }

    private void scheduleNextPageSwitch() {
        if (interval <= 0) {
            return;
        }
        handler.removeCallbacks(pageSwitchTask);
        handler.postDelayed(pageSwitchTask, interval);
    }

    private final Runnable pageSwitchTask = new Runnable() {
        @Override
        public void run() {
            pagerOperator.requestScrollToNextPage(true);
            scheduleNextPageSwitch();
        }
    };

}
