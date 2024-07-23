package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author siming323
 */
@SuppressWarnings("AlibabaCommentsMustBeJavadocFormat")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
/*
 * @Builder 是 Lombok 提供的一个注解，用于生成构建者模式相关的方法。
 * 构建者模式允许你通过链式调用方法来构建对象，从而提高可读性和可维护性。
 * 使用 @Builder 注解后，Lombok 自动生成一个内部静态类，用于创建对象，
 * 并为每个字段生成一个对应的方法，例如 name("John").age(30).build()
 */
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String name;

    private String password;

    private String phone;

    private String sex;

    private String idNumber;

    private Integer status;

    /**
     * 日期格式化注解：@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     */
    private LocalDateTime createTime;

    /**
     * 日期格式化注解：@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     */
    private LocalDateTime updateTime;

    private Long createUser;

    private Long updateUser;

}
