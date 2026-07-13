package com.och.system.domain.feature;

/**
 * @author danmo
 * @date 2023-11-01 16:13
 **/
public interface IUserVo {

    Long getCreateBy();
    Long getUpdateBy();

    void setCreateName(String createName);
    void setUpdateName(String updateName);
}
