package com.aeert.converter;

import com.aeert.bean.JsonFilterObject;
import com.aeert.filter.SimpleSerializerFilter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static com.alibaba.fastjson.util.IOUtils.UTF8;

/**
 * @Author l'amour solitaire
 * @Description 自定义消息转换器
 * @Date 2020/8/11 下午8:52
 **/
public class JsonFilterHttpMessageConverter extends FastJsonHttpMessageConverter {

    private FastJsonConfig fastJsonConfig = new FastJsonConfig();

    public JsonFilterHttpMessageConverter() {
        super();
        setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON_UTF8));
        this.fastJsonConfig.setCharset(UTF8);
        this.fastJsonConfig.setSerializerFeatures(SerializerFeature.DisableCircularReferenceDetect, SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteMapNullValue);
    }

    /**
     * @param obj-           the object to write to the output message
     * @param outputMessage- the HTTP output message to write to
     * @throws IOException-                    in case of I/O errors
     * @throws HttpMessageNotWritableException - in case of conversion errors
     */
    @Override
    protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (obj instanceof JsonFilterObject) {
            JsonFilterObject jsonFilterObject = (JsonFilterObject) obj;
            OutputStream out = outputMessage.getBody();
            SimpleSerializerFilter simpleSerializerFilter = new SimpleSerializerFilter(jsonFilterObject.getIncludes(), jsonFilterObject.getExcludes());
            String text = JSON.toJSONString(jsonFilterObject.getObject(), simpleSerializerFilter, fastJsonConfig.getSerializerFeatures());
            byte[] bytes = text.getBytes(this.fastJsonConfig.getCharset());
            out.write(bytes);
        } else {
            OutputStream out = outputMessage.getBody();
            String text = JSON.toJSONString(obj, fastJsonConfig.getSerializerFeatures());
            byte[] bytes = text.getBytes(this.fastJsonConfig.getCharset());
            out.write(bytes);
        }
    }
}
