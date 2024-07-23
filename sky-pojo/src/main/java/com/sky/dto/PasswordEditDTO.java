package com.sky.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class PasswordEditDTO implements Serializable {

    /** 员工id */
    //@JsonProperty("id")
    private Long id;

    //旧密码
    private String oldPassword;

    //新密码
    private String newPassword;

}
