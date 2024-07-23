package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author siming323
 * @date 2024/2/7 1:38
 */
@Mapper
public interface DeleteImageMapper {
    @Select("select image from setmeal union select image from dish")
    List<String> selectSetmealAndDishImage();
}
