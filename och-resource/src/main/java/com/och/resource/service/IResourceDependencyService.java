package com.och.resource.service;
import com.och.resource.domain.ResourceDependency;
import com.och.resource.domain.ResourceType;
import java.util.List;
public interface IResourceDependencyService {
    void addDependency(ResourceType sourceType, Long sourceId, ResourceType targetType, Long targetId, String relation);
    void removeDependencies(ResourceType sourceType, Long sourceId);
    void checkDeletable(ResourceType type, Long id);
    List<ResourceDependency> getDependents(ResourceType type, Long id);
    List<ResourceDependency> getDependencies(ResourceType type, Long id);
}
