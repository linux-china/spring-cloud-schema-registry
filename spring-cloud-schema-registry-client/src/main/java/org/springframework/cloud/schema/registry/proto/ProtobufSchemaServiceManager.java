package org.springframework.cloud.schema.registry.proto;

import com.google.protobuf.GeneratedMessageV3;
import io.protostuff.compiler.model.Proto;
import org.springframework.cloud.schema.registry.SchemaNotFoundException;
import org.springframework.cloud.schema.registry.SchemaReference;

import java.io.IOException;

/**
 * Protobuf Schema Service Manager
 *
 * @author linux_china
 */
public interface ProtobufSchemaServiceManager {
    void registerSchema(Class<? extends GeneratedMessageV3> clazz, SchemaReference schemaReference) throws SchemaNotFoundException, IOException;

    void registerSchema(Class<? extends GeneratedMessageV3> clazz, Proto schema);

    Proto getSchema(Class<? extends GeneratedMessageV3> clazz) throws SchemaNotFoundException;

}
