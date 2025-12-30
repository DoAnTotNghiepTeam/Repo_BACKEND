package com.example.WorkWite_Repo_BE.dtos.UserDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {


    @NotBlank (message = "username không được để trống")
    @Size (min = 6, max = 160, message = "username phải từ 6 đến 160 ký tự")
    private String username;

    @NotBlank (message = "Mật khẩu không được để trống")
    @Size (min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
}
