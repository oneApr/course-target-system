package com.fmk.cource.controller;

import com.fmk.cource.common.Result;
import com.fmk.cource.service.SysUserService;
import com.fmk.cource.vo.LoginVO;
import com.fmk.cource.vo.RegisterVO;
import com.fmk.cource.vo.ResetPasswordVO;
import com.fmk.cource.vo.UserInfoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserInfoVO> login(@RequestBody LoginVO loginVO) {
        return Result.success(sysUserService.login(loginVO));
    }

    @Operation(summary = "用户注册（默认分配 teacher 角色，可指定 roleKey）")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody RegisterVO registerVO) {
        sysUserService.register(registerVO);
        return Result.success();
    }

    @Operation(summary = "忘记密码 - 根据用户名重置密码")
    @PutMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordVO vo) {
        sysUserService.resetPassword(vo);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息+菜单")
    @GetMapping("/info")
    public Result<UserInfoVO> info(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getDetails();
        return Result.success(sysUserService.getUserInfo(userId));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        sysUserService.logout(userDetails.getUsername());
        return Result.success();
    }
}
