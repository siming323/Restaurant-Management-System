package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.ResolveIdSelfGrowingFailureMapper;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * @author siming323
 * @date 2023/11/3 8:56
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    /**
     * 微信服务接口地址
     */
    private static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties wechatProperties;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        //调用微信接口服务，获取当前微信用户的openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if (openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        //如果是新用户，自动完成注册
        if (user == null){
            user = User.builder().openid(openid).createTime(LocalDateTime.now()).build();
            resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.USER);
            userMapper.insert(user);
        }
        //返回这个用户对象
        return user;
    }

    /**
     * 调用微信接口服务，获取微信用户的openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        HashMap<String, String> map = new HashMap<>();
        map.put("appid",wechatProperties.getAppid());
        map.put("secret",wechatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_typ","authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getString("openid");
    }
}
