package com.sky.controller.user;

import com.sky.constant.DefaultAddressConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author siming323
 * @date 2023/11/23 10:20
 */
@RestController
@RequestMapping("/user/addressBook")
@Api(tags = "C端-地址簿接口")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result save(@RequestBody AddressBook addressBook){
        log.info("新增地址:{}",addressBook);
        addressBookService.addAddressBook(addressBook);
        return Result.success();
    }

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息")
    public Result<List<AddressBook>> list() {
        //TODO 这里虽然传userId也能实现，但是接口设计为传AddressBook提高可复用性
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBook);
        return Result.success(list);
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefaultAddress(){
        List<AddressBook> addressBooks = addressBookService.list(AddressBook
                .builder()
                .userId(BaseContext.getCurrentId())
                .isDefault(DefaultAddressConstant.DEFAULT)
                .build()
        );
        if (addressBooks != null && addressBooks.size() > 0){
            return Result.success(addressBooks.get(0));
        }
        return Result.error(MessageConstant.NON_DEFAULT);
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefaultAddress(@RequestBody AddressBook addressBook){
        addressBookService.setDefaultAddress(addressBook);
        return Result.success();
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @GetMapping("{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getAddressById(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getAddressById(id);
        return Result.success(addressBook);
    }

    /**
     * 根据id删除地址
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteAddressById(Long id){
        addressBookService.deleteAddressById(id);
        return Result.success();
    }

    /**
     * 根据id修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result updateAddressById(@RequestBody AddressBook addressBook){
        addressBookService.updateAddressById(addressBook);
        return Result.success();
    }
}
