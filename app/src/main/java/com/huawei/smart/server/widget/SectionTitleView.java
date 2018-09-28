package com.huawei.smart.server.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.huawei.smart.server.R;

public class SectionTitleView extends LinearLayout{

    private TextView mSectionViewTitle = null;

    public SectionTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), (int) getDefaultBottomPadding());
    }

    public SectionTitleView(Context context) {
        super(context);
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

        View mainView = inflate(context, R.layout.form_section_title_view, this);
        mSectionViewTitle = mainView.findViewById(R.id.section_view_title);

        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.SectionBar);
        String header = a.getString(R.styleable.SectionBar_section_title);
        a.recycle();

        if (null != header) {
            mSectionViewTitle.setText(header);
        }

    }

    public void setText(int stringId) {
        mSectionViewTitle.setText(stringId);
    }

    public void setText(String title) {
        mSectionViewTitle.setText(title);
    }
}
