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

package com.gotcake.protobuf.closure;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.WireFormat;
import com.gotcake.protobuf.CodeGenerator;
import com.gotcake.protobuf.Utils;
import com.gotcake.protobuf.builder.IndentedLineBuffer;
import com.gotcake.protobuf.builder.SectionBuffer;

import java.io.*;
import java.util.List;

/**
 * A {@code CodeGenerator} that generates closure compatible javascript.
 * @author Aaron Cake
 */
public class ClosureJavascriptGenerator implements CodeGenerator {


    final ClosureOptions options;

    /**
     * Create a new ClosureJavascriptGenerator
     * @param options the compiler options
     */
    public ClosureJavascriptGenerator(final ClosureOptions options) {
        this.options = options;
    }

    /**
     * Process a proto file and writes closure compatible javascript to the output buffer
     * @param protoFile the proto file descriptor
     * @param outputBuffer the output buffer
     * @throws IOException
     */
    @Override
    public void processProtoFile(final DescriptorProtos.FileDescriptorProto protoFile, final SectionBuffer outputBuffer) throws IOException {

        outputBuffer.lineBufferSection(GlobalSection.Docs)
                .line("// DO NOT EDIT!! This file contains auto-generated code")
                .line();

        outputBuffer.lineBufferSection(GlobalSection.Requires)
                .line("goog.require('gotcake.proto.Buffer');")
                .line("goog.require('gotcake.proto.Message');")
                .line();

        // blank line before content
        outputBuffer.lineBufferSection(GlobalSection.Content).line();

        processAllEnums(protoFile.getEnumTypeList(), outputBuffer);
        processAllMessages(protoFile.getMessageTypeList(), outputBuffer);

    }

    /**
     * Processes a list of enums, writing the output for each to the globalBuffer
     * @param enumList the list of enums to process
     * @param globalBuffer the global buffer
     * @throws IOException
     */
    private void processAllEnums(final List<DescriptorProtos.EnumDescriptorProto> enumList, final SectionBuffer globalBuffer) throws IOException {
        for (final DescriptorProtos.EnumDescriptorProto enumDescriptor : enumList) {
            final SectionBuffer enumBuffer = globalBuffer.childSection(GlobalSection.Content);
            processEnum(enumDescriptor, globalBuffer, enumBuffer);
        }
    }

    /**
     * Processes a list of messages, writing the output for each to the globalBuffer
     * @param messageList the list of messages to process
     * @param globalBuffer the global buffer
     * @throws IOException
     */
    private void processAllMessages(final List<DescriptorProtos.DescriptorProto> messageList, final SectionBuffer globalBuffer) throws IOException {
        for (final DescriptorProtos.DescriptorProto descriptor : messageList) {
            final SectionBuffer messageBuffer = globalBuffer.childSection(GlobalSection.Content);
            processMessage(descriptor, globalBuffer, messageBuffer);
        }
    }

    /**
     * Processes a single enum
     * @param descriptor the enum descriptor
     * @param globalBuffer the global buffer
     * @param buffer the enum buffer
     * @throws IOException
     */
    private void processEnum(final DescriptorProtos.EnumDescriptorProto descriptor,
                            final SectionBuffer globalBuffer,
                            final SectionBuffer buffer) throws IOException {

        final String javascriptName = options.getClosureTypeForEnumType(descriptor);

        globalBuffer.lineBufferSection(GlobalSection.Provides)
                .line("goog.provide('", javascriptName, "');");


        buffer.docSection(FieldSection.Docs)
                .setEnumType("number");

        IndentedLineBuffer body = buffer.lineBufferSection(FieldSection.Body)
                .write(javascriptName, " = {").in();

        boolean first = true;
        for (DescriptorProtos.EnumValueDescriptorProto value : descriptor.getValueList()) {
            if (first) {
                first = false;
                body.line();
            } else {
                body.line(',');
            }
            body.write(value.getName(), ": ", value.getNumber());
        }

        body.line().out().line("};").line();

    }

    /**
     * Processes a single message
     * @param descriptor the message descriptor
     * @param globalBuffer the global buffer
     * @param buffer the message buffer
     * @throws IOException
     */
    public void processMessage(final DescriptorProtos.DescriptorProto descriptor,
                               final SectionBuffer globalBuffer,
                               final SectionBuffer buffer) throws IOException {

        final String javascriptName = options.getClosureTypeForMessageType(descriptor);

        globalBuffer.lineBufferSection(GlobalSection.Provides)
                .line("goog.provide('", javascriptName, "');");

        writeMessageConstructor(descriptor, javascriptName, buffer.childSection(MessageSection.Constructor));

        writeMessageValidatorFunction(descriptor, javascriptName, buffer.childSection(MessageSection.Methods));

        writeMessageDecoderFunction(descriptor, javascriptName, buffer.childSection(MessageSection.Methods));

        writeMessageEncoderFunction(descriptor, javascriptName, buffer.childSection(MessageSection.Methods));

        processAllEnums(descriptor.getEnumTypeList(), globalBuffer);
        processAllMessages(descriptor.getNestedTypeList(), globalBuffer);

    }

    /**
     * Writes the message constructor to the given buffer
     * @param message the message descriptor
     * @param javascriptName the javascript name of the message
     * @param buffer the buffer to write to
     */
    public void writeMessageConstructor(final DescriptorProtos.DescriptorProto message,
                                            final String javascriptName,
                                            final SectionBuffer buffer) throws IOException {

        buffer.docSection(FunctionSection.Docs)
                .setDescription("Constructs an un-initialized ", javascriptName)
                .setConstructor(true)
                .setExtendsType("gotcake.proto.Message");

        buffer.lineBufferSection(FunctionSection.Header)
                .line(javascriptName, " = function() {");

        final SectionBuffer constructorBody = buffer.childSection(FunctionSection.Body);

        for (final DescriptorProtos.FieldDescriptorProto field : message.getFieldList()) {

            if (options.isFieldTypeSupported(field)) {

                final SectionBuffer fieldSection = constructorBody.indentedChildSection(ConstructorBodySection.Fields);
                final String fieldName = Utils.toCamelCase(field.getName(), false);
                final String typeName = options.getJavascriptTypeForField(field);
                final String defaultValue = field.getDefaultValue();
                final boolean isRepeated = field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED;

                fieldSection.docSection(FieldSection.Docs)
                        .setDescription("number = ", field.getNumber(), " type = ", Utils.getTypeName(field.getType()))
                        .setType(typeName + (isRepeated ? "[]" : ""));

                if (isRepeated) {
                    fieldSection.lineBufferSection(FieldSection.Body)
                            .line("this.", fieldName, " = [];");
                } else {
                    fieldSection.lineBufferSection(FieldSection.Body)
                            .line("this.", fieldName, " = ", defaultValue.isEmpty() ? "null" : defaultValue, ";");
                }

                fieldSection.lineBufferSection(FieldSection.Body).line();

            }

        }

        buffer.lineBufferSection(FunctionSection.Closer)
                .line("};")
                .line("goog.inherits(", javascriptName, ", gotcake.proto.Message);")
                .line();

    }

    /**
     * Writes the message encoder function to the given buffer
     * @param message the message descriptor
     * @param javascriptName the javascript name of the message
     * @param buffer the buffer to write to
     */
    public void writeMessageEncoderFunction(final DescriptorProtos.DescriptorProto message,
                                            final String javascriptName,
                                            final SectionBuffer buffer) throws IOException {

    }

    /**
     * Writes the message decoder function to the given buffer
     * @param message the message descriptor
     * @param javascriptName the javascript name of the message
     * @param buffer the buffer to write to
     */
    public void writeMessageDecoderFunction(final DescriptorProtos.DescriptorProto message,
                                            final String javascriptName,
                                            final SectionBuffer buffer) throws IOException {

        buffer.docSection(FunctionSection.Docs)
                .setDescription("A method that gets called by the decode method to decode each field")
                .addParameter("tag", "number", "The tag value for the field to decode")
                .addParameter("buffer", "gotcake.proto.Buffer", "The buffer to decode from")
                .setProtected();

        buffer.lineBufferSection(FunctionSection.Header)
                .line(javascriptName, ".prototype.decodeFieldCallback = function(tag, buffer){");

        final IndentedLineBuffer decoderBody = buffer.lineBufferSection(FunctionSection.Body)
                .in()
                .line("switch (tag) {")
                .in();

        for (final DescriptorProtos.FieldDescriptorProto field : message.getFieldList()) {

            if (options.isFieldTypeSupported(field)) {

                final String fieldName = Utils.toCamelCase(field.getName(), false);
                final int unpackedTag = Utils.getTag(field, false);

                final int nonPackedWireType = Utils.getWireType(field);
                final IndentedLineBuffer decoder = decoderBody.line("// wireType = ", Utils.getWireTypeName(nonPackedWireType),
                        ", number = ", field.getNumber())
                        .line("case ", unpackedTag, ":")
                        .in();
                if (field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED) {
                    decoder.line("this.", fieldName, ".push(", options.getDecoderJavascriptForType(field), "); break;")
                            .out();
                } else {
                    decoder.line("this.", fieldName, " = ", options.getDecoderJavascriptForType(field), "; break;")
                            .out();
                }

                if (field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED &&
                        nonPackedWireType != WireFormat.WIRETYPE_LENGTH_DELIMITED) {
                    decoderBody.line("// wireType = LENGTH_DELIMITED, number = ", field.getNumber())
                            .line("case ", Utils.getTag(field, true), ":")
                            .in()
                            .line("return ", unpackedTag, ";")
                            .out();
                }
            }

        }

        decoderBody.out().line("}");

        buffer.lineBufferSection(FunctionSection.Closer).line("};").line();

    }

    /**
     * Writes the message validator function to the given buffer
     * @param message the message descriptor
     * @param javascriptName the javascript name of the message
     * @param buffer the buffer to write to
     */
    public void writeMessageValidatorFunction(final DescriptorProtos.DescriptorProto message,
                                            final String javascriptName,
                                            final SectionBuffer buffer) throws IOException {

        buffer.docSection(FunctionSection.Docs)
                .setDescription("Validates that all required fields have been set")
                .setReturnType("boolean");

        buffer.lineBufferSection(FunctionSection.Header)
                .line(javascriptName, ".prototype.isInitialized = function(){");

        final IndentedLineBuffer validatorBody = buffer.lineBufferSection(FunctionSection.Body)
                .in().write("return ");

        boolean hasRequiredField = false;

        for (final DescriptorProtos.FieldDescriptorProto field : message.getFieldList()) {

            if (options.isFieldTypeSupported(field)) {

                final String fieldName = Utils.toCamelCase(field.getName(), false);
                if (field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED) {

                    if (hasRequiredField) {
                        validatorBody.line(" &&");
                    } else {
                        hasRequiredField = true;
                        validatorBody.in().in();
                    }
                    validatorBody.write("this.", fieldName, " !== null");

                }
            }
        }

        if (hasRequiredField) {
            validatorBody.line(";");
            buffer.lineBufferSection(FunctionSection.Closer)
                    .line("};")
                    .line();
        } else { // if there are no required fields, do no override the isInitialized method
            buffer.clearAll();
            buffer.lineBufferSection(FunctionSection.Docs)
                    .line("// No required fields for " + javascriptName + ", using default validator implementation.")
                    .line();
        }



    }

    /**
     * Writes the message validator function to the given buffer
     * @param message the message descriptor
     * @param javascriptName the javascript name of the message
     * @param buffer the buffer to write to
     */
    public void writeMessageSizeCalculatorFunction(final DescriptorProtos.DescriptorProto message,
                                              final String javascriptName,
                                              final SectionBuffer buffer) throws IOException {


    }

}
