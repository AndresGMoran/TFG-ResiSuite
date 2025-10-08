package com.andresgmoran.apptrabajadores.ui.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CustomBarChartView extends View {

    private Paint paint;
    private Paint textPaint;
    private float[] data = {};
    private boolean hasData = false;

    private final String[] diasSemana = {"L", "M", "X", "J", "V", "S", "D"};

    public CustomBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#4285F4"));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setData(float[] data) {
        this.data = data;
        hasData = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasData || data.length == 0) return;

        int width = getWidth();
        int height = getHeight();
        int marginBottom = 60;
        int marginTop = 30;

        int availableHeight = height - marginBottom - marginTop;
        int barWidth = width / (data.length * 2);

        // Escala máxima
        float maxVal = 0f;
        for (float val : data) maxVal = Math.max(maxVal, val);
        if (maxVal == 0f) maxVal = 1f;

        for (int i = 0; i < data.length; i++) {
            float left = i * 2 * barWidth + barWidth / 2f;
            float barHeight = (data[i] / maxVal) * availableHeight;
            float top = height - marginBottom - barHeight;
            float right = left + barWidth;
            float bottom = height - marginBottom;

            canvas.drawRoundRect(left, top, right, bottom, 20f, 20f, paint);

            float centerX = (left + right) / 2f;
            canvas.drawText(diasSemana[i], centerX, height - 20, textPaint);
        }
    }
}




