package com.huawei.smart.server.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.huawei.smart.server.R;

public class LabeledTextView extends FrameLayout {

    View container;
    TextView label;
    TextView value;
    View divider;

    String labelText;                   // label
    String valueText;                   // Hint
    Drawable drawableStart = null;      // value绑定的Icon
    Drawable drawableEnd = null;        // value绑定的Icon
    boolean withDivider;
    int backgroundColor;


    public LabeledTextView(Context context) {
        super(context);
        render(context);
    }

    public LabeledTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabeledText);
        labelText = a.getString(R.styleable.LabeledText_label);
        valueText = a.getString(R.styleable.LabeledText_value);
        drawableStart = a.getDrawable(R.styleable.LabeledText_android_drawableStart);
        drawableEnd = a.getDrawable(R.styleable.LabeledText_android_drawableEnd);
        backgroundColor = a.getColor(R.styleable.LabeledText_android_background, ContextCompat.getColor(context, R.color.colorWhite));
        withDivider = a.getBoolean(R.styleable.LabeledText_with_divider, true);
        a.recycle();
        render(context);
    }

    private void render(final Context context) {
        View layout = inflate(context, R.layout.widget_labeled_text, this);
        label = layout.findViewById(R.id.label);
        value = layout.findViewById(R.id.value);
        divider = layout.findViewById(R.id.divider);
        container = layout.findViewById(R.id.container);

        if (!TextUtils.isEmpty(labelText)) {
            label.setText(labelText);
        }

        if (!TextUtils.isEmpty(valueText)) {
            value.setText(valueText);
        }

        updateDrawable();

        divider.setVisibility(withDivider ? VISIBLE : GONE);
        layout.setBackgroundColor(backgroundColor);
    }

    private void updateDrawable() {
        value.setCompoundDrawablesWithIntrinsicBounds(drawableStart, null, drawableEnd, null);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setLabelText(int resourceId) {
        this.label.setText(resourceId);
    }

    public void setLabelText(String text) {
        this.setLabelText(text, false);
    }

    public void setLabelText(String text, boolean dismissIfNull) {
        if (TextUtils.isEmpty(text) && dismissIfNull) {
            container.setVisibility(GONE);
        } else {
            container.setVisibility(VISIBLE);
            this.label.setText(text);
        }
    }

    public void setText(int resourceId) {
        this.value.setText(resourceId);
    }

    public void setText(String text) {
        this.setText(text, false);
    }

    public void setText(String text, boolean dismissIfNull) {
        if (TextUtils.isEmpty(text) && dismissIfNull) {
            container.setVisibility(GONE);
        } else {
            container.setVisibility(VISIBLE);
            this.value.setText(text);
        }
    }

    public void setDrawableStart(int resId) {
        if (resId != 0) {
            this.drawableStart = getResources().getDrawable(resId);
            updateDrawable();
        }
    }

    public void setDrawableEnd(int resId) {
        if (resId != 0) {
            this.drawableEnd = getResources().getDrawable(resId);
            updateDrawable();
        }
    }

    public void dismiss() {
        container.setVisibility(VISIBLE);
    }

    public void setDivider(int visibility) {
        this.divider.setVisibility(visibility);
    }
}
