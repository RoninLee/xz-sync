package com.zeno;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lizelong
 * @date 2023/5/4
 * @description strava->行者
 **/
public class Strava2Xz {
    static final Logger log = LoggerFactory.getLogger(Strava2Xz.class);

    static String cookie = "";
    static BigDecimal maxId = BigDecimal.ZERO;

    @Test
    public void sync() {
        String stravaConfigPath = "src/main/resources/StravaConfig";

        try (FileReader reader = new FileReader(stravaConfigPath); BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("=", 2);
                if (split.length != 2) {
                    continue;
                }
                String key = split[0].trim();
                if (key.equals("COOKIE")) {
                    cookie = split[1];
                } else if (key.equals("MAX_ID")) {
                    maxId = new BigDecimal(split[1]);
                }
            }
        } catch (IOException e) {
            log.error("read config error.", e);
        }
        Strava2Xz strava2Xz = new Strava2Xz();
        BigDecimal lastId = strava2Xz.downloadFit();
        if (!maxId.equals(BigDecimal.ZERO) && !maxId.equals(lastId)) {
            try (FileWriter fileWriter = new FileWriter(stravaConfigPath)) {
                fileWriter.write("COOKIE=" + cookie + "\n");
                fileWriter.write("MAX_ID=" + maxId);
            } catch (IOException e) {
                log.error("update config error.", e);
            }
            XzManage.uploadFit("stravaFit");
        }
    }

    private BigDecimal downloadFit() {
        Map<String, String> param = new HashMap<>();
        int page = 1;
        int size = 20;
        int limit = 20;
        boolean loop = true;
        Map<String, String> ids = new HashMap<>();
        while (size == limit && loop) {
            param.put("page", String.valueOf(page));
            param.put("per_page", String.valueOf(limit));
            String response = HttpClientUtils.getRequest("https://www.strava.com/athlete/training_activities", param, buildExportHeader());
            JSONObject jsonObject = JSON.parseObject(response);
            JSONArray models = jsonObject.getJSONArray("models");
            for (Object model : models) {
                JSONObject object = (JSONObject) model;
                BigDecimal id = object.getBigDecimal("id");
                if (id.compareTo(maxId) < 1) {
                    loop = false;
                    break;
                }
                String name = object.getString("name");
                ids.put(id.toPlainString(), name);
            }
            page++;
            size = models.size();
        }
        for (Map.Entry<String, String> entry : ids.entrySet()) {
            // try {
            //     Thread.sleep(500);
            // } catch (InterruptedException e) {
            //     log.error("InterruptedException ", e);
            //     Thread.currentThread().interrupt();
            // }
            String id = entry.getKey();
            String name = entry.getValue();
            String fileName = (name + "_" + id);
            String exportUrl = "https://www.strava.com/activities/%s/export_original";
            String url = String.format(exportUrl, id);
            HttpClientUtils.downloadFile(url, "stravaFit", fileName, Collections.emptyMap(), buildFitHeader());
        }
        return ids.keySet().stream().map(BigDecimal::new).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
    }

    private Map<String, String> buildFitHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("cookie", cookie);
        header.put("x-requested-with", "XMLHttpRequest");
        return header;
    }

    private Map<String, String> buildExportHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("cookie", cookie);
        header.put("x-requested-with", "XMLHttpRequest");
        return header;
    }


}
