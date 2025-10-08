package com.andresgmoran.apptrabajadores.ui.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CustomPieChartView extends View {

    private Paint paint;
    private RectF rect;
    private float[] values = {};
    private int[] colors = {};
    private boolean hasData = false;

    public CustomPieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rect = new RectF();
    }

    public void setData(float[] values, int[] colors) {
        this.values = values;
        this.colors = colors;
        hasData = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!hasData || values.length == 0 || colors.length != values.length) return;

        float total = 0;
        for (float val : values) total += val;

        float startAngle = -90f;
        int size = Math.min(getWidth(), getHeight());
        float padding = 40;
        rect.set(padding, padding, size - padding, size - padding);

        for (int i = 0; i < values.length; i++) {
            float sweepAngle = (values[i] / total) * 360f;
            paint.setColor(colors[i]);
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
            startAngle += sweepAngle;
        }

        paint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, size / 4f, paint);
    }
}

