package com.xunlei.library.pulltorefresh.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.niuza.android.R;
import com.xunlei.library.pulltorefresh.ILoadingLayout;
import com.xunlei.library.pulltorefresh.PullToRefreshBase.Mode;
import com.xunlei.library.pulltorefresh.PullToRefreshBase.Orientation;

@SuppressLint({"ViewConstructor"})
public abstract class LoadingLayout extends FrameLayout implements ILoadingLayout {
    static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();
    static final String LOG_TAG = "PullToRefresh-LoadingLayout";
    protected final ImageView mHeaderClockBgImage;
    protected final ImageView mHeaderClockHourImage;
    protected final ImageView mHeaderImage;
    protected final ProgressBar mHeaderProgress;
    private final TextView mHeaderText;
    private FrameLayout mInnerLayout;
    protected final Mode mMode;
    private CharSequence mPullLabel;
    private CharSequence mRefreshingLabel;
    private CharSequence mReleaseLabel;
    protected final Orientation mScrollDirection;
    private final TextView mSubHeaderText;
    private boolean mUseIntrinsicAnimation;

    protected abstract int getDefaultDrawableResId();

    protected abstract void onLoadingDrawableSet(Drawable drawable);

    protected abstract void onPullImpl(float f);

    protected abstract void pullToRefreshImpl();

    protected abstract void refreshingImpl();

    protected abstract void releaseToRefreshImpl();

    protected abstract void resetImpl();

    public LoadingLayout(Context context, Mode mode, Orientation scrollDirection, TypedArray attrs) {
        super(context);
        ColorStateList colors;
        this.mMode = mode;
        this.mScrollDirection = scrollDirection;
        switch (scrollDirection) {
            case HORIZONTAL:
                LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_horizontal, this);
                break;
            default:
                LayoutInflater.from(context).inflate(R.layout.pull_to_refresh_header_vertical, this);
                break;
        }
        this.mInnerLayout = (FrameLayout) findViewById(R.id.fl_inner);
        this.mHeaderText = (TextView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_text);
        this.mHeaderProgress = (ProgressBar) this.mInnerLayout.findViewById(R.id.pull_to_refresh_progress);
        this.mSubHeaderText = (TextView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_sub_text);
        this.mHeaderImage = (ImageView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_image);
        this.mHeaderClockBgImage = (ImageView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_bgImage);
        this.mHeaderClockHourImage = (ImageView) this.mInnerLayout.findViewById(R.id.pull_to_refresh_image2);
        LayoutParams lp = (LayoutParams) this.mInnerLayout.getLayoutParams();
        switch (mode) {
            case PULL_FROM_END:
                lp.gravity = scrollDirection == Orientation.VERTICAL ? 48 : 3;
                this.mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label);
                this.mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
                this.mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label);
                break;
            default:
                lp.gravity = scrollDirection == Orientation.VERTICAL ? 80 : 5;
                this.mPullLabel = context.getString(R.string.pull_to_refresh_pull_label);
                this.mRefreshingLabel = context.getString(R.string.pull_to_refresh_refreshing_label);
                this.mReleaseLabel = context.getString(R.string.pull_to_refresh_release_label);
                break;
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
            Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
            if (background != null) {
                ViewCompat.setBackground(this, background);
            }
        }
        TypedValue styleID = null;
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextAppearance)) {
            styleID = new TypedValue();
            attrs.getValue(R.styleable.PullToRefresh_ptrHeaderTextAppearance, styleID);
            setTextAppearance(styleID.data);
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance)) {
            styleID = new TypedValue();
            attrs.getValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance, styleID);
            setSubTextAppearance(styleID.data);
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
            colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
            if (colors != null) {
                setTextColor(colors);
            }
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
            colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
            if (colors != null) {
                setSubTextColor(colors);
            }
        }
        Drawable imageDrawable = null;
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
            imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable);
        }
        switch (mode) {
            case PULL_FROM_END:
                if (!attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableEnd)) {
                    if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableBottom)) {
                        Utils.warnDeprecation("ptrDrawableBottom", "ptrDrawableEnd");
                        imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableBottom);
                        break;
                    }
                }
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableEnd);
                break;
            default:
                if (!attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableStart)) {
                    if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableTop)) {
                        Utils.warnDeprecation("ptrDrawableTop", "ptrDrawableStart");
                        imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableTop);
                        break;
                    }
                }
                imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableStart);
                break;
        }
        if (imageDrawable == null) {
            imageDrawable = context.getResources().getDrawable(getDefaultDrawableResId());
        }
        setLoadingDrawable(imageDrawable);
        reset();
    }

    public final void setHeight(int height) {
        getLayoutParams().height = height;
        requestLayout();
    }

    public final void setWidth(int width) {
        getLayoutParams().width = width;
        requestLayout();
    }

    public final void setPaddingTop(int addTop) {
        int top = this.mInnerLayout.getPaddingTop();
        this.mInnerLayout.setPadding(this.mInnerLayout.getPaddingLeft(), top + addTop, this.mInnerLayout.getPaddingRight(), this.mInnerLayout.getPaddingBottom());
    }

    public final void setPaddingBottom(int addBottom) {
        int top = this.mInnerLayout.getPaddingTop();
        this.mInnerLayout.setPadding(this.mInnerLayout.getPaddingLeft(), top, this.mInnerLayout.getPaddingRight(), this.mInnerLayout.getPaddingBottom() + addBottom);
    }

    public final int getContentSize() {
        switch (this.mScrollDirection) {
            case HORIZONTAL:
                return this.mInnerLayout.getWidth();
            default:
                return this.mInnerLayout.getHeight();
        }
    }

    public final void hideAllViews() {
        if (this.mHeaderText.getVisibility() == 0) {
            this.mHeaderText.setVisibility(View.INVISIBLE);
        }
        if (this.mHeaderProgress.getVisibility() == 0) {
            this.mHeaderProgress.setVisibility(View.INVISIBLE);
        }
        if (this.mHeaderImage.getVisibility() == 0) {
            this.mHeaderImage.setVisibility(View.INVISIBLE);
        }
        if (this.mSubHeaderText.getVisibility() == 0) {
            this.mSubHeaderText.setVisibility(View.INVISIBLE);
        }
    }

    public final void onPull(float scaleOfLayout) {
        if (!this.mUseIntrinsicAnimation) {
            onPullImpl(scaleOfLayout);
        }
    }

    public final void pullToRefresh() {
        if (this.mHeaderText != null) {
            this.mHeaderText.setText(this.mPullLabel);
        }
        pullToRefreshImpl();
    }

    public final void refreshing() {
        if (this.mHeaderText != null) {
            this.mHeaderText.setText(this.mRefreshingLabel);
        }
        if (this.mUseIntrinsicAnimation) {
            ((AnimationDrawable) this.mHeaderImage.getDrawable()).start();
        } else {
            refreshingImpl();
        }
        if (this.mSubHeaderText != null) {
            this.mSubHeaderText.setVisibility(View.GONE);
        }
    }

    public final void releaseToRefresh() {
        if (this.mHeaderText != null) {
            this.mHeaderText.setText(this.mReleaseLabel);
        }
        releaseToRefreshImpl();
    }

    public final void reset() {
        if (this.mHeaderText != null) {
            this.mHeaderText.setText(this.mPullLabel);
        }
        this.mHeaderImage.setVisibility(View.VISIBLE);
        if (this.mUseIntrinsicAnimation) {
            ((AnimationDrawable) this.mHeaderImage.getDrawable()).stop();
        } else {
            resetImpl();
        }
        if (this.mSubHeaderText == null) {
            return;
        }
        if (TextUtils.isEmpty(this.mSubHeaderText.getText())) {
            this.mSubHeaderText.setVisibility(View.GONE);
        } else {
            this.mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    public void setLastUpdatedLabel(CharSequence label) {
        setSubHeaderText(label);
    }

    public final void setLoadingDrawable(Drawable imageDrawable) {
        this.mHeaderImage.setImageDrawable(imageDrawable);
        this.mUseIntrinsicAnimation = imageDrawable instanceof AnimationDrawable;
        onLoadingDrawableSet(imageDrawable);
    }

    public void setPullLabel(CharSequence pullLabel) {
        this.mPullLabel = pullLabel;
    }

    public void setRefreshingLabel(CharSequence refreshingLabel) {
        this.mRefreshingLabel = refreshingLabel;
    }

    public void setReleaseLabel(CharSequence releaseLabel) {
        this.mReleaseLabel = releaseLabel;
    }

    public void setTextTypeface(Typeface tf) {
        this.mHeaderText.setTypeface(tf);
    }

    public final void showInvisibleViews() {
        if (4 == this.mHeaderText.getVisibility()) {
            this.mHeaderText.setVisibility(View.VISIBLE);
        }
        if (4 == this.mHeaderProgress.getVisibility()) {
            this.mHeaderProgress.setVisibility(View.VISIBLE);
        }
        if (4 == this.mHeaderImage.getVisibility()) {
            this.mHeaderImage.setVisibility(View.VISIBLE);
        }
        if (4 == this.mSubHeaderText.getVisibility()) {
            this.mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    private void setSubHeaderText(CharSequence label) {
        if (this.mSubHeaderText == null) {
            return;
        }
        if (TextUtils.isEmpty(label)) {
            this.mSubHeaderText.setVisibility(View.GONE);
            return;
        }
        this.mSubHeaderText.setText(label);
        if (8 == this.mSubHeaderText.getVisibility()) {
            this.mSubHeaderText.setVisibility(View.VISIBLE);
        }
    }

    private void setSubTextAppearance(int value) {
        if (this.mSubHeaderText != null) {
            this.mSubHeaderText.setTextAppearance(getContext(), value);
        }
    }

    private void setSubTextColor(ColorStateList color) {
        if (this.mSubHeaderText != null) {
            this.mSubHeaderText.setTextColor(color);
        }
    }

    private void setTextAppearance(int value) {
        if (this.mHeaderText != null) {
            this.mHeaderText.setTextAppearance(getContext(), value);
        }
        if (this.mSubHeaderText != null) {
            this.mSubHeaderText.setTextAppearance(getContext(), value);
        }
    }

    private void setTextColor(ColorStateList color) {
        if (this.mHeaderText != null) {
            this.mHeaderText.setTextColor(color);
        }
        if (this.mSubHeaderText != null) {
            this.mSubHeaderText.setTextColor(color);
        }
    }
}
