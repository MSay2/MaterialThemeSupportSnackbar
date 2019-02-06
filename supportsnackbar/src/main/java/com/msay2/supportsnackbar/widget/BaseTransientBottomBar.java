package com.msay2.supportsnackbar.widget;

import com.msay2.supportsnackbar.R;
import com.msay2.supportsnackbar.utils.AnimUtils;
import com.msay2.supportsnackbar.utils.ThemeUtils;

import android.support.annotation.IntDef;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.support.v4.view.ViewCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseTransientBottomBar<B extends BaseTransientBottomBar<B>>
{
    public abstract static class BaseCallback<B>
    {
        public static final int DISMISS_EVENT_SWIPE = 0;
        public static final int DISMISS_EVENT_ACTION = 1;
        public static final int DISMISS_EVENT_TIMEOUT = 2;
        public static final int DISMISS_EVENT_MANUAL = 3;
        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        @RestrictTo(LIBRARY_GROUP)
        @IntDef(
                {
                        DISMISS_EVENT_SWIPE,
                        DISMISS_EVENT_ACTION,
                        DISMISS_EVENT_TIMEOUT,
                        DISMISS_EVENT_MANUAL,
                        DISMISS_EVENT_CONSECUTIVE
                })

        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent
        { }

        public void onDismissed(B transientBottomBar, @DismissEvent int event)
        {
            // empty
        }

        public void onShown(B transientBottomBar)
        {
            // empty
        }
    }

    public interface ContentViewCallback
    {
        void animateContentIn(int delay, int duration);
        void animateContentOut(int delay, int duration);
    }

    public static final int LENGTH_INDEFINITE = -2;
    public static final int LENGTH_SHORT = -1;
    public static final int LENGTH_LONG = 0;

    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
            {
                    LENGTH_INDEFINITE,
                    LENGTH_SHORT,
                    LENGTH_LONG
            })

    @IntRange(from = 1)
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration
    { }

    static final int ANIMATION_DURATION = 250;
    static final int ANIMATION_FADE_DURATION = 180;
    static final Handler handler;
    static final int MSG_SHOW = 0;
    static final int MSG_DISMISS = 1;

    static
    {
        handler = new Handler(Looper.getMainLooper(), new Handler.Callback()
        {
            @Override
            public boolean handleMessage(Message message)
            {
                switch (message.what)
                {
                    case MSG_SHOW:
                        ((BaseTransientBottomBar)message.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((BaseTransientBottomBar)message.obj).hideView(message.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final ViewGroup targetParent;
    private final Context context;

    final SnackbarBaseLayout view;

    private final ContentViewCallback contentViewCallback;

    private int duration;
    private boolean aboveValue;
    private View[] aboves;
    private List<BaseCallback<B>> callbacks;

    private final AccessibilityManager accessibilityManager;

    @RestrictTo(LIBRARY_GROUP)
    interface OnLayoutChangeListener
    {
        void onLayoutChange(View view, int left, int top, int right, int bottom);
    }

    @RestrictTo(LIBRARY_GROUP)
    interface OnAttachStateChangeListener
    {
        void onViewAttachedToWindow(View v);
        void onViewDetachedFromWindow(View v);
    }

    protected BaseTransientBottomBar(@NonNull ViewGroup parent, @NonNull View content, @NonNull ContentViewCallback contentViewCallback)
    {
        if (parent == null)
        {
            throw new IllegalArgumentException("Transient bottom bar must have non-null parent");
        }
        if (content == null)
        {
            throw new IllegalArgumentException("Transient bottom bar must have non-null content");
        }
        if (contentViewCallback == null)
        {
            throw new IllegalArgumentException("Transient bottom bar must have non-null callback");
        }
        targetParent = parent;
        this.contentViewCallback = contentViewCallback;
        context = parent.getContext();

        ThemeUtils.checkMaterialTheme(context);

        LayoutInflater inflater = LayoutInflater.from(context);

        view = (SnackbarBaseLayout)inflater.inflate(R.layout.snackbar_layout, targetParent, false);
        view.addView(content);

        view.setAccessibilityLiveRegion(view.ACCESSIBILITY_LIVE_REGION_POLITE);
        view.setImportantForAccessibility(view.IMPORTANT_FOR_ACCESSIBILITY_YES);

        view.setFitsSystemWindows(true);
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener()
        {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets)
            {
                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.getSystemWindowInsetBottom());
                return insets;
            }
        });

        accessibilityManager = (AccessibilityManager)context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    }

    @NonNull
    public B setDuration(@Duration int duration)
    {
        this.duration = duration;
        return (B)this;
    }

    @NonNull
    public B setAbove(@NonNull boolean aboved)
    {
        aboveValue = aboved;
        return (B)this;
    }

    @NonNull
    public B setViewAbove(@NonNull View... aboves)
    {
        this.aboves = aboves;

        return (B)this;
    }

    @NonNull
    public boolean isAboved()
    {
        return aboveValue == true;
    }

    @Duration
    public int getDuration()
    {
        return duration;
    }

    @NonNull
    public Context getContext()
    {
        return context;
    }

    @NonNull
    public View getView()
    {
        return view;
    }

    public void show()
    {
        SnackbarManager.getInstance().show(duration, managerCallback);
    }

    public void dismiss()
    {
        dispatchDismiss(BaseCallback.DISMISS_EVENT_MANUAL);
    }
    void dispatchDismiss(@BaseCallback.DismissEvent int event)
    {
        SnackbarManager.getInstance().dismiss(managerCallback, event);
    }

    @NonNull
    public B addCallback(@NonNull BaseCallback<B> callback)
    {
        if (callback == null)
        {
            return (B)this;
        }
        if (callbacks == null)
        {
            callbacks = new ArrayList<BaseCallback<B>>();
        }
        callbacks.add(callback);
        return (B)this;
    }

    @NonNull
    public B removeCallback(@NonNull BaseCallback<B> callback)
    {
        if (callback == null)
        {
            return (B)this;
        }
        if (callbacks == null)
        {
            return (B)this;
        }
        callbacks.remove(callback);
        return (B)this;
    }

    public boolean isShown()
    {
        return SnackbarManager.getInstance().isCurrent(managerCallback);
    }

    public boolean isShownOrQueued()
    {
        return SnackbarManager.getInstance().isCurrentOrNext(managerCallback);
    }

    final SnackbarManager.Callback managerCallback = new SnackbarManager.Callback()
    {
        @Override
        public void show()
        {
            handler.sendMessage(handler.obtainMessage(MSG_SHOW, BaseTransientBottomBar.this));
        }
        @Override
        public void dismiss(int event)
        {
            handler.sendMessage(handler.obtainMessage(MSG_DISMISS, event, 0, BaseTransientBottomBar.this));
        }
    };

    final void showView()
    {
        if (view.getParent() == null)
        {
            targetParent.addView(view);
        }
        view.setOnAttachStateChangeListener(new BaseTransientBottomBar.OnAttachStateChangeListener()
        {
            @Override
            public void onViewAttachedToWindow(View v)
            { }

            @Override
            public void onViewDetachedFromWindow(View v)
            {
                if (isShownOrQueued())
                {
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            onViewHidden(BaseCallback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(view))
        {
            if (shouldAnimate())
            {
                animateViewIn();
            }
            else
            {
                onViewShown();
            }
        }
        else
        {
            view.setOnLayoutChangeListener(new BaseTransientBottomBar.OnLayoutChangeListener()
            {
                @Override
                public void onLayoutChange(View mView, int left, int top, int right, int bottom)
                {
                    view.setOnLayoutChangeListener(null);
                    if (shouldAnimate())
                    {
                        animateViewIn();
                    }
                    else
                    {
                        onViewShown();
                    }
                }
            });
        }
    }

    void animateViewIn()
    {
        final int viewHeight = view.getHeight();

        view.setTranslationY(viewHeight);
        if (isAboved())
        {
            for (int i = 0; i < aboves.length; i++)
            {
                int aboveViewHeight = aboves[i].getHeight();

                aboves[i].setTranslationY(aboveViewHeight);
            }
        }

        final ValueAnimator animatorAboved = new ValueAnimator();
        final ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(viewHeight, 0);
        animator.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(view.getContext()));
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(Animator animator)
            {
                contentViewCallback.animateContentIn(ANIMATION_DURATION - ANIMATION_FADE_DURATION, ANIMATION_FADE_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animator)
            {
                onViewShown();
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            private int previousAnimatedIntValue = viewHeight;

            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                int currentAnimatedIntValue = (int)animator.getAnimatedValue();

                view.setTranslationY(currentAnimatedIntValue);
                previousAnimatedIntValue = currentAnimatedIntValue;
            }
        });
        if (isAboved())
        {
            animatorAboved.setIntValues(0, -viewHeight);
            animatorAboved.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(view.getContext()));
            animatorAboved.setDuration(ANIMATION_DURATION);
            animatorAboved.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                private int previousAboveAnimatedIntValue = viewHeight;

                @Override
                public void onAnimationUpdate(ValueAnimator animator)
                {
                    for (int i = 0; i < aboves.length; i++)
                    {
                        int currentAboveAnimatedIntValue = (int)animator.getAnimatedValue();

                        aboves[i].setTranslationY(currentAboveAnimatedIntValue);
                        previousAboveAnimatedIntValue = currentAboveAnimatedIntValue;
                    }
                }
            });
        }

        animator.start();
        if (isAboved())
        {
            animatorAboved.start();
        }
    }

    private void animateViewOut(final int event)
    {
        final int viewHeight = view.getHeight();

        final ValueAnimator animatorAboved = new ValueAnimator();
        final ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(0, viewHeight);
        animator.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(view.getContext()));
        animator.setDuration(ANIMATION_DURATION);
        animator.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationStart(Animator animator)
            {
                contentViewCallback.animateContentOut(0, ANIMATION_FADE_DURATION);
            }

            @Override
            public void onAnimationEnd(Animator animator)
            {
                onViewHidden(event);
            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            private int previousAnimatedIntValue = 0;

            @Override
            public void onAnimationUpdate(ValueAnimator animator)
            {
                int currentAnimatedIntValue = (int)animator.getAnimatedValue();

                view.setTranslationY(currentAnimatedIntValue);
                previousAnimatedIntValue = currentAnimatedIntValue;
            }
        });
        if (isAboved())
        {
            animatorAboved.setIntValues(-viewHeight, 0);
            animatorAboved.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(view.getContext()));
            animatorAboved.setDuration(ANIMATION_DURATION);
            animatorAboved.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
            {
                private int previousAboveAnimatedIntValue = 0;

                @Override
                public void onAnimationUpdate(ValueAnimator animator)
                {
                    for (int i = 0; i < aboves.length; i++)
                    {
                        int currentAboveAnimatedIntValue = (int)animator.getAnimatedValue();

                        aboves[i].setTranslationY(currentAboveAnimatedIntValue);
                        previousAboveAnimatedIntValue = currentAboveAnimatedIntValue;
                    }
                }
            });
        }

        animator.start();
        if (isAboved())
        {
            animatorAboved.start();
        }
    }

    final void hideView(@BaseCallback.DismissEvent final int event)
    {
        if (shouldAnimate() && view.getVisibility() == View.VISIBLE)
        {
            animateViewOut(event);
        }
        else
        {
            onViewHidden(event);
        }
    }

    void onViewShown()
    {
        SnackbarManager.getInstance().onShown(managerCallback);
        if (callbacks != null)
        {
            int callbackCount = callbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--)
            {
                callbacks.get(i).onShown((B)this);
            }
        }
    }

    void onViewHidden(int event)
    {
        SnackbarManager.getInstance().onDismissed(managerCallback);
        if (callbacks != null)
        {
            int callbackCount = callbacks.size();
            for (int i = callbackCount - 1; i >= 0; i--)
            {
                callbacks.get(i).onDismissed((B)this, event);
            }
        }

        final ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup)
        {
            ((ViewGroup)parent).removeView(view);
        }
    }

    boolean shouldAnimate()
    {
        return !accessibilityManager.isEnabled();
    }

    @RestrictTo(LIBRARY_GROUP)
    static class SnackbarBaseLayout extends FrameLayout
    {
        private BaseTransientBottomBar.OnLayoutChangeListener onLayoutChangeListener;
        private BaseTransientBottomBar.OnAttachStateChangeListener onAttachStateChangeListener;

        SnackbarBaseLayout(Context context)
        {
            this(context, null);
        }

        SnackbarBaseLayout(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            if (typed.hasValue(R.styleable.SnackbarLayout_android_elevation))
            {
                setElevation(typed.getDimensionPixelSize(R.styleable.SnackbarLayout_android_elevation, 0));
            }
            typed.recycle();
            setClickable(true);
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b)
        {
            super.onLayout(changed, l, t, r, b);
            if (onLayoutChangeListener != null)
            {
                onLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow()
        {
            super.onAttachedToWindow();
            if (onAttachStateChangeListener != null)
            {
                onAttachStateChangeListener.onViewAttachedToWindow(this);
            }
            requestApplyInsets();
        }

        @Override
        protected void onDetachedFromWindow()
        {
            super.onDetachedFromWindow();
            if (onAttachStateChangeListener != null)
            {
                onAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(BaseTransientBottomBar.OnLayoutChangeListener onLayoutChangeListener)
        {
            this.onLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(BaseTransientBottomBar.OnAttachStateChangeListener listener)
        {
            onAttachStateChangeListener = listener;
        }
    }
}