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

package com.gotcake.protobuf;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.WireFormat;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * A set of common utility methods
 * @author Aaron Cake
 */
public class Utils {


    public static int getTag(final DescriptorProtos.FieldDescriptorProto field, boolean isPacked) {
        final int fieldNumber = field.getNumber();
        final int wireType = isPacked && field.getLabel() == DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED
                ? WireFormat.WIRETYPE_LENGTH_DELIMITED
                : getWireType(field);
        return (fieldNumber << 3) | wireType;
    }

    public static int getWireType(final DescriptorProtos.FieldDescriptorProto field) {
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

    public static String getTypeName(final DescriptorProtos.FieldDescriptorProto.Type type) {
        switch (type) {
            case TYPE_BOOL:
                return "bool";
            case TYPE_ENUM:
                return "enum";
            case TYPE_INT32:
                return "int32";
            case TYPE_UINT32:
                return "uint32";
            case TYPE_SINT32:
                return "sint32";
            case TYPE_BYTES:
                return "bytes";
            case TYPE_STRING:
                return "string";
            case TYPE_MESSAGE:
                return "message";
            case TYPE_DOUBLE:
                return "double";
            case TYPE_FLOAT:
                return "float";
            case TYPE_FIXED32:
                return "fixed32";
            case TYPE_SFIXED32:
                return "sfixed32";
            case TYPE_FIXED64:
                return "sfixed64";
            case TYPE_SFIXED64:
                return "sfixed64";
            case TYPE_INT64:
                return "int64";
            case TYPE_UINT64:
                return "uint64";
            case TYPE_SINT64:
                return "sint64";
            case TYPE_GROUP:
                return "group";
            default:
                throw new RuntimeException("Unrecognized type: " + type.name());
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
                throw new RuntimeException("Unrecognized wire type: " + wireType);
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

    public static void expandWildcardPath(final String wildcardPath, final Set<String> targetPathSet) throws IOException {
        com.esotericsoftware.wildcard.Paths p = new com.esotericsoftware.wildcard. Paths();
        p.glob("./", wildcardPath);
        Iterator<File> iterator = p.fileIterator();
        while (iterator.hasNext()) {
            final File file = iterator.next();
            if (file.exists())
                targetPathSet.add(file.getCanonicalPath());
        }
    }

    private static final StringBuilder STRING_BUILDER_INSTANCE = new StringBuilder();

    /**
     * Concatenates a list of strings or objects to a single string.
     * WARNING: NOT THREAD SAFE
     * @param strings
     * @return the concatenated string
     */
    public static String toString(final Object... strings) {
        STRING_BUILDER_INSTANCE.setLength(0);
        for (final Object o: strings) {
            STRING_BUILDER_INSTANCE.append(String.valueOf(o));
        }
        return STRING_BUILDER_INSTANCE.toString();
    }


}
