package org.springframework.cloud.schema.registry.proto;

import com.google.protobuf.StringValue;
import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.parser.FileDescriptorLoaderImpl;
import io.protostuff.compiler.parser.ParseErrorLogger;
import io.protostuff.compiler.parser.ProtoContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtobufSchemaServiceManagerImplTest {
    private static final FileDescriptorLoaderImpl fileDescriptorLoader = new FileDescriptorLoaderImpl(new ParseErrorLogger(), Collections.emptySet());
    private static final ProtobufSchemaServiceManagerImpl protobufSchemaManager = new ProtobufSchemaServiceManagerImpl(null);

    @BeforeAll
    public static void setUp() throws Exception {
        String protobufText = "syntax = \"proto3\";\n" +
                "\n" +
                "package org.mvnsearch.person;\n" +
                "\n" +
                "message Person {\n" +
                "  optional string name = 1;\n" +
                "  optional int32 id = 2;\n" +
                "  optional string email = 3;\n" +
                "}\n";
        protobufSchemaManager.registerSchema(StringValue.class, parseProto(protobufText));
    }

    @Test
    public void testRegisterSchema() throws Exception {
        assertThat(protobufSchemaManager.getSchema(StringValue.class)).isNotNull();
    }

    private static Proto parseProto(String text) {
        CharStream charStream = CharStreams.fromString(text);
        ProtoContext protoContext = fileDescriptorLoader.load(name -> charStream, "schema.proto");
        return protoContext.getProto();
    }
}
