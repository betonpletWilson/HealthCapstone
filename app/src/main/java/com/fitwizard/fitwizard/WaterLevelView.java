package com.fitwizard.fitwizard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;



//This is for the graphics component on the home screen
//User views and adds/subtracts from the amount of water they have drank that day

public class WaterLevelView extends View {
    private Paint backgroundPaint;
    private Paint waterPaint;
    private Paint textPaint;
    private Path waterPath;
    private RectF containerRect;
    private float waterLevel = 0.76f; // 76%
    private float cornerRadius = 40f;

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
        waterPaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(30);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        waterPath = new Path();
        containerRect = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw container background with rounded corners
        containerRect.set(0, 0, width, height);
        canvas.drawRoundRect(containerRect, cornerRadius, cornerRadius, backgroundPaint);

        // Calculate water height based on percentage
        int waterHeight = (int) (height * waterLevel);
        int waterY = height - waterHeight;

        // Create a clipping path for the container shape
        Path clipPath = new Path();
        clipPath.addRoundRect(containerRect, cornerRadius, cornerRadius, Path.Direction.CW);

        // Save canvas state before clipping
        canvas.save();
        canvas.clipPath(clipPath);

        // Draw water rectangle (will be clipped to container shape)
        waterPath.reset();
        waterPath.moveTo(0, waterY);
        waterPath.lineTo(0, height);
        waterPath.lineTo(width, height);
        waterPath.lineTo(width, waterY);
        waterPath.close();
        canvas.drawPath(waterPath, waterPaint);

        // Draw percentage text
        String percentText = (int)(waterLevel * 100) + "%";
        float textX = width / 2f;
        float textY = height / 2f + 10; // Adjust for text centering
        canvas.drawText(percentText, textX, textY, textPaint);

        // Restore canvas to unclipped state
        canvas.restore();
    }

    public void setWaterLevel(float level) {
        this.waterLevel = Math.max(0, Math.min(1, level));
        invalidate();
    }
}