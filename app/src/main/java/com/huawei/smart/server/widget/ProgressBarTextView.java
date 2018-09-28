package com.huawei.smart.server.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.huawei.smart.server.R;

public class ProgressBarTextView extends FrameLayout {

    View container;
    TextView label;
    TextView value;
    View divider;
    ProgressBar progressBar;

    String labelText;                   // label
    String valueText;                   // Hint
    boolean withDivider;
    int backgroundColor;


    public ProgressBarTextView(Context context) {
        super(context);
        render(context);
    }

    public ProgressBarTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressBarText);
        labelText = a.getString(R.styleable.ProgressBarText_progress_label);
        valueText = a.getString(R.styleable.ProgressBarText_progress_value);
        backgroundColor = a.getColor(R.styleable.ProgressBarText_android_background, ContextCompat.getColor(context, R.color.colorWhite));
        withDivider = a.getBoolean(R.styleable.ProgressBarText_contain_divider, true);
        a.recycle();
        render(context);
    }

    private void render(final Context context) {
        View layout = inflate(context, R.layout.progress_bar_text, this);
        label = layout.findViewById(R.id.label);
        value = layout.findViewById(R.id.value);
        divider = layout.findViewById(R.id.divider);
        container = layout.findViewById(R.id.container);
        progressBar = layout.findViewById(R.id.progress_bar);

        if (!TextUtils.isEmpty(labelText)) {
            label.setText(labelText);
        }

        if (!TextUtils.isEmpty(valueText)) {
            value.setText(valueText);
        }

        if (!withDivider) {
            divider.setVisibility(GONE);
        }

        layout.setBackgroundColor(backgroundColor);
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

    public void setProgressBarDrawable(int d) {
        progressBar.setProgressDrawable(ContextCompat.getDrawable(getContext(), d));
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

    public void setDivider(int visibility) {
        this.divider.setVisibility(visibility);
    }
}
