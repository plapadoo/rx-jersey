package net.winterly.rx.jersey;

import org.glassfish.jersey.message.MessageBodyWorkers;
import rx.Observable;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class RxBodyReader implements MessageBodyReader<Object> {

    @Inject
    private Provider<MessageBodyWorkers> workers;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return Observable.class.isAssignableFrom(type);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {
        final Type actualTypeArgument = actual(genericType);
        final Class entityType = entityType(actualTypeArgument);
        final MessageBodyReader reader = workers.get().getMessageBodyReader(entityType, actualTypeArgument, annotations, mediaType);

        return reader.readFrom(entityType, actualTypeArgument, annotations, mediaType, httpHeaders, entityStream);
    }

    private static Type actual(Type genericType) {
        final ParameterizedType actualGenericType = (ParameterizedType) genericType;
        return actualGenericType.getActualTypeArguments()[0];
    }

    private static Class entityType(Type actualTypeArgument) {
        if(actualTypeArgument instanceof Class) {
            return (Class) actualTypeArgument;
        }

        if(actualTypeArgument instanceof ParameterizedType) {
            ParameterizedType parameterized = (ParameterizedType) actualTypeArgument;
            return (Class) parameterized.getRawType();
        }

        throw new IllegalStateException();
    }

}
