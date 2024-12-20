package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.hmdp.utils.MailUtils;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //校验号码
        if(RegexUtils.isEmailInvalid(phone)) {
            //不符合返回错误信息
            return Result.fail("邮箱格式错误");
        }
        //符合的话生成验证码
        String code = MailUtils.achieveCode();
        //保存到session（改成redis）
        String redisKey = LOGIN_CODE_KEY + phone;
        redisTemplate.opsForValue().set(redisKey, code, LOGIN_CODE_TTL, TimeUnit.MINUTES);
        // 调用邮件发送工具类发送验证码
        try {
            MailUtils.sendTestMail(phone, code);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("验证码发送失败，请稍后再试！");
        }
        log.debug("验证码发送成功，邮箱: {}, code: {}", phone, code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        // 校验邮箱
        String phone = loginForm.getPhone();
        if(RegexUtils.isEmailInvalid(phone)) {
            //不符合返回错误信息
            return Result.fail("邮箱格式错误");
        }
        // 验证码
        String redisKey = LOGIN_CODE_KEY + phone;
        String cacheCode = redisTemplate.opsForValue().get(redisKey);
        String code = loginForm.getCode();

        if(cacheCode == null || !cacheCode.equals(code)){
            //不一致就报错
            return Result.fail("验证码错误");
        }
        //若一致则根据信息查询
        User user = query().eq("phone",phone).one();
        if(user==null){
            //如果不存在创建新用户
            user = createUserWithPhone(phone);
        }
        //存到redis
        //随机生成token，作为登录令牌
        String token = UUID.randomUUID().toString(true);
        //将User对象转为Hash存储
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO, new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->fieldValue.toString()));
        //存储(用hash来存储而不是string)
        String tokenKey = LOGIN_USER_KEY + token;
        redisTemplate.opsForHash().putAll(tokenKey, userMap);
        //设置有效期
        redisTemplate.expire(tokenKey,LOGIN_USER_TTL, TimeUnit.MINUTES);
        //返回token
        return Result.ok(token);
    }

    private User createUserWithPhone(String phone) {
        //创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //保存用户
        save(user);
        return user;
    }
}
