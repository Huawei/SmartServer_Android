package com.huawei.smart.server.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.huawei.smart.server.R;
import com.suke.widget.SwitchButton;

public class LabeledSwitch extends FrameLayout {

    View container;
    TextView label;
    TextView toggleLabel;
    SwitchButton toggle;
    View divider;


    String labelText;                   // label
    String toggleLabelText;                   // label
    boolean withDivider;
    int backgroundColor;
    boolean isToggleChecked;


    public LabeledSwitch(Context context) {
        super(context);
        render(context);
    }

    public LabeledSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabeledSwitch);
        labelText = a.getString(R.styleable.LabeledSwitch_label_value);
        toggleLabelText = a.getString(R.styleable.LabeledSwitch_toggle_label);
        backgroundColor = a.getColor(R.styleable.LabeledSwitch_android_background, ContextCompat.getColor(context, R.color.transparentColor));
        withDivider = a.getBoolean(R.styleable.LabeledSwitch_has_divider, true);
        isToggleChecked = a.getBoolean(R.styleable.LabeledSwitch_toggle_checked, false);
        a.recycle();
        render(context);
    }

    private void render(final Context context) {
        View layout = inflate(context, R.layout.widget_labeled_switch, this);
        label = layout.findViewById(R.id.label);
        toggle = layout.findViewById(R.id.toggle);
        toggleLabel = layout.findViewById(R.id.toggle_label);
        divider = layout.findViewById(R.id.divider);
        container = layout.findViewById(R.id.container);

        if (!TextUtils.isEmpty(labelText)) {
            label.setText(labelText);
        }

        if (!TextUtils.isEmpty(toggleLabelText)) {
            toggleLabel.setText(toggleLabelText);
            toggleLabel.setVisibility(View.VISIBLE);
        } else {
            toggleLabel.setVisibility(View.GONE);
        }

        if (!withDivider) {
            divider.setVisibility(GONE);
        }

        toggle.setChecked(isToggleChecked);
        layout.setBackgroundColor(backgroundColor);
        layout.invalidate();
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

    public boolean isChecked() {
        return toggle.isChecked();
    }

    public void setChecked(boolean checked) {
        toggle.setChecked(checked);
    }

    public void setEnabled(boolean enabled) {
        toggle.setEnabled(enabled);
    }

    public void setOnCheckedChangeListener(SwitchButton.OnCheckedChangeListener listener) {
        toggle.setOnCheckedChangeListener(listener);
    }

    public void setOnClickListener(SwitchButton.OnClickListener listener) {
        toggle.setOnClickListener(listener);
    }

    public void setOnContainerClickListener(OnClickListener listener) {
        container.setOnClickListener(listener);
    }

    public void showDivider(boolean show) {
        this.divider.setVisibility(show ? VISIBLE : GONE);
    }
}
