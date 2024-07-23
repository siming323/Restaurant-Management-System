package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @author siming323
 * @date 2023/11/1 10:45
 */
public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);
}
