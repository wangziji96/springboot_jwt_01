package com.wzj.dao;

import com.wzj.entity.User;
import com.wzj.entity.query.UserQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserDao {

    public User selectByNameAndPassword(User user);

    //查询所有用户
    public List<User> listUser();

    //根据用户名来查询用户 并分页展示
    public List<User> listUserByName(UserQuery userQuery);

}
