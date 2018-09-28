package com.huawei.smart.server.redfish.model;

import com.androidnetworking.common.ANResponse;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.huawei.smart.server.redfish.HttpClient;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class TaskServiceClient {

    public static final String GET_MANAGER_URI = "/redfish/v1/managers/{hardware}";
    public static final String RESET_MANAGER_URI = "/redfish/v1/managers/{hardware}/Actions/Manager.Reset";
    public static final String ROLLBACK_MANAGER_URI = "/redfish/v1/Managers/{hardware}/Actions/Oem/Huawei/Manager.RollBack";
    public static final String UPDATE_MANAGER_URI = "/redfish/v1/managers/{hardware}";
    public static final String GET_ETHERNET_INTERFACES_URI = "/redfish/v1/Managers/{hardware}/EthernetInterfaces";
    public static final String COLLECT_URI = "/redfish/v1/Managers/{hardware}/Actions/Oem/Huawei/Manager.Dump";


    HttpClient httpClient;

    public TaskServiceClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void get(String resourceOdataId, final OkHttpResponseAndParsedRequestListener<Task> listener) {
        httpClient.getResource(resourceOdataId, Task.class, listener);
    }

    public ANResponse<Task> syncGet(String resourceOdataId) {
        return httpClient.get(resourceOdataId).build().executeForObject(Task.class);
    }

}
