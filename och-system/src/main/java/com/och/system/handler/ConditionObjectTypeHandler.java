package com.och.system.handler;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.och.common.domain.ConditionInfo;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author danmo
 * @date 2025/06/16 17:44
 */
public class ConditionObjectTypeHandler extends BaseTypeHandler<List<ConditionInfo>> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    List<ConditionInfo> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, JSONObject.toJSONString(parameter));
    }

    @Override
    public List<ConditionInfo> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String json = rs.getString(columnName);
        return json == null ? null : JSONArray.parseArray(json, ConditionInfo.class);
    }

    @Override
    public List<ConditionInfo> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String json = rs.getString(columnIndex);
        return json == null ? null : JSONArray.parseArray(json, ConditionInfo.class);
    }

    @Override
    public List<ConditionInfo> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String json = cs.getString(columnIndex);
        return json == null ? null : JSONArray.parseArray(json, ConditionInfo.class);
    }
}
