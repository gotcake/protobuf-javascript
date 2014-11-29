package com.gotcake.protobuf.javascript;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.WireFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by aaron on 11/29/14.
 */
public class Utils {

    public static String getFullyQualifiedTypeName(final String typeName, final String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return typeName;
        } else {
            return packageName + '.' + typeName;
        }
    }

    public static String cleanTypeName(final String typeName) {
        if (typeName.startsWith(".")) {
            return typeName.substring(1);
        } else {
            return typeName;
        }
    }

    public static int getTag(final DescriptorProtos.FieldDescriptorProto field, boolean isPacked) {
        final int fieldNumber = field.getNumber();
        final int wireType = isPacked && field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
                ? WireFormat.WIRETYPE_LENGTH_DELIMITED
                : getNonPackedWireType(field);
        return (fieldNumber << 3) | wireType;
    }

    public static String getJavascriptType(final DescriptorProtos.FieldDescriptorProto field) {
        final DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        switch (type) {
            case TYPE_BOOL:
                return "boolean";
            case TYPE_BYTES:
                return "ArrayBuffer";
            case TYPE_DOUBLE:
            case TYPE_FLOAT:
            case TYPE_FIXED32:
            case TYPE_INT32:
            case TYPE_UINT32:
            case TYPE_SINT32:
            case TYPE_SFIXED32:
                return "number";
            case TYPE_ENUM:
            case TYPE_MESSAGE:
                return cleanTypeName(field.getTypeName());
            case TYPE_STRING:
                return "string";
            case TYPE_GROUP:
            default:
                return null;
        }
    }

    public static int getNonPackedWireType(final DescriptorProtos.FieldDescriptorProto field) {
        final DescriptorProtos.FieldDescriptorProto.Type type = field.getType();
        switch (type) {
            case TYPE_BOOL:
            case TYPE_ENUM:
            case TYPE_INT32:
            case TYPE_UINT32:
            case TYPE_SINT32:
                return WireFormat.WIRETYPE_VARINT;
            case TYPE_BYTES:
            case TYPE_STRING:
            case TYPE_MESSAGE:
                return WireFormat.WIRETYPE_LENGTH_DELIMITED;
            case TYPE_DOUBLE:
                return WireFormat.WIRETYPE_FIXED64;
            case TYPE_FLOAT:
            case TYPE_FIXED32:
            case TYPE_SFIXED32:
                return WireFormat.WIRETYPE_FIXED32;
            case TYPE_GROUP:
            default:
                throw new RuntimeException("Unsupported type: " + type.name());
        }
    }

    public static String getWireTypeName(final int wireType) {
        switch (wireType) {
            case WireFormat.WIRETYPE_FIXED32:
                return "FIXED32";
            case WireFormat.WIRETYPE_FIXED64:
                return "FIXED64";
            case WireFormat.WIRETYPE_VARINT:
                return "VARINT";
            case WireFormat.WIRETYPE_LENGTH_DELIMITED:
                return "LENGTH_DELIMITED";
            case WireFormat.WIRETYPE_START_GROUP:
                return "START_GROUP";
            case WireFormat.WIRETYPE_END_GROUP:
                return "END_GROUP";
            default:
                throw new RuntimeException("Unsupported wire type: " + wireType);
        }
    }

    public static String toCamelCase(final String identifier, boolean capital) {
        if (identifier == null)
            return null;
        StringBuilder sb = new StringBuilder(identifier);
        if (sb.length() == 0)
            return "";
        int i = 0;
        while (i < sb.length()) {
            if (sb.charAt(i) == '_') {
                sb.delete(i, i+1);
                if (sb.length() > i) {
                    sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
                }
            } else {
                sb.setCharAt(i, Character.toLowerCase(sb.charAt(i)));
            }
            ++i;
        }
        if (capital && sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static void expandWildcardPath(final String wildcardPath, final Set<Path> targetPathSet) throws IOException {
        // TODO: Implement something that works here
    }


}
