package org.springframework.cloud.schema.registry.proto;

import com.google.protobuf.GeneratedMessageV3;
import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.parser.FileDescriptorLoaderImpl;
import io.protostuff.compiler.parser.ParseErrorLogger;
import io.protostuff.compiler.parser.ProtoContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cloud.schema.registry.SchemaNotFoundException;
import org.springframework.cloud.schema.registry.SchemaReference;
import org.springframework.cloud.schema.registry.client.SchemaRegistryClient;

import java.io.IOException;
import java.util.Collections;

/**
 * Protobuf Schema Service Manager implementation
 *
 * @author linux_china
 */
public class ProtobufSchemaServiceManagerImpl implements ProtobufSchemaServiceManager {
    private final SchemaRegistryClient schemaRegistryClient;
    private final Cache protoSchemaCache = new ConcurrentMapCache("protoSchema");
    private static final FileDescriptorLoaderImpl fileDescriptorLoader = new FileDescriptorLoaderImpl(new ParseErrorLogger(), Collections.emptySet());

    public ProtobufSchemaServiceManagerImpl(SchemaRegistryClient schemaRegistryClient) {
        this.schemaRegistryClient = schemaRegistryClient;
    }

    @Override
    public void registerSchema(Class<? extends GeneratedMessageV3> clazz, SchemaReference schemaReference) throws SchemaNotFoundException, IOException {
        try {
            final String protoText = schemaRegistryClient.fetch(schemaReference);
            final Proto proto = parseProto(protoText);
            protoSchemaCache.put(clazz, proto);
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
    public void registerSchema(Class<? extends GeneratedMessageV3> clazz, Proto schema) {
        protoSchemaCache.put(clazz, schema);
    }

    @Override
    public Proto getSchema(Class<? extends GeneratedMessageV3> clazz) throws SchemaNotFoundException {
        final Cache.ValueWrapper valueWrapper = protoSchemaCache.get(clazz);
        if (valueWrapper == null) {
            throw new SchemaNotFoundException("Schema not found for " + clazz);
        } else {
            return (Proto) valueWrapper.get();
        }
    }

    private Proto parseProto(String text) {
        CharStream charStream = CharStreams.fromString(text);
        ProtoContext protoContext = fileDescriptorLoader.load(name -> charStream, "schema.proto");
        return protoContext.getProto();
    }
}
