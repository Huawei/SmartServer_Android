package com.huawei.smart.server.redfish;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.androidnetworking.common.ANResponse;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.OkHttpResponseAndJSONObjectRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.huawei.smart.server.redfish.constants.LogEntryStatus;
import com.huawei.smart.server.redfish.constants.ResourceResetType;
import com.huawei.smart.server.redfish.constants.Severity;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.redfish.model.LogEntries;
import com.huawei.smart.server.redfish.model.LogEntry;
import com.huawei.smart.server.redfish.model.LogEntryFilter;
import com.huawei.smart.server.redfish.model.LogService;
import com.huawei.smart.server.redfish.model.LogServices;
import com.huawei.smart.server.redfish.model.LogicalDrive;
import com.huawei.smart.server.redfish.model.Memory;
import com.huawei.smart.server.redfish.model.MemoryCollection;
import com.huawei.smart.server.redfish.model.MemoryView;
import com.huawei.smart.server.redfish.model.Processor;
import com.huawei.smart.server.redfish.model.Processors;
import com.huawei.smart.server.redfish.model.Storage;
import com.huawei.smart.server.redfish.model.Storages;
import com.huawei.smart.server.redfish.model.Volumes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Response;

import static com.huawei.smart.server.redfish.HttpClient.HEADER_IF_MATCH;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class SystemClient {

    public static final String GET_LOG_SERVICES_URI = "/redfish/v1/Systems/{hardware}/LogServices";
    public static final String GET_PROCESSORS_URI = "/redfish/v1/Systems/{hardware}/Processors";
    public static final String GET_MEMORY_COLLECTION_URI = "/redfish/v1/Systems/{hardware}/Memory";
    public static final String GET_MEMORY_VIEW_URI = "/redfish/v1/Systems/{hardware}/MemoryView";
    public static final String GET_STORAGES_URI = "/redfish/v1/Systems/{hardware}/Storages/";

    HttpClient httpClient;

    public SystemClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * <h3>get properties of a System. </h3>
     * Get sample response json in ComputerSystem.json
     */
    public void get(final OkHttpResponseAndParsedRequestListener<ComputerSystem> listener) {
        httpClient.getResource("/redfish/v1/Systems/{hardware}", ComputerSystem.class, listener);
    }

    /**
     * update manager
     */
    public void update(final ComputerSystem updated, final OkHttpResponseAndParsedRequestListener<ComputerSystem> listener) {
        this.get(new OkHttpResponseAndParsedRequestListener<ComputerSystem>() {
            @Override
            public void onResponse(Response okHttpResponse, ComputerSystem system) {
                httpClient.patch("/redfish/v1/Systems/{hardware}", updated)
                    .addHeaders(HEADER_IF_MATCH, system.getEtag()).build()
                    .getAsOkHttpResponseAndObject(ComputerSystem.class, listener);
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        });
    }

    /**
     * 重启服务器
     */
    public void reset(ResourceResetType resetType, final OkHttpResponseAndParsedRequestListener<ActionResponse> listener) {
        HashMap<String, String> body = new HashMap<>();
        body.put("ResetType", resetType.name());
        httpClient.post("/redfish/v1/Systems/{hardware}/Actions/ComputerSystem.Reset", body).build()
            .getAsOkHttpResponseAndObject(ActionResponse.class, listener);
    }

    /**
     * 2.4.33 查询指定日志服务资源信息
     * <p>
     * <li>查询之前需要先访问 2.4.32 查询日志服务集合资源信息 来获取对应的OdateId</li>
     */
    public void getLogService(final OkHttpResponseAndParsedRequestListener<LogService> listener) {
        httpClient.getResource(GET_LOG_SERVICES_URI, LogServices.class,
            new OkHttpResponseAndParsedRequestListener<LogServices>() {
                @Override
                public void onResponse(Response okHttpResponse, LogServices logServices) {
                    final LogService logService = logServices.getMembers().get(0);
                    httpClient.getResource(logService.getOdataId(), LogService.class, listener);
                }

                @Override
                public void onError(ANError anError) {
                    listener.onError(anError);
                }
            });

    }


    /**
     * <h3>Clear all log of LogService</h3>
     */
    public void clearLog(final OkHttpResponseAndJSONObjectRequestListener listener) {
        final String path = "/redfish/v1/Systems/{hardware}/LogServices/Log1/Actions/LogService.ClearLog";
        httpClient.post(path, new HashMap<>()).build().getAsOkHttpResponseAndJSONObject(listener);
    }

    /**
     * 2.4.35 清空日志信息
     */
    public void clearLog(String logOdataId, final OkHttpResponseAndParsedRequestListener<ActionResponse> listener) {
        final String path = logOdataId + "/Actions/LogService.ClearLog";
        httpClient.post(path, new HashMap<>()).build().getAsOkHttpResponseAndObject(ActionResponse.class, listener);
    }


    /**
     * <h3>get log entries</h3>
     */
    public void getLogEntries(final String resourceOdataId, final LogEntryFilter filter, final OkHttpResponseAndParsedRequestListener<LogEntries> listener) {
        // 上线API
//        httpClient.get(resourceOdataId).addQueryParameter(filter.convertToMap()).build().getAsOkHttpResponseAndObject(LogEntries.class, listener);

        // 测试模拟数据
        LogEntries entries = new LogEntries();
        entries.setEntryList(constructDatList());
        entries.setCount(19);
        listener.onResponse(null, entries);
    }

    /**
     * 测试模拟数据
     *
     * @return
     */
    private ArrayList constructDatList() {
        ArrayList mDataList = new ArrayList<>();
        mDataList.add(getLogEntry(Severity.OK));
        mDataList.add(getLogEntry(Severity.Warning));
        mDataList.add(getLogEntry(Severity.Critical));
        mDataList.add(getLogEntry(Severity.OK));
        mDataList.add(getLogEntry(Severity.Warning));
        mDataList.add(getLogEntry(Severity.Critical));
        return mDataList;
    }

    /**
     * @return
     */
    @NonNull
    private LogEntry getLogEntry(Severity severity) {
        LogEntry entry = new LogEntry();
        entry.setId("1270");
        entry.setEventSubject("Disk5");
        entry.setStatus(LogEntryStatus.Asserted);
        entry.setSeverity(severity);
        entry.setSuggest("1. Check whether the power cables are disconnected or loose.\n" +
            "2. Replace the power cables.\n" +
            "3. Replace the PSU.");
        entry.setCreated(new Date());
        entry.setEventId("0x06000015");
        entry.setMessage("RAID card1 BBU is present.");
        return entry;
    }


    /**
     * <h3>get log entry</h3>
     */
    public void getLogEntry(String resourceOdataId, final OkHttpResponseAndParsedRequestListener<LogEntry> listener) {
        httpClient.getResource(resourceOdataId, LogEntry.class, listener);
    }

    /**
     * 2.4.26 查询CPU集合资源信息
     */
    public void getProcessors(final OkHttpResponseAndParsedRequestListener<Processors> listener) {
        httpClient.getResource(GET_PROCESSORS_URI, Processors.class, listener);
    }

    /**
     * 2.4.27 查询指定CPU资源信息
     */
    public void getProcessor(String resourceOdataId, final OkHttpResponseAndParsedRequestListener<Processor> listener) {
        httpClient.getResource(resourceOdataId, Processor.class, listener);
    }

    /**
     * 2.4.8 查询内存集合资源信息
     */
    public void getMemoryCollection(final String resourceOdataId, final OkHttpResponseAndParsedRequestListener<MemoryCollection> listener) {
        httpClient.getResource(TextUtils.isEmpty(resourceOdataId) ? GET_MEMORY_COLLECTION_URI : resourceOdataId, MemoryCollection.class, listener);
    }

    /**
     * 新的分页内存接口
     */
    public void getMemoryView(final String resourceOdataId, final OkHttpResponseAndParsedRequestListener<MemoryView> listener) {
        httpClient.getResource(TextUtils.isEmpty(resourceOdataId) ? GET_MEMORY_VIEW_URI : resourceOdataId, MemoryView.class, listener);
    }

    /**
     * 2.4.9 查询指定内存资源信息
     */
    public void getMemory(String resourceOdataId, final OkHttpResponseAndParsedRequestListener<Memory> listener) {
        httpClient.getResource(resourceOdataId, Memory.class, listener);
    }

    /**
     * 2.4.12 查询存储集合资源信息
     */
    public void getStorages(final OkHttpResponseAndParsedRequestListener<Storages> listener) {
        httpClient.getResource(GET_STORAGES_URI, Storages.class, listener);
    }

    /**
     * 2.4.12 查询存储集合资源信息
     */
    public Storages getStorages() {
        final ANResponse<Storages> anResponse = httpClient.get(GET_STORAGES_URI).build().executeForObject(Storages.class);
        return anResponse.getResult();
    }

    /**
     * 2.4.12 查询存储集合资源信息
     */
    public void getStorage(String odataId, final OkHttpResponseAndParsedRequestListener<Storage> listener) {
        httpClient.getResource(odataId, Storage.class, listener);
    }


    /**
     * 2.4.12 查询存储集合资源信息
     */
    public Storage getStorage(String odataId) {
        final ANResponse<Storage> anResponse = httpClient.get(odataId).build().executeForObject(Storage.class);
        return anResponse.getResult();
    }


    /**
     * 2.3.28 查询逻辑盘集合资源信息
     */
    public void getVolumes(String odataId, final OkHttpResponseAndParsedRequestListener<Volumes> listener) {
        httpClient.getResource(odataId, Volumes.class, listener);
    }

    /**
     * 2.3.28 查询逻辑盘集合资源信息
     */
    public Volumes getVolumes(String odataId) {
        final ANResponse<Volumes> anResponse = httpClient.get(odataId).build().executeForObject(Volumes.class);
        return anResponse.getResult();
    }

    /**
     * 2.3.29 查询指定逻辑盘资源信息
     */
    public void getLogicalDrive(String odataId, final OkHttpResponseAndParsedRequestListener<LogicalDrive> listener) {
        httpClient.getResource(odataId, LogicalDrive.class, listener);
    }

    /**
     * 2.3.29 查询指定逻辑盘资源信息
     */
    public LogicalDrive getLogicalDrive(String odataId) {
        final ANResponse<LogicalDrive> anResponse = httpClient.get(odataId).build().executeForObject(LogicalDrive.class);
        return anResponse.getResult();
    }

}
