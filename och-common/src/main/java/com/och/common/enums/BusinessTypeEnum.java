package com.och.common.enums;

/**
 * 操作业务类型枚举
 * <p>用于 {@link com.och.common.annotation.Log} 注解的 businessType 属性，
 * 标识当前操作属于哪种业务类型（新增、修改、删除、查询、登录、登出等）。</p>
 *
 * @author danmo
 */
public enum BusinessTypeEnum {

    /** 新增 */
    INSERT,

    /** 修改 */
    UPDATE,

    /** 删除 */
    DELETE,

    /** 查询 */
    SELECT,

    /** 登录 */
    LOGIN,

    /** 登出 */
    LOGOUT,

    /** 导入 */
    IMPORT,

    /** 导出 */
    EXPORT,

    /** 其它 */
    OTHER
}