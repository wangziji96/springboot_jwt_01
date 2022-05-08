package com.wzj.interceptors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wzj.utils.JWTUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class JWTInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String,Object> map = new HashMap<>();
        //获取请求头的令牌
        String token = request.getHeader("token");
        try {
            JWTUtils.verify(token);//获取令牌
            return true;//放行请求
        }catch (Exception e){
            e.printStackTrace();
            map.put("msg","token不合法");
        }
        map.put("state",false);//设置状态
        //将map 转为json Jackson
        String json = new ObjectMapper().writeValueAsString(map);
        response.setContentType("application/json;charset=UTF-8");
        //将验证不通过的信息返回给前端
        response.getWriter().println(json);
        return false;
    }
}
