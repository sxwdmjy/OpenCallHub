package com.och.system.domain.query.captcha;


import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
public class CheckCaptchaQuery {

    @Schema(description = "验证码id")
    private String id;

    @Schema(description = "验证轨迹")
    private ImageCaptchaTrack captchaTrack;
}
