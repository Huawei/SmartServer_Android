package com.huawei.smart.server.redfish;

import android.text.TextUtils;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.redfish.model.EthernetInterfaceCollection;
import com.huawei.smart.server.redfish.model.Manager;
import com.huawei.smart.server.redfish.model.NetworkProtocol;
import com.huawei.smart.server.redfish.model.Task;
import com.huawei.smart.server.utils.StringUtils;

import java.util.HashMap;

import okhttp3.Response;

import static com.huawei.smart.server.redfish.HttpClient.HEADER_IF_MATCH;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class ManagerClient {

    public static final String GET_MANAGER_URI = "/redfish/v1/managers/{hardware}";
    public static final String RESET_MANAGER_URI = "/redfish/v1/managers/{hardware}/Actions/Manager.Reset";
    public static final String ROLLBACK_MANAGER_URI = "/redfish/v1/Managers/{hardware}/Actions/Oem/Huawei/Manager.RollBack";
    public static final String UPDATE_MANAGER_URI = "/redfish/v1/managers/{hardware}";
    public static final String GET_ETHERNET_INTERFACES_URI = "/redfish/v1/Managers/{hardware}/EthernetInterfaces";
    public static final String COLLECT_URI = "/redfish/v1/Managers/{hardware}/Actions/Oem/Huawei/Manager.Dump";
    public static final String GET_NETWORK_PROTOCOL_URI = "/redfish/v1/Managers/{hardware}/NetworkProtocol";


    HttpClient httpClient;

    public ManagerClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * <h3>get properties of a Manager</h3>
     */
    public void get(final OkHttpResponseAndParsedRequestListener<Manager> listener) {
        httpClient.getResource(GET_MANAGER_URI, Manager.class, listener);
    }

    /**
     * 2.3.37 重启iBMC
     */
    public void reset(final OkHttpResponseAndParsedRequestListener<ActionResponse> listener) {
        final HashMap<String, String> body = new HashMap<>();
        body.put("ResetType", "ForceRestart");
        httpClient.post(RESET_MANAGER_URI, body).build().getAsOkHttpResponseAndObject(ActionResponse.class, listener);
    }

    /**
     * 2.3.38 切换iBMC镜像
     */
    public void rollback(final OkHttpResponseAndParsedRequestListener<ActionResponse> listener) {
        final HashMap<String, String> body = new HashMap<>();
        httpClient.post(ROLLBACK_MANAGER_URI, body).build().getAsOkHttpResponseAndObject(ActionResponse.class, listener);
    }

    /**
     * update manager
     */
    public void update(final Manager updated, final OkHttpResponseAndParsedRequestListener<Manager> listener) {
        this.get(new OkHttpResponseAndParsedRequestListener<Manager>() {
            @Override
            public void onResponse(Response okHttpResponse, Manager manager) {
                httpClient.patch(UPDATE_MANAGER_URI, updated)
                    .addHeaders(HEADER_IF_MATCH, manager.getEtag()).build()
                    .getAsOkHttpResponseAndObject(Manager.class, listener);
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    /**
     * 2.3.32 查询iBMC网口集合资源信息
     *
     * @param listener
     */
    public void getEthernetInterfaces(final OkHttpResponseAndParsedRequestListener<EthernetInterfaceCollection> listener) {
        this.httpClient.getResource(GET_ETHERNET_INTERFACES_URI, EthernetInterfaceCollection.class, listener);
    }

    /**
     * 2.3.35 查询iBMC服务信息
     *
     * @param listener
     */
    public void getNetworkProtocol(final OkHttpResponseAndParsedRequestListener<NetworkProtocol> listener) {
        this.httpClient.getResource(GET_NETWORK_PROTOCOL_URI, NetworkProtocol.class, listener);
    }

    /**
     * 2.3.33 查询指定iBMC网口资源信息
     *
     * @param resourceOdataId
     * @param listener
     */
    public void getEthernetInterface(String resourceOdataId,
                                     final OkHttpResponseAndParsedRequestListener<EthernetInterface> listener) {
        this.httpClient.getResource(resourceOdataId, EthernetInterface.class, listener);
    }

    /**
     * 2.3.34 修改指定iBMC网口信息
     *
     * @param resourceOdataId
     * @param listener
     */
    public void updateEthernetInterface(final String resourceOdataId, final EthernetInterface updated,
                                        final OkHttpResponseAndParsedRequestListener<EthernetInterface> listener) {
        this.httpClient.getResource(resourceOdataId, EthernetInterface.class, new OkHttpResponseAndParsedRequestListener<EthernetInterface>() {
            @Override
            public void onResponse(Response okHttpResponse, EthernetInterface get) {
                // update FQDN
                if (updated.getFQDN() != null) {
                    String fqdn = StringUtils.defaultString(updated.getFQDN(), "");
                    if (!TextUtils.isEmpty(get.getHostName())) {
                        updated.setFQDN(get.getHostName() + "." + fqdn);
                        updated.setHostName(get.getHostName());
                    } else {
                        updated.setFQDN(fqdn);
                    }
                }

                httpClient.patch(resourceOdataId, updated)
                    .addHeaders(HEADER_IF_MATCH, get.getEtag()).build()
                    .getAsOkHttpResponseAndObject(EthernetInterface.class, listener);
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    /**
     * 2.3.4 一键收集
     */
    public void collect(String filename, final OkHttpResponseAndParsedRequestListener<Task> listener) {
        final HashMap<String, String> body = new HashMap<>();
        body.put("Type", "URI");
        body.put("Content", "/tmp/" + filename);
        httpClient.post(COLLECT_URI, body).build().getAsOkHttpResponseAndObject(Task.class, listener);
    }
}
