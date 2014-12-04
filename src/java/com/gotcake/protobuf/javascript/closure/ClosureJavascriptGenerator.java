/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Aaron Cake
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.gotcake.protobuf.javascript.closure;

import com.beust.jcommander.Parameter;
import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.javascript.JavascriptOptionProtos;
import com.gotcake.protobuf.javascript.JavascriptGenerator;
import com.gotcake.protobuf.javascript.Utils;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;

import java.io.*;
import java.nio.file.Paths;
import java.util.Map;

/**
 * A {@code JavascriptGenerator} that generates closure compatible code.
 * @author Aaron Cake
 */
public class ClosureJavascriptGenerator implements JavascriptGenerator {

    private PrintStream debugWriter;

    @Override
    public boolean supportsMultipleOutputFiles() {
        return true;
    }

    @Override
    public void enableDebug(final PrintStream debugWriter) {
        this.debugWriter = debugWriter;
    }

    @Override
    public void setExtraArgs(final Map<String, String> extraArgs) {
        // TODO: handle extra args
    }

    @Override
    public void processProtoFile(final DescriptorProtos.FileDescriptorProto protoFile, final SectionBuffer outputBuffer) throws IOException {

        outputBuffer.lineBufferSection(GlobalSection.Docs)
                .line("// DO NOT EDIT!! This file contains auto-generated code")
                .line();

        outputBuffer.lineBufferSection(GlobalSection.Requires)
                .line("goog.require('protolib.Buffer');")
                .line("goog.require('protolib.Message');")
                .line();

        // blank line before content
        outputBuffer.lineBufferSection(GlobalSection.Content).line();

        final JavascriptOptionProtos.JavascriptFileOptions fileOptions = protoFile.getOptions().getExtension(JavascriptOptionProtos.jsFileOptions);

        final String namespace = fileOptions.hasNamespace()
                ? fileOptions.getNamespace()
                : protoFile.getPackage();

        final EnumJavascriptGenerator enumGenerator = new EnumJavascriptGenerator();
        final MessageJavascriptGenerator messageGenerator = new MessageJavascriptGenerator();

        for (final DescriptorProtos.EnumDescriptorProto enumDescriptor : protoFile.getEnumTypeList()) {
            final String enumName = Utils.getFullyQualifiedTypeName(enumDescriptor.getName(), namespace);
            final SectionBuffer enumBuffer = outputBuffer.childSection(GlobalSection.Content);
            enumGenerator.processEnum(enumDescriptor, enumName, outputBuffer, enumBuffer);
        }

        for (final DescriptorProtos.DescriptorProto messageDescriptor : protoFile.getMessageTypeList()) {
            final String messageName = Utils.getFullyQualifiedTypeName(messageDescriptor.getName(), namespace);
            final SectionBuffer messageBuffer = outputBuffer.childSection(GlobalSection.Content);
            messageGenerator.processMessage(messageDescriptor, messageName, outputBuffer, messageBuffer);
        }

    }

    /**
     * Write a message to the debug writer, if set
     * @param message a varargs list of object to write as strings to the debug writer
     */
    private void debug(final Object... message) {
        if (debugWriter != null) {
            debugWriter.print("DEBUG: ");
            for (final Object msgPart: message) {
                debugWriter.print(msgPart.toString());
            }
            debugWriter.write('\n');
        }
    }

    private static String getReaderJavascriptForType(DescriptorProtos.FieldDescriptorProto field) {
        final DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        switch (type) {
            case TYPE_BOOL:
                return "!!buffer.readVarint32()";
            case TYPE_ENUM:
                return "buffer.readVarint32()";
            case TYPE_INT32:
                return "buffer.readVarint32() | 0";
            case TYPE_UINT32:
                return "buffer.readVarint32() >>> 0";
            case TYPE_SINT32:
                return "buffer.readVarint32ZigZag() | 0";
            case TYPE_BYTES:
                return "buffer.readVBytes()";
            case TYPE_STRING:
                return "buffer.readVString()";
            case TYPE_MESSAGE:
                return "new " + Utils.getJavascriptType(field) + "().decode(buffer)";
            case TYPE_DOUBLE:
                return "buffer.readFloat64()";
            case TYPE_FLOAT:
                return "buffer.readFloat32()";
            case TYPE_FIXED32:
                return "buffer.readUint32() >>> 0";
            case TYPE_SFIXED32:
                return "buffer.readInt32() | 0";
            default:
                throw new RuntimeException("Unsupported type: " + type.name());
        }
    }

}
