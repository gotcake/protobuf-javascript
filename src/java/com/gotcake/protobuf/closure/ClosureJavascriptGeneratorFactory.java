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
import com.google.protobuf.ExtensionRegistry;
import com.gotcake.protobuf.CodeGeneratorFactory;
import com.gotcake.protobuf.proto.ClosureOptionProtos;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * A factory for ClosureJavascriptGenerator
 * @author Aaron Cake
 */
public class ClosureJavascriptGeneratorFactory implements CodeGeneratorFactory<ClosureJavascriptGenerator> {

    public ClosureJavascriptGenerator createGenerator(final DescriptorProtos.FileDescriptorSet descriptorSet) {
        final ClosureOptions options = new ClosureOptions(descriptorSet);
        return new ClosureJavascriptGenerator(options);
    }

    @Override
    public void registerExtensions(ExtensionRegistry registry) {
        ClosureOptionProtos.registerAllExtensions(registry);
    }

    @Override
    public Path getOutputFile(final DescriptorProtos.FileDescriptorProto file, final Path outputDir) {

        // get and sanitize the file name
        String fileName = file.getName().replaceFirst("\\.proto$", "");
        int index = fileName.lastIndexOf(File.separator);
        if (index > -1) {
            fileName = fileName.substring(index+1);
        }
        fileName = fileName.replace("_", "").toLowerCase() + ".js";

        // get the namespace
        final String namespace = ClosureUtil.getNamespace(file);

        // splice it all together
        if (namespace != null) {
            final String[] pkgParts = namespace.split("\\.");
            return Paths.get(Paths.get(outputDir.toString(), pkgParts).toString(), fileName);
        } else {
            return Paths.get(outputDir.toString(), fileName);
        }
    }


}
