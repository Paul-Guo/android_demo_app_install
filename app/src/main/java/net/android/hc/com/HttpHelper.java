package net.android.hc.com;

import android.util.Log;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

/**
 * Created by paulguo on 2016/1/11.
 */
public class HttpHelper {
    public static class OkHttpResponse {
        private int httpCode;
        private String msg;
        private String responseString;
        private InputStream inputStream;
        private Headers headers;
        private ResponseBody body;
        private long length;

        public long getLength() {
            if (length <= 0 && null != body) {
                length = body.contentLength();
            }
            return length;
        }

        public InputStream getInputStream() {
            if (null == inputStream && null != body) {
                inputStream = body.byteStream();
            }
            return inputStream;
        }

        public String getResponseString() {
            if (null == responseString && null != body) {
                try {
                    responseString = body.string();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return responseString;
        }

        public void setResponseBody(ResponseBody body) {
            this.body = body;
        }

        public void close() {
            if (null != body) {
                body.close();
            }
        }

        public boolean isSuccess() {
            return httpCode >= 200 && httpCode < 300;
        }

        public Headers getHeaders() {
            return headers;
        }

        public void setHeaders(Headers headers) {
            this.headers = headers;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public int getHttpCode() {
            return httpCode;
        }

        public void setHttpCode(int httpCode) {
            this.httpCode = httpCode;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("####");
            stringBuilder.append("code , " + getHttpCode());
            stringBuilder.append("####");
            stringBuilder.append("msg , " + getMsg());
            stringBuilder.append("####");
            stringBuilder.append("response , " + getResponseString());
            stringBuilder.append("####");
            return stringBuilder.toString();
        }
    }

    protected static final String logger = HttpHelper.class.getName();

    public final static MediaType MediaTypeBytes = MediaType.parse("application/octet-stream");

    public OkHttpClient client = new OkHttpClient();

    void run() throws IOException {
        OkHttpResponse result = getWizHttpCodeReturn(new URL("https://raw.github.com/square/okhttp/master/README.md"));
        System.out.println(result.getResponseString());
    }

    private Call getCall(Request.Builder builder) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };
//
//        // Install the all-trusting trust manager
//        SSLContext sc = SSLContext.getInstance("SSL");
//        sc.init(null, trustAllCerts, new java.security.SecureRandom());
//        SSLSocketFactory socketFactory = sc.getSocketFactory();
//        HttpsURLConnection.setDefaultSSLSocketFactory(socketFactory);
//        client.setSocketFactory(socketFactory);
//        client.setSslSocketFactory(socketFactory);
//
//        // Create all-trusting host name verifier
//        HostnameVerifier allHostsValid = new HostnameVerifier() {
//            public boolean verify(String hostname, SSLSession session) {
//                return true;
//            }
//        };
//        // Install the all-trusting host verifier
//        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
//        client.setHostnameVerifier(allHostsValid);

//        SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
//        sslContextBuilder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
//        SSLContext build = sslContextBuilder.build();
//        build.init(null, trustAllCerts, new SecureRandom());
//        client.setSslSocketFactory(build.getSocketFactory());
//        client.setHostnameVerifier(new AllowAllHostnameVerifier());
        return client.newCall(builder.build());
    }

    public OkHttpResponse getWizHttpCodeReturn(URL url, HashMap<String, String> headers) throws IOException {
        Log.d(logger, url.toString());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (null != headers) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (null != key && null != value) {
                    builder.addHeader(key, value);
                }
            }
        }
        return getOkHttpResponse(builder);
    }

    public OkHttpResponse getWizHttpCodeReturn(URL url) throws IOException {
        return getWizHttpCodeReturn(url, null);
    }

    public OkHttpResponse putWizHttpCodeReturn(URL url) throws IOException {
        return putWizHttpCodeReturn(url, null);
    }

    public OkHttpResponse putWizHttpCodeReturn(URL url, byte[] body) throws IOException {
        return putWizHttpCodeReturn(url, null, null, body);
    }

    public OkHttpResponse putWizHttpCodeReturn(
            URL url, HashMap<String, String> params,
            HashMap<String, String> headers,
            byte[] body) throws IOException {
        return putWizHttpCodeReturn(url, params, headers, body, null);
    }

    public OkHttpResponse putWizHttpCodeReturn(
            URL url, HashMap<String, String> params,
            HashMap<String, String> headers,
            byte[] body,
            final InputStream inputStream) throws IOException {
        Log.d(logger, url.toString());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (null != body) {
            builder.put(RequestBody.create(MediaTypeBytes, body));
        }
        if (null != inputStream) {
            RequestBody inputStreamBody = getRequestBody(inputStream);
            builder.put(inputStreamBody);
        }
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        if (null != params) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (null != key && null != value) {
                    formEncodingBuilder.add(key, value);
                }
            }
            if (!params.isEmpty()) {
                RequestBody formBody = formEncodingBuilder.build();
                builder.put(formBody);
            }
        }
        if (null != headers) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (null != key && null != value) {
                    builder.addHeader(key, value);
                }
            }
        }
        return getOkHttpResponse(builder);
    }

    private RequestBody getRequestBody(final InputStream inputStream) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaTypeBytes;
            }

            @Override
            public long contentLength() {
                try {
                    return inputStream.available();
                } catch (IOException e) {
                    return 0;
                }
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                Source source = null;
                try {
                    source = Okio.source(inputStream);
                    sink.writeAll(source);
                } finally {
                    Util.closeQuietly(source);
                }
            }
        };
    }

    public OkHttpResponse headWizHttpCodeReturn(URL url) throws IOException {
        return headWizHttpCodeReturn(url, null);
    }

    public OkHttpResponse headWizHttpCodeReturn(
            URL url, HashMap<String, String> headers) throws IOException {
        Log.d(logger, url.toString());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.head();
        if (null != headers) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (null != key && null != value) {
                    builder.addHeader(key, value);
                }
            }
        }
        return getOkHttpResponse(builder);
    }

    public OkHttpResponse deleteWizHttpCodeReturn(URL url) throws IOException {
        return deleteWizHttpCodeReturn(url, null);
    }

    public OkHttpResponse deleteWizHttpCodeReturn(
            URL url, HashMap<String, String> headers) throws IOException {
        Log.d(logger, url.toString());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete();
        if (null != headers) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (null != key && null != value) {
                    builder.addHeader(key, value);
                }
            }
        }
        return getOkHttpResponse(builder);
    }

    public OkHttpResponse postWizHttpCodeReturn(URL url) throws IOException {
        return postWizHttpCodeReturn(url, null, null, null);
    }

    public OkHttpResponse postWizHttpCodeReturn(URL url, byte[] body) throws IOException {
        return postWizHttpCodeReturn(url, null, null, body);
    }

    public OkHttpResponse postWizHttpCodeReturn(URL url, HashMap<String, String> params) throws IOException {
        return postWizHttpCodeReturn(url, params, null, null);
    }

    public OkHttpResponse postWizHttpCodeReturn(
            URL url,
            HashMap<String, String> params,
            HashMap<String, String> headers,
            byte[] body) throws IOException {
        return postWizHttpCodeReturn(url, params, headers, body, null);
    }

    public OkHttpResponse postWizHttpCodeReturn(
            URL url,
            HashMap<String, String> params,
            HashMap<String, String> headers,
            byte[] body,
            final InputStream inputStream) throws IOException {
        Log.d(logger, url.toString());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        if (null != body) {
            builder.post(RequestBody.create(MediaTypeBytes, body));
        }
        if (null != inputStream) {
            RequestBody inputStreamBody = getRequestBody(inputStream);
            builder.post(inputStreamBody);
        }
        FormBody.Builder formEncodingBuilder = new FormBody.Builder();
        if (null != params) {
            for (String key : params.keySet()) {
                String value = params.get(key);
                if (null != key && null != value) {
                    formEncodingBuilder.add(key, value);
                }
            }
            if (!params.isEmpty()) {
                RequestBody formBody = formEncodingBuilder.build();
                builder.post(formBody);
            }
        }
        if (null != headers) {
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                if (null != key && null != value) {
                    builder.addHeader(key, value);
                }
            }
        }
        return getOkHttpResponse(builder);
    }

    private OkHttpResponse getOkHttpResponse(Request.Builder builder) {
        try {
            Call connection = getCall(builder);
            OkHttpResponse okHttpResponse = new OkHttpResponse();
            Response response = connection.execute();
            okHttpResponse.setResponseBody(response.body());
            okHttpResponse.setMsg(response.message());
            okHttpResponse.setHttpCode(response.code());
            okHttpResponse.setHeaders(response.headers());
            return okHttpResponse;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void main(String[] args) throws IOException {
        new HttpHelper().run();
    }
}
