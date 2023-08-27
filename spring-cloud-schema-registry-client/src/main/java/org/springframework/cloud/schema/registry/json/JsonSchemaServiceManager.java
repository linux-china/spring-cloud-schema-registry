package org.springframework.cloud.schema.registry.json;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import org.springframework.cloud.schema.registry.SchemaNotFoundException;
import org.springframework.cloud.schema.registry.SchemaReference;

import java.io.IOException;
import java.util.Set;

/**
 * Json Schema Service Manager
 *
 * @author linux_china
 */
public interface JsonSchemaServiceManager {

    void registerSchema(Class<?> clazz, SchemaReference schemaReference) throws SchemaNotFoundException, IOException;

    void registerSchema(Class<?> clazz, JsonSchema schema);

    JsonSchema getSchema(Class<?> clazz) throws SchemaNotFoundException;

    Set<ValidationMessage> validate(Object object) throws SchemaNotFoundException, IllegalArgumentException;

    <T> T readData(Class<? extends T> targetClass, byte[] payload) throws SchemaNotFoundException, IOException, IllegalArgumentException;
}
