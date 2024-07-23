package com.sky.utils; // 声明代码所在的包

import com.alibaba.fastjson.JSONObject; // 导入JSON处理库
import org.apache.http.NameValuePair; // 导入HTTP请求参数相关类
import org.apache.http.client.config.RequestConfig; // 导入HTTP请求配置相关类
import org.apache.http.client.entity.UrlEncodedFormEntity; // 导入用于构建POST请求表单的类
import org.apache.http.client.methods.CloseableHttpResponse; // 导入HTTP响应类
import org.apache.http.client.methods.HttpGet; // 导入HTTP GET请求类
import org.apache.http.client.methods.HttpPost; // 导入HTTP POST请求类
import org.apache.http.client.utils.URIBuilder; // 导入用于构建URI的类
import org.apache.http.entity.StringEntity; // 导入用于构建POST请求JSON实体的类
import org.apache.http.impl.client.CloseableHttpClient; // 导入HTTP客户端类
import org.apache.http.impl.client.HttpClients; // 导入HTTP客户端工厂类
import org.apache.http.message.BasicNameValuePair; // 导入HTTP请求参数相关类
import org.apache.http.util.EntityUtils; // 导入HTTP响应内容处理类

import java.io.IOException; // 导入IOException异常类
import java.net.URI; // 导入URI类
import java.util.ArrayList; // 导入ArrayList类
import java.util.List; // 导入List类
import java.util.Map; // 导入Map类

/**
 * Http工具类
 */
public class HttpClientUtil {

    static final int TIMEOUT_MSEC = 5 * 1000; // 声明一个常量表示超时时间（5秒）

    /**
     * 发送GET方式请求
     *
     * @param url      - 请求的URL
     * @param paramMap  - GET请求参数的映射
     * @return 返回响应内容的字符串
     */
    public static String doGet(String url, Map<String, String> paramMap) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        String result = ""; // 存储响应结果的字符串
        CloseableHttpResponse response = null;

        try {
            URIBuilder builder = new URIBuilder(url); // 创建URI构建器
            if (paramMap != null) {
                for (String key : paramMap.keySet()) {
                    builder.addParameter(key, paramMap.get(key)); // 将参数添加到URI中
                }
            }
            URI uri = builder.build(); // 构建最终的URI

            // 创建GET请求
            HttpGet httpGet = new HttpGet(uri);

            // 发送请求
            response = httpClient.execute(httpGet);

            // 判断响应状态
            if (response.getStatusLine().getStatusCode() == 200) {
                result = EntityUtils.toString(response.getEntity(), "UTF-8"); // 从响应中提取内容
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                response.close(); // 关闭响应
                httpClient.close(); // 关闭HTTP客户端
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 发送POST方式请求
     *
     * @param url      - 请求的URL
     * @param paramMap  - POST请求参数的映射
     * @return 返回响应内容的字符串
     * @throws IOException - 抛出IOException异常
     */
    public static String doPost(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            // 创建参数列表
            if (paramMap != null) {
                List<NameValuePair> paramList = new ArrayList();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    paramList.add(new BasicNameValuePair(param.getKey(), param.getValue())); // 将参数添加到POST请求中
                }
                // 模拟表单
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList);
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig()); // 设置请求配置

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8"); // 从响应中提取内容
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    /**
     * 发送POST方式请求，传递JSON数据
     *
     * @param url      - 请求的URL
     * @param paramMap  - POST请求参数的映射
     * @return 返回响应内容的字符串
     * @throws IOException - 抛出IOException异常
     */
    public static String doPost4Json(String url, Map<String, String> paramMap) throws IOException {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        String resultString = "";

        try {
            // 创建Http Post请求
            HttpPost httpPost = new HttpPost(url);

            if (paramMap != null) {
                //构造json格式数据
                JSONObject jsonObject = new JSONObject();
                for (Map.Entry<String, String> param : paramMap.entrySet()) {
                    jsonObject.put(param.getKey(), param.getValue());
                }
                StringEntity entity = new StringEntity(jsonObject.toString(), "utf-8"); // 创建JSON实体
                //设置请求编码
                entity.setContentEncoding("utf-8");
                //设置数据类型
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }

            httpPost.setConfig(builderRequestConfig()); // 设置请求配置

            // 执行http请求
            response = httpClient.execute(httpPost);

            resultString = EntityUtils.toString(response.getEntity(), "UTF-8"); // 从响应中提取内容
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return resultString;
    }

    private static RequestConfig builderRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(TIMEOUT_MSEC) // 设置连接超时时间
                .setConnectionRequestTimeout(TIMEOUT_MSEC) // 设置连接请求超时时间
                .setSocketTimeout(TIMEOUT_MSEC) // 设置Socket超时时间
                .build(); // 构建RequestConfig对象
    }
}
