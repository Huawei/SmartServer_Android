package com.huawei.smart.server.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.androidnetworking.error.ANError;
import com.blankj.utilcode.util.ActivityUtils;
import com.huawei.smart.server.BaseActivity;
import com.huawei.smart.server.HWConstants;
import com.huawei.smart.server.R;
import com.huawei.smart.server.redfish.RRLB;
import com.huawei.smart.server.redfish.RedfishClient;
import com.huawei.smart.server.redfish.RedfishResponseListener;
import com.huawei.smart.server.redfish.constants.HealthRollupState;
import com.huawei.smart.server.redfish.constants.IPVersion;
import com.huawei.smart.server.redfish.constants.IPv4AddressOrigin;
import com.huawei.smart.server.redfish.constants.ResourceState;
import com.huawei.smart.server.redfish.model.Chassis;
import com.huawei.smart.server.redfish.model.ComputerSystem;
import com.huawei.smart.server.redfish.model.EthernetInterface;
import com.huawei.smart.server.redfish.model.EthernetInterfaceCollection;
import com.huawei.smart.server.redfish.model.FirmwareInventory;
import com.huawei.smart.server.redfish.model.Manager;
import com.huawei.smart.server.redfish.model.Memory;
import com.huawei.smart.server.redfish.model.MemoryCollection;
import com.huawei.smart.server.redfish.model.MemoryView;
import com.huawei.smart.server.redfish.model.Power;
import com.huawei.smart.server.redfish.model.Processor;
import com.huawei.smart.server.redfish.model.Processors;
import com.huawei.smart.server.redfish.model.ResourceId;
import com.huawei.smart.server.redfish.model.SoftwareInventory;
import com.huawei.smart.server.redfish.model.Storage;
import com.huawei.smart.server.redfish.model.StorageController;
import com.huawei.smart.server.redfish.model.Storages;
import com.huawei.smart.server.redfish.model.Thermal;
import com.huawei.smart.server.task.LoadMemoriesTask;
import com.huawei.smart.server.task.LoadProcessorsTask;
import com.huawei.smart.server.task.LoadSoftwareInventoryListTask;
import com.huawei.smart.server.task.LoadStorageListTask;
import com.huawei.smart.server.utils.BundleBuilder;
import com.huawei.smart.server.utils.StringUtils;
import com.huawei.smart.server.widget.LabeledSwitch;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Response;

import static com.huawei.smart.server.activity.FirmwareActivity.Mapper;
import static com.huawei.smart.server.adapter.StorageControllerListAdapter.byte2FitMemorySize;

/**
 * 生成报告
 */
public class GenerateReportActivity2 extends BaseActivity {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GenerateReportActivity2.class.getSimpleName());

    @BindView(R.id.system) LabeledSwitch system;
    @BindView(R.id.cpu) LabeledSwitch cpu;
    @BindView(R.id.memory) LabeledSwitch memory;
    @BindView(R.id.storage) LabeledSwitch storage;
    @BindView(R.id.firmware) LabeledSwitch firmware;
    @BindView(R.id.network) LabeledSwitch network;
    @BindView(R.id.health) LabeledSwitch health;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_report);
        this.initialize(R.string.ds_label_menu_report, true);
    }

    @OnClick(R.id.submit)
    public void generateReport() {
        showLoadingDialog();
        if (memory.isChecked()) {
            // try to get memory view
            this.getRedfishClient().systems().getMemoryView(null, RRLB.<MemoryView>create(this).callback(
                new RedfishResponseListener.Callback<MemoryView>() {
                    @Override
                    public void onResponse(Response okHttpResponse, MemoryView response) {

                    }

                    @Override
                    public void onError(ANError anError) {
                        new MaterialDialog.Builder(GenerateReportActivity2.this)
                            .title("操作失败")
                            .content("当前设备不支持导出内存报告")
                            .positiveText(R.string.button_sure)
                            .show();
                    }
                }).build());
        }
        new LoadReportDataTask(this, system.isChecked(), cpu.isChecked(), memory.isChecked(), storage.isChecked(),
            firmware.isChecked(), network.isChecked(), health.isChecked()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    public boolean reportSystem() {
        return system.isChecked();
    }

    public boolean reportCpu() {
        return cpu.isChecked();
    }

    public boolean reportMemory() {
        return memory.isChecked();
    }

    public boolean reportStorage() {
        return storage.isChecked();
    }

    public boolean reportFirmware() {
        return firmware.isChecked();
    }

    public boolean reportNetwork() {
        return network.isChecked();
    }

    public boolean reportHealth() {
        return health.isChecked();
    }


    public class LoadReportDataTask extends AsyncTask<Void, Void, String> {

        final RedfishClient redfish;
        private final GenerateReportActivity2 activity;
        /**
         * 需要的数据
         */
        Power power;
        Manager manager;
        Thermal thermal;
        Chassis chassis;
        ComputerSystem system;
        MemoryCollection memoryCollection;
        EthernetInterface ethernetInterface;
        List<Memory> memoryList = new ArrayList<>();
        List<Processor> processorList = new ArrayList<>();
        List<Storage> storageList = new ArrayList<>();
        List<SoftwareInventory> softwareInventoryList = new ArrayList<>();
        private boolean systemChecked;
        private boolean cpuChecked;
        private boolean memoryChecked;
        private boolean storageChecked;
        private boolean firmwareChecked;
        private boolean networkChecked;
        private boolean healthChecked;

        public LoadReportDataTask(GenerateReportActivity2 activity, boolean systemChecked, boolean cpuChecked,
                                  boolean memoryChecked, boolean storageChecked,
                                  boolean firmwareChecked, boolean networkChecked, boolean healthChecked) {
            this.activity = activity;
            this.redfish = activity.getRedfishClient();
            this.systemChecked = systemChecked;
            this.cpuChecked = cpuChecked;
            this.memoryChecked = memoryChecked;
            this.storageChecked = storageChecked;
            this.firmwareChecked = firmwareChecked;
            this.networkChecked = networkChecked;
            this.healthChecked = healthChecked;
        }

        protected void onPostExecute(String html) {
            final Bundle bundle = BundleBuilder.instance().with(HWConstants.BUNDLE_KEY_REPORT_HTML, html).build();
            ActivityUtils.startActivity(bundle, DisplayReportActivity.class);
            activity.dismissLoadingDialog();
        }

        @Override
        protected String doInBackground(Void... voids) {
            final CountDownLatch latch = new CountDownLatch(10);

            // count down == 5 | manager + memory + cpu + network + system
//            if (activity.reportSystem()) {
            getManager(latch);
//            }

//            if (activity.reportMemory()) {  // count down == 1
            loadMemories(latch);
//            }

//            if (activity.reportCpu()) {     // count down == 1
            getProcessors(latch);
//            }

            // 网络
//            if (activity.reportNetwork()) { // count down == 1
            getNetwork(latch);
//            }

//            if (activity.reportFirmware()) {    // count down == 1
            getSoftwareInventories(latch);
//            }

//            if (activity.reportHealth()) {      // count down == 4
            getHealth(latch);
//            }

            // 存储
//            if (activity.reportStorage()) {     // count down == 1
            getStorages(latch);
//            }

            try {
                latch.await(300, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                activity.showToast(R.string.msg_access_api_failed, Toast.LENGTH_SHORT, Gravity.CENTER);
            }

            return generateReport();
        }

        private String generateReport() {
            // generate report
            try {
                // 读取模板文件
                final InputStream is = activity.getAssets().open("report.html");
                final Document doc = Jsoup.parse(is, "UTF-8", "");
                final Element body = doc.body();


                try {
                    if (systemChecked) {
                        generateSystemOverview(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate system overview", e);
                }


                try {
                    if (cpuChecked) {
                        generateCpuReport(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate processor report", e);
                }


                try {
                    if (memoryChecked) {
                        generateMemoryReport(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate memory report", e);
                }


                try {
                    if (storageChecked) {
                        generateStorageReport(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate Storage report", e);
                }


                try {
                    if (firmwareChecked) {
                        generateFirmwareReport(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate Firmware report", e);
                }


                try {
                    if (networkChecked) {
                        generateNetworkReport(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate Network report", e);

                }


                try {
                    if (healthChecked) {
                        generateHealthReport(body);
                    }
                } catch (Exception e) {
                    LOG.error("failed to generate Health report", e);
                }

                return doc.html();
            } catch (IOException e) {
                // should not happen
                LOG.error("Failed to generate report html", e);
            }

            return null;
        }

        private void generateHealthReport(Element body) {
            String title = getString(R.string.ds_label_menu_realtime_state);
            List<LinkedHashMap<Object, Object>> propertiesList = new ArrayList<>();

            // 实时温度
            LinkedHashMap<Object, Object> temperature = new LinkedHashMap<>();
            temperature.put(R.string.rs_section_temperature, "");
            for (Thermal.Temperature temp : thermal.getTemperatures()) {
                if (temp.getReadingCelsius() != null) {
                    final String name = temp.getName();
                    boolean noUnit = !TextUtils.isEmpty(name) && (name.endsWith(" DTS") || name.endsWith(" Margin"));
                    temperature.put(name, temp.getReadingCelsius() + (noUnit ? "" : "℃"));
                }
            }
            propertiesList.add(temperature);

            // 健康状态
            LinkedHashMap<Object, Object> health = new LinkedHashMap<>();
            health.put(R.string.rs_section_health_state, "");

            if (thermal.getOem() != null && thermal.getOem().getFanSummary() != null
                && thermal.getOem().getFanSummary().getStatus() != null)
            {
                final HealthRollupState status = thermal.getOem().getFanSummary().getStatus().getHealthRollup();
                health.put(R.string.rs_label_fan, status != null ? status.getDisplayResId() : "");
            } else {
                health.put(R.string.rs_label_fan, "");
            }

            if (chassis.getOem() != null && chassis.getOem().getPowerSupplySummary() != null
                && chassis.getOem().getPowerSupplySummary().getStatus() != null)
            {
                final HealthRollupState healthRollup = chassis.getOem().getPowerSupplySummary().getStatus().getHealthRollup();
                health.put(R.string.rs_label_power, healthRollup != null ? healthRollup.getDisplayResId() : "");
            } else {
                health.put(R.string.rs_label_power, "");
            }

            if (chassis.getOem() != null && chassis.getOem().getDriveSummary() != null
                && chassis.getOem().getDriveSummary().getStatus() != null)
            {
                final HealthRollupState healthRollup = chassis.getOem().getDriveSummary().getStatus().getHealthRollup();
                health.put(R.string.rs_label_drive, healthRollup != null ? healthRollup.getDisplayResId() : "");
            } else {
                health.put(R.string.rs_label_drive, "");
            }

            if (thermal.getOem() != null && thermal.getOem().getTemperatureSummary() != null
                && thermal.getOem().getTemperatureSummary().getStatus() != null)
            {
                final HealthRollupState healthRollup = thermal.getOem().getTemperatureSummary().getStatus().getHealthRollup();
                health.put(R.string.rs_label_temperature, healthRollup != null ? healthRollup.getDisplayResId() : "");
            } else {
                health.put(R.string.rs_label_temperature, "");
            }

            if (system.getProcessorSummary() != null && system.getProcessorSummary().getStatus() != null) {
                final HealthRollupState healthRollup = system.getProcessorSummary().getStatus().getHealthRollup();
                health.put(R.string.rs_label_cpu, healthRollup != null ? healthRollup.getDisplayResId() : "");
            } else {
                health.put(R.string.rs_label_cpu, "");
            }

            if (system.getMemorySummary() != null && system.getMemorySummary().getStatus() != null) {
                final HealthRollupState healthRollup = system.getMemorySummary().getStatus().getHealthRollup();
                health.put(R.string.rs_label_memory, healthRollup != null ? healthRollup.getDisplayResId() : "");
            } else {
                health.put(R.string.rs_label_memory, "");
            }
            propertiesList.add(health);

            // 功耗
            if (power.getPowerControl() != null && power.getPowerControl().size() > 0) {
                LinkedHashMap<Object, Object> consumption = new LinkedHashMap<>();
                consumption.put(R.string.rs_section_power_consumption, "");
                final Integer powerConsumedWatts = power.getPowerControl().get(0).getPowerConsumedWatts();
                consumption.put(R.string.rs_label_power_consumption, powerConsumedWatts != null ? powerConsumedWatts + "W" : "");
                propertiesList.add(consumption);
            }

            body.appendChild(generate(title, propertiesList.toArray(new LinkedHashMap[0])));
        }

        private void generateNetworkReport(Element body) {
            String title = getString(R.string.ns_tab_network);
//            properties.put(R.string.ns_network_ip_version, ethernetInterface.getOem().getIPVersion().getDisplayResId());
            List<LinkedHashMap<Object, Object>> propertiesList = new ArrayList<>();
            if (!ethernetInterface.getOem().getIPVersion().equals(IPVersion.IPv6)) {
                LinkedHashMap<Object, Object> properties = new LinkedHashMap<>();
                properties.put(R.string.ns_network_ip_version_ipv4, "");
                properties.put(R.string.ns_network_label_dhcp, ethernetInterface.getIPv4().getAddressOrigin().equals(IPv4AddressOrigin.DHCP) ? R.string.yes : R.string.no);
                properties.put(R.string.ns_network_label_ip_address, ethernetInterface.getIPv4().getAddress());
                properties.put(R.string.ns_network_label_subnet_mask, ethernetInterface.getIPv4().getSubnetMask());
                properties.put(R.string.ns_network_label_gateway, ethernetInterface.getIPv4().getGateway());
                properties.put(R.string.ns_network_label_mac, ethernetInterface.getPermanentMACAddress());
                propertiesList.add(properties);
            }

            if (!ethernetInterface.getOem().getIPVersion().equals(IPVersion.IPv4)) {
                LinkedHashMap<Object, Object> properties = new LinkedHashMap<>();
                properties.put(R.string.ns_network_ip_version_ipv6, "");
                if (ethernetInterface.getIPv6() != null) {
                    final boolean dhcp = ethernetInterface.getIPv6().getAddressOrigin().equals(IPv4AddressOrigin.DHCP);
                    properties.put(R.string.ns_network_label_dhcp, dhcp ? R.string.yes : R.string.no);
                    properties.put(R.string.ns_network_label_ip_address, ethernetInterface.getIPv6().getAddress());
                    properties.put(R.string.ns_network_label_prefix_len, ethernetInterface.getIPv6().getPrefixLength() == null ? "" : ethernetInterface.getIPv6().getPrefixLength() + "");
                    properties.put(R.string.ns_network_label_gateway, ethernetInterface.getIPv6DefaultGateway());
                }
                propertiesList.add(properties);
            }

            body.appendChild(generate(title, propertiesList.toArray(new LinkedHashMap[0])));
        }

        private void generateFirmwareReport(Element body) {
            String title = getString(R.string.ds_label_menu_firmware);
            Collections.sort(softwareInventoryList);
            LinkedHashMap<Object, Object> properties = new LinkedHashMap<>();
            for (SoftwareInventory software : softwareInventoryList) {
                String name = StringUtils.defaultIfBlank(Mapper.get(software.getName()), software.getName());
                properties.put(name, software.getVersion());
            }
            body.appendChild(generate(title, properties));
        }

        private void generateStorageReport(Element body) {
            String title = getString(R.string.hardware_tab_storage);
            List<LinkedHashMap<Object, Object>> propertiesList = new ArrayList<>();
            Collections.sort(storageList);
            for (Storage storage : storageList) {
                final List<StorageController> controllers = storage.getStorageControllers();
                for (StorageController controller : controllers) {
                    LinkedHashMap<Object, Object> properties = new LinkedHashMap<>();
                    final Serializable status = controller.getStatus().getState() != null ?
                        controller.getStatus().getState().getLabelResId() : "";
                    properties.put(controller.getName(), status);
                    properties.put(R.string.hardware_storage_label_model, controller.getModel());
                    properties.put(R.string.hardware_storage_label_firmware_version, controller.getFirmwareVersion());

                    final StorageController.Oem oem = controller.getOem();
                    final List<String> supportedRAIDLevels = oem.getSupportedRAIDLevels();
                    final String joined = TextUtils.join(",", supportedRAIDLevels);
                    properties.put(R.string.hardware_storage_label_support_raid_level, "RAID(" + joined.replaceAll("RAID", "") + ")");
                    properties.put(R.string.hardware_storage_label_configure_version, oem.getConfigurationVersion());
                    properties.put(R.string.hardware_storage_label_memory_size_MB, oem.getMemorySizeMiB() != null ? oem.getMemorySizeMiB() + " MB" : "");
                    properties.put(R.string.hardware_storage_label_speed, controller.getSpeedGbps() != null ? controller.getSpeedGbps() + " G" : "");
                    properties.put(R.string.hardware_storage_label_sas_addr, oem.getSASAddress());

                    final String min = byte2FitMemorySize(oem.getMinStripeSizeBytes());
                    final String max = byte2FitMemorySize(oem.getMaxStripeSizeBytes());
                    properties.put(R.string.hardware_storage_label_stripe_range, min + "-" + max);

                    if (oem.getMaintainPDFailHistory() != null) {
                        final ResourceState maintainable = oem.getMaintainPDFailHistory() ? ResourceState.Enabled : ResourceState.Disabled;
                        properties.put(R.string.hardware_storage_label_maintain_pd_fail_history, maintainable.getLabelResId());
                    } else {
                        properties.put(R.string.hardware_storage_label_maintain_pd_fail_history, null);
                    }

                    if (oem.getCopyBackState() != null) {
                        final ResourceState copyBackEnabled = oem.getCopyBackState() ? ResourceState.Enabled : ResourceState.Disabled;
                        properties.put(R.string.hardware_storage_label_copy_back_state, copyBackEnabled.getLabelResId());
                    } else {
                        properties.put(R.string.hardware_storage_label_copy_back_state, null);
                    }


                    if (oem.getSmarterCopyBackState() != null) {
                        final ResourceState smartCopyBackEnabled = oem.getSmarterCopyBackState() ? ResourceState.Enabled : ResourceState.Disabled;
                        properties.put(R.string.hardware_storage_label_smart_copy_back_state, smartCopyBackEnabled.getLabelResId());
                    } else {
                        properties.put(R.string.hardware_storage_label_smart_copy_back_state, null);
                    }

                    if (oem.getJBODState() != null) {
                        final ResourceState JBOD = oem.getJBODState() ? ResourceState.Enabled : ResourceState.Disabled;
                        properties.put(R.string.hardware_storage_label_JOBD_state, JBOD.getLabelResId());
                    } else {
                        properties.put(R.string.hardware_storage_label_JOBD_state, null);
                    }

                    properties.put(R.string.hardware_storage_label_bbu_name, TextUtils.isEmpty(oem.getCapacitanceName()) ? "N/A" : oem.getCapacitanceName());
                    if (oem.getCapacitanceStatus() != null && oem.getCapacitanceStatus().getState() != null) {
                        properties.put(R.string.hardware_storage_label_bbu_state, oem.getCapacitanceStatus().getState().getLabelResId());
                    } else {
                        properties.put(R.string.hardware_storage_label_bbu_state, null);
                    }
                    propertiesList.add(properties);
                }
            }
            body.appendChild(generate(title, propertiesList.toArray(new LinkedHashMap[0])));
        }

        private void generateMemoryReport(Element body) {
            String title = getString(R.string.hardware_tab_memory);
            List<LinkedHashMap<Object, Object>> propertiesList = new ArrayList<>();
            Collections.sort(memoryList);
            for (Memory memory : memoryList) {
                LinkedHashMap<Object, Object> properties = new LinkedHashMap<>();
                properties.put(memory.getDeviceLocator(), memory.getStatus() != null && memory.getStatus().getState() != null ?
                    memory.getStatus().getState().getLabelResId() : "");
                properties.put(R.string.hardware_memo_label_manufacturer, memory.getManufacturer() == null ? HWConstants.DFT_NULL_VALUE : memory.getManufacturer());
                properties.put(R.string.hardware_memo_label_serial_no, memory.getSerialNumber() == null ? HWConstants.DFT_NULL_VALUE : memory.getSerialNumber());
                properties.put(R.string.hardware_memo_label_part_no, memory.getPartNumber() == null ? HWConstants.DFT_NULL_VALUE : memory.getPartNumber());
                properties.put(R.string.hardware_memo_label_type, memory.getMemoryDeviceType() == null ? HWConstants.DFT_NULL_VALUE : memory.getMemoryDeviceType());
                properties.put(R.string.hardware_memo_label_capacity, memory.getCapacityMiB() == null ? HWConstants.DFT_NULL_VALUE : memory.getCapacityMiB() + " MB");
                properties.put(R.string.hardware_memo_label_speed, memory.getOperatingSpeedMhz() == null ? HWConstants.DFT_NULL_VALUE : memory.getOperatingSpeedMhz() + " MHz");
                properties.put(R.string.hardware_memo_label_width_bit, memory.getDataWidthBits() == null ? HWConstants.DFT_NULL_VALUE : memory.getDataWidthBits() + " bit");
                properties.put(R.string.hardware_memo_label_rank_count, memory.getRankCount() == null ? HWConstants.DFT_NULL_VALUE : memory.getRankCount() + " rank");
                properties.put(R.string.hardware_memo_label_min_voltage, memory.getOem() != null && memory.getOem().getMinVoltageMillivolt() != null ?
                    memory.getOem().getMinVoltageMillivolt() + " mV" : HWConstants.DFT_NULL_VALUE);
                properties.put(R.string.hardware_memo_label_position, memory.getOem() != null && memory.getOem().getPosition() != null ?
                    memory.getOem().getPosition() : HWConstants.DFT_NULL_VALUE);
                properties.put(R.string.hardware_memo_label_technology, memory.getOem() != null && memory.getOem().getTechnology() != null ?
                    memory.getOem().getTechnology() : HWConstants.DFT_NULL_VALUE);
                propertiesList.add(properties);
            }
            body.appendChild(generate(title, propertiesList.toArray(new LinkedHashMap[0])));
        }

        private void generateCpuReport(Element body) {
            String title = getString(R.string.hardware_tab_cpu);
            List<LinkedHashMap<Object, Object>> propertiesList = new ArrayList<>();

            Collections.sort(processorList);
            for (Processor processor : processorList) {
                LinkedHashMap<Object, Object> properties = new LinkedHashMap<>();
                properties.put(processor.getName(), processor.getStatus().getState().getLabelResId());
                properties.put(R.string.hardware_cpu_label_manufacturer, processor.getManufacturer());
                properties.put(R.string.hardware_cpu_label_model, processor.getModel());
                properties.put(R.string.hardware_cpu_label_processor_id, processor.getProcessorId() == null ?
                    HWConstants.DFT_NULL_VALUE : processor.getProcessorId().getIdentificationRegisters());
                properties.put(R.string.hardware_cpu_label_speed_MHZ, processor.getOem().getFrequencyMHz() == null ?
                    HWConstants.DFT_NULL_VALUE : processor.getOem().getFrequencyMHz() + " MHz");
                properties.put(R.string.hardware_cpu_label_cores_and_threads,
                    String.format(Locale.US, "%s Cores/%s Threads",
                        StringUtils.defaultString(processor.getTotalCores(), HWConstants.DFT_NULL_VALUE),
                        StringUtils.defaultString(processor.getTotalThreads(), HWConstants.DFT_NULL_VALUE)));

                final Processor.Oem oem = processor.getOem();
                properties.put(R.string.hardware_cpu_label_L123_cache,
                    String.format(Locale.US, "%s/%s/%s KB",
                        StringUtils.defaultString(oem.getL1CacheKiB(), HWConstants.DFT_NULL_VALUE),
                        StringUtils.defaultString(oem.getL2CacheKiB(), HWConstants.DFT_NULL_VALUE),
                        StringUtils.defaultString(oem.getL3CacheKiB(), HWConstants.DFT_NULL_VALUE)));
                properties.put(R.string.hardware_cpu_label_part_no, TextUtils.isEmpty(oem.getPartNumber()) ? HWConstants.DFT_NULL_VALUE : oem.getPartNumber());
                propertiesList.add(properties);
            }
            body.appendChild(generate(title, propertiesList.toArray(new LinkedHashMap[0])));
        }


        private void generateSystemOverview(Element body) {
            // 基础信息
            String title = getString(R.string.ds_label_basic_info);
            LinkedHashMap properties = new LinkedHashMap<>();
            properties.put(R.string.ds_label_model, system.getModel());
            properties.put(R.string.ds_label_health_state, system.getStatus().getHealth().getDisplayResId());
            properties.put(R.string.ds_label_power_state, system.getPowerState().getDisplayResId());
            if (ethernetInterface.getIPv4() != null) {
                properties.put(R.string.ds_label_ipv4, ethernetInterface.getIPv4().getAddress());
            }
            if (ethernetInterface.getIPv6() != null) {
                properties.put(R.string.ds_label_ipv6, ethernetInterface.getIPv6().getAddress());
            }
            properties.put(R.string.ds_label_location, manager.getOem().getDeviceLocation());
            properties.put(R.string.ds_label_asset_tag, system.getAssetTag());
            properties.put(R.string.ds_label_serial_number, system.getSerialNumber());
            properties.put(R.string.ds_label_bmc_version, manager.getFirmwareVersion());
            properties.put(R.string.ds_label_bios_version, system.getBiosVersion());
            if (processorList.size() > 0) {
                properties.put(R.string.ds_label_cpu, processorList.size() + "*" + processorList.get(0).getModel());
            } else {
                properties.put(R.string.ds_label_cpu, "");
            }
            final Integer totalSystemMemoryGiB = system.getMemorySummary().getTotalSystemMemoryGiB();
            if (totalSystemMemoryGiB != null) {
                properties.put(R.string.ds_label_memory, totalSystemMemoryGiB + "GB");
            }
            body.appendChild(generate(title, properties));
        }

        public Element generate(String resource, LinkedHashMap... propertiesList) {
            final Element container = new Element("div");
            if (!TextUtils.isEmpty(resource)) {
                Element header = new Element("h3").text(resource);
                container.appendChild(header);
            }

            for (LinkedHashMap<Object, Object> properties : propertiesList) {
                Element table = new Element("table");
                for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                    final Element tr = new Element("tr");
                    final Object key = entry.getKey();
                    final Object display = entry.getValue();

                    if (key != null) {
                        final String labelValue = key instanceof String ? key.toString() : getString((Integer) key);
                        Element label = new Element("td").text(labelValue);
                        tr.appendChild(label);
                    } else {
                        tr.appendChild(new Element("td"));
                    }

                    if (display != null) {
                        final String displayValue = display instanceof String ? display.toString() : getString((Integer) display);
                        Element value = new Element("td").text(displayValue);
                        tr.appendChild(value);
                    } else {
                        tr.appendChild(new Element("td"));
                    }
                    table.appendChild(tr);
                }
                container.appendChild(table);
            }
            return container;
        }

        private void getHealth(final CountDownLatch latch) {
            redfish.chassis().getThermal(RRLB.<Thermal>create(activity).callback(
                new RedfishResponseListener.Callback<Thermal>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Thermal thermal) {
                        LoadReportDataTask.this.thermal = thermal;
                        latch.countDown();
                    }
                }).build());

            redfish.systems().get(RRLB.<ComputerSystem>create(activity).callback(
                new RedfishResponseListener.Callback<ComputerSystem>() {
                    @Override
                    public void onResponse(Response okHttpResponse, ComputerSystem system) {
                        LoadReportDataTask.this.system = system;
                        latch.countDown();
                    }
                }).build());

            redfish.chassis().get(RRLB.<Chassis>create(activity).callback(new RedfishResponseListener.Callback<Chassis>() {
                @Override
                public void onResponse(Response okHttpResponse, Chassis chassis) {
                    LoadReportDataTask.this.chassis = chassis;
                    latch.countDown();
                }
            }).build());

            redfish.chassis().getPower(RRLB.<Power>create(activity).callback(
                new RedfishResponseListener.Callback<Power>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Power power) {
                        LoadReportDataTask.this.power = power;
                        latch.countDown();
                    }
                }).build());
        }

        private void getStorages(final CountDownLatch latch) {
            activity.getRedfishClient().systems().getStorages(RRLB.<Storages>create(activity).callback(
                new RedfishResponseListener.Callback<Storages>() {
                    @Override
                    public void onResponse(Response okHttpResponse, Storages storages) {
                        final LoadStorageListTask task = new LoadStorageListTask(activity, LoadReportDataTask.this.storageList);
                        task.submit(storages.getMembers(), latch);
                    }
                }
            ).build());
        }

        private void getManager(final CountDownLatch latch) {
            redfish.managers().get(RRLB.<Manager>create(activity).callback(new RedfishResponseListener.Callback<Manager>() {
                @Override
                public void onResponse(Response okHttpResponse, Manager response) {
                    LoadReportDataTask.this.manager = response;
                    latch.countDown();
                }
            }).build());
        }

        private void getNetwork(final CountDownLatch latch) {
            redfish.managers().getEthernetInterfaces(RRLB.<EthernetInterfaceCollection>create(activity).callback(
                new RedfishResponseListener.Callback<EthernetInterfaceCollection>() {
                    @Override
                    public void onResponse(Response okHttpResponse, EthernetInterfaceCollection collection) {
                        ResourceId resourceOdataId = collection.getMembers().get(0); // 获取对应的网口资源链接
                        redfish.managers().getEthernetInterface(resourceOdataId.getOdataId(), RRLB.<EthernetInterface>create(activity).callback(
                            new RedfishResponseListener.Callback<EthernetInterface>() {
                                @Override
                                public void onResponse(Response okHttpResponse, EthernetInterface ethernetInterface) {
                                    LoadReportDataTask.this.ethernetInterface = ethernetInterface;
                                    latch.countDown();
                                }
                            }).build());
                    }
                }).build());
        }

        private void getSoftwareInventories(final CountDownLatch latch) {
            redfish.updateService().getFirmwareInventory(RRLB.<FirmwareInventory>create(activity)
                .callback(new RedfishResponseListener.Callback<FirmwareInventory>() {
                    @Override
                    public void onResponse(Response okHttpResponse, FirmwareInventory firmwareInventory) {
                        LoadSoftwareInventoryListTask task = new LoadSoftwareInventoryListTask(activity, softwareInventoryList);
                        task.submit(firmwareInventory.getMembers(), latch);
                    }
                }).build());
        }

        private void getProcessors(final CountDownLatch latch) {
            redfish.systems().getProcessors(RRLB.<Processors>create(activity).callback(
                new RedfishResponseListener.Callback<Processors>() {
                    @Override
                    public void onResponse(Response okHttpResponse, final Processors response) {
                        LoadProcessorsTask task = new LoadProcessorsTask(activity, processorList);
                        task.submit(response.getMembers(), latch);
                    }
                }
            ).build());
        }

        private void loadMemories(final CountDownLatch latch) {
            final List<Memory> all = new ArrayList<>();
            this.loadMemoryInto(null, all, latch);
        }

        private void loadMemoryInto(final String resourceOdataId, final List<Memory> results, final CountDownLatch latch) {
            redfish.systems().getMemoryCollection(resourceOdataId, RRLB.<MemoryCollection>create(activity).callback(
                new RedfishResponseListener.Callback<MemoryCollection>() {
                    @Override
                    public void onResponse(Response okHttpResponse, MemoryCollection response) {
                        if (response.getCount() > 0) {
                            results.addAll(response.getMembers());
                        }
                        if (!TextUtils.isEmpty(response.getNextLink())) {
                            loadMemoryInto(response.getNextLink(), results, latch);
                        } else {
                            final LoadMemoriesTask task = new LoadMemoriesTask(activity, memoryList);
                            task.submit(results, latch);
                        }
                    }
                }).build());
        }
    }
}
