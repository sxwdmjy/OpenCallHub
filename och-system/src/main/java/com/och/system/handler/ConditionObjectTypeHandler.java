package com.och.system.handler;


import com.alibaba.fastjson2.JSONObject;
import com.och.common.domain.ConditionInfo;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author danmo
 * @date 2025/06/16 17:44
 */
public class ConditionObjectTypeHandler extends BaseTypeHandler<ConditionInfo> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    ConditionInfo parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONObject.toJSONString(parameter));
    }

    @Override
    public ConditionInfo getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return json == null ? null : JSONObject.parseObject(json, ConditionInfo.class);
    }

    @Override
    public ConditionInfo getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return json == null ? null : JSONObject.parseObject(json, ConditionInfo.class);
    }

    @Override
    public ConditionInfo getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return json == null ? null : JSONObject.parseObject(json, ConditionInfo.class);
    }
}
