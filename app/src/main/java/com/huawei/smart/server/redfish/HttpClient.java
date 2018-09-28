package com.huawei.smart.server.redfish;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.ANRequest;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.androidnetworking.interfaces.OkHttpResponseAndParsedRequestListener;
import com.androidnetworking.interfaces.OkHttpResponseListener;
import com.androidnetworking.utils.ParseUtil;
import com.google.gson.JsonObject;
import com.huawei.smart.server.BuildConfig;
import com.huawei.smart.server.model.Device;
import com.huawei.smart.server.redfish.model.ActionResponse;
import com.huawei.smart.server.redfish.model.Chassis;
import com.huawei.smart.server.redfish.model.ChassisCollection;
import com.huawei.smart.server.redfish.model.Resource;
import com.huawei.smart.server.redfish.model.ResourceId;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.realm.Realm;
import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

import static java.net.HttpURLConnection.HTTP_CREATED;

/**
 * Created by DuoQi on 2018-02-11.
 */
public class HttpClient {

    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    public static final MediaType FORM = MediaType.parse("multipart/form-data");
    public static final String PATH_VAR_HARDWARE = "hardware";
    public static final String HEADER_ETAG = "etag";
    public static final String HEADER_IF_MATCH = "If-Match";
    public static final String HEADER_TOKEN = "x-auth-token";
    public static final String HEADER_AUTH_TIMES = "x-auth-times";
    public static final String HEADER_LOCATION = "location";
    public static final String TAG_REDFISH = "redfish";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(HttpClient.class.getSimpleName());
    private final String device;
    private final int port;
    private final String user;
    private final String password;
    private final String baseUrl;
    private String deviceId;

    private String authToken;
    private Integer tryAuthTimes = 0; // try authentication times
    private boolean authFailed = false;
    private Response authResponse;
    private ActionResponse authActionResponse;

    private OkHttpClient okHttpClient; // custom ok-httpclient for every Redfish client
    private String hardware;            // activity_hardware type of the redfish server
    private String sessionOdataId;

    /**
     * @param device   the IP or domain of the Redfish API
     * @param port     the API port of the Redfish API
     * @param user     Redfish authentication username ("root" typically)
     * @param password Redfish authentication password
     */
    public HttpClient(final String device, Integer port, final String user, final String password) {
        this.device = device;
        this.port = port;
        this.user = user;
        this.password = password;
        this.baseUrl = getBaseUrl();
        initialOkHttpClient();
    }

    public void bindExistDevice(Device device) {
        this.deviceId = device.getId();
        this.authToken = device.getToken();
        this.sessionOdataId = device.getSessionOdataId();
    }

    /**
     * TODO: timeout settings?
     */
    private void initialOkHttpClient() {
        try {
            // Install the all-trusting trust manager
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
            };

            // Create an ssl socket factory with our all-trusting manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            this.okHttpClient = new OkHttpClient().newBuilder()
                .retryOnConnectionFailure(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                })
                .addInterceptor(getHttpLoggingInterceptor())
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        final Response proceed = chain.proceed(addAuthHeader(chain.request()));
                        return proceed;
                    }
                })
//                .addNetworkInterceptor(new Interceptor() {
//                    @Override
//                    public Response intercept(Chain chain) throws IOException {
//                        final Response proceed = chain.proceed(addAuthHeader(chain.request()));
//                        return proceed.newBuilder().header("Cache-Control", "max-age=600").build();
//                    }
//                })
                .authenticator(new Authenticator() {
                    @Nullable
                    @Override
                    public Request authenticate(Route route, Response response) {
                        if (authFailed) return null;
                        Request request = response.request();
                        // if destroy session
                        if (request.method().equalsIgnoreCase("delete")
                            && sessionOdataId != null && request.url().toString().endsWith(sessionOdataId))
                        {
                            return null;
                        }
                        final int round = Integer.parseInt(request.header(HEADER_AUTH_TIMES));
                        refreshAuthToken(round);
                        return addAuthHeader(request);
                    }
                }).build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            // should not happen?
        }
    }

    @NonNull
    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        final HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (BuildConfig.DEBUG) {
                    LOG.info(message);
                }
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
        return logging;
    }

    private Request addAuthHeader(Request request) {
        final Request.Builder builder = request.newBuilder()
            .header(HEADER_AUTH_TIMES, String.valueOf(tryAuthTimes));
        if (this.authToken != null) {
            builder.header(HEADER_TOKEN, authToken);
        }
        return builder.build();
    }

    public void refreshAuthToken(int round) {
        synchronized (this) {
            if (tryAuthTimes == round) {
                tryAuthTimes++;

                JsonObject plain = new JsonObject();
                plain.addProperty("UserName", this.user);
                plain.addProperty("Password", this.password);
                RequestBody body = RequestBody.create(JSON, plain.toString());

                try {
                    LOG.info("Try to get auth token now, round " + tryAuthTimes);
                    Request request = new Request.Builder()
                        .url(getAbsUrl("/redfish/v1/SessionService/Sessions"))
                        .post(body).build();
                    authResponse = this.okHttpClient.newCall(request).execute();
                    if (authResponse.code() == HTTP_CREATED) {
                        this.authToken = authResponse.header(HEADER_TOKEN);
                        this.sessionOdataId = authResponse.header(HEADER_LOCATION);
                        if (this.deviceId != null) {
                            try (Realm realm = Realm.getDefaultInstance()) {
                                final Device found = realm.where(Device.class).equalTo("id", this.deviceId).findFirst();
                                if (found != null) {
                                    realm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            found.setToken(authToken);
                                            found.setSessionOdataId(sessionOdataId);
                                            realm.copyToRealmOrUpdate(found);
                                        }
                                    });
                                }
                            }
                        }
                        LOG.info("Authentication accepted");
                    } else {
                        this.authFailed = true;
                        authActionResponse = (ActionResponse) ParseUtil.getParserFactory().responseBodyParser(ActionResponse.class)
                            .convert(authResponse.body());
                        LOG.info("Authentication failed");
                    }
                    // mark authentication failed, discard all requests
                } catch (IOException e) {
                    LOG.info("Authentication failed", e);
                }
            } else {
                LOG.info("Authentication has been processed by another thread, will retry now.");
            }
        }
    }

    private String getBaseUrl() {
        if (!device.startsWith("http://") && !device.startsWith("https://")) {
            return "https://" + device + ":" + port;
        }
        return device + ":" + port;
    }

    public String getAbsUrl(String relatedPath) {
        if (relatedPath.startsWith("http://") || relatedPath.startsWith("https://")) {
            return relatedPath;
        }
        return getBaseUrl() + (relatedPath.startsWith("/") ? "" : "/") + relatedPath;
    }

    /**
     * create a custom FAN get request builder
     *
     * @param path
     * @return
     */
    public ANRequest.GetRequestBuilder get(String path) {
        return AndroidNetworking.get(getAbsUrl(path))
            .addPathParameter(PATH_VAR_HARDWARE, hardware)
            .setOkHttpClient(this.okHttpClient).setTag(this.okHttpClient);
    }

    /**
     * create a custom FAN post request builder
     *
     * @param path
     * @return
     */
    public ANRequest.PostRequestBuilder post(String path, Object body) {
        // TODO, is the default content-accessMethod support by Redfish?
        return AndroidNetworking.post(getAbsUrl(path))
            .addPathParameter(PATH_VAR_HARDWARE, hardware)
            .addApplicationJsonBody(body)
            .setContentType(JSON.toString())
            .setOkHttpClient(this.okHttpClient);
    }

    public ANRequest.PostRequestBuilder patch(String path, Object body) {
        return AndroidNetworking.patch(getAbsUrl(path))
            .addPathParameter(PATH_VAR_HARDWARE, hardware)
            .addApplicationJsonBody(body)
            .setContentType(JSON.toString())
            .setOkHttpClient(this.okHttpClient);
    }

    /**
     * get an Odata resource through Redfish API with default {@link Priority#MEDIUM}
     *
     * @param identifier
     * @param listener
     */
    public void getResource(ResourceId identifier, final OkHttpResponseAndParsedRequestListener listener) {
        this.getResource(identifier.getOdataId(), identifier.getResourceType(), listener, Priority.MEDIUM);
    }

    /**
     * get an Odata resource through Redfish API with default {@link Priority#MEDIUM}
     *
     * @param resourceOdataId
     * @param resourceType
     * @param listener
     */
    public void getResource(String resourceOdataId, Class resourceType, final OkHttpResponseAndParsedRequestListener listener) {
        this.getResource(resourceOdataId, resourceType, listener, Priority.MEDIUM);
    }

    /**
     * get a resource with specified priority
     *
     * @param resourceOdataId
     * @param resourceType
     * @param listener
     * @param priority
     */
    public void getResource(String resourceOdataId, Class resourceType, final OkHttpResponseAndParsedRequestListener listener, Priority priority) {
        this.get(resourceOdataId).setPriority(priority).getResponseOnlyFromNetwork().build()
            .getAsOkHttpResponseAndObject(resourceType, new OkHttpResponseAndParsedRequestListener() {
                @Override
                public void onResponse(Response okHttpResponse, Object response) {
                    if (response instanceof Resource) {
                        final Resource resource = (Resource) response;
                        resource.setEtag(okHttpResponse.header(HEADER_ETAG));
                        listener.onResponse(okHttpResponse, response);
                    } else {
                        listener.onResponse(okHttpResponse, response);
                    }
                }

                @Override
                public void onError(ANError anError) {
                    listener.onError(anError);
                }
            });
    }

    /**
     * 初始化 HttpClient
     * <li>获取硬件资源类型</li>
     *
     * @param listener
     */
    public void initialize(final OkHttpResponseListener listener) {
        LOG.info("Initialize " + getServerTag());
        this.getResource("/redfish/v1/Chassis", ChassisCollection.class, new OkHttpResponseAndParsedRequestListener<ChassisCollection>() {
            @Override
            public void onResponse(Response okHttpResponse, ChassisCollection collection) {
                if (collection.getMembers() != null && collection.getMembers().size() == 1) {
                    final Chassis chassis = collection.getMembers().get(0);
                    final String odataId = chassis.getOdataId();
                    hardware = odataId.replace("/redfish/v1/Chassis/", "");
                    LOG.info("Initialize" + getServerTag() + " done, hardware type is: " + hardware);
                    listener.onResponse(okHttpResponse);
                } else {
                    // should not happen indeed
                    listener.onError(new ANError("", okHttpResponse));
                }
            }

            @Override
            public void onError(ANError anError) {
                listener.onError(anError);
            }
        }, Priority.HIGH);
    }

    public void destroy(final OkHttpResponseListener listener) {
        final String server = getServerTag();
        LOG.info(server + ", cancel all not finished requests");
        AndroidNetworking.cancel(this.okHttpClient);

        if (!TextUtils.isEmpty(this.sessionOdataId)) {
            LOG.info(server + "destroy created redfish session");
            Request request = new Request.Builder()
                .url(getAbsUrl(this.sessionOdataId))
                .delete(null).build();
            this.okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LOG.info(server + "Failed to destroy session", e);
                }

                @Override
                public void onResponse(Call call, Response response) {
                    if (deviceId != null) {
                        try (Realm realm = Realm.getDefaultInstance()) {
                            realm.executeTransaction(new Realm.Transaction() {
                                @Override
                                public void execute(Realm realm) {
                                    final Device found = realm.where(Device.class).equalTo("id", deviceId).findFirst();
                                    if (found != null) {
                                        found.setToken(null);
                                        found.setSessionOdataId(null);
                                        realm.copyToRealm(found);
                                    }
                                }
                            });
                        }
                    }

                    if (listener != null) {
                        listener.onResponse(response);
                    }
                    if (response.isSuccessful()) {
                        LOG.info(server + " destroy session done");
                    } else {
                        LOG.info(server + " Failed to destroy session, reason is: " + response.message());
                    }
                }
            });
        }
    }

    @NonNull
    private String getServerTag() {
        return "[" + this.getBaseUrl() + "] ";
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public Response getAuthenticationResponse() {
        return authResponse;
    }

    public ActionResponse getAuthActionResponse() {
        return this.authActionResponse;
    }
}
