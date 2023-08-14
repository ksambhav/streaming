package org.example;

import org.apache.kafka.common.serialization.Deserializer;

public class JsonDeserializer<T> implements Deserializer<T> {


    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        return null;//MAPPER.readValue(data, String.class);
    }

}
