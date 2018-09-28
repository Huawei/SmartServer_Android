package com.huawei.smart.server;

import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.RedfishClient;

import java.util.HashMap;

public class RedfishClientManager {

    private static RedfishClientManager instance = null;

    // redfish client container, key is device-id and value is redfish client instance
    private final HashMap<String, RedfishClient> container = new HashMap<>();

    public static RedfishClientManager getInstance() {
        if (instance == null) {
            synchronized (RedfishClientManager.class) {
                if (instance == null) {
                    instance = new RedfishClientManager();
                }
            }
        }
        return instance;
    }

    public RedfishClient get(String deviceId) {
        return container.get(deviceId);
    }

    public RedfishClient getOrCreate(Device device) {
        final String deviceId = device.getId();
        if (!container.containsKey(deviceId)) {
            synchronized (deviceId) {
                if (!container.containsKey(deviceId)) {
                    final RedfishClient client = new RedfishClient(device.getHostname(), device.getPort(), device.getUsername(), device.getPassword());
                    client.bindExistDevice(device);
                    container.put(deviceId, client);
                }
            }
        }
        return container.get(deviceId);
    }

    public void destroy(String deviceId, final OkHttpResponseListener listener) {
        RedfishClient client = container.remove(deviceId);
        if (client != null) {
            client.destroy(listener);
        }
    }
}
