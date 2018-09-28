package com.huawei.smart.server.upgrade;

import android.support.annotation.NonNull;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.androidnetworking.interfaces.DownloadProgressListener;
import com.androidnetworking.interfaces.ParsedRequestListener;

import java.io.File;
import java.util.Map;


/**
 * 网络接口
 * <p>
 * Copied from https://github.com/WVector/AppUpdate
 */
public class FANHttpManager extends HttpManager {

    /**
     * 异步get
     *
     * @param url      get请求地址
     * @param params   get参数
     * @param callBack 回调
     */
    public void asyncGet(
        @NonNull String url, @NonNull Map<String, String> params, @NonNull final HttpManager.Callback callBack) {
        AndroidNetworking.get(url)
            .addQueryParameter(params)
            .build()
            .getAsObject(AppUpgrade.class, new ParsedRequestListener<AppUpgrade>() {
                @Override
                public void onResponse(AppUpgrade response) {
                    callBack.onResponse(response);
                }

                @Override
                public void onError(ANError anError) {
                    callBack.onError(anError.getErrorDetail());
                }
            });
    }

    @Override
    void asyncPost(@NonNull String url, @NonNull Map<String, String> params, @NonNull final Callback callBack) {
        AndroidNetworking.post(url)
            .addUrlEncodeFormBodyParameter(params)
            .build()
            .getAsObject(AppUpgrade.class, new ParsedRequestListener<AppUpgrade>() {
                @Override
                public void onResponse(AppUpgrade response) {
                    callBack.onResponse(response);
                }

                @Override
                public void onError(ANError anError) {
                    callBack.onError(anError.getErrorDetail());
                }
            });
    }


    /**
     * 下载
     *
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    @Override
    public void download(
        @NonNull final String url, final @NonNull String path,
        @NonNull final String fileName, @NonNull final HttpManager.FileCallback callback) {

        callback.onBefore();
        AndroidNetworking.download(url, path, fileName)
            .doNotCacheResponse()
            .build()
            .setDownloadProgressListener(new DownloadProgressListener() {
                @Override
                public void onProgress(long bytesDownloaded, long totalBytes) {
                    callback.onProgress(bytesDownloaded * 1F / totalBytes, totalBytes);
                }
            })
            .startDownload(new DownloadListener() {
                @Override
                public void onDownloadComplete() {
                    final File downloadFile = new File(path + File.separator + fileName);
                    callback.onResponse(downloadFile);
                }

                @Override
                public void onError(ANError error) {
                    callback.onError(error.getErrorDetail());
                }
            });

    }
}

