package com.example.joystickUltron;

import android.content.Context;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class JoystickView extends View implements Runnable {
    // Constants
    public static String corBtnBorder = "#81b7ff";
    public static String corBtn = "#81b7ff";


    private final double RAD = 57.2957795;
    public final static long DEFAULT_LOOP_INTERVAL = 100; // 100 ms
    public final static int FRONT = 3;
    public final static int FRONT_RIGHT = 4;
    public final static int RIGHT = 5;
    public final static int RIGHT_BOTTOM = 6;
    public final static int BOTTOM = 7;
    public final static int BOTTOM_LEFT = 8;
    public final static int LEFT = 1;
    public final static int LEFT_FRONT = 2;
    // Variables
    private OnJoystickMoveListener onJoystickMoveListener; // Listener
    private Thread thread = new Thread(this);
    private long loopInterval = DEFAULT_LOOP_INTERVAL;
    private int xPosition = 0; // Touch x position
    private int yPosition = 0; // Touch y position
    private double centerX = 0; // Center view x position
    private double centerY = 0; // Center view y position
    private Paint mainCircle;
    private Paint secondaryCircle;
    private Paint button;
    private Paint buttonBorder;

    private Paint horizontalLine;
    private Paint verticalLine;
    private int joystickRadius;
    private int buttonRadius;
    private int lastPower = 0;

    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public JoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView();
    }

    protected void initJoystickView() {

        mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainCircle.setColor(Color.parseColor("#81b7ff"));
        mainCircle.setStyle(Paint.Style.STROKE);

        mainCircle.setStrokeWidth(12);

        secondaryCircle = new Paint();
        secondaryCircle.setColor(Color.BLACK);
        secondaryCircle.setStyle(Paint.Style.STROKE);

        verticalLine = new Paint();
        verticalLine.setStrokeWidth(5);
        verticalLine.setColor(Color.RED);
        verticalLine.setAlpha(0);

        horizontalLine = new Paint();
        horizontalLine.setStrokeWidth(2);
        horizontalLine.setColor(Color.BLACK);
        horizontalLine.setAlpha(0);


        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.parseColor("#626262"));
        button.setStyle(Paint.Style.FILL);

        buttonBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonBorder.setStrokeWidth(12);
        buttonBorder.setColor(Color.parseColor("#000000"));
        buttonBorder.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = (int) getWidth() / 2;
        yPosition = (int) getWidth() / 2;
        int d = Math.min(xNew, yNew);
        buttonRadius = (int) (d / 2 * 0.25);
        joystickRadius = (int) (d / 2 * 0.75);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

        setMeasuredDimension(d, d);

    }

    private int measure(int measureSpec) {
        int result = 0;

        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        button.setColor(Color.parseColor(corBtn));
        buttonBorder.setColor(Color.parseColor(corBtnBorder));
        mainCircle.setColor(Color.parseColor(corBtnBorder));

        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

        // painting the main circle
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius,
                mainCircle);
        // painting the secondary circle
        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius / 2,
                secondaryCircle);
        // paint lines
        canvas.drawLine((float) centerX, (float) centerY, (float) centerX,
                (float) (centerY - joystickRadius), verticalLine);
        canvas.drawLine((float) (centerX - joystickRadius), (float) centerY,
                (float) (centerX + joystickRadius), (float) centerY,
                horizontalLine);
        canvas.drawLine((float) centerX, (float) (centerY + joystickRadius),
                (float) centerX, (float) centerY, horizontalLine);

        // painting the move button
        canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
        canvas.drawCircle(xPosition, yPosition, buttonRadius, buttonBorder);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPosition = (int) event.getX();
        yPosition = (int) event.getY();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }
        invalidate();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            MainActivity.pressionado = false;
            xPosition = (int) centerX;
            yPosition = (int) centerY;
            thread.interrupt();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getXPower(), getYPower());
        }
        if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(!MainActivity.pressionado){ MainActivity.pressionado = true;}
        }
        if (onJoystickMoveListener != null
                && event.getAction() == MotionEvent.ACTION_DOWN) {
            if(!MainActivity.pressionado){ MainActivity.pressionado = true;}
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getXPower(), getYPower());
        }
        return true;
    }

    private int getXPower() {
        return (int) (100* (xPosition - centerX) / joystickRadius);
    }

    private int getYPower() {
        return (int) -(100* (yPosition - centerY) / joystickRadius);
    }


    public void setOnJoystickMoveListener(OnJoystickMoveListener listener,
                                          long repeatInterval) {
        this.onJoystickMoveListener = listener;
        this.loopInterval = repeatInterval;
    }

    public interface OnJoystickMoveListener {
        public void onValueChanged(int xPower, int yPower);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            post(new Runnable() {
                public void run() {
                    if (onJoystickMoveListener != null)
                        onJoystickMoveListener.onValueChanged(getXPower(),
                                getYPower());
                }
            });
            try {
                Thread.sleep(loopInterval);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
