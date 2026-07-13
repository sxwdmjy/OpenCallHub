package com.och.security.handler;

import com.och.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * MyBatis-Plus 字段自动填充器
 * <p>
 * 在插入和更新操作时，自动填充 createTime、createBy、updateTime、updateBy 字段。
 * 通过 SecurityUtils 获取当前登录用户 ID 作为操作人。
 * </p>
 *
 * @author danmo
 * @date 2023/6/19 21:42
 */
@Slf4j
@Component
public class MetaObjectHandler implements com.baomidou.mybatisplus.core.handlers.MetaObjectHandler {
    private static final String CREATE_TIME = "createTime";
    private static final String CREATE_BY = "createBy";
    private static final String UPDATE_TIME = "updateTime";
    private static final String UPDATE_BY = "updateBy";

    @Override
    public void insertFill(MetaObject metaObject) {
        //创建时间
        this.setFieldValByName(CREATE_TIME, new Date(), metaObject);
        this.setFieldValByName(UPDATE_TIME, new Date(), metaObject);
        //创建人
        Long userId = getUserId();

        if(StringUtils.isEmpty((String) this.getFieldValByName(CREATE_BY, metaObject))){
            this.setFieldValByName(CREATE_BY, userId, metaObject);
        }

        if(StringUtils.isEmpty((String) this.getFieldValByName(UPDATE_BY, metaObject))){
            this.setFieldValByName(UPDATE_BY, userId, metaObject);
        }

    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新时间
        this.setFieldValByName(UPDATE_TIME, new Date(), metaObject);
        //更新人
        this.setFieldValByName(UPDATE_BY, getUserId(), metaObject);
    }


    private Long getUserId() {
        Long userId;
        try {
            userId = SecurityUtils.getUserId();
        } catch (RuntimeException e){
            userId = 1L;
        }
        return userId;
    }
}
