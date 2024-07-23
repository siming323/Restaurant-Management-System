package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author siming323
 * @date 2023/12/18 9:24
 */
@Component
@ConfigurationProperties(prefix = "sky.baidu")
@Data
public class BaiduProperties {
    private String ak;
}
