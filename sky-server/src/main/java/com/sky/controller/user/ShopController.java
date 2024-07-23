package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @author siming323
 * @date 2023/10/27 9:28
 * 店铺的营业状态设置不涉及复杂业务逻辑，所以操作redis直接写在controller层
 */
@RestController(value = "userShopController")
@RequestMapping("/user/shop")
@Api(tags = "店铺相关Controller接口")
@Slf4j
public class ShopController {
    private static final String KEY = "SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 获取店铺的营业状态
     *
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺的营业状态")
    public Result<Integer> getShopStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取到店铺的营业状态为:{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }
}
