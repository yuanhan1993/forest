package org.forest.backend;

import org.forest.config.ForestConfiguration;
import org.forest.exceptions.ForestRuntimeException;
import org.forest.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gongjun[jun.gong@thebeastshop.com]
 * @since 2018-03-01 11:24
 */
public class HttpBackendSelector {

    private final static Map<String, HttpBackendCreator> backendMap = new HashMap<>();

    private final static String HTTPCLIENT_BACKEND_NAME = "httpclient";
    private final static String OKHTTP3_BACKEND_NAME = "okhttp3";

    private final static String HTTPCLIENT_CLIENT_CLASS_NAME = "org.apache.http.client.HttpClient";
    private final static String OKHTTP3_CLIENT_CLASS_NAME = "okhttp3.OkHttpClient";

    private final static String HTTPCLIENT_BACKEND_CLASS_NAME = "org.forest.backend.httpclient.HttpclientBackend";
    private final static String OKHTTP3_BACKEND_CLASS_NAME = "org.forest.backend.okhttp3.OkHttp3Backend";

    private final static HttpBackendCreator HTTPCLIENT_BACKEND_CREATOR = new HttpBackendCreator(HTTPCLIENT_BACKEND_CLASS_NAME);
    private final static HttpBackendCreator OKHTTP3_BACKEND_CREATOR = new HttpBackendCreator(OKHTTP3_BACKEND_CLASS_NAME);

    static {
        backendMap.put(HTTPCLIENT_BACKEND_NAME, HTTPCLIENT_BACKEND_CREATOR);
        backendMap.put(OKHTTP3_BACKEND_NAME, OKHTTP3_BACKEND_CREATOR);
    }

    public static HttpBackend select(ForestConfiguration configuration) {
        String name = configuration.getBackendName();
        if (StringUtils.isNotEmpty(name)) {
            HttpBackendCreator backendCreator = backendMap.get(name);
            if (backendCreator == null) {
                throw new ForestRuntimeException("Http setBackend \"" + name + "\" can not be found.");
            }
            return backendCreator.create();
        }

        try {
            Class.forName(OKHTTP3_CLIENT_CLASS_NAME);
            return OKHTTP3_BACKEND_CREATOR.create();
        } catch (ClassNotFoundException e) {
        }

        try {
            Class.forName(HTTPCLIENT_CLIENT_CLASS_NAME);
            return HTTPCLIENT_BACKEND_CREATOR.create();
        } catch (ClassNotFoundException e) {
        }
        throw new ForestRuntimeException("Http Backed is undefined.");
    }


    static class HttpBackendCreator {

        public String className;

        public HttpBackendCreator(String className) {
            this.className = className;
        }

        public HttpBackend create() {
            try {
                Class klass = Class.forName(className);
                return (HttpBackend) klass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new ForestRuntimeException(e);
            } catch (InstantiationException e) {
                throw new ForestRuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new ForestRuntimeException(e);
            }
        }
    }

}