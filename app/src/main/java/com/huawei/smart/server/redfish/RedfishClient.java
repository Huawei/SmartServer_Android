package com.huawei.smart.server.redfish;

import android.graphics.Bitmap;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.BitmapRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.redfish.model.TaskServiceClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class RedfishClient {

    HttpClient httpClient;
    SystemClient systemClient;
    ManagerClient managerClient;
    ChassisClient chassisClient;
    UpdateServiceClient updateServiceClient;
    TaskServiceClient taskServiceClient;

    /**
     * @param device   the IP or domain of the Redfish API
     * @param user     Redfish authentication username ("root" typically)
     * @param password Redfish authentication password
     */
    public RedfishClient(final String device, final Integer port, final String user, final String password) {
        this.httpClient = new HttpClient(device, port, user, password);
        this.systemClient = new SystemClient(this.httpClient);
        this.managerClient = new ManagerClient(this.httpClient);
        this.chassisClient = new ChassisClient(this.httpClient);
        this.updateServiceClient = new UpdateServiceClient(this.httpClient);
        this.taskServiceClient = new TaskServiceClient(this.httpClient);
    }

    public void bindExistDevice(Device device) {
        this.httpClient.bindExistDevice(device);
    }

    public void setHardware(String hardware) {
        this.httpClient.setHardware(hardware);
    }

    /**
     * 初始化Redfish，获取服务器类型ID。(附带同时会自动认证)
     * <p>
     * <li>针对机架服务器，取值为1</li>
     * <li>针对高密服务器，取值为BladeN（N表示节点槽位号），例如“Blade1”</li>
     * <li>针对刀片服务器，取值可以为BladeN（N表示计算节点槽位号）或SwiN（N表示交换模块槽位号），例如“Swi1”</li>
     */
    public void initialize(final OkHttpResponseListener listener) {
        this.httpClient.initialize(listener);
    }

    /**
     * destroy current redfish client
     */
    public void destroy(final OkHttpResponseListener listener) {
        httpClient.destroy(listener);
    }

    public void getResource(ResourceId identifier, OkHttpResponseAndParsedRequestListener listener) {
        httpClient.getResource(identifier, listener);
    }

    public void getResource(String resourceOdataId, Class resourceType, OkHttpResponseAndParsedRequestListener listener) {
        httpClient.getResource(resourceOdataId, resourceType, listener);
    }

    public void getDeviceThumbnail(final BitmapRequestListener listener) {
        httpClient.get("").build().getAsString(new StringRequestListener() {
            @Override
            public void onResponse(String response) {
                final Element productPic = Jsoup.parse(response).getElementById("imgProductPic");
                final String path = productPic.attr("src");
                final String absUrl = httpClient.getAbsUrl(path);
                httpClient.get(absUrl)
//                    .setBitmapMaxHeight(maxHeight)
//                    .setBitmapMaxWidth(maxWidth)
                    .build()
                    .getAsBitmap(new BitmapRequestListener() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            listener.onResponse(bitmap);
                        }
                        @Override
                        public void onError(ANError error) {
                            listener.onError(error);
                        }
                    });
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });

    }

    /**
     * the entry of Redfish System API
     *
     * @return
     */
    public SystemClient systems() {
        return this.systemClient;
    }

    /**
     * the entry of Redfish Manager API
     *
     * @return
     */
    public ManagerClient managers() {
        return this.managerClient;
    }

    /**
     * the entry of Redfish Manager API
     *
     * @return
     */
    public ChassisClient chassis() {
        return this.chassisClient;
    }

    /**
     * the entry of Redfish Update Service API
     *
     * @return
     */
    public UpdateServiceClient updateService() {
        return this.updateServiceClient;
    }

    /**
     * the entry of Redfish Task Service API
     *
     * @return
     */
    public TaskServiceClient taskService() {
        return this.taskServiceClient;
    }

    public ActionResponse getAuthActionResponse() {
        return httpClient.getAuthActionResponse();
    }
}
