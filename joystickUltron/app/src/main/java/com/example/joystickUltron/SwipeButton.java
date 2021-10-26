package com.example.joystickUltron;

import com.example.joystickUltron.MainActivity;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class SwipeButton extends RelativeLayout {
    private ImageView slidingButton;
    private float initialY;
    private boolean active;
    private int initialButtonWidth;
    private TextView centerText;
    private Drawable disabledDrawable;
    private Drawable enabledDrawable;
    public RelativeLayout background;

    public static boolean trancado = true;

    boolean pressionado = false;


    public SwipeButton(Context context) {
        super(context);

        init(context, null, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(21)
    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        background = new RelativeLayout(context);

        LayoutParams layoutParamsView = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        background.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rounded));
        background.setAlpha(0f);
        addView(background, layoutParamsView);

        final TextView centerText = new TextView(context);
        this.centerText = centerText;

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        centerText.setText("S"); //add any text you need
        centerText.setAlpha(0f);
        centerText.setTextColor(Color.WHITE);
        centerText.setPadding(25, 25, 25, 25);
        background.addView(centerText, layoutParams);

        final ImageView swipeButton = new ImageView(context);
        this.slidingButton = swipeButton;

        disabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_lock);
        enabledDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_lock2);
        slidingButton.setImageDrawable(disabledDrawable);
        slidingButton.setPadding(40, 40, 40, 40);

        LayoutParams layoutParamsButton = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        layoutParamsButton.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        swipeButton.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_button));
        swipeButton.setImageDrawable(disabledDrawable);
        addView(swipeButton, layoutParamsButton);
        setOnTouchListener(getButtonTouchListener());
    }

    private OnTouchListener getButtonTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if(MainActivity.conexao == true && pressionado == false && event.getY() > 410){
                            showBar();
                            centerText.setAlpha(0f);
                            pressionado = true;
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:

                        if(pressionado) {
                            if (initialY == 0) {
                                initialY = slidingButton.getY();
                            }
                            slidingButton.setY(event.getY() - slidingButton.getHeight() / 2);
                            if (slidingButton.getY() < 0) {
                                slidingButton.setY(0);
                            }
                            if (slidingButton.getY() > getHeight() - slidingButton.getHeight()) {
                                slidingButton.setY(getHeight() - slidingButton.getHeight());
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if(pressionado) {
                            pressionado = false;
                            hideBar();
                        }
                        if (active) {
                            collapseButton();
                        }else {
                            initialButtonWidth = slidingButton.getHeight();
                            if (slidingButton.getY() <= 0 ) {
                                expandButton();
                            } else {
                                moveButtonBack();
                            }
                        }
                        return true;
                }
                return false;
            }
        };
    }
    private void expandButton() {
        if(trancado){

            Toast.makeText(getContext(), "destrancado!", Toast.LENGTH_SHORT).show();
            slidingButton.setImageDrawable(enabledDrawable);
            trancado = false;
        }
        else{

            Toast.makeText(getContext(), "trancado!", Toast.LENGTH_SHORT).show();
            slidingButton.setImageDrawable(disabledDrawable);
            trancado = true;
        }
        moveButtonBack();
    }

    private void showBar() {
        final ValueAnimator fadeAnimation = ValueAnimator.ofFloat(0,1);
        fadeAnimation.setInterpolator(new LinearInterpolator());
        fadeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) fadeAnimation.getAnimatedValue();
                background.setAlpha(alpha);
            }
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                background, "alpha", 1);

        fadeAnimation.setDuration(200);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, fadeAnimation);
        animatorSet.start();
    }
    private void hideBar() {
        final ValueAnimator fadeAnimation = ValueAnimator.ofFloat(1,0);
        fadeAnimation.setInterpolator(new LinearInterpolator());
        fadeAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float alpha = (Float) fadeAnimation.getAnimatedValue();
                background.setAlpha(alpha);
            }
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                background, "alpha", 0);

        fadeAnimation.setDuration(200);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, fadeAnimation);
        animatorSet.start();
    }

    private void collapseButton() {
        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                slidingButton.getHeight(),
                initialButtonWidth);

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params =  slidingButton.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                slidingButton.setLayoutParams(params);
            }
        });

        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                active = false;
                slidingButton.setImageDrawable(disabledDrawable);
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(objectAnimator, widthAnimator);
        animatorSet.start();
    }
    private void moveButtonBack() {
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(slidingButton.getY(), getHeight() - slidingButton.getHeight());
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float y = (Float) positionAnimator.getAnimatedValue();
                slidingButton.setY(y);
            }
        });
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        positionAnimator.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        animatorSet.start();
    }
}

