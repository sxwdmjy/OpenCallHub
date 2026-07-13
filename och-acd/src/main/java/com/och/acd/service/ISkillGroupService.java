package com.och.acd.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.och.acd.domain.SkillGroup;

/**
 * 技能组服务接口
 * <p>封装技能组的 CRUD 操作，Controller 通过本接口访问数据。</p>
 *
 * @author danmo
 */
public interface ISkillGroupService {

    /** 分页查询技能组 */
    IPage<SkillGroup> pageList(int pageNum, int pageSize);

    /** 创建技能组 */
    SkillGroup create(SkillGroup group);

    /** 更新技能组 */
    SkillGroup update(Long id, SkillGroup group);

    /** 删除技能组 */
    void delete(Long id);
}