package com.gotcake.protobuf.closure;

import com.google.protobuf.DescriptorProtos;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aaron on 1/11/15.
 */
public class ClosureOptions {

    private final Map<Object, String> descriptorJavascriptTypeNameMap = new HashMap<>();
    private final Map<String, String> typeNameJavascriptTypeNameMap = new HashMap<>();

    public ClosureOptions(final DescriptorProtos.FileDescriptorSet descriptorSet) {
        initTypeNameMap(descriptorSet);
    }

    private void initTypeNameMap(final DescriptorProtos.FileDescriptorSet descriptorSet) {
        for (final DescriptorProtos.FileDescriptorProto file: descriptorSet.getFileList()) {
            final String namespace = ClosureUtil.getNamespace(file);
            final String protoPackage = file.hasPackage() ? file.getPackage() : null;
            for (final DescriptorProtos.DescriptorProto innerMessageType: file.getMessageTypeList()) {
                initTypeNameMapMessage(namespace, protoPackage, innerMessageType);
            }
            for (final DescriptorProtos.EnumDescriptorProto innerEnumType: file.getEnumTypeList()) {
                initTypeNameMapEnum(namespace, protoPackage, innerEnumType);
            }
        }
    }

    private void initTypeNameMapMessage(final String namespace, final String protoPackage, final DescriptorProtos.DescriptorProto messageType) {
        final String typeName = ClosureUtil.concatNames(namespace, messageType.getName());
        final String protoTypeName = ClosureUtil.concatNames(protoPackage, messageType.getName());
        descriptorJavascriptTypeNameMap.put(messageType, typeName);
        typeNameJavascriptTypeNameMap.put(protoTypeName, typeName);
        for (final DescriptorProtos.DescriptorProto innerMessageType: messageType.getNestedTypeList()) {
            initTypeNameMapMessage(typeName, protoTypeName, innerMessageType);
        }
        for (final DescriptorProtos.EnumDescriptorProto innerEnumType: messageType.getEnumTypeList()) {
            initTypeNameMapEnum(typeName, protoTypeName, innerEnumType);
        }
    }

    private void initTypeNameMapEnum(final String namespace, final String protoPackage, final DescriptorProtos.EnumDescriptorProto enumType) {
        final String typeName = ClosureUtil.concatNames(namespace, enumType.getName());
        descriptorJavascriptTypeNameMap.put(enumType, typeName);
        typeNameJavascriptTypeNameMap.put(ClosureUtil.concatNames(protoPackage, enumType.getName()), typeName);
    }

    public String getClosureTypeForMessageType(final DescriptorProtos.DescriptorProto messageType) {
        return descriptorJavascriptTypeNameMap.get(messageType);
    }

    public String getClosureTypeForEnumType(final DescriptorProtos.EnumDescriptorProto enumType) {
        return descriptorJavascriptTypeNameMap.get(enumType);
    }

    public String getClosureTypeForProtoTypeName(final String protoTypeName) {
        return typeNameJavascriptTypeNameMap.get(ClosureUtil.cleanTypeName(protoTypeName));
    }

    public String getJavascriptTypeForField(final DescriptorProtos.FieldDescriptorProto field) {
        final DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        switch (type) {
            case TYPE_BOOL:
                return "boolean";
            case TYPE_DOUBLE:
            case TYPE_FLOAT:
            case TYPE_FIXED32:
            case TYPE_SFIXED32:
            case TYPE_INT32:
            case TYPE_UINT32:
            case TYPE_SINT32:
                return "number";
            case TYPE_BYTES:
                return "ArrayBuffer";
            case TYPE_STRING:
                return "string";
            case TYPE_ENUM:
            case TYPE_MESSAGE:
                return getClosureTypeForProtoTypeName(field.getTypeName());
            default:
                throw new RuntimeException("Unsupported type: " + type.name());
        }
    }

    public boolean isFieldTypeSupported(DescriptorProtos.FieldDescriptorProto field) {
        final DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        switch (type) {
            case TYPE_INT64:
            case TYPE_FIXED64:
            case TYPE_UINT64:
            case TYPE_SFIXED64:
            case TYPE_SINT64:
            case TYPE_GROUP:
                return false;
            default:
                return true;
        }
    }

    public String getDecoderJavascriptForType(final DescriptorProtos.FieldDescriptorProto field) {
        final DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        switch (type) {
            case TYPE_BOOL:
                return "!!buffer.readVarint32()";
            case TYPE_ENUM:
                return "buffer.readVarint32()";
            case TYPE_INT32:
                return "buffer.readVarint32()";
            case TYPE_UINT32:
                return "buffer.readVarint32() >>> 0";
            case TYPE_SINT32:
                return "buffer.readVarint32ZigZag() | 0";
            case TYPE_BYTES:
                return "buffer.readVBytes()";
            case TYPE_STRING:
                return "buffer.readVString()";
            case TYPE_MESSAGE:
                return "new " + getClosureTypeForProtoTypeName(field.getTypeName()) + "().decode(buffer)";
            case TYPE_DOUBLE:
                return "buffer.readFloat64()";
            case TYPE_FLOAT:
                return "buffer.readFloat32()";
            case TYPE_FIXED32:
                return "buffer.readUint32()";
            case TYPE_SFIXED32:
                return "buffer.readInt32()";
            default:
                throw new RuntimeException("Unsupported type: " + type.name());
        }
    }

}
