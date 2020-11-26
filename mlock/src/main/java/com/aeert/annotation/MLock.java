package com.aeert.annotation;

import java.lang.annotation.*;

/**
 * @Author l'amour solitaire
 * @Description 用于标记redis锁
 * @Date 2020/11/17 下午3:14
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MLock {

    /**
     * redis锁的key值
     * Example: @RedisLock(key = "targetClass + methodName + ':' + #p0")
     */
    String key() default "";

    /**
     * redis锁的registry key值
     */
    String registryKey() default "redis-lock";

    /**
     * redis锁文案
     */
    String message() default "操作正在进行中～";

    /**
     * redis锁的有效期(单位:毫秒)
     **/
    long expires() default 60000L;

}
