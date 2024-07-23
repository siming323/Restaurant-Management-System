package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(description = "员工登录时需要传递的数据模型")
public class EmployeeLoginDTO implements Serializable {

    @ApiModelProperty("username")
    private String username;

    @ApiModelProperty("password")
    private String password;

}
