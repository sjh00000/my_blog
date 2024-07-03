package com.markerhub.common.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class LoginVo implements Serializable {

    @NotBlank(message = "昵称不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}