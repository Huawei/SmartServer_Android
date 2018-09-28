package com.huawei.smart.server.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.smart.server.utils.Compatibility;
import com.huawei.smart.server.utils.DensityUtils;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;

public class HeaderViewBase extends LinearLayout implements OnClickListener {

    private ImageView btnRight = null;
    private ImageView btnLeft = null;

    private TextView title = null;
    private TextView subtitle = null;

    private boolean headerAutoSize;

    private HeaderButtons hbc = null;

    private Context mContext;

    public HeaderViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context, attrs);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (int) getDefaultBottomPadding());
    }

    public HeaderViewBase(Context context) {
        super(context);
        mContext = context;
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (int) getDefaultBottomPadding());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }


    public float getDefaultBottomPadding() {
        return getResources().getDimensionPixelSize(R.dimen.actoin_bar_border_width);
    }

    private void init(Context context, AttributeSet attrs) {

        View mainView = inflate(context, R.layout.header, this);
        btnLeft = mainView.findViewById(R.id.btnTopLeft);
        btnRight = mainView.findViewById(R.id.btnTopRight);

        title = mainView.findViewById(R.id.title);
        subtitle = mainView.findViewById(R.id.subtitle);


        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HeaderBar);

        String header = a.getString(R.styleable.HeaderBar_header_header_text);
        headerAutoSize = a.getBoolean(R.styleable.HeaderBar_header_header_autosize, false);
        Drawable leftButton = a.getDrawable(R.styleable.HeaderBar_header_left_button_drawable);
        Drawable rightButton = a.getDrawable(R.styleable.HeaderBar_header_right_button_drawable);
//        boolean isShowMenuButton = a.getBoolean(R.styleable.HeaderBar_header_menu_button_label, false);
//
//        Drawable rightButtonDrawable = a.getDrawable(R.styleable.HeaderBar_header_right_button_drawable);
//        Drawable leftButtonDrawable = a.getDrawable(R.styleable.HeaderBar_header_left_button_drawable);
//        Drawable rightFirstButtonDrawable = a.getDrawable(R.styleable.HeaderBar_header_right_first_button_drawable);
//        Drawable rightSecondButtonDrawable = a.getDrawable(R.styleable.HeaderBar_header_right_second_button_drawable);
//        boolean isNeedBackGround = a.getBoolean(R.styleable.HeaderBar_header_is_need_background, true);
//        boolean mShouldResetTextColor = a.getBoolean(R.styleable.HeaderBar_header_left_button_is_need_background, true);
        int titleBarTextColor = a.getColor(R.styleable.HeaderBar_header_title_text_color, ContextCompat.getColor(context, R.color.colorWhite));
        a.recycle();

        if (null != rightButton) {
            btnRight.setImageDrawable(rightButton);
            btnRight.setVisibility(View.VISIBLE);
            btnRight.setOnClickListener(this);
        }

        if (null != leftButton) {
            btnLeft.setImageDrawable(leftButton);
            btnLeft.setVisibility(View.VISIBLE);
            btnLeft.setOnClickListener(this);
        }

        if (null != header) {
            title.setText(header);
        }

    }

    public void setRightEnabled(boolean enabled) {
        if (btnRight.isEnabled() != enabled) {
            btnRight.setEnabled(enabled);
            titleInvalidate();
        }
    }

    public void setRightEnabledForVoipReplyMessage(boolean enabled) {
        if (btnRight.isEnabled() != enabled) {
            btnRight.setEnabled(enabled);
        }
    }


    public void setTitleMinWidth(Activity activity) {
        btnRight.setVisibility(View.GONE);
        int offset = DensityUtils.dip2px(activity, activity.getResources().getDimension(R.dimen.header_btn_width));
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int screen_width = metric.widthPixels;
        //title.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.FILL_PARENT));
    }

    /**
     * Button
     *
     * @param resId
     */
    public void setRightButton(int resId) {
        btnRight.setVisibility(View.VISIBLE);
        btnRight.setImageResource(resId);
        btnRight.setOnClickListener(this);
    }

    /**
     * Button
     *
     * @param visibility
     */
    public void setRightVisibility(int visibility) {
        btnRight.setVisibility(visibility);
        if (VISIBLE == btnRight.getVisibility()) {
            btnRight.setOnClickListener(this);
        } else {
            btnRight.setOnClickListener(null);
        }
    }

    /**
     * Button
     *
     * @param resId
     */
    public void setLefButton(int resId) {
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setImageResource(resId);
        btnLeft.setOnClickListener(this);
    }

    /**
     * Button
     *
     * @param visibility
     */
    public void setLeftVisibility(int visibility) {
        btnLeft.setVisibility(visibility);
        if (VISIBLE == btnLeft.getVisibility()) {
            btnLeft.setOnClickListener(this);
        } else {
            btnLeft.setOnClickListener(null);
        }
    }

    public void setButtonsClickCallback(HeaderButtons _hbc) {
        hbc = _hbc;
    }

    public interface HeaderButtons {

        void onLeftButtonClicked();


        void onRightButtonClicked();
    }

    @Override
    public void onClick(View v) {
        if (null == hbc) {
            return;
        }

        switch (v.getId()) {
            case R.id.btnTopLeft:
                hbc.onLeftButtonClicked();
                break;
            case R.id.btnTopRight:
                hbc.onRightButtonClicked();
                break;
        }
    }

    public void setTitleText(int stringId) {
        title.setText(stringId);
    }

    public void setTitleText(String title) {
        this.title.setText(title);
    }

    public void setSubTitleText(String title) {
        subtitle.setVisibility(View.VISIBLE);
        this.subtitle.setText(title);
    }

    public void setNameText(String name) {
        title.setText(name);
    }

    public void setNameText(String name, Activity activity) {
        float mTextWidth = title.getPaint().measureText(name);
        int offset = activity.getResources().getDimensionPixelSize(R.dimen.header_btn_width);
        int paddingLeft = activity.getResources().getDimensionPixelSize(R.dimen.header_padding_left_right);
        float width = HWConstants.screenWidth - 2 * offset - 2 * paddingLeft;
        if (width < mTextWidth) {
            setTitleMinWidth(activity);
        }
        title.setText(name);
    }

    private OnFinishSendListener onFinishSendListener;


    public void setOnFinishSendListener(OnFinishSendListener listener) {
        onFinishSendListener = listener;
    }

    public interface OnFinishSendListener {
        void onFinishSend();
    }

    /**
     * StringUtils workaround for Android 4.0 or above
     */
    private void titleInvalidate() {
        if (title != null && Compatibility.isCompatibleWith(14)) {
            title.invalidate();
        }
    }

}
