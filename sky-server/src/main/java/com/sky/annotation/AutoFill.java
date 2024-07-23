package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，表示某个方法需要进行公共字段自动填充处理
 * @author siming323
 * @date 2023/10/8 21:51
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    /**
     * 数据库操作类型：UPDATE,INSERT
     * @param value 参数value，它的类型是OperationType。
     * 这个参数没有默认值，因此在使用AutoFill注解时，必须提供一个OperationType值。
     */
    OperationType value();
}
