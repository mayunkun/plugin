package com.aeert.aop;

import com.aeert.annotation.MLock;
import com.aeert.exception.MLockException;
import io.vavr.Function3;
import io.vavr.control.Try;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @Author l'amour solitaire
 * @Description redis分布式锁的切面
 * @Date 2020/11/17 上午10:47
 **/
@Aspect
@Component
public class RedisLockAspect {
    Logger log = LoggerFactory.getLogger(RedisLockAspect.class);

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Around(value = "@annotation(mLock)")
    public Object redisLock(ProceedingJoinPoint joinPoint, MLock mLock) {
        RedisLockRegistry redisLockRegistry = new RedisLockRegistry(redisConnectionFactory, mLock.registryKey(), mLock.expires());
        return Try.of(() -> {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            signature.getClass();
            Method method = signature.getMethod();
            Object[] arguments = joinPoint.getArgs();

            String key = keyFormatter.apply(mLock.key(), method, arguments);
            java.util.concurrent.locks.Lock lock = redisLockRegistry.obtain(key);
            return Try.of(() -> {
                boolean ifLock = lock.tryLock();
                if (ifLock) {
                    return joinPoint.proceed();
                } else {
                    throw new MLockException(mLock.message());
                }
            }).onFailure((e) -> {
                log.error(e.getMessage());
            }).andFinally(() -> {
                try {
                    lock.unlock();
                } catch (Exception e) {
                    log.error("解锁出错:{}", e.getMessage());
                }
            }).get();
        }).onFailure((e) -> log.error("Redis lock exception :{}", e.getLocalizedMessage())).get();
    }

    /**
     * key格式化
     **/
    Function3<String, Method, Object[], String> keyFormatter = (key, method, arguments) -> {
        if (StringUtils.hasText(key)) {
            key = key.replace("targetClass", method.getDeclaringClass().getName() + ".");
            key = key.replace("methodName", method.getName() + ".");
            for (int i = 0; i < arguments.length; i++) {
                key = key.replace("#p" + i, arguments[i].toString() + ",");
            }
            key = key.replace(" + ':' + ", ":");
            key = key.replace(". + ", ".");
            key = key.replace(", + ", ",");
            if (key.endsWith(",") || key.endsWith(":") || key.endsWith(".")) {
                key = key.substring(0, key.length() - 1);
            }
        } else {
            key = key = method.getDeclaringClass().getName() + "." + method.getName() + ":";
            for (int i = 0; i < arguments.length; i++) {
                key += arguments[i].toString() + ",";
            }
            if (key.endsWith(",") || key.endsWith(":") || key.endsWith(".")) {
                key = key.substring(0, key.length() - 1);
            }
        }
        return key;
    };

}

