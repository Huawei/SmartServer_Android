package com.huawei.smart.server.redfish.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Created by DuoQi on 2018-02-18.
 */
public class WrappedObjectDeserializer extends JsonDeserializer<Object> implements ContextualDeserializer {

    private Class<?> wrappedType;
    private String wrappedWith;

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
                                                BeanProperty property) {
        this.wrappedWith = property.getAnnotation(WrappedWith.class).value();
        this.wrappedType = property.getType().getRawClass();
        return this;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectNode objectNode = jp.readValueAsTree();
        JsonNode wrapped = objectNode.get(wrappedWith);
        if (wrapped != null) {
            JsonParser parser = wrapped.traverse();
            parser.setCodec(jp.getCodec());
            return parser.readValueAs(wrappedType);
        }

        try {
            return wrappedType.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

}