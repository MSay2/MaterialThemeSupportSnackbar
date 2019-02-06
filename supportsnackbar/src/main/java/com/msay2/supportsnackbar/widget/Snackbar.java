package com.msay2.supportsnackbar.widget;

import com.msay2.supportsnackbar.R;
import com.msay2.supportsnackbar.layout.SnackbarContentLayout;

import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.TextView;

public final class Snackbar extends BaseTransientBottomBar<Snackbar>
{
    public static final int LENGTH_INDEFINITE = BaseTransientBottomBar.LENGTH_INDEFINITE;
    public static final int LENGTH_SHORT = BaseTransientBottomBar.LENGTH_SHORT;

    public static final int LENGTH_LONG = BaseTransientBottomBar.LENGTH_LONG;

    private static Snackbar snackbar;

    public static class Callback extends BaseCallback<Snackbar>
    {
        public static final int DISMISS_EVENT_SWIPE = BaseCallback.DISMISS_EVENT_SWIPE;
        public static final int DISMISS_EVENT_ACTION = BaseCallback.DISMISS_EVENT_ACTION;
        public static final int DISMISS_EVENT_TIMEOUT = BaseCallback.DISMISS_EVENT_TIMEOUT;
        public static final int DISMISS_EVENT_MANUAL = BaseCallback.DISMISS_EVENT_MANUAL;
        public static final int DISMISS_EVENT_CONSECUTIVE = BaseCallback.DISMISS_EVENT_CONSECUTIVE;

        @Override
        public void onShown(Snackbar sb)
        {
            // Stub implementation to make API check happy.
        }
        @Override
        public void onDismissed(Snackbar transientBottomBar, @DismissEvent int event)
        {
            // Stub implementation to make API check happy.
        }
    }

    @Nullable private
    BaseCallback<Snackbar> callback;

    private Snackbar(ViewGroup parent, View content, ContentViewCallback contentViewCallback)
    {
        super(parent, content, contentViewCallback);
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @NonNull CharSequence text, @Duration int duration)
    {
        final ViewGroup parent = findSuitableParent(view);
        if (parent == null)
        {
            throw new IllegalArgumentException("No suitable parent found from the given view. " + "Please provide a valid view.");
        }

        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final SnackbarContentLayout content = (SnackbarContentLayout)inflater.inflate(R.layout.snackbar_layout_include, parent, false);

        snackbar = new Snackbar(parent, content, content);
        snackbar.setText(text);
        snackbar.setDuration(duration);

        return snackbar;
    }

    @NonNull
    public static Snackbar make(@NonNull View view, @StringRes int resId, @Duration int duration)
    {
        return make(view, view.getResources().getText(resId), duration);
    }

    @NonNull
    public static Snackbar above(@NonNull View... views)
    {
        snackbar.setAbove(true);
        snackbar.setViewAbove(views);

        return snackbar;
    }

    private static ViewGroup findSuitableParent(View view)
    {
        ViewGroup fallback = null;
        do
        {
            if (view instanceof FrameLayout)
            {
                if (view.getId() == android.R.id.content)
                {
                    return (ViewGroup)view;
                }
                else
                {
                    fallback = (ViewGroup)view;
                }
            }

            if (view != null)
            {
                final ViewParent parent = view.getParent();
                view = parent instanceof View ? (View)parent : null;
            }
        }
        while (view != null);

        return fallback;
    }

    @NonNull
    public Snackbar setText(@NonNull CharSequence message)
    {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout)view.getChildAt(0);
        final TextView tv = contentLayout.getMessageView();
        tv.setText(message);

        return this;
    }

    @NonNull
    public Snackbar setText(@StringRes int resId)
    {
        return setText(getContext().getText(resId));
    }

    @NonNull
    public Snackbar setAction(@StringRes int resId, View.OnClickListener listener)
    {
        return setAction(getContext().getText(resId), listener);
    }

    @NonNull
    public Snackbar setAction(CharSequence text, final View.OnClickListener listener)
    {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout)view.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        if (TextUtils.isEmpty(text) || listener == null)
        {
            tv.setVisibility(View.GONE);
            tv.setOnClickListener(null);
        }
        else
        {
            tv.setVisibility(View.VISIBLE);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(final View view)
                {
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            listener.onClick(view);
                        }
                    }, 665);
                    dispatchDismiss(BaseCallback.DISMISS_EVENT_ACTION);
                }
            });
        }
        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(ColorStateList colors)
    {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout)view.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(colors);

        return this;
    }

    @NonNull
    public Snackbar setActionTextColor(@ColorInt int color)
    {
        final SnackbarContentLayout contentLayout = (SnackbarContentLayout)view.getChildAt(0);
        final TextView tv = contentLayout.getActionView();
        tv.setTextColor(color);

        return this;
    }

    @Deprecated
    @NonNull
    public Snackbar setCallback(Callback callback)
    {
        if (this.callback != null)
        {
            removeCallback(this.callback);
        }
        if (callback != null)
        {
            addCallback(callback);
        }
        this.callback = callback;

        return this;
    }

    @RestrictTo(LIBRARY_GROUP)
    public static final class SnackbarLayout extends BaseTransientBottomBar.SnackbarBaseLayout
    {
        public SnackbarLayout(Context context)
        {
            super(context);
        }

        public SnackbarLayout(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            int childCount = getChildCount();
            int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            for (int i = 0; i < childCount; i++)
            {
                View child = getChildAt(i);
                if (child.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT)
                {
                    child.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(), MeasureSpec.EXACTLY));
                }
            }
        }
    }
}