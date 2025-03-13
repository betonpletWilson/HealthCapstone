package com.fitwizard.fitwizard_v1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class WaterLevelView extends View {
    private Paint backgroundPaint;
    private Paint waterPaint;
    private Paint textPaint;
    private Path wavePath;
    private RectF containerRect;
    private float waterLevel = 0.76f; // 76%

    public WaterLevelView(Context context) {
        super(context);
        init();
    }

    public WaterLevelView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.FILL);

        waterPaint = new Paint();
        waterPaint.setColor(Color.parseColor("#70A1FF")); // Light blue
        waterPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);

        wavePath = new Path();
        containerRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw container background with rounded corners
        containerRect.set(0, 0, width, height);
        canvas.drawRoundRect(containerRect, 40, 40, backgroundPaint);

        // Calculate water height based on percentage
        int waterHeight = (int) (height * waterLevel);
        int waterY = height - waterHeight;

        // Draw water
        wavePath.reset();
        wavePath.moveTo(0, waterY);

        // Add simple wave effect
        /*
         * This is simplified. For a real wave animation,
         * you would use ValueAnimator to animate the wave
         */
        wavePath.lineTo(0, height);
        wavePath.lineTo(width, height);
        wavePath.lineTo(width, waterY);
        wavePath.close();

        canvas.drawPath(wavePath, waterPaint);

        // Draw percentage text
        String percentText = (int)(waterLevel * 100) + "%";
        float textX = width / 2f;
        float textY = height / 2f + 10; // Adjust for text centering
        canvas.drawText(percentText, textX, textY, textPaint);
    }

    public void setWaterLevel(float level) {
        this.waterLevel = Math.max(0, Math.min(1, level));
        invalidate();
    }
}