package com.aeert.bean;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;

/**
 * @Author l'amour solitaire
 * @Description 存储待处理类的对象及待过滤字段信息
 * @Date 2020/8/11 下午8:53
 **/
public class JsonFilterObject {

    private Object object;

    private Map<Class, HashSet<String>> includes = new HashMap<>();

    private Map<Class, HashSet<String>> excludes = new HashMap<>();

    public JsonFilterObject(Object object, Map<Class, HashSet<String>> includes, Map<Class, HashSet<String>> excludes) {
        this.object = object;
        this.includes = includes;
        this.excludes = excludes;
    }

    public JsonFilterObject() {
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Map<Class, HashSet<String>> getIncludes() {
        return includes;
    }

    public void setIncludes(Map<Class, HashSet<String>> includes) {
        this.includes = includes;
    }

    public Map<Class, HashSet<String>> getExcludes() {
        return excludes;
    }

    public void setExcludes(Map<Class, HashSet<String>> excludes) {
        this.excludes = excludes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JsonFilterObject that = (JsonFilterObject) o;
        return Objects.equals(object, that.object) &&
                Objects.equals(includes, that.includes) &&
                Objects.equals(excludes, that.excludes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object, includes, excludes);
    }
}
