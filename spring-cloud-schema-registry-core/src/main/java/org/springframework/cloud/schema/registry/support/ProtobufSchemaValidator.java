/*
 * Copyright 2016-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.schema.registry.support;

import io.protostuff.compiler.model.Message;
import io.protostuff.compiler.model.Package;
import io.protostuff.compiler.model.Proto;
import io.protostuff.compiler.parser.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.springframework.cloud.schema.registry.model.Compatibility;
import org.springframework.cloud.schema.registry.model.Schema;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Protobuf Schema validator
 *
 * @author linux_china
 */
public class ProtobufSchemaValidator implements SchemaValidator {
    private static final FileDescriptorLoaderImpl fileDescriptorLoader = new FileDescriptorLoaderImpl(new ParseErrorLogger(), Collections.emptySet());

    /**
     * Unique Avro schema format identifier.
     */
    public static final String PROTO_FORMAT = "proto";

    @Override
    public boolean isValid(String definition) {
        boolean result = true;
        try {
            parseProto(definition);
        } catch (Exception ex) {
            result = false;
        }
        return result;
    }

    @Override
    public void validate(String definition) {
        try {
            parseProto(definition);
        } catch (Exception ex) {
            throw new InvalidSchemaException((ex.getMessage()));
        }
    }

    @Override
    public Compatibility compatibilityCheck(String source, String other) {
        return null;
    }

    @Override
    public Schema match(List<Schema> schemas, String definition) {
        Schema result = null;
        try {
            final Proto source = parseProto(definition);
            for (Schema s : schemas) {
                final Proto target = parseProto(s.getDefinition());
                if (isSameProto(source, target)) {
                    result = s;
                    break;
                }
            }
        } catch (Exception ignore) {

        }
        return result;
    }

    @Override
    public String getFormat() {
        return PROTO_FORMAT;
    }

    private Proto parseProto(String text) {
        CharStream charStream = CharStreams.fromString(text);
        ProtoContext protoContext = fileDescriptorLoader.load(name -> charStream, "schema.proto");
        return protoContext.getProto();
    }

    private boolean isSameProto(Proto source, Proto target) {
        final Package sourcePackage = source.getPackage();
        final Package targetPackage = target.getPackage();
        boolean samePackage = false;
        if (sourcePackage == null && targetPackage == null) {
            samePackage = true;
        } else if (sourcePackage != null) {
            samePackage = sourcePackage.equals(targetPackage);
        }
        if (samePackage) {
            final List<Message> sourceMessages = source.getMessages();
            final List<Message> targetMessages = target.getMessages();
            if (sourceMessages.size() == targetMessages.size() && !sourceMessages.isEmpty()) {
                boolean sameMessages = true;
                for (Message targetMessage : targetMessages) {
                    final Message sourceMessage = source.getMessage(targetMessage.getName());
                    if (sourceMessage == null || !Objects.equals(sourceMessage.toString(), targetMessage.toString())) {
                        sameMessages = false;
                        break;
                    }
                }
                return sameMessages;
            }
        }
        return false;
    }

}
