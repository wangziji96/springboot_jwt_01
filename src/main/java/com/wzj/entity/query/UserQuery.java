package com.wzj.entity.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserQuery {
    private Integer pageNum = 1; //当前页码
    private Integer pageSize = 2; //每一页显示条数
    private String name; //根据用户查询
}
