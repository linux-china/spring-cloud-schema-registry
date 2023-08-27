package org.springframework.cloud.schema.registry.json;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class JsonSchemaServiceManagerImplTest {
    private static final JsonSchemaServiceManagerImpl jsonSchemaServiceManager = new JsonSchemaServiceManagerImpl(null);
    private static final JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);

    @BeforeAll
    public static void setUp() throws Exception {
        jsonSchemaServiceManager.registerSchema(User3.class, userJsonSchema());
    }

    @Test
    public void testRegisterSchema() throws Exception {
        final JsonSchema schema = jsonSchemaServiceManager.getSchema(User3.class);
        assertThat(schema).isNotNull();
    }

    @Test
    public void testReadData() throws Exception {
        String jsonText = "{\"id\":1,\"name\":\"linux_china\"}";
        final User3 user = jsonSchemaServiceManager.readData(User3.class, jsonText.getBytes());
        assertThat(user).isNotNull();
    }

    @Test
    public void testReadDataFailed() {
        String jsonText = "{\"id\": \"1\",\"name\":\"linux_china\"}";
        assertThatThrownBy(() -> {
            jsonSchemaServiceManager.readData(User3.class, jsonText.getBytes());
        }).isInstanceOf(IOException.class);
    }

    @Test
    public void testValidateObject() throws Exception {
        User3 user3 = new User3();
        user3.setId(1);
        user3.setName("linux_china");
        assertThat(jsonSchemaServiceManager.validate(user3)).isEmpty();
    }

    public static JsonSchema userJsonSchema() {
        String schema1Text = "{\n" +
                "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"id\": {\n" +
                "      \"type\": \"integer\",\n" +
                "      \"description\": \"User id\"\n" +
                "    },\n" +
                "    \"name\": {\n" +
                "      \"type\": \"string\",\n" +
                "      \"description\": \"User name\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"description\": \"User class\"\n" +
                "}";
        return jsonSchemaFactory.getSchema(schema1Text);
    }
}
