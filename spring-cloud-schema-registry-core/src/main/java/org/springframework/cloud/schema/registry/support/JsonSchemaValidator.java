package org.springframework.cloud.schema.registry.support;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.springframework.cloud.schema.registry.model.Compatibility;
import org.springframework.cloud.schema.registry.model.Schema;

import java.util.List;

/**
 * JSON Schema validator
 *
 * @author linux_china
 */
public class JsonSchemaValidator implements SchemaValidator {
    private static final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    public static final String JSON_FORMAT = "json";

    @Override
    public boolean isValid(String definition) {
        boolean result = true;
        try {
            jsonSchemaFactory.getSchema(definition);
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    @Override
    public Compatibility compatibilityCheck(String source, String other) {
        return null;
    }

    @Override
    public Schema match(List<Schema> schemas, String definition) {
        Schema result = null;
        for (Schema s : schemas) {
            if (s.getDefinition().equals(definition)) {
                result = s;
                break;
            }
        }
        return result;
    }

    @Override
    public String getFormat() {
        return JSON_FORMAT;
    }
}
