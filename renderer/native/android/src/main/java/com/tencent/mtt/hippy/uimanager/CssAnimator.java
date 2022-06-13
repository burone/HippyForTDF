package com.tencent.mtt.hippy.uimanager;

import android.animation.ArgbEvaluator;
import android.animation.Keyframe;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import androidx.core.view.animation.PathInterpolatorCompat;
import com.tencent.mtt.hippy.utils.PixelUtil;
import com.tencent.renderer.utils.ArrayUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2020 Tencent. All rights reserved.
 *
 * @author MikaelHuang
 * @date 2022/4/1
 */
class CssAnimator {


    static CssAnimator query(View view, List params, CssAnimator current) {
        final AnimDesc desc = AnimDesc.parse(params);
        CssAnimator animator = current;
        if (animator == null || animator.isInvalidate(desc)) {
            animator = new CssAnimator(view, desc);
        }
        animator.setDuration(desc.duration);
        animator.setStartDelay(desc.delay);
        animator.setPlayCount(desc.iterationCount);
        animator.setDirection(desc.direction);
        animator.setFillMode(desc.fillMode);
        animator.setTimingFunction(desc.timingFunc, desc.cubicArgs);
        return animator;
    }


    private final AnimDesc animDesc;
    private final AnimViewOperator operator;
    private final ValueAnimator animator;

    CssAnimator(View view, AnimDesc desc) {
        animDesc = desc;
        operator = new AnimViewOperator(view);
        animator = CssAnimParser.parse(desc.keyframeGroup, operator);
        if (animator == null) {
            throw new IllegalStateException("no validate anim");
        }
    }

    public boolean isInvalidate(AnimDesc newDesc) {
        return false;
    }

    public CssAnimator setDuration(long duration) {
        animator.setDuration(duration);
        return this;
    }

    public CssAnimator setTimingFunction(String name, List<Float> cubicArgs) {
        Interpolator interpolator = InterpolatorParser.parse(name, cubicArgs);
        animator.setInterpolator(interpolator);
        return this;
    }

    public CssAnimator setStartDelay(long delay) {
        animator.setStartDelay(delay);
        return this;
    }

    public CssAnimator setPlayCount(int playCount) {
        final int repeatCount;
        if (playCount > 0) {
            repeatCount = playCount - 1;
        } else {
            repeatCount = playCount == 0 ? 0 : ValueAnimator.INFINITE;
        }
        animator.setRepeatCount(repeatCount);
        return this;
    }

    public CssAnimator setDirection(int direction) {
        final int repeatMode = direction == 1 // REVERSE
                ? ValueAnimator.REVERSE : ValueAnimator.RESTART;
        animator.setRepeatMode(repeatMode);
        return this;
    }

    public CssAnimator setFillMode(int fillMode) {
        // TODO: fillMode
        return this;
    }

    public void start() {
        animator.start();
    }

    public void cancel() {
        animator.cancel();
    }


    private static class AnimDesc {
        public String name;
        public long duration; // ms
        public long delay; // ms
        public int iterationCount;
        public int direction;
        public String timingFunc;
        public int fillMode;
        public List<Map<String, Object>> keyframeGroup;
        public List<Float> cubicArgs;

        public static AnimDesc parse(List params) {
            AnimDesc config = new AnimDesc();
            config.name = ArrayUtils.getStringValue(params, 0);
            config.duration = ArrayUtils.getLongValue(params, 1);
            config.delay = ArrayUtils.getLongValue(params, 2);
            config.iterationCount = ArrayUtils.getIntValue(params, 3);
            config.direction = ArrayUtils.getIntValue(params, 4);
            config.timingFunc = ArrayUtils.getStringValue(params, 5);
            config.fillMode = ArrayUtils.getIntValue(params, 6);
            config.keyframeGroup = ArrayUtils.getListValue(params, 7);
            config.cubicArgs = ArrayUtils.getListValue(params, 8);
            return config;
        }
    }


    private static class AnimViewOperator implements CssAnimParser.ViewOperator {

        private final WeakReference<View> targetRef;

        AnimViewOperator(View target) {
            targetRef = new WeakReference<>(target);
        }

        @Override
        public Object getCurrentStateValue(String propName) {
            final View target = targetRef.get();
            if (target == null) {
                return 0;
            }
            switch (propName) {
                case CssAnimParser.TRANSFORM_ROTATION:
                    return target.getRotation() * Math.PI / 180;
                case CssAnimParser.TRANSFORM_TRANSLATION_X:
                    return PixelUtil.px2dp(target.getTranslationX());
                case CssAnimParser.TRANSFORM_TRANSLATION_Y:
                    return PixelUtil.px2dp(target.getTranslationY());
                case CssAnimParser.TRANSFORM_SCALE_X:
                    return target.getScaleX();
                case CssAnimParser.TRANSFORM_SCALE_Y:
                    return target.getScaleY();
                case CssAnimParser.OPACITY:
                    return target.getAlpha();
                case CssAnimParser.BACKGROUND_COLOR:
                    return 0;
            }
            return 0;
        }

        @Override
        public void setAttribute(String propName, Object value) {
            final View target = targetRef.get();
            if (target == null) {
                return;
            }
            switch (propName) {
                case CssAnimParser.TRANSFORM_ROTATION:
                    target.setRotation((float) ((180 * (float) value) / Math.PI));
                    break;
                case CssAnimParser.TRANSFORM_TRANSLATION_X:
                    target.setTranslationX(PixelUtil.dp2px((float) value));
                    break;
                case CssAnimParser.TRANSFORM_TRANSLATION_Y:
                    target.setTranslationY(PixelUtil.dp2px((float) value));
                    break;
                case CssAnimParser.TRANSFORM_SCALE_X:
                    target.setScaleX((float) value);
                    break;
                case CssAnimParser.TRANSFORM_SCALE_Y:
                    target.setScaleY((float) value);
                    break;
                case CssAnimParser.OPACITY:
                    target.setAlpha((float) value);
                    break;
                case CssAnimParser.BACKGROUND_COLOR:
                    target.setBackgroundColor((int) value);
                    break;
                default:
                    break;
            }
        }
    }


    private static class CssAnimParser {

        private static final String TRANSFORM_ROTATION = "transform.rotation";
        private static final String TRANSFORM_TRANSLATION_X = "transform.translation.x";
        private static final String TRANSFORM_TRANSLATION_Y = "transform.translation.y";
        private static final String TRANSFORM_SCALE_X = "transform.scale.x";
        private static final String TRANSFORM_SCALE_Y = "transform.scale.y";
        private static final String OPACITY = "opacity";
        private static final String BACKGROUND_COLOR = "background-color";

        interface ViewOperator {

            Object getCurrentStateValue(String propName);

            void setAttribute(String propName, Object value);
        }

        static ValueAnimator parse(List<Map<String, Object>> keyFrameGroup, final ViewOperator operator) {
            // 所有关键帧中的同一类属性操作聚合
            final Map<String, List<Keyframe>> propOperates = new HashMap<>();
            // 关键帧集合
            List<Map<String, Object>> cssKeyFrames = keyFrameGroup;
            // 遍历每一帧
            for (Map<String, Object> cssFrame : cssKeyFrames) {
                final float progress = readProgress(cssFrame);
                final List<Map<String, Object>> propOps = readPropOperations(cssFrame);
                // 遍历该帧的每一个操作，一个操作产生一个android的KeyFrame
                for (Map<String, Object> op : propOps) {
                    String propName = readPropName(op);
                    List<Keyframe> samePropOps = propOperates.get(propName);
                    if (samePropOps == null) {
                        propOperates.put(propName, samePropOps = new ArrayList<>());
                    }
                    Object value = readPropValue(op, propName);
                    Keyframe kf = value instanceof Float ? Keyframe.ofFloat(progress, (Float) value)
                            : value instanceof Integer ? Keyframe.ofInt(progress, (Integer) value)
                            : Keyframe.ofObject(progress, value);
                    samePropOps.add(kf);
                }
            }
            // 补帧策略
            for (Map.Entry<String, List<Keyframe>> entry : propOperates.entrySet()) {
                String propName = entry.getKey();
                List<Keyframe> keyframes = entry.getValue();
                boolean addFirst = true;
                boolean addLast = true;
                for (Keyframe kf : keyframes) {
                    float fraction = kf.getFraction();
                    if (fraction == 0) {
                        addFirst = false;
                    } else if (fraction == 1) {
                        addLast = false;
                    }
                }
                if (addFirst) {
                    Keyframe first = keyframes.get(0).clone();
                    first.setFraction(0);
                    first.setValue(operator.getCurrentStateValue(propName));
                    keyframes.add(0, first);
                }
                if (addLast) {
                    Keyframe last = keyframes.get(keyframes.size() - 1).clone();
                    last.setFraction(1);
                    keyframes.add(last);
                }
            }
            // 生成PropertyValuesHolder
            final List<PropertyValuesHolder> holders = new ArrayList<>();
            for (Map.Entry<String, List<Keyframe>> entry : propOperates.entrySet()) {
                String propName = entry.getKey();
                List<Keyframe> keyframes = entry.getValue();
                PropertyValuesHolder samePropHolder = PropertyValuesHolder
                        .ofKeyframe(propName, keyframes.toArray(new Keyframe[0]));
                // config evaluator
                if (isColorProp(propName)) {
                    samePropHolder.setEvaluator(new ArgbEvaluator());
                }
                holders.add(samePropHolder);
            }
            PropertyValuesHolder[] propValuesHolders = holders.toArray(new PropertyValuesHolder[0]);
            ValueAnimator animator = ValueAnimator.ofPropertyValuesHolder(propValuesHolders);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator a) {
                    PropertyValuesHolder[] hds = a.getValues();
                    for (PropertyValuesHolder holder : hds) {
                        String propName = holder.getPropertyName();
                        operator.setAttribute(propName, a.getAnimatedValue(propName));
                    }
                }
            });
            return animator;
        }

        private static float readProgress(Map<String, Object> cssFrame) {
            Object value = cssFrame.get("progress");
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
            return 0;
        }

        private static List<Map<String, Object>> readPropOperations(Map<String, Object> cssFrame) {
            Object value = cssFrame.get("property_frames");
            if (value instanceof List) {
                return (List<Map<String, Object>>) value;
            }
            return new ArrayList<>();
        }

        private static String readPropName(Map<String, Object> propOperation) {
            Object value = propOperation.get("property_key");
            if (value instanceof String) {
                return (String) value;
            }
            return "";
        }

        private static Object readPropValue(Map<String, Object> propOperation, String propName) {
            Object value = propOperation.get("property_value");
            if (!(value instanceof Number)) {
                return value;
            }
            final Number num = (Number) value;
            switch (propName) {
                case TRANSFORM_ROTATION:
                case TRANSFORM_TRANSLATION_X:
                case TRANSFORM_TRANSLATION_Y:
                case TRANSFORM_SCALE_X:
                case TRANSFORM_SCALE_Y:
                case OPACITY:
                    return num.floatValue();
                case BACKGROUND_COLOR:
                    return num.intValue();
                default:
                    break;
            }
            return value;
        }

        private static boolean isColorProp(String propName) {
            return BACKGROUND_COLOR.equals(propName);
        }

    }


    private static class InterpolatorParser {

        static Interpolator parse(String timingName, List<Float> cubicArgs) {
            if (cubicArgs != null && cubicArgs.size() == 4) {
                return PathInterpolatorCompat.create(
                        cubicArgs.get(0), cubicArgs.get(1), cubicArgs.get(2), cubicArgs.get(3));
            }
            Interpolator interpolator = null;
            if (timingName != null) {
                switch (timingName) {
                    case "linear":
                        interpolator = new LinearInterpolator();
                        break;
                    case "ease":
                        interpolator = new AccelerateDecelerateInterpolator();
                        break;
                    case "ease-in":
                        interpolator = new AccelerateInterpolator();
                        break;
                    case "ease-out":
                        interpolator = new DecelerateInterpolator();
                        break;
                    default:
                        break;
                }
            }
            if (interpolator == null) {
                interpolator = new LinearInterpolator();
            }
            return interpolator;
        }
    }

}
