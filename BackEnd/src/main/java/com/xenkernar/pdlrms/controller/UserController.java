package com.xenkernar.pdlrms.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.xenkernar.pdlrms.entity.ExtUser;
import com.xenkernar.pdlrms.entity.SysUser;
import com.xenkernar.pdlrms.handler.CustomException;
import com.xenkernar.pdlrms.service.UserService;
import com.xenkernar.pdlrms.utils.Result;

import java.security.Principal;
import java.util.List;

@RestController
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询用户")
    @GetMapping("/admin/user")
    @Secured("ROLE_ADMIN")
    public Result getUsersByPage(
            @RequestParam(value = "sections",required = false) List<String> sections,
            @RequestParam("page") int page,
            @RequestParam("size") int size
            ) {
        sections = sections == null ? List.of() : sections;
        Page<SysUser> userPages = userService.getBySections(sections, page, size);
        return Result.ok().data("users",userPages.getRecords()).data("total",userPages.getTotal());
    }

    @Operation(summary = "根据id获取用户")
    @GetMapping("/admin/user/{id}")
    @Secured("ROLE_ADMIN")
    public Result getUserById(@PathVariable("id") String id) {
        return Result.ok().data("user", userService.getUserById(id));
    }

    @Operation(summary = "用户获取自身信息")
    @GetMapping("/user/{id}")
    @Secured("ROLE_USER")
    public Result getUserInfo(@PathVariable("id") String id, Principal principal)   {
        if (!principal.getName().equals(id)) {
            throw new CustomException(HttpStatus.FORBIDDEN.value(),"无权限");
        }
        return Result.ok().data("user", userService.getUserById(id));
    }

    @Operation(summary = "注册用户")
    @PostMapping("/user")
    public Result postUser(@RequestBody ExtUser extUser)   {
        userService.postUser(extUser);
        return Result.ok();
    }

    @Operation(summary = "管理员添加用户")
    @PostMapping("/admin/user")
    @Secured("ROLE_ADMIN")
    public Result postUser(@RequestBody SysUser user)   {
        userService.postUser(user);
        return Result.ok();
    }

    @Operation(summary = "修改用户名")
    @PostMapping("/username")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Result changeUsername(@RequestBody String username, Principal principal)   {
        userService.updateUsername(principal.getName(), username);
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_ADMIN')")
    public Result changePassword(@RequestPart ExtUser extUser, Principal principal)   {
        if (!principal.getName().equals(extUser.getId())) {
            throw new CustomException(HttpStatus.FORBIDDEN.value(),"无权限");
        }
        SysUser user = userService.getUserById(extUser.getId());
        if (!passwordEncoder.matches(extUser.getOriginPassword(), user.getPassword())){
            throw new CustomException(HttpStatus.BAD_REQUEST.value(), "原密码错误");
        }
        user.setPassword(extUser.getPassword());
        userService.putUser(user);
        return Result.ok();
    }

    @Operation(summary = "管理员更新用户用户名")
    @PostMapping("/admin/user/username")
    @Secured("ROLE_ADMIN")
    public Result updateUsername(
            @RequestParam String id,
            @RequestParam String username
    ) {
        userService.updateUsername(id,username);
        return Result.ok();
    }

    @Operation(summary = "管理员更新用户密码")
    @PostMapping("/admin/user/password")
    @Secured("ROLE_ADMIN")
    public Result updatePassword(
            @RequestParam String id,
            @RequestParam String password
    ) {
        userService.updatePassword(id,password);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/admin/user")
    @Secured("ROLE_ADMIN")
    public Result deleteUser(@RequestParam("id") String id)   {
        if ("ROLE_ADMIN".equals(userService.getUserById(id).getUserRole())) {
            throw new CustomException(HttpStatus.FORBIDDEN.value(),"无权限");
        }
        userService.deleteUser(id);
        return Result.ok();
    }

}
