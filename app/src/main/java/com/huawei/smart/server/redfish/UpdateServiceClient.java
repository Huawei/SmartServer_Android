package com.huawei.smart.server.redfish;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.huawei.smart.server.redfish.model.FirmwareInventory;
import com.huawei.smart.server.redfish.model.SoftwareInventory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Response;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class UpdateServiceClient {

    public static final String GET_FIRMWARE_INVENTORY_URL = "/redfish/v1/UpdateService/FirmwareInventory";


    HttpClient httpClient;

    public UpdateServiceClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     *
     */
    public void getFirmwareInventory(final OkHttpResponseAndParsedRequestListener<FirmwareInventory> listener) {
        httpClient.getResource(GET_FIRMWARE_INVENTORY_URL, FirmwareInventory.class, listener);
    }

    /**
     *
     */
    public void getSoftwareInventory(String odataResourceId, final OkHttpResponseAndParsedRequestListener<SoftwareInventory> listener) {
        httpClient.getResource(odataResourceId, SoftwareInventory.class, listener);
    }

    public void getManagedFirmwareInventory(final OkHttpResponseAndParsedRequestListener<FirmwareInventory> listener) {
        getFirmwareInventory(new OkHttpResponseAndParsedRequestListener<FirmwareInventory>() {
                @Override
                public void onResponse(Response okHttpResponse, final FirmwareInventory response) {
                    final List<SoftwareInventory> members = response.getMembers();
                    final CountDownLatch latch = new CountDownLatch(members.size());
                    for (int idx = 0; idx < members.size(); idx++) {
                        final int i = idx;
                        httpClient.getResource(members.get(idx), new OkHttpResponseAndParsedRequestListener<SoftwareInventory>() {
                            @Override
                            public void onResponse(Response okHttpResponse, SoftwareInventory response) {
                                members.add(i, response);
                                latch.countDown();
                            }

                            @Override
                            public void onError(ANError anError) {
                                latch.countDown();
                            }
                        });
                    }
                    try {
                        latch.await();
                        listener.onResponse(okHttpResponse, response);
                    } catch (InterruptedException e) {
                        listener.onError(new ANError(e));
                    }
                }

                @Override
                public void onError(ANError anError) {
                    listener.onError(anError);
                }
            });
    }


}
