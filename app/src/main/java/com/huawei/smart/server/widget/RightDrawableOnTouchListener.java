package com.huawei.smart.server.widget;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public abstract class RightDrawableOnTouchListener implements View.OnTouchListener {

    Drawable drawable;
    private int fuzz = 10;

    TextView view;

    public RightDrawableOnTouchListener(TextView view) {
        this.view = view;
    }

    private Drawable getDrawable() {
        if (this.drawable == null) {
            final Drawable[] drawables = view.getCompoundDrawables();
            if (drawables != null && drawables.length == 4)
                this.drawable = drawables[2];
        }
        return this.drawable;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && getDrawable() != null) {
            final int x = (int) event.getX();
            final int y = (int) event.getY();
            final Rect bounds = getDrawable().getBounds();
            if (x >= (v.getWidth() - v.getPaddingRight() - bounds.width() - view.getCompoundDrawablePadding())
                && x < v.getRight())
            {
                return onDrawableTouch(event);
            }
        }
        return false;
    }

    public abstract boolean onDrawableTouch(final MotionEvent event);
}