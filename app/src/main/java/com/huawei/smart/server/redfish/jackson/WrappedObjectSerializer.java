package com.huawei.smart.server.redfish.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by DuoQi on 2018-02-18.
 */
public class WrappedObjectSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private Class<?> wrappedType;
    private String wrappedWith;

    public Object deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectNode objectNode = jp.readValueAsTree();
        JsonNode wrapped = objectNode.get(wrappedWith);
        JsonParser parser = wrapped.traverse();
        parser.setCodec(jp.getCodec());
        return parser.readValueAs(wrappedType);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
        this.wrappedWith = property.getAnnotation(WrappedWith.class).value();
        this.wrappedType = property.getType().getRawClass();
        return this;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            HashMap<String, Object> wrapped = new HashMap<>();
            wrapped.put(this.wrappedWith, value);
            serializers.defaultSerializeValue(wrapped, gen);
        }
    }
}