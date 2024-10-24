package com.och.api.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.TACBuilder;
import cloud.tianai.captcha.application.vo.CaptchaResponse;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.validator.common.model.dto.MatchParam;
import cn.hutool.core.img.FontUtil;
import com.och.api.config.CaptchaRedisCacheStore;
import com.och.common.base.BaseController;
import com.och.common.base.ResResult;
import com.och.common.utils.SpringUtils;
import com.och.common.utils.StringUtils;
import com.och.system.domain.query.captcha.CheckCaptchaQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author danmo
 * @date 2024-02-23 16:38
 **/
@Tag(name = "验证码")
@RestController
@RequestMapping("/captcha/v1")
public class CaptchaController extends BaseController implements InitializingBean {

    private ImageCaptchaApplication application;

    /**
     * 生成验证码
     *
     * @param type
     * @return
     */
    @Operation(summary = "生成验证码", method = "GET")
    @GetMapping("/gen")
    public ResResult<CaptchaResponse<ImageCaptchaVO>> genCaptcha(@RequestParam(value = "type", required = false) String type) {
        if (StringUtils.isBlank(type)) {
            type = CaptchaTypeConstant.SLIDER;
        }
        if ("RANDOM".equals(type)) {
            int i = ThreadLocalRandom.current().nextInt(0, 4);
            if (i == 0) {
                type = CaptchaTypeConstant.SLIDER;
            } else if (i == 1) {
                type = CaptchaTypeConstant.CONCAT;
            } else if (i == 2) {
                type = CaptchaTypeConstant.ROTATE;
            } else {
                type = CaptchaTypeConstant.WORD_IMAGE_CLICK;
            }
        }

        CaptchaResponse<ImageCaptchaVO> res = application.generateCaptcha(type);
        return success(res);
    }

    @Operation(summary = "生成验证码", method = "POST")
    @PostMapping("/check")
    public ApiResponse genCaptcha(@RequestBody CheckCaptchaQuery query) {
        return application.matching(query.getId(), new MatchParam(query.getCaptchaTrack()));
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        StringRedisTemplate redisTemplate = SpringUtils.getBean(StringRedisTemplate.class);
        application = TACBuilder.builder()
                .setCacheStore(new CaptchaRedisCacheStore(redisTemplate))
                .addDefaultTemplate() // 添加默认模板
                // 给滑块验证码 添加背景图片，宽高为600*360, Resource 参数1为 classpath/file/url , 参数2 为具体url
                .addResource("SLIDER", new Resource("classpath", "images/1.png")) // 滑块验证的背景图
                .addResource("SLIDER", new Resource("classpath", "images/2.png")) // 滑块验证的背景图
                .addResource("SLIDER", new Resource("classpath", "images/3.png")) // 滑块验证的背景图
                .addResource("SLIDER", new Resource("classpath", "images/4.png")) // 滑块验证的背景图
                .addResource("SLIDER", new Resource("classpath", "images/6.png")) // 滑块验证的背景图
                .addResource("SLIDER", new Resource("classpath", "images/6.png")) // 滑块验证的背景图
                .addResource("WORD_IMAGE_CLICK", new Resource("classpath", "images/1.png"))
                .addResource("WORD_IMAGE_CLICK", new Resource("classpath", "images/2.png"))
                .addResource("WORD_IMAGE_CLICK", new Resource("classpath", "images/3.png"))
                .addResource("WORD_IMAGE_CLICK", new Resource("classpath", "images/4.png"))
                .addResource("WORD_IMAGE_CLICK", new Resource("classpath", "images/6.png"))
                .addFont(new Font(Font.DIALOG, Font.PLAIN, 12))
                .build();
    }
}
