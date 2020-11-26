package com.aeert.advice;

import com.aeert.annotation.MoreSerializeField;
import com.aeert.annotation.MultiSerializeField;
import com.aeert.annotation.SerializeField;
import com.aeert.bean.JsonFilterObject;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @Author l'amour solitaire
 * @Description ResponseBodyAdvice是spring4.1的新特性，其作用是在响应体写出之前做一些处理；比如，修改返回值、加密等
 * @Date 2020/8/11 下午8:48
 **/
@ControllerAdvice
public class JsonFilterResponseBodyAdvice implements ResponseBodyAdvice {

    @Override
    public boolean supports(MethodParameter methodParameter, Class converterType) {
        return methodParameter.hasMethodAnnotation(MoreSerializeField.class) || methodParameter.hasMethodAnnotation(MultiSerializeField.class) || methodParameter.hasMethodAnnotation(SerializeField.class);
    }

    /**
     * @param object-             the body to be written
     * @param methodParameter-    the return type of the controller method
     * @param mediaType-          the content type selected through content negotiation
     * @param converterType-      the converter type selected to write to the response
     * @param serverHttpRequest-  the current request
     * @param serverHttpResponse- the current response
     * @return
     */
    @Override
    public Object beforeBodyWrite(Object object, MethodParameter methodParameter, MediaType mediaType, Class converterType, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        JsonFilterObject jsonFilterObject = new JsonFilterObject();
        if (null == object) {
            return null;
        }

        if (!methodParameter.getMethod().isAnnotationPresent(SerializeField.class) &&
                !methodParameter.getMethod().isAnnotationPresent(MultiSerializeField.class) &&
                !methodParameter.getMethod().isAnnotationPresent(MoreSerializeField.class)) {
            return object;
        }

        /**
         * 处理类进行过滤处理
         */
        if (methodParameter.getMethod().isAnnotationPresent(SerializeField.class)) {
            Object obj = methodParameter.getMethod().getAnnotation(SerializeField.class);
            handleAnnotation(SerializeField.class, obj, jsonFilterObject);
        }
        if (methodParameter.getMethod().isAnnotationPresent(MultiSerializeField.class)) {
            Object obj = methodParameter.getMethod().getAnnotation(MultiSerializeField.class);
            handleAnnotation(MultiSerializeField.class, obj, jsonFilterObject);
        }
        if (methodParameter.getMethod().isAnnotationPresent(MoreSerializeField.class)) {
            MoreSerializeField moreSerializeField = methodParameter.getMethod().getAnnotation(MoreSerializeField.class);
            SerializeField[] serializeFields = moreSerializeField.value();
            if (serializeFields.length > 0) {
                for (int i = 0; i < serializeFields.length; i++) {
                    handleAnnotation(SerializeField.class, serializeFields[i], jsonFilterObject);
                }
            }
        }
        jsonFilterObject.setObject(object);
        return jsonFilterObject;
    }

    private void handleAnnotation(Class clazz, Object object, JsonFilterObject jsonFilterObject) {
        String[] includes = {};
        String[] excludes = {};
        Class objClass = null;
        if (clazz.equals(SerializeField.class)) {
            SerializeField serializeField = (SerializeField) object;
            includes = serializeField.includes();
            excludes = serializeField.excludes();
            objClass = serializeField.clazz();
        }
        if (clazz.equals(MultiSerializeField.class)) {
            MultiSerializeField serializeField = (MultiSerializeField) object;
            includes = serializeField.includes();
            excludes = serializeField.excludes();
            objClass = serializeField.clazz();
        }
        if (includes.length > 0 && excludes.length > 0) {
            throw new RuntimeException("Can not use both includes and excludes in the same annotation!");
        } else if (includes.length > 0) {
            jsonFilterObject.getIncludes().put(objClass, new HashSet<String>(Arrays.asList(includes)));
        } else if (excludes.length > 0) {
            jsonFilterObject.getExcludes().put(objClass, new HashSet<String>(Arrays.asList(excludes)));
        }
    }
}
