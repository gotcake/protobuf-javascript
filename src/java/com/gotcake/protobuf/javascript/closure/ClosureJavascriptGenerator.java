package com.gotcake.protobuf.javascript.closure;

import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.javascript.JavascriptOptionProtos;
import com.gotcake.protobuf.javascript.JavascriptGenerator;
import com.gotcake.protobuf.javascript.Utils;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;

import java.io.*;
import java.util.Map;

/**
 * Created by aaron on 12/1/14.
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

    @Override
    public String getRelativeOutputPathFor(DescriptorProtos.FileDescriptorProto protoFile) {
        final String pkg = protoFile.getPackage();
        if (pkg == null) {
            return protoFile.getName().replace("_", "").toLowerCase() + ".js";
        } else {
            return pkg.replace(".", "/").toLowerCase() + "/" +  protoFile.getName().replace("_", "").toLowerCase() + ".js";
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
