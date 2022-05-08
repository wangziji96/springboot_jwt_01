package com.wzj.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.wzj.dao.UserDao;
import com.wzj.entity.User;
import com.wzj.entity.query.UserQuery;
import com.wzj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User login(User user) {
        //从数据库查询的用户信息
        User userDB = userDao.selectByNameAndPassword(user);
        if (user != null){
            return userDB;
        }
        throw new RuntimeException("登录失败...");
    }

    @Override
    public List<User> listUser() {
        return userDao.listUser();
    }

    @Override
    public PageInfo<User> listUserByName(UserQuery userQuery) {
        PageHelper.startPage(userQuery.getPageNum(),userQuery.getPageSize());
        return new PageInfo<User>(userDao.listUserByName(userQuery));
    }
}
