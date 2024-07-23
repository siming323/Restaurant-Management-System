package com.sky;

import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author siming323
 * @date 2023/10/29 9:28
 */
//@SpringBootTest
public class HttpClientTest {
    /**
     * 发送get请求
     */
    @SneakyThrows
    @Test
    public void testGet() {
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建请求对象
        HttpGet httpGet = new HttpGet("http://localhost:8080/user/shop/status");
        //HttpGet httpGet = new HttpGet("https://www.baidu.com");
        //发送请求,接收响应结果
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        //获取服务端返回的状态码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        HttpEntity entity = httpResponse.getEntity();
        String body = EntityUtils.toString(entity);
        System.out.println("服务端返回的状态码为:" + statusCode);
        System.out.println("服务端返回的数据为:" + body);
        //释放资源
        httpResponse.close();
        httpClient.close();
    }

    /**
     * 发送post请求
     */
    @SneakyThrows
    @Test
    public void testPost(){
        //创建HttpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建请求对象
        HttpPost httpPost = new HttpPost("http://localhost:8080/admin/employee/login");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username","xlain");
        jsonObject.put("password","123456");
        StringEntity entity = new StringEntity(jsonObject.toString());
        //指定请求编码方式
        entity.setContentEncoding("utf-8");
        //指定数据格式
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        //发送请求,接收响应结果
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        //获取服务端返回的状态码
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        HttpEntity entity1 = httpResponse.getEntity();
        String body = EntityUtils.toString(entity1);
        System.out.println("响应码为:"+statusCode);
        System.out.println("响应数据为:"+body);
        //释放资源
        httpResponse.close();
        httpClient.close();
    }
}
