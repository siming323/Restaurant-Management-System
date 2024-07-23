package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author siming323
 * @date 2023/10/7 21:42
 */
@Mapper
public interface QueryWhetherTheFieldExistsMapper {
    /**
     * 查询category表中是否存在某个id
     * @param id
     * @return
     */
    @Select("SELECT COUNT(*) FROM category WHERE id = #{id}")
    Integer queryId(Long id);
}
