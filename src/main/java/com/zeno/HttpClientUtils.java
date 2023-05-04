package com.zeno;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author lizelong
 * @date 2023/5/4
 * @description httpClient工具类
 **/
@Slf4j
public class HttpClientUtils {

    private HttpClientUtils() {
    }

    private static final OkHttpClient client;

    static {
        client = new OkHttpClient().newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static void downloadFile(String url, String path, String fileName, Map<String, String> params, Map<String, String> headers) {
        try {
            fileName = fileName.replace(":", "_")
                    .replace("/", "_")
                    .replace("\\*", "_")
                    .replace("\\?", "_")
                    .replace("\"", "_")
                    .replace("<", "_")
                    .replace(">", "_")
                    .replace("\\|", "_")
                    .replace("\\\\", "_");
            File file = new File(String.format("%s/%s.fit", path, fileName));
            if (file.exists()) {
                return;
            }
            Response response = client.newCall(getRequestBuild(url, params, headers)).execute();
            InputStream bis = response.body().byteStream();
            try (FileOutputStream fis = new FileOutputStream(file); bis) {
                byte[] buffer = new byte[100];
                int count;
                while ((count = bis.read(buffer)) != -1) {
                    fis.write(buffer, 0, count);
                }
            }
        } catch (Exception e) {
            log.error("download file error.", e);
        }
    }

    public static String getRequest(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            Response response = client.newCall(getRequestBuild(url, params, headers)).execute();
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("get request error.", e);
        }
        return null;
    }

    private static Request getRequestBuild(String url, Map<String, String> params, Map<String, String> headers) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        Request.Builder reqBuilder = new Request.Builder().url(urlBuilder.build()).get();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        return reqBuilder.build();
    }

    public static String postRequest(String url, String requestBody, Map<String, String> headers) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        Request.Builder reqBuilder = new Request.Builder().url(urlBuilder.build()).post(RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8")));
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        try {
            Response response = client.newCall(reqBuilder.build()).execute();
            if (response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("post request error.", e);
        }
        return null;
    }

    public static void postFormRequest(String url, File file, Map<String, String> params, Map<String, String> headers) {
        Request.Builder reqBuilder = new Request.Builder().url(url);
        MultipartBody.Builder multipartBodyBuilder = new MultipartBody.Builder();
        multipartBodyBuilder.setType(MultipartBody.FORM);
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                reqBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multipartBodyBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        if (file != null) {
            multipartBodyBuilder.addFormDataPart("upload_file_name", file.getName(), RequestBody.create(file, MediaType.parse("multipart/form-data")));
        }
        try {
            Response response = client.newCall(reqBuilder.post(multipartBodyBuilder.build()).build()).execute();
            String result;
            if (response.body() != null) {
                result = response.body().string();
                if (result.contains("server")) {
                    file.renameTo(new File(file.getParent() + "/【Completed】" + file.getName()));
                } else {
                    file.renameTo(new File(file.getParent() + "/【Failed】" + file.getName()));
                }
                log.info("result: " + result + "\t FileName: " + file.getName());
            }
        } catch (IOException e) {
            log.error("upload file error.", e);
        }
    }
}
