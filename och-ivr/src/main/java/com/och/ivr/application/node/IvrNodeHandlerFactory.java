package com.och.ivr.application.node;

import com.och.ivr.domain.IvrNodeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * IVR节点处理器工厂（工厂模式）
 * <p>
 * 根据节点类型返回对应的处理器实现，将不同节点类型的路由逻辑分派给专门的处理器。
 * 新增节点类型只需添加新的处理器实现并在此注册，无需修改引擎主流程。
 * </p>
 *
 * <h3>设计模式说明</h3>
 * <ul>
 *   <li><b>工厂模式</b>：根据 IvrNodeType 返回对应的 IvrNodeHandler 实现</li>
 *   <li><b>策略模式</b>：每种节点类型封装独立的路由策略，可插拔替换</li>
 *   <li><b>默认回退</b>：未注册的节点类型使用 DefaultNodeHandler 处理</li>
 * </ul>
 *
 * @author danmo
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IvrNodeHandlerFactory {

    private final MenuNodeHandler menuHandler;
    private final ScheduleCheckNodeHandler scheduleCheckHandler;
    private final ConditionNodeHandler conditionHandler;
    private final PlayNodeHandler playHandler;
    private final QueueNodeHandler queueHandler;
    private final TransferNodeHandler transferHandler;
    private final HangupNodeHandler hangupHandler;
    private final VoicemailNodeHandler voicemailHandler;
    private final DefaultNodeHandler defaultHandler;

    /**
     * 根据节点类型获取对应的处理器
     *
     * @param nodeType IVR节点类型枚举
     * @return 对应的节点处理器，未知类型返回 DefaultNodeHandler
     */
    public IvrNodeHandler getHandler(IvrNodeType nodeType) {
        if (nodeType == null) {
            return defaultHandler;
        }

        return switch (nodeType) {
            case MENU                -> menuHandler;
            case SCHEDULE_CHECK      -> scheduleCheckHandler;
            case CONDITION           -> conditionHandler;
            case PLAY, PLAYBACK      -> playHandler;
            case TRANSFER_VOICEMAIL   -> voicemailHandler;
            case QUEUE, TRANSFER_SKILL_GROUP -> queueHandler;
            case TRANSFER            -> transferHandler;
            case HANGUP              -> hangupHandler;
            // START、COLLECT_DIGITS、API_CALL、TRANSFER_AGENT
            default                  -> defaultHandler;
        };
    }
}