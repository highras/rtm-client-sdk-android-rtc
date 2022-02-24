package com.highras.voiceDemo.weight;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import com.highras.voiceDemo.R;
import com.highras.voiceDemo.common.DisplayUtils;

public class CustomSwitch extends FrameLayout implements View.OnClickListener {

    private static String TAG = "OnOffView";

    private int onColor = 0;
    private int offColor = 0;
    private float iconSize = 0;
    private float iconPaddingSize = 0;
    private int animationTime = 450;

    private CardView icon;

    private boolean animationRun = false;

    private ImageView backView;

    private boolean defOff = false;
    private boolean defOff2 = false;

    private CheckBoxCall checkBoxCall;

    public CustomSwitch(Context context) {
        super(context);
    }

    Context context;

    @RequiresApi(api = Build.VERSION_CODES.P)
    public CustomSwitch(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.OnOffView);
        iconSize = typedArray.getDimension(R.styleable.OnOffView_iconSize, 0f);
        iconPaddingSize = typedArray.getDimension(R.styleable.OnOffView_paddingSize, 5);
        defOff2 = defOff = typedArray.getBoolean(R.styleable.OnOffView_defOff, false);
        onColor = typedArray.getColor(R.styleable.OnOffView_onColor, 0x4CAF50);
        offColor = typedArray.getColor(R.styleable.OnOffView_offColor, 0xFFFFFF);

        backView = new ImageView(getContext());
        addView(backView);
        LayoutParams layoutParams1 = (LayoutParams) backView.getLayoutParams();
        layoutParams1.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams1.width = ViewGroup.LayoutParams.MATCH_PARENT;

        icon = new CardView(getContext());
        addView(icon);
        LayoutParams layoutParams2 = (LayoutParams) icon.getLayoutParams();
        layoutParams2.height = (int) (iconSize - iconPaddingSize * 2);
        layoutParams2.width = (int) (iconSize - iconPaddingSize * 2);
        icon.setX(iconPaddingSize);
        icon.setY(iconPaddingSize);
        icon.setRadius(((iconSize - iconPaddingSize)) / 2);
        typedArray.recycle();
        setOnClickListener(this);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initIcon();
    }

    public CustomSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomSwitch(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startAnimation(final boolean isOff) {
        TranslateAnimation animation;
        int w = backView.getMeasuredWidth();

        if (w == 0 || w == -1) {
            w = backView.getWidth();
        }

        if (!defOff2) {
            if (isOff)
                animation = new TranslateAnimation(w - icon.getLayoutParams().width - iconPaddingSize * 2, 0, 0, 0);
            else
                animation = new TranslateAnimation(0, w - icon.getLayoutParams().width - iconPaddingSize * 2, 0, 0);
        } else {
            if (isOff)
                animation = new TranslateAnimation(0, -(w - icon.getLayoutParams().width - iconPaddingSize * 2), 0, 0);
            else
                animation = new TranslateAnimation(-(w - icon.getLayoutParams().width - iconPaddingSize * 2), 0, 0, 0);
        }
        animation.setDuration(animationTime);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        animation.setFillAfter(true);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animationRun = true;

                //如果想动画结束再执行回调的话， 就把这段话放在  onAnimationEnd 就阔以了
                if (checkBoxCall != null) {
                    checkBoxCall.check(!isOff);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                animationRun = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        icon.startAnimation(animation);

        GradientDrawable background = (GradientDrawable) backView.getBackground();

        ValueAnimator animator;
        if (isOff)
            animator = ObjectAnimator.ofInt(background, "color", onColor, offColor);
        else
            animator = ObjectAnimator.ofInt(background, "color", offColor, onColor);
        animator.setDuration(animationTime);
        animator.setEvaluator(new ArgbEvaluator());//渐变色平滑
        animator.start();
    }

    private void initIcon() {
        if (defOff) {
            icon.setX(backView.getMeasuredWidth() - icon.getLayoutParams().width - iconPaddingSize);
        } else {
            icon.setX(iconPaddingSize);
        }
        GradientDrawable drawable = new GradientDrawable();
        //小球圆角角度
        drawable.setCornerRadius(DisplayUtils.dip2px(context, 50));
        //外框颜色
        drawable.setStroke(DisplayUtils.dip2px(context, 1), Color.parseColor("#EAEAEA"));
        if (defOff)
            drawable.setColor(onColor);
        else
            drawable.setColor(offColor);
        backView.setBackground(drawable);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View v) {
        animationCheck();
    }

    //设置初始状态  也可以在xml中设置  app:defOff = false|true
    public void setDefOff(boolean off) {
        defOff2 = defOff = off;
        initIcon();
    }

    //动画切换按钮状态
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void animationCheck() {
        if (animationRun)
            return;
        if (defOff) {
            startAnimation(true);
            defOff = false;
        } else {
            startAnimation(false);
            defOff = true;
        }
    }

    public void setCheckBoxCall(CheckBoxCall checkBoxCall) {
        this.checkBoxCall = checkBoxCall;
    }

    public interface CheckBoxCall {
        void check(boolean isOff);
    }
}
