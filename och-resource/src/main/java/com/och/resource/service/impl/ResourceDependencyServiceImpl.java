package com.och.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.och.common.error.BizException;
import com.och.common.error.ErrorCode;
import com.och.resource.domain.ResourceDependency;
import com.och.resource.domain.ResourceType;
import com.och.resource.mapper.ResourceDependencyMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 资源依赖关系服务，负责管理资源间的依赖关系。
 * 提供依赖创建、删除、查询以及删除安全检查功能。
 *
 * @author danmo
 * @date 2025-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResourceDependencyServiceImpl extends com.baomidou.mybatisplus.extension.service.impl.ServiceImpl<com.och.resource.mapper.ResourceDependencyMapper, com.och.resource.domain.ResourceDependency> implements com.och.resource.service.IResourceDependencyService {


    /**
     * 创建依赖关系
     */
    @Transactional
    public void addDependency(ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId, String relation) {
        ResourceDependency dep = ResourceDependency.builder()
                .sourceType(sourceType.name())
                .sourceId(sourceId)
                .targetType(targetType.name())
                .targetId(targetId)
                .relation(relation != null ? relation : "DEPENDS_ON")
                .build();
        baseMapper.insert(dep);
    }

    /**
     * 删除某资源的出站依赖
     */
    @Transactional
    public void removeDependencies(ResourceType sourceType, Long sourceId) {
        LambdaQueryWrapper<ResourceDependency> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ResourceDependency::getSourceType, sourceType.name())
                .eq(ResourceDependency::getSourceId, sourceId);
        baseMapper.delete(wrapper);
    }

    /**
     * 检查资源是否可以安全删除。
     * <p>删除资源前必须先确认没有其他资源引用它，否则会造成号码池、分机、AI 插件等业务配置悬空。</p>
     *
     * @throws BizException 如果存在依赖则抛出异常
     */
    public void checkDeletable(ResourceType type, Long id) {
        List<ResourceDependency> dependents = getDependents(type, id);
        if (!dependents.isEmpty()) {
            String message = "资源【" + type.getLabel() + "(" + id + ")】正被以下对象引用，无法删除: "
                    + formatBlockingDependents(dependents);
            throw new BizException(ErrorCode.BIZ_OPERATION_DENIED, message, dependents);
        }
    }

    /**
     * 获取资源的依赖列表（谁依赖它）
     */
    public List<ResourceDependency> getDependents(ResourceType type, Long id) {
        return baseMapper.findByTarget(type.name(), id);
    }

    /**
     * 获取资源依赖的其他资源
     */
    public List<ResourceDependency> getDependencies(ResourceType type, Long id) {
        return baseMapper.findBySource(type.name(), id);
    }

    /**
     * 批量注册依赖关系
     */
    @Transactional
    public void registerDependencies(ResourceType sourceType, Long sourceId, List<DependencyRef> targets) {
        if (targets == null || targets.isEmpty()) {
            return;
        }
        for (DependencyRef ref : targets) {
            addDependency(sourceType, sourceId, ref.type, ref.id, ref.relation);
        }
    }

    private String formatBlockingDependents(List<ResourceDependency> dependents) {
        return dependents.stream()
                .map(d -> formatType(d.getSourceType()) + "(" + d.getSourceId() + ")" + formatRelation(d.getRelation()))
                .collect(Collectors.joining(", "));
    }

    private String formatType(String type) {
        try {
            return ResourceType.valueOf(type).getLabel();
        } catch (Exception e) {
            return type;
        }
    }

    private String formatRelation(String relation) {
        return relation == null || relation.isBlank() ? "" : "[" + relation + "]";
    }

    public record DependencyRef(ResourceType type, Long id, String relation) {
        public DependencyRef(ResourceType type, Long id) {
            this(type, id, "DEPENDS_ON");
        }
    }
}
