package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import com.sky.exception.AutoFillDatabasePublicFieldException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段自动填充逻辑
 *
 * @author siming323
 * @date 2023/10/8 22:06
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 指定切点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    /**
     * 前置通知
     * 在通知中进行公共字段赋值
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充");
        //获取当前被拦截方法上的数据库操作类型
        /*
         * 获取方法签名对象，类型为MethodSignature,是Signature的子接口
         */
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        /*
         * 获得方法上的注解对象
         */
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        /*
         * 获得数据库操作类型
         */
        OperationType operationType = autoFill.value();

        //获取到当前被拦截的方法的参数->实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0){
            throw new AutoFillDatabasePublicFieldException(MessageConstant.AUTO_FILL_ARGS_EXCEPTION);
        }
        /*
         * 约定参数第一个为实体对象
         */
        Object entity = args[0];
        //准备好赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        //根据当前拦截到注解配置的不同的操作类型，为对应的属性通过反射机制来完成赋值
        if (operationType == OperationType.INSERT){
            /*
             * 为四个公共字段赋值
             */
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                /*
                 * 通过反射为对象属性赋值
                 */
                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }
        if (operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

}
