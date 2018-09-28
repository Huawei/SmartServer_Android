package com.huawei.smart.server.redfish;

import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.huawei.smart.server.redfish.constants.IndicatorState;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.Chassis;
import com.huawei.smart.server.redfish.model.Drive;
import com.huawei.smart.server.redfish.model.NetworkAdapter;
import com.huawei.smart.server.redfish.model.NetworkAdapters;
import com.huawei.smart.server.redfish.model.NetworkPort;
import com.huawei.smart.server.redfish.model.Power;
import com.huawei.smart.server.redfish.model.Thermal;

import java.util.HashMap;

import okhttp3.Response;

import static com.huawei.smart.server.redfish.HttpClient.HEADER_IF_MATCH;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class ChassisClient {

    public static final String GET_CHASSIS_URI = "/redfish/v1/Chassis/{hardware}";
    public static final String GET_NETWORK_ADAPTER_URI ="/redfish/v1/Chassis/{hardware}/NetworkAdapters";
    public static final String RECOUNT_POWER_URI = "/redfish/v1/Chassis/{hardware}/Power/Oem/Huawei/Actions/Power.ResetHistoryData";

    HttpClient httpClient;

    public ChassisClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 2.5.2 查询指定机箱资源信息
     */
    public void get(final OkHttpResponseAndParsedRequestListener<Chassis> listener) {
        httpClient.getResource(GET_CHASSIS_URI, Chassis.class, listener);
    }

    /**
     * <h3>get properties of a Manager</h3>
     */
    public void updateIndicatorState(final IndicatorState changeStatusTo,
                                     final OkHttpResponseAndParsedRequestListener<Chassis> listener) {
        this.get(new OkHttpResponseAndParsedRequestListener<Chassis>() {
            @Override
            public void onResponse(Response okHttpResponse, Chassis response) {
                HashMap<String, String> body = new HashMap<>();
                body.put("IndicatorLED", changeStatusTo.name());
                httpClient.patch("/redfish/v1/Chassis/{hardware}", body)
                    .addHeaders(HEADER_IF_MATCH, response.getEtag()).build()
                    .getAsOkHttpResponseAndObject(Chassis.class, listener);
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    /**
     * <h3>get properties of the Power</h3>
     */
    public void getPower(final OkHttpResponseAndParsedRequestListener<Power> listener) {
        httpClient.getResource("/redfish/v1/Chassis/{hardware}/Power", Power.class, listener);
    }

    /**
     * <h3>清零功耗</h3>
     */
    public void recountPower(final OkHttpResponseAndParsedRequestListener<ActionResponse> listener) {
        httpClient.post(RECOUNT_POWER_URI, new HashMap<>()).build()
            .getAsOkHttpResponseAndObject(ActionResponse.class, listener);
    }

    /**
     * 2.5.4 查询指定机箱散热资源信息
     */
    public void getThermal(final OkHttpResponseAndParsedRequestListener<Thermal> listener) {
        httpClient.getResource("/redfish/v1/Chassis/{hardware}/Thermal", Thermal.class, listener);
    }


    /**
     * 2.5.9 查询网络适配器集合资源信息
     */
    public void getNetworkAdapters(final OkHttpResponseAndParsedRequestListener<NetworkAdapters> listener) {
        httpClient.getResource(GET_NETWORK_ADAPTER_URI, NetworkAdapters.class, listener);
    }

    /**
     * 2.5.10 查询网络适配器单个资源信息
     */
    public void getNetworkAdapter(String odataId, final OkHttpResponseAndParsedRequestListener<NetworkAdapter> listener) {
        httpClient.getResource(odataId, NetworkAdapter.class, listener);
    }

    /**
     * 2.5.12 查询网络端口单个资源信息
     */
    public void getNetworkPort(String odataId, final OkHttpResponseAndParsedRequestListener<NetworkPort> listener) {
        httpClient.getResource(odataId, NetworkPort.class, listener);
    }

    /**
     * 2.4.14 查询指定驱动器资源信息
     */
    public void getDrive(String odataId, final OkHttpResponseAndParsedRequestListener<Drive> listener) {
        httpClient.getResource(odataId, Drive.class, listener);
    }

    /**
     * 2.4.14 查询指定驱动器资源信息
     */
    public Drive getDrive(String odataId) {
        final ANResponse<Drive> anResponse = httpClient.get(odataId).build().executeForObject(Drive.class);
        return anResponse.getResult();
    }

}
