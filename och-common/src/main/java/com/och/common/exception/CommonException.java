package com.och.common.exception;

import com.och.common.enums.ExceptionStatusEnum;
import lombok.Data;

/**
 * 通用异常
 * @author danmo
 * @date 2024-07-10 10:33
 **/
@Data
public class CommonException extends RuntimeException{

    private static final long serialVersionUID = 1L;

    protected String message;

    private Integer code = -1;

    public CommonException(String message)
    {
        this.message = message;
    }

    public CommonException(Integer code, String message)
    {
        this.code=code;
        this.message = message;
    }

    public CommonException(ExceptionStatusEnum statusEnum){
        this.code = statusEnum.getCode();
        this.message = statusEnum.getMsg();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
