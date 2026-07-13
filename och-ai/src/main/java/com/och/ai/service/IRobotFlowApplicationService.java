package com.och.ai.service;
import com.och.ai.domain.RobotFlow;
import com.och.ai.domain.RobotFlowVersion;
import java.util.List;
public interface IRobotFlowApplicationService {
    List<RobotFlow> list();
    RobotFlow create(RobotFlow flow);
    RobotFlowVersion publish(Long flowId);
    void delete(Long id);
}
