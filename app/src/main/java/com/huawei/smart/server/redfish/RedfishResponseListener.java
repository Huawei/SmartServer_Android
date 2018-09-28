package com.huawei.smart.server.redfish;

import android.support.annotation.NonNull;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.model.ActionResponse;

import org.slf4j.LoggerFactory;

import okhttp3.Response;

import static java.net.HttpURLConnection.HTTP_NOT_IMPLEMENTED;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class RedfishResponseListener<T> implements OkHttpResponseAndParsedRequestListener<T> {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(RedfishResponseListener.class.getSimpleName());

    public static final String TAG_REDFISH = "redfish";
    public static final String TOO_MANY_SESSION = "Reduce the number of other sessions before trying to establish the session" +
        " or increase the limit of simultaneous sessions (if supported).";

    private final BaseActivity activity;
    private final Callback<T> callback;

    public RedfishResponseListener(BaseActivity activity, Callback<T> callback) {
        this.activity = activity;
        this.callback = callback;
        this.callback.setActivity(activity);
    }

    public static <T> RRLB<T> builder() {
        return new RRLB<T>();
    }

    /**
     * 处理FAN异常
     *
     * @param anError
     */
    public static void handleFANError(final BaseActivity activity, ANError anError) {
        // dismiss loading dialog if necessary
        activity.dismissLoadingDialog();
        final int code = anError.getErrorCode();
        if (code == HTTP_UNAUTHORIZED) {
            final ActionResponse actionResponse = activity.getRedfishClient().getAuthActionResponse();
            int resolutionResId = R.string.msg_auth_illegal_account;
            if (actionResponse.getError().getExtendedInfoList() != null) {
                final ActionResponse.ExtendedInfo extendedInfo = actionResponse.getError().getExtendedInfoList().get(0);
                String resolution = extendedInfo.getResolution();
                if (TOO_MANY_SESSION.equalsIgnoreCase(resolution)) {
                    resolutionResId = R.string.msg_auth_too_many_session;
                }
            }

            new MaterialDialog.Builder(activity)
                .title(R.string.msg_auth_failed_title)
                .content(resolutionResId)
                .positiveText(R.string.button_sure)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ActivityUtils.finishActivity(activity);
                    }
                })
                .show();
        }
        else if (code == HTTP_NOT_IMPLEMENTED) {
            new MaterialDialog.Builder(activity)
                .title(R.string.msg_action_failed)
                .content(R.string.msg_not_implement)
                .positiveText(R.string.button_sure)
                .show();
        }
        else {
            if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                activity.showToast(R.string.msg_access_api_failed, Toast.LENGTH_SHORT, Gravity.CENTER);
                activity.finishLoadingViewData(false);
            }
        }
    }

    @Override
    public void onResponse(Response okHttpResponse, T response) {
        callback._onResponse(okHttpResponse, response);
    }

    @Override
    public void onError(ANError anError) {
        callback.onError(anError);
    }

    public static abstract class Callback<T> {
        protected BaseActivity activity;

        public abstract void onResponse(Response okHttpResponse, T response);

        protected void _onResponse(Response okHttpResponse, T response) {
            try {
                this.onResponse(okHttpResponse, response);
            } catch (Exception e) {
                LOG.error("Failed to process redfish response", e);
            }
        }

        public void onError(ANError anError) {
            try {
                LOG.error("Access Redfish API failed", anError.getCause());
                handleFANError(activity, anError);
            } catch (Exception e) {
                LOG.error("Failed to process redfish error response", e);
            }
        }

        public void setActivity(BaseActivity activity) {
            this.activity = activity;
        }
    }

}
