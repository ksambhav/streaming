package org.example;

import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

import static org.example.JsonSerializer.MAPPER;

public class JsonDeserializer<T> implements Deserializer<T> {


    private final Class<T> className;

    public JsonDeserializer(Class<T> className) {
        this.className = className;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            return MAPPER.readValue(data, className);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
