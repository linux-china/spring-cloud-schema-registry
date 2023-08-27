package org.springframework.cloud.schema.registry.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cloud.schema.registry.SchemaNotFoundException;
import org.springframework.cloud.schema.registry.SchemaReference;
import org.springframework.cloud.schema.registry.client.SchemaRegistryClient;

import java.io.IOException;
import java.util.Set;

/**
 * Json Schema Service Manager implementation
 *
 * @author linux_china
 */
public class JsonSchemaServiceManagerImpl implements JsonSchemaServiceManager {
    private final SchemaRegistryClient schemaRegistryClient;
    private final Cache jsonSchemaCache = new ConcurrentMapCache("jsonSchema");
    private final ObjectMapper objectMapper;
    private static final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    public JsonSchemaServiceManagerImpl(SchemaRegistryClient schemaRegistryClient) {
        this.schemaRegistryClient = schemaRegistryClient;
        objectMapper = new ObjectMapper();
    }

    public JsonSchemaServiceManagerImpl(SchemaRegistryClient schemaRegistryClient, ObjectMapper objectMapper) {
        this.schemaRegistryClient = schemaRegistryClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void registerSchema(Class<?> clazz, SchemaReference schemaReference) throws SchemaNotFoundException, IOException {
        try {
            final String schemaText = schemaRegistryClient.fetch(schemaReference);
            final JsonSchema schema = jsonSchemaFactory.getSchema(schemaText);
            jsonSchemaCache.put(clazz, schema);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Failed to fetch schema")) {
                throw new SchemaNotFoundException(e.getMessage());
            } else {
                throw new IOException("Failed to register schema for " + clazz, e);
            }
        }
    }

    @Override
    public void registerSchema(Class<?> clazz, JsonSchema schema) {
        jsonSchemaCache.put(clazz, schema);
    }

    @Override
    public JsonSchema getSchema(Class<?> clazz) throws SchemaNotFoundException {
        final Cache.ValueWrapper valueWrapper = jsonSchemaCache.get(clazz);
        if (valueWrapper == null) {
            throw new SchemaNotFoundException("Schema not found for " + clazz);
        } else {
            return (JsonSchema) valueWrapper.get();
        }
    }

    @Override
    public Set<ValidationMessage> validate(Object object) throws SchemaNotFoundException, IllegalArgumentException {
        Class<?> targetClass = object.getClass();
        final JsonSchema schema = getSchema(targetClass);
        final JsonNode jsonNode = objectMapper.convertValue(object, JsonNode.class);
        return schema.validate(jsonNode);
    }

    @Override
    public <T> T readData(Class<? extends T> targetClass, byte[] payload) throws SchemaNotFoundException, IOException, IllegalArgumentException {
        final JsonNode jsonNode = objectMapper.readTree(payload);
        final JsonSchema schema = getSchema(targetClass);
        final Set<ValidationMessage> result = schema.validate(jsonNode);
        if (result.isEmpty()) {
            return objectMapper.convertValue(jsonNode, targetClass);
        } else {
            throw new IOException(String.join("\n", result.stream().map(ValidationMessage::getMessage).toArray(String[]::new)));
        }
    }
}
