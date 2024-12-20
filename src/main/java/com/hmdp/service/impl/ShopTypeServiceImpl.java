package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    private static final String REDIS_KEY = "shop:type:list"; // Redis 缓存键
    private static final long CACHE_TTL = 10; // 缓存过期时间，单位：分钟

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public Result getShopListWithCache() {
        //从redis中获取缓存
        String shopJson = stringRedisTemplate.opsForValue().get(REDIS_KEY);
        //如果命中则反序列化成对象
        if(shopJson !=null){
            List<ShopType> shopTypes = JSONUtil.toList(shopJson, ShopType.class);
            return Result.ok(shopTypes);
        }
        //没有命中则查询数据库
        List<ShopType> shopTypes = list();
        //如果数据库没有的话返回错误
        if(shopTypes == null || shopTypes.isEmpty()){
            return Result.fail("商铺不存在");
        }
        //有的话写入reids
        stringRedisTemplate.opsForValue().set(
                REDIS_KEY,
                JSONUtil.toJsonStr(shopTypes),
                CACHE_TTL,
                TimeUnit.MINUTES
                );
        //返回
        return Result.ok(shopTypes);
    }
}
