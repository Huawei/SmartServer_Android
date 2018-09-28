package com.huawei.smart.server;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.Utils;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.smart.server.lock.LockManager;
import com.huawei.smart.server.model.Preference;
import com.huawei.smart.server.perference.KeyChainEncryptedPreference;
import com.huawei.smart.server.utils.Compatibility;
import com.jacksonandroidnetworking.JacksonParserFactory;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.logging.Level;

import io.realm.DynamicRealm;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import io.realm.RealmSchema;
import io.realm.log.RealmLog;
import pl.brightinventions.slf4android.FileLogHandlerConfiguration;
import pl.brightinventions.slf4android.LoggerConfiguration;

import static com.huawei.smart.server.HWConstants.USER_PREFERENCE_ID;

public class HWApplication extends MultiDexApplication {

    public static final String PREFERENCE_NAME = "RealmKeys";
    public static final String REALM_ENCRYPTION_KEY = "Realm";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HWApplication.class.getSimpleName());
    public static ObjectMapper mapper = new ObjectMapper();
    public static String theme;
    public static FileLogHandlerConfiguration fileLogHandlerConfiguration;
    private static Context sApplicationContext = null;

    static {
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
//        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);
//        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    //static 代码段可以防止内存泄露
    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorContentBackground, R.color.colorPrimary);//全局设置主题颜色
                return new ClassicsHeader(context);//.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
            }
        });

        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @NonNull
            @Override
            public RefreshFooter createRefreshFooter(@NonNull Context context, @NonNull RefreshLayout layout) {
                layout.setPrimaryColorsId(R.color.colorContentBackground, R.color.colorPrimary);//全局设置主题颜色
                return new ClassicsFooter(context);
            }
        });
    }

    public static Context getContextHW() {
        return sApplicationContext;
    }

    public static String getLogFileFolder() {
        return getContextHW().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsoluteFile() + File.separator + "logs";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApplicationContext = getApplicationContext();
        Utils.init(this);
        initializeRealm();
        initializeAndroidNetworking();
        LockManager.getInstance().enableAppLock(this);
        initializeLogSystem();
    }

    private void resetAppLanguage() {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        SharedPreferences preferences = this.getSharedPreferences(HWConstants.PREFERENCE_SETTINGS, Context.MODE_PRIVATE);
        final String lang = preferences.getString(HWConstants.PREFERENCE_SETTING_LANG, Compatibility.getLocale().getLanguage());
        if (lang.equals("zh")) {
            config.locale = Locale.CHINESE;
        } else { // default
            config.locale = Locale.US;
        }
        resources.updateConfiguration(config, dm);
    }

    private void initializeLogSystem() {
        fileLogHandlerConfiguration = LoggerConfiguration.fileLogHandler(this);
        String logFileFolder = getLogFileFolder();
        FileUtils.createOrExistsDir(logFileFolder);
        fileLogHandlerConfiguration.setFullFilePathPattern(logFileFolder + File.separator + "smart-server.%g.%u.log");
        fileLogHandlerConfiguration.setLevel(Level.INFO);
        try {
            fileLogHandlerConfiguration.setEncoding("UTF-8");
        } catch (Exception e) {
        }
        LoggerConfiguration.configuration().addHandlerToRootLogger(fileLogHandlerConfiguration);


        // crash logger
        final Thread.UncaughtExceptionHandler _wrapped = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                LOG.error("UncaughtException cause application crash", throwable);
                _wrapped.uncaughtException(thread, throwable);
            }
        });
    }

    /**
     * 初始化网络请求组件
     */
    private void initializeAndroidNetworking() {
        AndroidNetworking.initialize(this);
        AndroidNetworking.setParserFactory(new JacksonParserFactory(mapper));
    }

    /**
     * 初始化Realm数据库
     */
    private void initializeRealm() {
        Realm.init(this);
        if (BuildConfig.DEBUG) { // 测试环境
            RealmLog.setLevel(Log.DEBUG); // Enable full log output when debugging
            RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .migration(new Migration()).name("server-manager.realm")
                .encryptionKey(getRealmEncryptionKey())
                .build();
            Realm.setDefaultConfiguration(config);
        } else { // 正式环境
            RealmConfiguration config = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .migration(new Migration()).name("server-manager.realm")
                .encryptionKey(getRealmEncryptionKey())
                .build();
            Realm.setDefaultConfiguration(config);
        }

        // 初始化数据
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    if (realm.where(Preference.class).count() == 0) {
                        // 用户首选项
                        Preference preference = new Preference();
                        preference.setId(USER_PREFERENCE_ID);
                        preference.setAcceptEventPush(true);
                        realm.copyToRealm(preference);
                    }
                }
            });
        }
    }

    public byte[] getRealmEncryptionKey() {
        KeyChainEncryptedPreference preference = getKeyChainEncryptedPreference();
        String realmEncryptionKey = preference.read(REALM_ENCRYPTION_KEY);
        if (TextUtils.isEmpty(realmEncryptionKey)) {
            byte[] encryptionKey = new byte[64];
            new SecureRandom().nextBytes(encryptionKey);
            realmEncryptionKey = ConvertUtils.bytes2HexString(encryptionKey);
            preference.write(REALM_ENCRYPTION_KEY, realmEncryptionKey);
        }
        return ConvertUtils.hexString2Bytes(realmEncryptionKey);
    }

    @NonNull
    private KeyChainEncryptedPreference getKeyChainEncryptedPreference() {
        return new KeyChainEncryptedPreference(getApplicationContext(), PREFERENCE_NAME);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sApplicationContext = null;
    }

    public static class Migration implements RealmMigration {
        @Override
        public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
            RealmSchema schema = realm.getSchema();
            if (oldVersion == 0) {
                schema.get("Device")
                    .addField("token", String.class)
                    .addField("sessionOdataId", String.class);
                oldVersion++;
            }
        }
    }

}
