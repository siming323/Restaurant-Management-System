package com.sky.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.Page;
import com.sky.constant.MessageConstant;
import com.sky.constant.ResolveDatabaseTableIdSelfGrowingFailureConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author siming323
 * @date 2023/11/9 23:20
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    private static final String KEY = "ImagePath";

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private ResolveIdSelfGrowingFailureMapper resolveIdSelfGrowingFailureMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DeleteImageMapper deleteImageMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSetmeal(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //重置id索引
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.SETMEAL);
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.SETMEAL_DISH);
        //向setmeal表插入数据
        setmealMapper.insert(setmeal);
        //获取套餐id
        Long setmealId = setmeal.getId();
        //根据套餐id关联该套餐想要关联菜品
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        int pageNum = setmealPageQueryDTO.getPage();
        int pageSize = setmealPageQueryDTO.getPageSize();
        PageHelper.startPage(pageNum,pageSize);
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 套餐起售/停售
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status.equals(StatusConstant.ENABLE)){
            //select a.* from dish a left join setmeal_dish b on a.id = b.dish_id where b.setmeal_id = ?
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() > 0){
                dishList.forEach(dish -> {
                    if(StatusConstant.DISABLE.equals(dish.getStatus())){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder().id(id).status(status).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐
     * 直接修改setmeal表，然后删除原先setmeal_dish表中的对应关系，重新添加
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {
        //在这里删除原图片
        String deleteImage = (String) redisTemplate.opsForValue().get(KEY);
        if (deleteImage != null){
            if (!deleteImage.equals(setmealDTO.getImage())){
                //查询套餐和菜品表单的image表单项数据
                List<String> lists = deleteImageMapper.selectSetmealAndDishImage();
                for (String list : lists) {
                    if (deleteImage.equals(list)){
                        //正则表达式匹配文件名
                        String regex = "http://127.0.0.1:8080/images/(.*)";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(list);
                        if (matcher.find()){
                            String deleteFileName = matcher.group(1);
                            //删除图片
                            if (new File("E:/javaSTUDY/sky-take-out-imageFile/"+deleteFileName).delete()){
                                break;
                            }else{
                                log.error("删除原图片失败");
                            }
                        }
                    }
                }
            }
        }
        redisTemplate.delete(KEY);

        //拷贝属性
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.SETMEAL);
        setmealMapper.update(setmeal);
        //获取套餐id
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteSetmealDishBySetmealId(setmealId);
        //获取新的关联关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }
        resolveIdSelfGrowingFailureMapper.resolveIdSelfGrowingFailureMapper(ResolveDatabaseTableIdSelfGrowingFailureConstant.SETMEAL_DISH);
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 根据setmeal表的id获取需要回显到页面的内容
     * @param id
     * @return
     */
    @Override
    public SetmealVO getSetmealAndDishBySetmealId(Long id) {
        SetmealVO setmealVO = new SetmealVO();
        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.getDishBySetmealId(id);
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        redisTemplate.opsForValue().set(KEY,setmealVO.getImage());
        return setmealVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> ids) {
        //如果套餐在起售中则不可删除
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        setmealMapper.deleteBatch(ids);
        setmealDishMapper.deleteBatch(ids);
    }

    @Override
    public List<Setmeal> list(Long categoryId) {
        return setmealMapper.list(categoryId);
    }

    /**
     * 根据套餐id查询包含的菜品
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemsBySetmealId(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
