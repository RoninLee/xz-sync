package com.zeno;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lizelong
 * @date 2023/5/4
 * @description 行者接口
 **/

public class XzManage {

    static final Logger log = LoggerFactory.getLogger(XzManage.class);
    private static final String cookie = "Hm_lvt_7b262f3838ed313bc65b9ec6316c79c4=1680256416,1682406363; csrftoken=HN3TdfYxyf26Yd0YKJzFm7Y7SZ64taTV; sessionid=sxvc84ayjnq63251khqinyku4xh1ybsj";

    public static void uploadFit(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files) {
                if (!file.getName().startsWith("【Completed】") && !file.getName().startsWith("【Failed】") && !file.getName().startsWith(".")) {
                    postFormRequest(file);
                    // try {
                    //     Thread.sleep(1000);
                    // } catch (InterruptedException e) {
                    //     log.error("xzManage InterruptedException", e);
                    //     Thread.currentThread().interrupt();
                    // }
                }
            }
        }
    }

    public static void postFormRequest(File file) {
        String url = "http://www.imxingzhe.com/api/v4/upload_fits";
        // 每个post参数之间的分隔。随意设定，只要不会和其他的字符串重复即可。
        String boundary = "----WebKitFormBoundarypru9MVh5KnoZa4oN";
        // 文件名
        String fileName = file.getName().split("\\.")[0];

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);
        headers.put("Content-Type", "multipart/form-data; boundary=" + boundary);
        headers.put("X-Requested-With", "XMLHttpRequest");
        // headers.put("Accept", "application/json, text/plain, */*");
        // headers.put("Accept-Encoding", "gzip, deflate");
        // headers.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
        // headers.put("Host", "www.imxingzhe.com");
        // headers.put("Origin", "http://www.imxingzhe.com");
        // headers.put("Proxy-Connection", "keep-alive");
        // headers.put("Referer", "http://www.imxingzhe.com/portal/");
        // headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        Map<String, String> params = new HashMap<>();
        params.put("title", fileName);
        params.put("device", "6");
        params.put("sport", "3");
        HttpClientUtils.postFormRequest(url, file, params, headers);
    }

}
