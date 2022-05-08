# 1.jwt入门

用JWT必须先导入JWT的依赖

```java
//用jwt生成token
    @Test
    void contextLoads() throws UnsupportedEncodingException {

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND,100);

        //如果你想定义header的内容，可以创建一个map
        HashMap<String,Object> map = new HashMap<>();
        String token = JWT.create().withHeader(map)//header 这个header也可以不写
                .withClaim("userId", 21) //payload，负载，存放用户信息之类的内容，可以有多个
                .withClaim("username", "wzj")
                .withExpiresAt(instance.getTime())//指定令牌过期时间
                .sign(Algorithm.HMAC256("!qwer"));//签名，括号里的内容是自己定义的

        System.out.println("JWT生成的token："+token);

    }


//验证token
    @Test
    public void test() throws UnsupportedEncodingException {
        //验签，创建验证对象，里面的算法和签名要和我们生成token时的算法和签名一致
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256("!qwer")).build();
        //这个token是上面的方法生成的
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2NDQ1MDQxMjIsInVzZXJJZCI6MjEsInVzZXJuYW1lIjoid3pqIn0.dRbSyQrD-mj1XuxXYb_LVYmmzejkYZ9XTaDGKnS11ng";

        DecodedJWT verify = jwtVerifier.verify(token);

        //你是以什么类型存的，就以什么类型取。userid是以int类型存的，所以用int类型取
        System.out.println(verify.getClaim("userId").asInt());
        System.out.println(verify.getClaim("username").asString());

    }
```

# 2.JWT工具类封装

```java
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

/*    //获取token信息方法
    public static DecodedJWT getTokenInfo(String token) throws UnsupportedEncodingException {
        DecodedJWT verify = JWT.require(Algorithm.HMAC256(SING)).build().verify(token);
        return verify;
    }*/

}
```

# 3.SpringBoot整合JWT

用JWT的话，需要和前端程序员做好沟通。如：除了登录，其他时候访问接口的时候需要在**header里面带上token**。

登录界面：

![image-20220212211312086](picture/image-20220212211312086.png)

每次访问其他接口：

![image-20220212211355111](picture/image-20220212211355111.png)

实现上面做法的具体步骤如下(**JWT的工具类上面已经实现**)：

1. dao层

   ```java
   @Mapper
   public interface UserDao {
   
       public User selectByNameAndPassword(User user);
   
       //查询所有用户
       public List<User> listUser();
   
       //根据用户名来查询用户 并分页展示
       public List<User> listUserByName(UserQuery userQuery);
   
   }

2. service层

   ```java
   public interface UserService {
   
       User login(User user);//登录接口
   
       //查询所有用户
       public List<User> listUser();
   
       //根据用户名来查询用户 并分页展示
       public PageInfo<User> listUserByName(UserQuery userQuery);
   
   }
   
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
   
   }
   ```

3. controller层

   ```java
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
   ```

4. 拦截器（**避免代码冗余，让验证过程放在拦截器里面**）

   ```java
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
   ```

5. 配置拦截器(**你不能什么都拦截，比如登录的时候，因为这时候还没有token生成**)

   ```java
   package com.wzj.config;
   
   import com.wzj.interceptors.JWTInterceptor;
   import org.springframework.context.annotation.Configuration;
   import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
   import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
   @Configuration
   public class InterceptorConfig implements WebMvcConfigurer {
   
       @Override
       public void addInterceptors(InterceptorRegistry registry) {
           registry.addInterceptor(new JWTInterceptor())
                   .addPathPatterns("/**")//什么都拦截
                   .excludePathPatterns("/users/login");///users/login这是登录路径，不拦截
       }
   }
   ```

   
