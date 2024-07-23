package com.sky.service.impl;

import com.sky.constant.DefaultAddressConstant;
import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.ResolveIdSelfGrowingFailureMapper;
import com.sky.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author siming323
 * @date 2023/11/24 8:44
 */
@Service
public class AddressBookServiceImpl implements AddressBookService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void addAddressBook(AddressBook addressBook) {
        //TODO 这里应该先设置用户id和是否默认
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(DefaultAddressConstant.NON_DEFAULT);
        //重置数据库自增长id索引
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.ADDRESS_BOOK);
        addressBookMapper.insert(addressBook);
    }

    /**
     * 查询当前登录用户的所有地址信息
     * @param userId
     * @return
     */
    @Override
    public List<AddressBook> list(AddressBook addressBook)  {
        return addressBookMapper.list(addressBook);
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(AddressBook addressBook) {
        //默认地址只能有一个
        Long userId = BaseContext.getCurrentId();
        addressBookMapper.updateDefaultAddressAsNondefaultByUserId(userId);
        addressBook.setIsDefault(DefaultAddressConstant.DEFAULT);
        addressBookMapper.update(addressBook);
    }

    @Override
    public AddressBook getAddressById(Long id) {
        return addressBookMapper.getAddressById(id);
    }

    @Override
    public void deleteAddressById(Long id) {
        addressBookMapper.deleteAddressById(id);
    }

    @Override
    public void updateAddressById(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }
}
