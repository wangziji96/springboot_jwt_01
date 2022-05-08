package com.wzj.service;

import com.github.pagehelper.PageInfo;
import com.wzj.entity.User;
import com.wzj.entity.query.UserQuery;

import java.util.List;

public interface UserService {

    User login(User user);//登录接口

    //查询所有用户
    public List<User> listUser();

    //根据用户名来查询用户 并分页展示
    public PageInfo<User> listUserByName(UserQuery userQuery);

}
