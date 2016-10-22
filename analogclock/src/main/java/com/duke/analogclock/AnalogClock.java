package com.duke.analogclock;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;

import com.andy.analogclock.R;

import java.lang.Math;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * This widget display an analogic clock with two hands for hours and
 * minutes.
 */
public class AnalogClock extends View {
    private Calendar mCalendar;
    private final int mDialWidth;
    private final int mDialHeight;

    private boolean mAttached;

    private final Handler mHandler = new Handler();
    private float mSeconds;
    private float mMinutes;
    private float mHour;
    private boolean mChanged;
    private String mTimeZoneId;
    private boolean mNoSeconds = false;
    /**
     * the ring background
     * */
    private float mDotRadius;
    private float mDotOffset;
    private float mDotStrokeWidth;
    private Paint mDotPaint;
    /**
     * the hour hand
     * */
    private float mHourWidth;
    private float mHourLength;
    private Paint mHourPaint;

    /**
     * the minute hand
     * */
    private float mMinuteWidth;
    private float mMinuteLength;
    private Paint mMinutePaint;

    /**
     * the second hand
     * */
    private float mSecondWidth;
    private float mSecondLength;
    private Paint mSecondPaint;

    private Bitmap mBufferBitmap;
    private Canvas mBufferCanvas;
    private Paint mBufferPaint;

    public AnalogClock(Context context) {
        this(context, null);
    }

    public AnalogClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AnalogClock(Context context, AttributeSet attrs,
                       int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClock, defStyle, R.style.default_analogClock);
        //obtain the ring background
        mDotRadius = a.getDimension(R.styleable.AnalogClock_jewelRadius, 0);
        mDotOffset = a.getDimension(R.styleable.AnalogClock_jewelOffset, 0);
        mDotStrokeWidth = a.getDimension(R.styleable.AnalogClock_jewelStrokeWidth, 0);
        final int dotColor = a.getColor(R.styleable.AnalogClock_jewelColor, Color.WHITE);

        //obtain the hour hand
        mHourWidth = a.getDimension(R.styleable.AnalogClock_hourWidth, 0);
        mHourLength = a.getDimension(R.styleable.AnalogClock_hourLength, 0);
        final int hourColor = a.getColor(R.styleable.AnalogClock_hourColor, Color.WHITE);
        //obtain the minute hand
        mMinuteWidth = a.getDimension(R.styleable.AnalogClock_minuteWidth, 0);
        mMinuteLength = a.getDimension(R.styleable.AnalogClock_minuteLength, 0);
        final int minuteColor = a.getColor(R.styleable.AnalogClock_minuteColor, Color.WHITE);
        //obtain the second hand
        mSecondWidth = a.getDimension(R.styleable.AnalogClock_secondWidth, 0);
        mSecondLength = a.getDimension(R.styleable.AnalogClock_secondLength, 0);
        final int secondColor = a.getColor(R.styleable.AnalogClock_secondColor, Color.RED);
        setElevation(100);
        //init paint color
        if (dotColor != 0) {
            mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDotPaint.setColor(dotColor);
            mDotPaint.setStyle(Paint.Style.FILL);
            mDotPaint.setStrokeWidth(mDotStrokeWidth);
        }
        if(hourColor != 0) {
            mHourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHourPaint.setColor(hourColor);
            mHourPaint.setStrokeWidth(mHourWidth);
        }
        if(minuteColor != 0) {
            mMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mMinutePaint.setColor(minuteColor);
            mMinutePaint.setStrokeWidth(mMinuteWidth);
        }
        if(secondColor != 0) {
            mSecondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSecondPaint.setColor(secondColor);
            mSecondPaint.setStrokeWidth(mSecondWidth);
        }

        mCalendar = Calendar.getInstance();
        mDialWidth = (int)mDotRadius;
        mDialHeight = (int)mDotRadius;

        if(mBufferCanvas == null) {
            mBufferCanvas = new Canvas();
        }
        if(mBufferPaint == null){
            mBufferPaint = new Paint();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            if(getContext() != null){
                getContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
            }
        }

        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
        // in the main thread, therefore the receiver can't run before this method returns.

        // The time zone may have changed while the receiver wasn't registered, so update the Time
        mCalendar = Calendar.getInstance();

        // Make sure we update to the current time
        onTimeChanged();

        // tick the seconds
        post(mClockTick);

    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mAttached = false;
            removeCallbacks(mClockTick);
            if(getContext() != null){
                getContext().unregisterReceiver(mIntentReceiver);
            }
        }
        mHourPaint = null;
        mCalendar = null;
        mDotPaint = null;
        mMinutePaint = null;
        mSecondPaint = null;
        mBufferCanvas = null;
        mBufferPaint = null;
        if(mBufferBitmap != null){
            mBufferBitmap.recycle();
            mBufferBitmap = null;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize =  MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize =  MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth) {
            hScale = (float) widthSize / (float) mDialWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight) {
            vScale = (float )heightSize / (float) mDialHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mDialWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mDialHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;
        if(mBufferBitmap != null) {
            mBufferBitmap.recycle();
            mBufferBitmap = null;
        }
        if(mBufferBitmap == null || mBufferBitmap.getWidth() != w || mBufferBitmap.getHeight() != h) {
            mBufferBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        }
        if(mBufferBitmap != null && mBufferCanvas != null) {
            mBufferCanvas.setBitmap(mBufferBitmap);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOnBuffer();
        canvas.drawBitmap(mBufferBitmap, 0, 0, mBufferPaint);
    }
    private void drawOnBuffer() {
        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }
        int availableWidth = getWidth();
        int availableHeight = getHeight();
        int x = availableWidth / 2;
        int y = availableHeight / 2;
        mDotRadius = Math.min(availableWidth ,availableHeight)/ 2 - mDotStrokeWidth * 2;
        initValuesIfZero();
        if (mDotRadius > 0f && mDotPaint != null) {
            if(mNoSeconds && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                mDotPaint.setShadowLayer(12, 0, 6, Color.parseColor("#FFEEEEEE"));
            }
            mBufferCanvas.drawCircle(x, y, mDotRadius, mDotPaint);
        }
        drawHand(mBufferCanvas, x, y, mHourWidth, 2 * mDotStrokeWidth, mHourLength, mHour / 12.0f * 360.0f, changed, R.color.needle_color1, R.color.needle_color2, mHourPaint);
        drawHand(mBufferCanvas, x, y, mMinuteWidth, 3 * mDotStrokeWidth, mMinuteLength, mMinutes / 60.0f * 360.0f, changed, R.color.needle_color1, R.color.needle_color2, mMinutePaint);
        if(!mNoSeconds) {
            drawHand(mBufferCanvas, x, y, mSecondWidth, 4 * mDotStrokeWidth, mSecondLength, mSeconds / 60.0f * 360.0f, changed, R.color.needle_second, R.color.needle_second, mSecondPaint);
        }
        mBufferCanvas.drawCircle(x, y, 8, mDotPaint);
    }
    /**
     * init some values of hand if it is zero
     *
     * */
    private void initValuesIfZero(){
        if(mHourWidth == 0){
            mHourWidth = mDotStrokeWidth * 2;
        }
        if(mHourLength == 0){
            mHourLength = mDotRadius / 2 + mHourWidth * (mNoSeconds ? 2 : 4);
        }
        if(mMinuteWidth == 0){
            mMinuteWidth = mDotStrokeWidth;
        }
        if(mMinuteLength == 0){
            mMinuteLength = mDotRadius;
        }
        if(mSecondWidth == 0){
            mSecondWidth = mMinuteWidth / 2;
        }
        if(mSecondLength == 0){
            mSecondLength = mDotRadius;
        }
    }
    private void drawHand(Canvas canvas, int x, int y, float widthOffset,float heightOffset, float handLength, float angle,
          boolean changed, int color1, int color2, Paint paint) {
        double sin = Math.sin(angle * Math.PI / 180);
        double cos = Math.cos(angle * Math.PI / 180);
        double sin45 = Math.sin(45 * Math.PI / 180);
        float startX = (float)(x - heightOffset * sin);
        float startY = (float)(y + heightOffset * cos);
        float endX = (float)(x + (handLength - 3.5 * mDotStrokeWidth) * sin);
        float endY = (float)(y - (handLength - 3.5 * mDotStrokeWidth) * cos);
        float xOffset1 = (float)(widthOffset / 2 * sin45 * (sin - cos));
        float yOffset1 = (float)(widthOffset / 2 * sin45 * (sin + cos));
        float xOffset2 = (float)(widthOffset / 2 * sin45 * (cos + sin));
        float yOffset2 = (float)(widthOffset / 2 * sin45 * (cos - sin));

        if(changed) {
            Path path1 = new Path();
            path1.moveTo(startX, startY);
            path1.lineTo(startX + xOffset1, startY - yOffset1);
            path1.lineTo(endX - xOffset2, endY + yOffset2);
            path1.lineTo(endX, endY);
            if(color1 != 0){
                paint.setColor(getResources().getColor(color1));
            }
            canvas.drawPath(path1, paint);
            Path path2 = new Path();
            path2.moveTo(startX, startY);
            path2.lineTo(startX + xOffset2, startY - yOffset2);
            path2.lineTo(endX - xOffset1, endY + yOffset1);
            path2.lineTo(endX, endY);
            if(color2 != 0){
                paint.setColor(getResources().getColor(color2));
            }
            canvas.drawPath(path2, paint);
        }
    }
    private void onTimeChanged() {
//        if (Math.abs(mTime.toMillis(false) - mCalendar.getTimeInMillis()) < 1000) {
//            mTime.setToNow();
//        } else {
//            mTime.set(mCalendar.getTimeInMillis());
//        }

        mCalendar.add(Calendar.SECOND, 1);
        if (mTimeZoneId != null) {
            mCalendar.setTimeZone(TimeZone.getTimeZone(mTimeZoneId));
        }

        int hour = mCalendar.get(Calendar.HOUR);
        int minute = mCalendar.get(Calendar.MINUTE);
        int second = mCalendar.get(Calendar.SECOND);

        mSeconds = second;//(float) ((second * 1000 + millis) / 166.666);
        mMinutes = minute + second / 60.0f;
        mHour = hour + mMinutes / 60.0f;
        mChanged = true;
        try {
            if (mBufferCanvas != null) {
                mBufferCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        updateContentDescription(mCalendar);
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = Calendar.getInstance();
                mCalendar.setTimeZone(TimeZone.getTimeZone(tz));
            }
            onTimeChanged();
            invalidate();
        }
    };

    private final Runnable mClockTick = new Runnable () {

        @Override
        public void run() {
            if(getVisibility() == View.VISIBLE)
            {
                onTimeChanged();
                invalidate();
            }
            AnalogClock.this.postDelayed(mClockTick, 1000);
        }
    };

    private void updateContentDescription(Calendar calendar) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        if(getContext() != null){
            String contentDescription = DateUtils.formatDateTime(getContext(),
                    calendar.getTimeInMillis(), flags);
            setContentDescription(contentDescription);
        }
    }

    public void setTimeZone(String id) {
        mTimeZoneId = id;
        onTimeChanged();
    }

    public void enableSeconds(boolean enable) {
        mNoSeconds = !enable;
    }

    public void setCalendar(Calendar calendar){
        setTimeZone(calendar.getTimeZone().getID());
        mCalendar.setTimeInMillis(calendar.getTimeInMillis());
    }

    public void setTimeInMillis(long timeInMillis){
        mCalendar.setTimeInMillis(timeInMillis);
    }

}

