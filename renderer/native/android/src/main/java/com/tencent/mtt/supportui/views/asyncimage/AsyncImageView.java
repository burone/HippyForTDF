/* 	Copyright (C) 2018 Tencent, Inc.
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

package com.tencent.mtt.supportui.views.asyncimage;

import com.tencent.link_supplier.proxy.framework.ImageDataSupplier;
import com.tencent.link_supplier.proxy.framework.ImageLoaderAdapter;
import com.tencent.link_supplier.proxy.framework.ImageRequestListener;
import com.tencent.mtt.supportui.views.IBorder;
import com.tencent.mtt.supportui.views.IGradient;
import com.tencent.mtt.supportui.views.IShadow;

import android.animation.Animator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;

/**
 * Created by leonardgong on 2017/12/7 0007.
 */

public class AsyncImageView extends ViewGroup implements Animator.AnimatorListener,
        ValueAnimator.AnimatorUpdateListener, IBorder, IShadow,
        IGradient {

    public static final int FADE_DURATION = 150;
    public final static int IMAGE_UNLOAD = 0;
    public final static int IMAGE_LOADING = 1;
    public final static int IMAGE_LOADED = 2;

    public static final int SHAPE_NORMAL = 0;
    public static final int SHAPE_CIRCLE = 1;

    protected final static int SOURCE_TYPE_SRC = 1;
    protected final static int SOURCE_TYPE_DEFAULT_SRC = 2;
    protected ImageDataSupplier mSourceDrawable;
    private ImageDataSupplier mDefaultSourceDrawable;

    protected String mUrl;
    protected String mDefaultSourceUrl;
    protected String mImageType;

    // the 'mURL' is fetched succeed
    protected int mUrlFetchState = IMAGE_UNLOAD;

    protected int mTintColor;
    protected ScaleType mScaleType;
    protected Drawable mContentDrawable;
    protected Drawable mRippleDrawable;

    private boolean mIsAttached;
    protected ImageLoaderAdapter mImageAdapter;
    private boolean mFadeEnable;
    private long mFadeDuration;
    private ValueAnimator mAlphaAnimator;

    protected BackgroundDrawable mBGDrawable;

    private int mImagePositionX;
    private int mImagePositionY;

    private int mShape = SHAPE_NORMAL;

    public enum ScaleType {
        FIT_XY,
        CENTER,
        CENTER_INSIDE,
        CENTER_CROP,
        ORIGIN,
        REPEAT // Robinsli add for hippy
    }

    public AsyncImageView(Context context) {
        super(context);
        mUrl = null;
        mDefaultSourceUrl = null;
        mImageType = null;
        setFadeEnabled(false);
        setFadeDuration(FADE_DURATION);
    }

    public void setImageAdapter(ImageLoaderAdapter imageAdapter) {
        mImageAdapter = imageAdapter;
    }

    public void setImageType(String type) {
        mImageType = type;
    }

    public void setUrl(String url) {
        if (!TextUtils.equals(url, mUrl)) {
            mUrl = url;
            mUrlFetchState = IMAGE_UNLOAD;
            if (isAttached()) {
                fetchImageByUrl(mUrl, SOURCE_TYPE_SRC);
            }
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public void setFadeEnabled(boolean enable) {
        mFadeEnable = enable;
    }

    public boolean isFadeEnabled() {
        return mFadeEnable;
    }

    public void setFadeDuration(long duration) {
        mFadeDuration = duration;
    }

    protected void resetFadeAnimation() {
        if (mFadeEnable) {
            if (mAlphaAnimator != null && mAlphaAnimator.isRunning()) {
                mAlphaAnimator.cancel();
            }
            mAlphaAnimator = null;
        }
    }

    protected void startFadeAnimation() {
        if (mFadeEnable) {
            if (this.mFadeDuration > 0 && this.mAlphaAnimator == null) {
                this.mAlphaAnimator = ValueAnimator.ofInt(new int[]{0, 255});
                this.mAlphaAnimator.setEvaluator(new IntEvaluator());
                this.mAlphaAnimator.addUpdateListener(this);
                this.mAlphaAnimator.addListener(this);
                this.mAlphaAnimator.setDuration((long) this.mFadeDuration);
            }
            if (this.mAlphaAnimator != null) {
                if (this.mAlphaAnimator.isRunning()) {
                    this.mAlphaAnimator.cancel();
                }

                this.mAlphaAnimator.setCurrentPlayTime(0L);
                this.mAlphaAnimator.start();
            }
        }
    }

    protected void performFetchImage() {
        fetchImageByUrl(mUrl, SOURCE_TYPE_SRC);
    }

    public void setDefaultSource(String defaultSource) {
        if (!TextUtils.equals(mDefaultSourceUrl, defaultSource)) {
            mDefaultSourceUrl = defaultSource;
            fetchImageByUrl(mDefaultSourceUrl, SOURCE_TYPE_DEFAULT_SRC);
        }
    }

    public void setTintColor(int tintColor) {
        mTintColor = tintColor;
        if (mContentDrawable instanceof ContentDrawable) {
            ((ContentDrawable) mContentDrawable).setTintColor(tintColor);
            invalidate();
        }
    }

    public void setTintColorBlendMode(int tintColorBlendMode) {
        if (mContentDrawable instanceof ContentDrawable) {
            ((ContentDrawable) mContentDrawable).setTintColorBlendMode(tintColorBlendMode);
            invalidate();
        }
    }

    public void setScaleType(ScaleType scaleType) {
        mScaleType = scaleType;
    }

    public void setImagePositionX(int positionX) {
        mImagePositionX = positionX;
    }

    public void setImagePositionY(int positionY) {
        mImagePositionY = positionY;
    }

    public void setSrcShape(String shape) {
        int shapeType = SHAPE_NORMAL;
        if ("circle".equalsIgnoreCase(shape)) {
            shapeType = SHAPE_CIRCLE;
        }
        mShape = shapeType;
        invalidate();
    }

    protected void onFetchImage(String url) {

    }

    protected boolean shouldFetchImage() {
        return true;
    }

    protected boolean isAttached() {
        return mIsAttached;
    }

    private void fetchImageByUrl(final String url, final int sourceType) {
        if (url == null) {
            return;
        }

        if (mImageAdapter != null) {
            // fetch or get image, depending on url type

            // http or https => async fetch
            // eg. <Image source={{uri: 'https://abc.png'}} />
            if (shouldUseFetchImageMode(url)) {
                String tempUrl = url.trim().replaceAll(" ", "%20");
                if (sourceType == SOURCE_TYPE_SRC) {
                    if (!shouldFetchImage()) {
                        return;
                    }
                    mUrlFetchState = IMAGE_LOADING;
                }
                onFetchImage(tempUrl);
                handleGetImageStart();
                doFetchImage(null, sourceType);
            } else {
                handleGetImageStart();
                mImageAdapter.getLocalImage(url, new ImageRequestListener() {
                    @Override
                    public void onRequestStart(ImageDataSupplier imageDataSupplier) {
                    }

                    @Override
                    public void onRequestProgress(float total, float loaded) {

                    }

                    @Override
                    public void onRequestSuccess(ImageDataSupplier imageDataSupplier) {
                        handleImageRequest(imageDataSupplier, sourceType, null);
                    }

                    @Override
                    public void onRequestFail(Throwable cause, String source) {
                    }
                });
            }
        }
    }

    protected boolean shouldUseFetchImageMode(String url) {
        return true;
    }

    protected void doFetchImage(Object param, final int sourceType) {
        if (mImageAdapter != null) {
            // 这里不判断下是取背景图片还是取当前图片怎么行？
            String url = sourceType == SOURCE_TYPE_SRC ? mUrl : mDefaultSourceUrl;
            mImageAdapter.fetchImage(url, new ImageRequestListener() {
                @Override
                public void onRequestStart(ImageDataSupplier supplier) {
                    mSourceDrawable = supplier;
                }

                @Override
                public void onRequestProgress(float total, float loaded) {
                    handleGetImageProgress(total, loaded);
                }

                @Override
                public void onRequestSuccess(ImageDataSupplier supplier) {
                    handleImageRequest(supplier, sourceType, null);
                }

                @Override
                public void onRequestFail(Throwable throwable, String source) {
                    handleImageRequest(null, sourceType, throwable);
                }
            }, param);
        }
    }

    protected void handleImageRequest(ImageDataSupplier resultDrawable, int sourceType,
            Object requestInfo) {
        if (resultDrawable == null) {
            if (sourceType == SOURCE_TYPE_SRC) {
                mSourceDrawable = null;
                mUrlFetchState = IMAGE_UNLOAD;
                if (mDefaultSourceDrawable != null) {
                    if (mContentDrawable == null) {
                        mContentDrawable = generateContentDrawable();
                    }
                    setContent(SOURCE_TYPE_DEFAULT_SRC);
                } else {
                    mContentDrawable = null;
                }
                handleGetImageFail(
                        requestInfo instanceof Throwable ? (Throwable) requestInfo : null);
            } else if (sourceType == SOURCE_TYPE_DEFAULT_SRC) {
                mDefaultSourceDrawable = null;
            }
        } else {
            mContentDrawable = generateContentDrawable();
            if (sourceType == SOURCE_TYPE_SRC) {
                mSourceDrawable = resultDrawable;
                mUrlFetchState = IMAGE_LOADED;
                handleGetImageSuccess();
            } else if (sourceType == SOURCE_TYPE_DEFAULT_SRC) {
                mDefaultSourceDrawable = resultDrawable;
            }
            setContent(sourceType);
        }
    }

    protected ContentDrawable generateContentDrawable() {
        return new ContentDrawable();
    }

    protected BackgroundDrawable generateBackgroundDrawable() {
        return new BackgroundDrawable();
    }

    @Override
    protected void onDetachedFromWindow() {
        mIsAttached = false;
        if (mFadeEnable) {
            if (mAlphaAnimator != null) {
                mAlphaAnimator.cancel();
            }
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    @Override
    protected void onAttachedToWindow() {
        mIsAttached = true;
        super.onAttachedToWindow();
        if (mDefaultSourceDrawable != null && shouldFetchImage()) {
            setContent(SOURCE_TYPE_DEFAULT_SRC);
        }

        fetchImageByUrl(mUrl, SOURCE_TYPE_SRC);
    }

    protected void resetContent() {
        mContentDrawable = null;
        mBGDrawable = null;
        super.setBackgroundDrawable(null);
    }

    protected void onSetContent(String url) {

    }

    protected void afterSetContent(String url) {

    }

    protected void performSetContent() {
        setContent(SOURCE_TYPE_SRC);
    }

    protected boolean shouldSetContent() {
        return true;
    }

    protected Bitmap getBitmap() {
        if (mSourceDrawable != null) {
            return mSourceDrawable.getBitmap();
        }
        return null;
    }

    protected void setContent(int sourceType) {
        if (mContentDrawable != null) {
            if (!shouldSetContent()) {
                return;
            }
            onSetContent(mUrl);
            updateContentDrawableProperty(sourceType);
            resetBackgroundDrawable();
            afterSetContent(mUrl);
        }
    }

    protected void updateContentDrawableProperty(int sourceType) {
        if (!(mContentDrawable instanceof ContentDrawable)) {
            return;
        }
        final ContentDrawable contentDrawable = (ContentDrawable) mContentDrawable;
        Bitmap bitmap = getBitmap();
        if (sourceType == SOURCE_TYPE_DEFAULT_SRC && mDefaultSourceDrawable != null
                && (mUrlFetchState != IMAGE_LOADED || mSourceDrawable == null)) {
            bitmap = mDefaultSourceDrawable.getBitmap();
        }
        if (bitmap != null) {
            contentDrawable.setSourceType(sourceType);
            contentDrawable.setBitmap(bitmap);
            contentDrawable.setTintColor(mTintColor);
            contentDrawable.setScaleType(mScaleType);
            contentDrawable.setShape(mShape);
            contentDrawable.setImagePositionX(mImagePositionX);
            contentDrawable.setImagePositionY(mImagePositionY);
        }
        if (mBGDrawable != null) {
            contentDrawable.setBorder(mBGDrawable.getBorderRadiusArray(),
                            mBGDrawable.getBorderWidthArray());
            contentDrawable.setShadowOffsetX(mBGDrawable.getShadowOffsetX());
            contentDrawable.setShadowOffsetY(mBGDrawable.getShadowOffsetY());
            contentDrawable.setShadowRadius(mBGDrawable.getShadowRadius());
        }
    }

    protected void handleGetImageStart() {
    }

    protected void handleGetImageSuccess() {
    }

    protected void handleGetImageFail(Throwable throwable) {
    }

    protected void handleGetImageProgress(float total, float loaded) {
    }

    @Override
    public void onAnimationStart(Animator animation) {

    }

    @Override
    public void onAnimationEnd(Animator animation) {
        if (mFadeEnable) {
            restoreBackgroundColorAfterSetContent();
        }
    }

    @Override
    public void onAnimationCancel(Animator animation) {
        if (mFadeEnable) {
            if (mContentDrawable != null) {
                mContentDrawable.setAlpha(255);
            }
            restoreBackgroundColorAfterSetContent();
        }
    }

    @Override
    public void onAnimationRepeat(Animator animation) {

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        if (mFadeEnable) {
            if (!isAttached() && mAlphaAnimator != null) {
                mAlphaAnimator.cancel();
            }
            if (mContentDrawable != null) {
                mContentDrawable.setAlpha(((Integer) animation.getAnimatedValue()).intValue());
            }
        }
    }

    protected void restoreBackgroundColorAfterSetContent() {
        setBackgroundColor(Color.TRANSPARENT);
    }

    protected void setCustomBackgroundDrawable(BackgroundDrawable commonBackgroundDrawable) {
        mBGDrawable = commonBackgroundDrawable;
        super.setBackgroundDrawable(mBGDrawable);
    }

    @Override
    public void setBorderRadius(float radius, int position) {
        getBackGround().setBorderRadius(radius, position);
        if (mContentDrawable instanceof ContentDrawable) {
            ((ContentDrawable) mContentDrawable).setBorder(mBGDrawable.getBorderRadiusArray(),
                    mBGDrawable.getBorderWidthArray());
            invalidate();
        }
    }

    @Override
    public void setBorderWidth(float width, int position) {
        getBackGround().setBorderWidth(width, position);
        if (mContentDrawable instanceof ContentDrawable) {
            ((ContentDrawable) mContentDrawable).setBorder(mBGDrawable.getBorderRadiusArray(),
                    mBGDrawable.getBorderWidthArray());
            invalidate();
        }
    }

    @Override
    public void setBorderColor(int color, int position) {
        getBackGround().setBorderColor(color, position);
        invalidate();
    }

    @Override
    public void setBorderStyle(int borderStyle) {
        getBackGround().setBorderStyle(borderStyle);
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        getBackGround().setBackgroundColor(color);
        invalidate();
    }

    @Override
    public void setShadowOffsetX(float x) {
        getBackGround().setShadowOffsetX(x);
        invalidate();
    }

    @Override
    public void setShadowOffsetY(float y) {
        getBackGround().setShadowOffsetY(y);
        invalidate();
    }

    @Override
    public void setShadowOpacity(float opacity) {
        getBackGround().setShadowOpacity(opacity);
        invalidate();
    }

    @Override
    public void setShadowRadius(float radius) {
        getBackGround().setShadowRadius(Math.abs(radius));
        if (radius != 0) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }
        invalidate();
    }

    @Override
    public void setShadowSpread(float spread) {

    }

    @Override
    public void setShadowColor(int color) {
        getBackGround().setShadowColor(color);
        invalidate();
    }

    @Override
    public void setGradientAngle(String angle) {
        getBackGround().setGradientAngle(angle);
        invalidate();
    }

    @Override
    public void setGradientColors(ArrayList<Integer> colors) {
        getBackGround().setGradientColors(colors);
        invalidate();
    }

    @Override
    public void setGradientPositions(ArrayList<Float> positions) {
        getBackGround().setGradientPositions(positions);
        invalidate();
    }

    private BackgroundDrawable getBackGround() {
        if (mBGDrawable == null) {
            mBGDrawable = generateBackgroundDrawable();
            Drawable currBGDrawable = getBackground();
            super.setBackgroundDrawable(null);
            if (currBGDrawable == null) {
                super.setBackgroundDrawable(mBGDrawable);
            } else {
                LayerDrawable layerDrawable = new LayerDrawable(
                        new Drawable[]{mBGDrawable, currBGDrawable});
                super.setBackgroundDrawable(layerDrawable);
            }
        }
        return mBGDrawable;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setRippleDrawable(@NonNull Drawable rippleDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mRippleDrawable = rippleDrawable;
            resetBackgroundDrawable();
        }
    }

    private void resetBackgroundDrawable() {
        ArrayList<Drawable> drawableList = new ArrayList<>();
        if (mBGDrawable != null) {
            drawableList.add(mBGDrawable);
        }
        if (mContentDrawable != null) {
            drawableList.add(mContentDrawable);
        }
        if (mRippleDrawable != null) {
            drawableList.add(mRippleDrawable);
        }
        if (drawableList.size() > 0) {
            super.setBackground(null);
            Drawable[] drawables = new Drawable[drawableList.size()];
            for (int i = 0; i < drawableList.size(); i++) {
                drawables[i] = drawableList.get(i);
            }
            LayerDrawable layerDrawable = new LayerDrawable(drawables);
            super.setBackground(layerDrawable);
        }
    }
}
