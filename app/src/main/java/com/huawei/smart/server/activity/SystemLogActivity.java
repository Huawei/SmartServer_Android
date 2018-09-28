package com.huawei.smart.server.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.KeyboardUtils;
import com.flipboard.bottomsheet.BottomSheetLayout;
import com.flipboard.bottomsheet.commons.MenuSheetView;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.adapter.LogEntryAdapter;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.Severity;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.LogEntries;
import com.huawei.smart.server.redfish.model.LogEntry;
import com.huawei.smart.server.redfish.model.LogEntryFilter;
import com.huawei.smart.server.redfish.model.LogService;
import com.huawei.smart.server.utils.WidgetUtils;
import com.huawei.smart.server.widget.EnhanceRecyclerView;
import com.huawei.smart.server.widget.HWMEditText;
import com.huawei.smart.server.widget.SimpleMenuSheetView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

/**
 * 系统日志
 */
public class SystemLogActivity extends BaseActivity {

    @BindView(R.id.refresher) protected RefreshLayout refresher;
    @BindView(R.id.log_level_selection) TextView mLogLevelSelection;
    @BindView(R.id.log_subject_selection) TextView mLogSubjectSelection;
    @BindView(R.id.log_create_date) TextView mLogCreateDateSelection;
    @BindView(R.id.bottomsheet) BottomSheetLayout bottomSheetLayout;
    @BindView(R.id.logList) EnhanceRecyclerView mRecyclerView;
    @BindView(R.id.empty_view) View listEmptyView;
    @BindView(R.id.search) HWMEditText searchEditText;
    @BindView(R.id.cancel) View cancelEditInput;

    View dateRangePickerView;
    DatePicker startTimeView;
    DatePicker endTimeView;

    MenuSheetView mSelectLogLevelSheet;
    MenuSheetView mSelectCreateDateSheet;
    SimpleMenuSheetView mSelectLogSubjectSheet;

    LogService logService;
    LogEntryAdapter logEntryAdapter;
    List<LogService.Subject> eventSubjects = new ArrayList<>();

    // 查询条件
    LogEntryFilter filter = new LogEntryFilter();

    public static java.util.Date getDateFromDatePicker(DatePicker datePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return calendar.getTime();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_log);
        this.initialize(R.string.ds_label_menu_system_log, true);
        initializeView();
    }

    private void initializeView() {
        // 时间范围选择
        initializeSearchEvents();
        initializeListView();
        initializeDateRangeView();
    }

    private void initializeDateRangeView() {
        dateRangePickerView = LayoutInflater.from(this).inflate(R.layout.log_date_range_picker, null);
        startTimeView = dateRangePickerView.findViewById(R.id.startTime);
        endTimeView = dateRangePickerView.findViewById(R.id.endTime);
    }

    private void initializeListView() {
        logEntryAdapter = new LogEntryAdapter(this, new ArrayList<LogEntry>());
        mRecyclerView.setAdapter(logEntryAdapter);
        mRecyclerView.addItemDecoration(WidgetUtils.newDividerItemDecoration(this));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setEmptyView(listEmptyView);

        refresher.setEnableLoadMore(true); // 关闭上拉加载更多
        refresher.setEnableAutoLoadMore(false);
        refresher.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                SystemLogActivity.this.onRefresh(refresher);
            }
        });

        refresher.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                final int itemCount = logEntryAdapter.getItemCount();
                final LogEntryFilter clone = filter.toBuilder().offset(itemCount).build();
                String logEntryOdataId = logService.getId() + "/EntrySet";
                getRedfishClient().systems().getLogEntries(logEntryOdataId, clone, RRLB.<LogEntries>create(SystemLogActivity.this)
                    .callback(
                        new RedfishResponseListener.Callback<LogEntries>() {
                            @Override
                            public void onResponse(Response okHttpResponse, LogEntries entries) {
                                refresher.finishLoadMore(true);
                                logEntryAdapter.addItems(entries.getEntryList());
                                refresher.setNoMoreData(logEntryAdapter.getItemCount() >= entries.getCount());
                            }
                        }
                    ).build());
            }
        });
    }

    private void initializeSearchEvents() {
        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                cancelEditInput.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
            }
        });

        searchEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    filter.setKeyword(searchEditText.getText().toString());
                    showLoadingDialog();
                    onRefresh(null);
                }
                return false;
            }
        });

        searchEditText.setDrawableRightListener(new HWMEditText.DrawableRightListener() {
            @Override
            public void onDrawableRightClick(View view) {
                filter.setKeyword(null);
                searchEditText.setText("");
            }
        });

        cancelEditInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchEditText.setText(filter.getKeyword());
                searchEditText.clearFocus();
                KeyboardUtils.hideSoftInput(SystemLogActivity.this);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLoadingDialog();

        // 初始化默认主体类型
        eventSubjects = new ArrayList<>();
        eventSubjects.add(new LogService.Subject(0, getString(R.string.system_log_select_all)));

        getRedfishClient().systems().getLogService(RRLB.<LogService>create(this).callback(
            new RedfishResponseListener.Callback<LogService>() {
                public void onResponse(Response okHttpResponse, LogService logService) {
                    SystemLogActivity.this.logService = logService;
                    if (logService.getEventSubject() != null) {
                        eventSubjects.addAll(logService.getEventSubject()); // 设置eventSubject
                    }

                    // 加载数据
                    onRefresh(null);
                }
            }).build());
    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        if (logService != null) {
            String logEntryOdataId = logService.getId() + "/EntrySet";
            getRedfishClient().systems().getLogEntries(logEntryOdataId, filter, RRLB.<LogEntries>create(this).callback(
                new RedfishResponseListener.Callback<LogEntries>() {
                    @Override
                    public void onResponse(Response okHttpResponse, LogEntries entries) {
                        final List<LogEntry> entryList = entries.getEntryList();
                        refresher.finishRefresh(true);
                        logEntryAdapter.resetItems(entryList);
                        refresher.setNoMoreData(logEntryAdapter.getItemCount() >= entries.getCount());
                        dismissLoadingDialog();
                    }
                }
            ).build());
        } else {
            refresher.finishRefresh(true);
            refresher.setNoMoreData(true);
            dismissLoadingDialog();
        }
    }

    @OnClick(R.id.log_level_selection)
    void onLogLevelSelectionClick() {
        initSelectLogLevelSheet();
        bottomSheetLayout.showWithSheetView(mSelectLogLevelSheet);
    }

    @OnClick(R.id.log_subject_selection)
    void onLogTypeSelectionClick() {
        initSelectLogSubjectSheet();
        bottomSheetLayout.showWithSheetView(mSelectLogSubjectSheet);
    }

    @OnClick(R.id.log_create_date)
    void onLogCreateDateSelectionClick() {
        initSelectLogCreateDateSheet();
        bottomSheetLayout.showWithSheetView(mSelectCreateDateSheet);
    }

    public void showSelectDateRangeDialog() {
        if (filter.getStartTime() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filter.getStartTime());
            startTimeView.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        }
        if (filter.getEndTime() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filter.getEndTime());
            endTimeView.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        }

        new MaterialDialog.Builder(this)
            .customView(dateRangePickerView, true)
            .positiveText(android.R.string.ok)
            .negativeText(android.R.string.cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    filter.setStartTime(getDateFromDatePicker(startTimeView));
                    ;
                    filter.setEndTime(getDateFromDatePicker(endTimeView));
                    ;
                    mLogCreateDateSelection.setText(R.string.system_log_select_create_date_range);
                    onRefresh(null);
                }
            })
            .show();

    }

    private void initSelectLogLevelSheet() {
        if (mSelectLogLevelSheet == null) {
            mSelectLogLevelSheet =
                new MenuSheetView(this, MenuSheetView.MenuType.LIST, R.string.system_log_select_level_title,
                    new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            item.setChecked(true);
                            Severity selected = null;
                            switch (item.getItemId()) {
                                case R.id.system_log_level_critical:
                                    selected = Severity.Critical;
                                    mLogLevelSelection.setText(R.string.system_log_select_level_critical);
                                    break;
                                case R.id.system_log_level_warning:
                                    selected = Severity.Warning;
                                    mLogLevelSelection.setText(R.string.system_log_select_level_warning);
                                    break;
                                case R.id.system_log_level_ok:
                                    selected = Severity.OK;
                                    mLogLevelSelection.setText(R.string.system_log_select_level_ok);
                                    break;
                                default:
                                    selected = null;
                                    mLogLevelSelection.setText(R.string.system_log_level);
                                    break;
                            }

                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }

                            if (filter.getLevel() != selected) {
                                filter.setLevel(selected);
                                showLoadingDialog();
                                onRefresh(null);
                            }
                            return true;
                        }
                    });

            mSelectLogLevelSheet.inflateMenu(R.menu.system_log_level);
            mSelectLogLevelSheet.setListItemLayoutRes(R.layout.sheet_list_no_icon_item);
        }
    }

    private void initSelectLogCreateDateSheet() {
        if (mSelectCreateDateSheet == null) {
            mSelectCreateDateSheet =
                new MenuSheetView(this, MenuSheetView.MenuType.LIST, R.string.system_log_select_level_title,
                    new MenuSheetView.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            item.setChecked(true);
                            Date startTime = null;
                            Date endTime = null;
                            switch (item.getItemId()) {
                                case R.id.system_log_create_date_today:
                                    startTime = new Date();
                                    endTime = null;
                                    mLogCreateDateSelection.setText(R.string.system_log_select_create_date_today);
                                    break;
                                case R.id.system_log_create_date_seven_days:
                                    startTime = new Date(new Date().getTime() - 7 * 24 * 60 * 60 * 1000L);
                                    endTime = new Date();
                                    mLogCreateDateSelection.setText(R.string.system_log_select_create_date_seven_days);
                                    break;
                                case R.id.system_log_create_date_one_month:
                                    startTime = new Date(new Date().getTime() - 30 * 24 * 60 * 60 * 1000L);
                                    endTime = new Date();
                                    mLogCreateDateSelection.setText(R.string.system_log_select_create_date_one_month);
                                    break;
                                case R.id.system_log_create_data_range:
                                    showSelectDateRangeDialog();
                                    break;
                                default:
                                    startTime = null;
                                    endTime = null;
                                    mLogCreateDateSelection.setText(R.string.system_log_create_date);
                                    break;
                            }

                            if (bottomSheetLayout.isSheetShowing()) {
                                bottomSheetLayout.dismissSheet();
                            }

                            if (!filter.isSameStartTime(startTime) || !filter.isSameEndTime(endTime)) {
                                filter.setStartTime(startTime);
                                filter.setEndTime(endTime);
                                showLoadingDialog();
                                onRefresh(null);
                            }
                            return true;
                        }
                    });
            mSelectCreateDateSheet.inflateMenu(R.menu.system_log_create_date);
            mSelectCreateDateSheet.setListItemLayoutRes(R.layout.sheet_list_no_icon_item);
        }
    }

    private SimpleMenuSheetView initSelectLogSubjectSheet() {
        if (mSelectLogSubjectSheet == null) {
            // build sheet view
            mSelectLogSubjectSheet = new SimpleMenuSheetView<LogService.Subject>(
                this, R.string.system_log_select_type_title);
            mSelectLogSubjectSheet.updateDataSource(this.eventSubjects);
            mSelectLogSubjectSheet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    final LogService.Subject subject =
                        (LogService.Subject) adapterView.getItemAtPosition(position);

                    Integer selected = null;
                    if (subject.getId() == 0) {
                        selected = null;
                        mLogSubjectSelection.setText(R.string.system_log_subject);
                    } else {
                        selected = subject.getId();
                        mLogSubjectSelection.setText(subject.getLabel());
                    }

                    if (bottomSheetLayout.isSheetShowing()) {
                        bottomSheetLayout.dismissSheet();
                    }

                    if (filter.getSubject() != selected) {
                        showLoadingDialog();
                        filter.setSubject(selected);
                        onRefresh(null);
                    }
                }
            });
        }
        return mSelectLogSubjectSheet;
    }

    @OnClick(R.id.title_bar_actions)
    public void onDeleteLogs() {
        new MaterialDialog.Builder(this)
            .content(R.string.system_log_prompt_clear)
            .positiveText(R.string.button_sure)
            .negativeText(R.string.button_cancel)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    SystemLogActivity.this.showLoadingDialog();
                    getRedfishClient().systems().clearLog(logService.getOdataId(), RRLB.<ActionResponse>create(SystemLogActivity.this)
                        .callback(
                            new RedfishResponseListener.Callback<ActionResponse>() {
                                @Override
                                public void onResponse(Response okHttpResponse, ActionResponse response) {
                                    showToast(R.string.msg_action_success, Toast.LENGTH_SHORT, Gravity.CENTER);
                                    onRefresh(null);
                                }
                            }).build());
                }
            })
            .show();


    }
}
