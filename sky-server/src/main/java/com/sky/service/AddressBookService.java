package com.sky.service;

import com.sky.entity.AddressBook;

import java.util.List;

/**
 * @author siming323
 * @date 2023/11/24 8:44
 */
public interface AddressBookService {
    /**
     * 新增地址
     * @param addressBook
     */
    void addAddressBook(AddressBook addressBook);

    /**
     * 查询当前登录用户的所有地址信息
     * @param addressBook
     * @return
     */
    List<AddressBook> list(AddressBook addressBook);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefaultAddress(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getAddressById(Long id);

    /**
     * 根据id删除地址
     * @param id
     */
    void deleteAddressById(Long id);

    /**
     * 根据id修改地址
     * @param addressBook
     */
    void updateAddressById(AddressBook addressBook);
}
