package com.sky;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author siming323
 * @date 2024/2/6 15:01
 */
//@SpringBootTest
public class JwtTest {
    @Test
    public void genJwt(){
        Map<String,Object> claims = new HashMap<>();
        claims.put("id",1);
        claims.put("username","Tom");

        String jwt = Jwts.builder()
                .setClaims(claims) //自定义内容(载荷)
                .signWith(SignatureAlgorithm.HS256, "xzl@csdn@zmcq") //签名算法
                .setExpiration(new Date(System.currentTimeMillis() + 24*3600*1000)) //有效期
                .compact();

        System.out.println(jwt);
    }

    @Test
    public void parseJwt(){
        Claims claims = Jwts.parser()
                .setSigningKey("xzl@csdn@zmcq")//指定签名密钥（必须保证和生成令牌时使用相同的签名密钥）
                .parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJpZCI6MSwiZXhwIjoxNzA3Mjg5NTY0LCJ1c2VybmFtZSI6IlRvbSJ9.E2NpgJPxu-frYWF05GENtOJPKGhpn7boBQ-b0LnPLnk")
                .getBody();
        System.out.println(claims);
    }
}
