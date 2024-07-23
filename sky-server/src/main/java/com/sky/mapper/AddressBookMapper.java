package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author siming323
 * @date 2023/11/24 8:45
 */
@Mapper
public interface AddressBookMapper {
    /**
     * 新增地址
     * @param addressBook
     */
    void insert(AddressBook addressBook);

    /**
     * 动态查询地址簿
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 将address_book表中属于当前用户的地址全部设置为非默认地址
     * @param userId
     */
    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void updateDefaultAddressAsNondefaultByUserId(Long userId);

    /**
     * 更新操作
     * @param addressBook
     */
    void update(AddressBook addressBook);

    AddressBook getAddressById(Long id);

    /**
     * 根据id删除地址
     * @param id
     */
    @Delete("delete from address_book where id = #{id}")
    void deleteAddressById(Long id);
}
