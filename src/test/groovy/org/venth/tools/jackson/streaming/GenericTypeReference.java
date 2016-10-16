package org.venth.tools.jackson.streaming;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Iterator;

import com.fasterxml.jackson.core.type.TypeReference;
import org.spockframework.util.ReflectionUtil;

/**
 * @author Venth on 08/10/2016
 */
public abstract class GenericTypeReference  {

    public static Class<Iterator<SimpleObject>> simpleObjectIteratorClass() throws NoSuchFieldException {
        return (Class<Iterator<SimpleObject>>) DeserializedObjectWithArray.class.getDeclaredField("array").getType();
//        return new GenericTypeReferenceExtractor<Iterator<SimpleObject>>() {}.toClass();
    }

    Iterator<DeserializedObjectWithArray> fieldToGetClass;

    public static Class<Iterator<DeserializedObjectWithArray>> deserializedObjectWithArrayIteratorClass() throws NoSuchFieldException {
        return (Class<Iterator<DeserializedObjectWithArray>>) GenericTypeReference.class.getDeclaredField("fieldToGetClass").getType();
//        return new GenericTypeReferenceExtractor<Iterator<DeserializedObjectWithArray>>() {}.toClass();
    }


    static abstract class GenericTypeReferenceExtractor<T> {
        public Class<T> getTo() throws Exception{
            try{
                return ((Class<T>)((ParameterizedType)this.getClass().
                        getGenericSuperclass()).getActualTypeArguments()[0]);
            }catch(ClassCastException cce){
                cce.printStackTrace();
                return ((Class<T>)((ParameterizedType)(((Class<T>)
                        this.getClass().getAnnotatedSuperclass().getType()).getGenericSuperclass()))
                        .getActualTypeArguments()[0]);
            }
        }

        public Class<T> toClass() {
            Type[] actualTypeArguments = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments();
            Type type = Arrays.stream(getClass().getGenericInterfaces())
                    .findFirst().orElse(actualTypeArguments[0]);
            return (Class<T>)type;
        }
    }
}
