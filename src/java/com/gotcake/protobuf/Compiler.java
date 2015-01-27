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

import com.beust.jcommander.*;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistry;
import com.gotcake.protobuf.builder.SectionBuffer;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * The main compiler class which provides the command line interface for compilation
 * @author Aaron Cake
 */
public class Compiler {

    private CompilerOptions options;
    private ExtensionRegistry extensionRegistry;
    private CodeGeneratorFactory factory;

    public void setOptions(final CompilerOptions options) {
        this.options = options;
    }

    public CompilerOptions getOptions() {
        return options;
    }

    public void run() throws Exception {

        if (options == null) {
            throw new Exception("CompilerOptions not provided.");
        }

        final String errMsg = options.validate();
        if (errMsg != null) {
            throw new Exception(errMsg);
        }

        factory = options.getCodeGeneratorFactoryClass().newInstance();

        extensionRegistry = ExtensionRegistry.newInstance();

        factory.registerExtensions(extensionRegistry);

        if (options.isModeOutputLanguageNative()) {
            if (options.isModeReadProtoStream()) {
                final InputStream protoStream = options.getProtoInputStream();
                compileProtoStreamToLanguage(protoStream, options.getOutputDir(), options.getOutputLanguage());
            } else if (options.isModeReadProtoFiles()) {
                compileProtoToLanguage(options.getInputProtoFiles(), options.getProtoImportSearhDirs(),
                        options.getOutputDir(), options.getOutputLanguage());
            }
        } else if (options.isModeReadProtoStream()) {
            final InputStream protoStream = options.getProtoInputStream();
            compileProtoStreamToJavascript(protoStream);
        } else if (options.isModeReadDescriptorStream()) {
            final InputStream descriptorStream = options.getDescriptorInputStream();
            compileDescriptor(descriptorStream);
        } else if (options.isModeReadProtoFiles()) {
            final List<String> inputFiles = options.getInputProtoFiles();
            final List<String> searchDirs = options.getProtoImportSearhDirs();
            compileProtosToJavascript(inputFiles, searchDirs);
        }

    }

    private void compileProtoStreamToLanguage(final InputStream protoStream, final Path outputDir, final String lang) throws IOException {
        final File tempFile = File.createTempFile("temp", "proto");
        final FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile);
        pipeStream(protoStream, tempFileOutputStream);
        protoStream.close();
        tempFileOutputStream.flush();
        tempFileOutputStream.close();
        compileProtoToLanguage(Arrays.asList(tempFile.getPath()), Arrays.asList(tempFile.getParent()), outputDir, lang);
    }

    private void compileProtoToLanguage(final List<String> protoFiles, final List<String> searchDirs, final Path outputDir, final String lang) throws IOException {
        final HashSet<String> protoFileSet = new HashSet<>();
        final HashSet<String> searchDirSet = new HashSet<>();
        for (final String protoFile: protoFiles) {
            Utils.expandWildcardPath(protoFile, protoFileSet);
        }
        for (final String searchDir: searchDirs) {
            Utils.expandWildcardPath(searchDir, searchDirSet);
        }
        final ArrayList<String> commandParts = new ArrayList<>();
        commandParts.add("protoc");
        for (final String searchDir: searchDirSet) {
            commandParts.add("--proto_path="+searchDir);
        }
        commandParts.add("--"+lang+"_out="+outputDir);
        for (final String protoFile: protoFileSet) {
            commandParts.add(protoFile);
        }
        //System.out.println(commandParts);
        final ProcessBuilder builder = new ProcessBuilder(commandParts);
        final Process p = builder.inheritIO().start();
        try {
            p.waitFor();
            if (p.exitValue() != 0) {
                throw new RuntimeException("protoc returned exit code: " + p.exitValue());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void compileProtoToDescriptor(final List<String> protoFiles, final List<String> searchDirs, final String outputFile) throws IOException {
        final HashSet<String> protoFileSet = new HashSet<>();
        final HashSet<String> searchDirSet = new HashSet<>();
        for (final String protoFile: protoFiles) {
            Utils.expandWildcardPath(protoFile, protoFileSet);
        }
        for (final String searchDir: searchDirs) {
            Utils.expandWildcardPath(searchDir, searchDirSet);
        }
        final ArrayList<String> commandParts = new ArrayList<>();
        commandParts.add("protoc");
        for (final String searchDir: searchDirSet) {
            commandParts.add("--proto_path="+searchDir);
        }
        commandParts.add("--descriptor_set_out="+outputFile);
        for (final String protoFile: protoFileSet) {
            commandParts.add(protoFile);
        }
        //System.out.println(commandParts);
        final ProcessBuilder builder = new ProcessBuilder(commandParts);
        final Process p = builder.inheritIO().start();
        try {
            p.waitFor();
            if (p.exitValue() != 0) {
                throw new RuntimeException("protoc returned exit code: " + p.exitValue());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void compileProtosToJavascript(final List<String> inputFiles, final List<String> searchDirs) throws IOException {
        final File tempFile = File.createTempFile("protoc_output", "desc");
        compileProtoToDescriptor(inputFiles, searchDirs, tempFile.getPath());
        final FileInputStream inputStream = new FileInputStream(tempFile);
        compileDescriptor(inputStream);
        inputStream.close();
        tempFile.delete();
    }

    private void compileProtoStreamToJavascript(final InputStream in) throws IOException{
        final File tempFile = File.createTempFile("temp", "proto");
        final FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile);
        pipeStream(in, tempFileOutputStream);
        tempFileOutputStream.flush();
        tempFileOutputStream.close();
        compileProtosToJavascript(Arrays.asList(tempFile.getPath()), Arrays.asList(tempFile.getParent()));
        tempFile.delete();
    }

    private void compileDescriptor(final InputStream input) throws IOException {

        final DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(input, extensionRegistry);

        final CodeGenerator generator = factory.createGenerator(descriptorSet);

        if (options.isModeOutputDir()) {

            final Path outputDir = options.getOutputDir();

            System.out.println("Compiling " + descriptorSet.getFileCount() + " proto files to " + outputDir);

            for (final DescriptorProtos.FileDescriptorProto proto: descriptorSet.getFileList()) {
                final SectionBuffer buffer = new SectionBuffer();
                generator.processProtoFile(proto, buffer);
                final File outputFile = factory.getOutputFile(proto, outputDir).toFile();
                System.out.println(proto.getName() + " -> " + outputFile.toString());
                outputFile.getParentFile().mkdirs();
                final FileWriter writer = new FileWriter(outputFile);
                buffer.writeTo(writer);
                writer.flush();
                writer.close();
            }

        } else {
            final OutputStream outputStream = options.getOutputStream();
            final SectionBuffer buffer = new SectionBuffer();
            for (final DescriptorProtos.FileDescriptorProto proto : descriptorSet.getFileList()) {
                generator.processProtoFile(proto, buffer);
            }
            final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            buffer.writeTo(writer);
            writer.flush();
            writer.close();
        }

    }

    private static void pipeStream(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[1024];
        int n;
        while ((n = in.read(buffer)) > -1) {
            out.write(buffer, 0, n);
        }
    }


    public static void main(final String[] args) {
        final Compiler compiler = new Compiler();
        final CompilerOptions options = new CompilerOptions();
        try {
            final JCommander commander = new JCommander(options, args);
            if (options.getShowArgumentHelp()) {
                commander.usage();
            } else {
                compiler.setOptions(options);
                compiler.run();
            }
        } catch (ParameterException e) {
            System.err.println("ERROR: Invalid parameters: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ERROR: Exception caught: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

}
