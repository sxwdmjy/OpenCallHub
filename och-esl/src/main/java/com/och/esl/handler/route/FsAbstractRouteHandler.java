package com.och.esl.handler.route;

import cn.hutool.core.util.RandomUtil;
import com.och.common.config.redis.RedisService;
import com.och.common.constant.CacheConstants;
import com.och.common.domain.CallInfo;
import com.och.esl.client.FsClient;
import com.och.esl.service.IFsCallCacheService;
import com.och.system.domain.vo.display.CallDisplayPoolVo;
import com.och.system.domain.vo.display.CallDisplaySimpleVo;
import com.och.system.service.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author danmo
 * @date 2023-11-10 17:19
 **/
@Slf4j
@Component
public abstract class FsAbstractRouteHandler {

    @Lazy
    @Autowired
    protected FsClient fsClient;


    @Autowired
    protected RedisService redisService;

    @Lazy
    @Autowired
    protected IFsCallCacheService fsCallCacheService;

    @Resource
    protected ISipAgentService iSipAgentService;

    @Autowired
    protected ICallSkillService iCallSkillService;

    @Autowired
    protected IFsSipGatewayService iFsSipGatewayService;

    @Autowired
    protected IVoiceFileService iVoiceFileService;

    @Autowired
    private  ICallDisplayPoolService iCallDisplayPoolService;

    public abstract void handler(String address, CallInfo callInfo, String uniqueId,  String routeValue);

    protected void saveCallInfo(CallInfo callInfo){
        fsCallCacheService.saveCallInfo(callInfo);
    }


    /**
     * 获取号码池号码
     */
    protected String getNumberPoolNumber(Long poolId) {
        CallDisplayPoolVo poolDetail = iCallDisplayPoolService.getPoolDetail(poolId);
        if (Objects.isNull(poolDetail)) {
            return "";
        }
        Integer type = poolDetail.getType();
        switch (type){
            //随机
            case 1 -> {
                return RandomUtil.randomEle(poolDetail.getPhoneList()).getDisplayNumber();
            }
            //轮询
            case 2 -> {
                Integer pollingNum = redisService.keyIsExists(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId) ? redisService.getCacheObject(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId) : 0;
                Integer nextPollingNum = findNextPollingNum(poolDetail.getPhoneList(), pollingNum);
                redisService.setCacheObject(CacheConstants.CALL_DISPLAY_POLLING_KEY + poolId, nextPollingNum);
                return poolDetail.getPhoneList().get(nextPollingNum).getDisplayNumber();
            }
        }
        return "";
    }

    private Integer findNextPollingNum(List<CallDisplaySimpleVo> phoneList, Integer pollingNum) {
        if (Objects.isNull(phoneList) || phoneList.isEmpty()) {
            return 0;
        }
        if (pollingNum >= phoneList.size()) {
            return 0;
        }
        return pollingNum + 1;
    }
}
