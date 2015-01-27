package com.gotcake.protobuf.closure;

import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.Utils;
import com.gotcake.protobuf.proto.ClosureOptionProtos;

/**
 * Created by aaron on 1/10/15.
 */
public class ClosureUtil {

    public static String getNamespace(final DescriptorProtos.FileDescriptorProto file) {
        final ClosureOptionProtos.ClosureOptions options = file.getOptions().getExtension(ClosureOptionProtos.closureOptions);
        if (options.hasNamespace()) {
            return options.getNamespace();
        } else if (file.hasPackage()) {
            return file.getPackage();
        } else {
            return null;
        }
    }

    public static String cleanTypeName(final String typeName) {
        if (typeName.startsWith(".")) {
            return typeName.substring(1);
        } else {
            return typeName;
        }
    }

    public static String concatNames(final String namespace, final String typeName) {
        if (namespace == null || namespace.isEmpty()) {
            return cleanTypeName(typeName);
        } else {
            return namespace + '.' + cleanTypeName(typeName);
        }
    }

}
