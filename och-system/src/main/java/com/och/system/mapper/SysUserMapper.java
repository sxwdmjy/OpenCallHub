package com.och.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.och.system.domain.entity.SysUser;
import com.och.system.domain.query.user.SysUserQuery;
import com.och.system.domain.vo.user.SysSimpleUserVo;
import com.och.system.domain.vo.user.SysUserVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户信息表(sysUser)表数据库访问层
 *
 * @author danmo
 * @since 2024-02-20 18:41:33
 */
@Repository()
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    SysUserVo getByUserId(@Param("userId") Long userId);

    List<SysUserVo> getList(SysUserQuery query);

    List<Long> selectUserIdsByQuery(SysUserQuery query);

    List<SysUserVo> getPageList(SysUserQuery query);

    List<SysSimpleUserVo> getSelectList(SysUserQuery query);
}

