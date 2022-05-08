package com.wzj.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Map;

public class JWTUtils {

    //加密和验签用到的签名,这个签名不要给别人
    private static final String SING = "wzj123";

    /*生成token header.platload.sing*/
    public static String getToken(Map<String,String> map) throws UnsupportedEncodingException {

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE,7);//7天过期

        //创建JWT builder
        JWTCreator.Builder builder = JWT.create();

        /*playload，map存放用户信息，
        header不设置的话会用默认的，这里不设置header*/
        map.forEach((k,v) ->{
            builder.withClaim(k,v);
        });

        String token = builder.withExpiresAt(instance.getTime())//令牌过期时间
                .sign(Algorithm.HMAC256(SING));//sin

        return token;
    }

    //验证token合法性
    public static DecodedJWT verify(String token) throws UnsupportedEncodingException {
        DecodedJWT verify = JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
        //如果能返回DecodedJWT对象，说明验签成功，而且获取到这个对象还可以获取token内容
        return verify;
    }

}
