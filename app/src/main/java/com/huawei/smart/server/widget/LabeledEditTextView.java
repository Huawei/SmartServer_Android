package com.huawei.smart.server.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.huawei.smart.server.R;

public class LabeledEditTextView extends FrameLayout {

    TextView label;
    EditText inputEditText;
    ImageView inputEditTextIcon;
    TextInputLayout inputLayout;

    String labelText;            // label
    String inputHint;            // Hint
    String inputValue;           // 初始值
    int inputType;               // 输入框模式 - 密码/数字/..
    int inputGravity;           // 输入框模式 - 密码/数字/..
    Drawable inputIcon;          // 右边Icon
    boolean clickable;          // 点击模式
    BorderType borderType;       // 边框设置
    View bottomDivider;


    public LabeledEditTextView(Context context) {
        super(context);
        render(context);
    }

    public LabeledEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabeledEditText);
        labelText = a.getString(R.styleable.LabeledEditText_label_text);
        inputHint = a.getString(R.styleable.LabeledEditText_input_hint);
        inputValue = a.getString(R.styleable.LabeledEditText_input_value);
        inputType = a.getInt(R.styleable.LabeledEditText_android_inputType, InputType.TYPE_CLASS_TEXT);
        inputGravity = a.getInt(R.styleable.LabeledEditText_android_gravity, Gravity.LEFT);
        inputIcon = a.getDrawable(R.styleable.LabeledEditText_input_icon);
        clickable = a.getBoolean(R.styleable.LabeledEditText_clickable, false);
        borderType = BorderType.from(a.getString(R.styleable.LabeledEditText_border_type));
        a.recycle();
        render(context);
    }

    private void render(final Context context) {
        View layout = inflate(context, R.layout.widget_labeled_edit_text, this);
        label = layout.findViewById(R.id.label);
        inputEditText = layout.findViewById(R.id.input);
        inputEditTextIcon = layout.findViewById(R.id.icon);
        bottomDivider = layout.findViewById(R.id.bottom_divider);
        inputLayout = layout.findViewById(R.id.input_layout);

        label.setText(labelText);
        inputEditText.setHint(inputHint);
        inputEditText.setText(inputValue);
        inputEditText.setInputType(inputType);
        inputEditText.setGravity(inputGravity | Gravity.CENTER_VERTICAL);

        setupBorder(layout);
        showInputIcon();
        setClickable(clickable);
    }

    private void setupBorder(View layout) {
        switch (borderType) {
            case None:
                break;
            case Both:
                break;
            case Top:
                break;
            case Bottom:
                bottomDivider.setVisibility(VISIBLE);
                break;
        }
    }

    private void showInputIcon() {
        // render input icon
        if (inputIcon != null) {
            inputEditTextIcon.setImageDrawable(inputIcon);
            inputEditTextIcon.setVisibility(VISIBLE);

            // 重置padding-right
            int paddingRight = getResources().getDimensionPixelOffset(R.dimen.form_list_item_input_with_icon_addition_padding);
            inputEditText.setPadding(inputEditText.getPaddingLeft(), inputEditText.getPaddingTop(),
                paddingRight, inputEditText.getPaddingBottom());
        }
    }

    public void setClickable(boolean clickable) {
        if (clickable) {
            inputEditText.setFocusable(false);
            inputEditText.setCursorVisible(false);
            inputEditText.setClickable(false);
            inputEditText.setLongClickable(false);
            inputEditText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    LabeledEditTextView.this.performClick();
                }
            });
        } else {
            inputEditText.setFocusable(true);
            inputEditText.setCursorVisible(true);
            inputEditText.setClickable(true);
            inputEditText.setLongClickable(true);
            inputEditText.setOnClickListener(null);
        }
    }

    public void enableInputEditText(boolean enable) {
        inputEditText.setEnabled(enable);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public EditText getInputEditText() {
        return inputEditText;
    }

    public Editable getText() {
        return this.inputEditText.getText();
    }

    public void setText(int resourceId) {
        this.inputEditText.setText(resourceId);
    }

    public void setText(String text) {
        this.inputEditText.setText(text);
    }

    public void setTogglePassword(boolean enable) {
        this.inputLayout.setPasswordVisibilityToggleEnabled(enable);
    }

    enum BorderType {
        None, Both, Top, Bottom;

        public static BorderType from(String borderType) {
            final BorderType[] values = BorderType.values();
            for (BorderType bt : values) {
                if (bt.name().equalsIgnoreCase(borderType)) {
                    return bt;
                }
            }
            return BorderType.None;
        }
    }

}
