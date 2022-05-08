package com.wzj.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.wzj.entity.User;
import com.wzj.entity.query.TestJWT;
import com.wzj.service.impl.UserServiceImpl;
import com.wzj.utils.JWTUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    @RequestMapping("/login")
    public Map<String,Object> login(@RequestBody User user){
        log.info("用户名：[{}]",user.getName());
        log.info("密码：[{}]",user.getPassword());
        Map<String,Object> map = new HashMap<>();
        try{
            User userDB = userService.login(user);
            Map<String,String> playload = new HashMap<>();
            playload.put("id",userDB.getId().toString());
            playload.put("name",userDB.getName());

            //生成jwt的令牌
            String token = JWTUtils.getToken(playload);
            if (user != null){
                map.put("state",true);
                map.put("msg","认证成功");
                map.put("token",token);
            }
        }catch (Exception e){
            map.put("state",false);
            map.put("msg",e.getMessage());
        }
        return map;
    }

    @GetMapping
    public Map<String,Object> test(){
        Map<String,Object> map = new HashMap<>();
        map.put("state",true);
        map.put("msg","jwt拦截器设置成功");
        return map;
    }

    @PostMapping
    public Map<String,Object> test2(){
        Map<String,Object> map = new HashMap<>();
        map.put("state",true);
        map.put("msg","再次测试拦截器里的JWT验证是否成功");
        return map;
    }



}
