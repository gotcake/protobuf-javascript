package com.gotcake.protobuf.javascript;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistry;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;
import com.gotcake.protobuf.javascript.closure.ClosureJavascriptGenerator;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Created by aaron on 11/29/14.
 */
public class Compiler {

    @Parameter(names = { "-i", "--input" }, variableArity = true, description = "Specify one or more input proto files.")
    private ArrayList<String> inputFiles = new ArrayList<>();

    @Parameter(names = { "-I", "--searchDir" }, variableArity = true, description = "Specify one or more search dirs for imported proto files.")
    private ArrayList<String> searchDirs = new ArrayList<>();

    @Parameter(names = { "-p", "--inputDescriptor" }, description = "Specify a files to read descriptors from.")
    private String inputDescriptor = null;

    @Parameter(names = { "-o", "--outputFile" }, description = "Specify to output all generated code to a single file.")
    private String outputFile = null;

    @Parameter(names = { "-d", "--outputDir" }, description = "Specify to write individual files for each descriptor.")
    private String outputDir = null;

    @Parameter(names = { "--stdinDescriptor" }, description = "Specify to read descriptors from stdin.")
    private boolean stdinDescriptor = false;

    @Parameter(names = { "--stdin" }, description = "Specify to read a proto file from stdin.")
    private boolean stdin = false;

    @Parameter(names = { "--stdout" }, description = "Specify to write all generated code to standard out.")
    private boolean stdout = false;

    @Parameter(names = { "--debug" }, description = "Enable debugging output. Writes to stderr if --stdout was specified.")
    private boolean debug = false;

    @Parameter(names = { "-h", "--help" }, description = "Display usage message.", help = true)
    private boolean help = false;

    @Parameter(names = { "-f", "--outputFormat" }, description = "Specify an output format by class name. Defaults to the closure output format.")
    private String outputFormat = ClosureJavascriptGenerator.class.getCanonicalName();

    @DynamicParameter(names = { "-E" }, description = "Specify an extra parameter for the output format. -Ekey=value")
    private HashMap<String, String> extraArgs = new HashMap<>();

    private OutputStream outputStream = null;

    private ExtensionRegistry extensionRegistry;

    public String validateArgs() {

        // validate input source
        int inputCounts = 0;
        if (!inputFiles.isEmpty())
            inputCounts++;
        if (inputDescriptor != null)
            inputCounts++;
        if (stdin)
            inputCounts++;
        if (stdinDescriptor)
            inputCounts++;

        if (inputCounts == 0)
            return "Must specify at least one input source.";
        if (inputCounts > 1)
            return "Please specify only one type of input source.";


        // validate output target
        int outputCounts = 0;
        if (outputDir != null)
            outputCounts++;
        if (outputFile != null)
            outputCounts++;
        if (stdout)
            outputCounts++;

        if (outputCounts == 0)
            return "Must specify an output target.";
        if (outputCounts > 1)
            return "Please specify only one output target.";

        return null;
    }

    public void run() throws ClassNotFoundException, IllegalAccessException, InstantiationException, IOException {
        final Class<?> formatClass = Class.forName(outputFormat);
        final JavascriptGenerator format = (JavascriptGenerator)formatClass.newInstance();
        if (debug) {
            format.enableDebug(stdout ? System.err : System.out);
        }
        format.setExtraArgs(extraArgs);

        extensionRegistry = ExtensionRegistry.newInstance();

        JavascriptOptionProtos.registerAllExtensions(extensionRegistry);

        if (outputFile != null) {
            outputStream = new FileOutputStream(outputFile);
        } else if (stdout) {
            outputStream = System.out;
        } else if (!format.supportsMultipleOutputFiles()) {
            throw new RuntimeException("Given output format does not support multiple output files.");
        }

        if (!inputFiles.isEmpty()) {
            compileProtosToJavascript(inputFiles, searchDirs, format);
        } else if (stdin) {
            compileProtoStreamToJavascript(System.in, format);
        } else if (stdinDescriptor) {
            compileDescriptor(System.in, format);
        } else if (inputDescriptor != null) {
            final FileInputStream in = new FileInputStream(inputDescriptor);
            compileDescriptor(System.in, format);
            in.close();
        }
    }

    private void compileProtoToDescriptor(final List<String> protoFiles, final List<String> searchDirs, final String outputFile) throws IOException {
        final HashSet<Path> protoFileSet = new HashSet<>();
        final HashSet<Path> searchDirSet = new HashSet<>();
        for (final String protoFile: protoFiles) {
            Utils.expandWildcardPath(protoFile, protoFileSet);
        }
        for (final String searchDir: searchDirs) {
            Utils.expandWildcardPath(searchDir, searchDirSet);
        }
        final ArrayList<String> commandParts = new ArrayList<>();
        commandParts.add("protoc");
        for (final Path searchDir: searchDirSet) {
            commandParts.add("--proto_path="+searchDir.toString());
        }
        commandParts.add("--descriptor_set_out="+outputFile);
        for (final Path protoFile: protoFileSet) {
            commandParts.add(protoFile.toString());
        }
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

    private void compileProtosToJavascript(final List<String> inputFiles, final List<String> searchDirs, final JavascriptGenerator format) throws IOException {
        final File tempFile = File.createTempFile("protoc_output", "desc");
        compileProtoToDescriptor(inputFiles, searchDirs, tempFile.getPath());
        final FileInputStream inputStream = new FileInputStream(tempFile);
        compileDescriptor(inputStream, format);
        inputStream.close();
        tempFile.delete();
    }

    private void compileProtoStreamToJavascript(final InputStream in, final JavascriptGenerator format) throws IOException{
        final File tempFile = File.createTempFile("temp", "proto");
        final FileOutputStream tempFileOutputStream = new FileOutputStream(tempFile);
        pipeStream(in, tempFileOutputStream);
        tempFileOutputStream.flush();
        tempFileOutputStream.close();
        compileProtosToJavascript(Arrays.asList(tempFile.getPath()), Arrays.asList(tempFile.getParent()), format);
        tempFile.delete();
    }

    private void compileDescriptor(final InputStream input, final JavascriptGenerator format) throws IOException {

        final DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(input, extensionRegistry);

        if (format.supportsMultipleOutputFiles() && outputDir != null) {

            for (final DescriptorProtos.FileDescriptorProto proto: descriptorSet.getFileList()) {
                final SectionBuffer buffer = new SectionBuffer();
                format.processProtoFile(proto, buffer);
                final FileWriter writer = new FileWriter(outputDir + "/" + format.getRelativeOutputPathFor(proto));
                buffer.writeTo(writer);
                writer.flush();
                writer.close();
            }

        } else if (outputStream != null) {
            final SectionBuffer buffer = new SectionBuffer();
            for (final DescriptorProtos.FileDescriptorProto proto: descriptorSet.getFileList()) {
                format.processProtoFile(proto, buffer);
            }
            final OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            buffer.writeTo(writer);
            writer.flush();
            writer.close();
        } else {
            throw new RuntimeException("Output not specified.");
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
        try {
            final JCommander commander = new JCommander(compiler, args);
            final String errMsg = compiler.validateArgs();
            if (errMsg != null) {
                System.err.println("ERROR: " + errMsg);
            } else if (compiler.help) {
                commander.usage();
            } else {
                try {
                    compiler.run();
                } catch (ClassNotFoundException e) {
                    System.err.println("ERROR: Could not find output format class '" + compiler.outputFormat + "'.");
                    System.exit(1);
                } catch (InstantiationException | IllegalAccessException  e) {
                    System.err.println("ERROR: Failed to instantiate class '" + compiler.outputFormat + "'.");
                    if (compiler.debug) {
                        e.printStackTrace(System.err);
                    } else{
                        System.err.println("ERROR: Re-run with --debug for more info.");
                    }
                    System.exit(1);
                } catch (IOException | RuntimeException e) {
                    System.err.println("ERROR: Exception caught: " + e.getMessage());
                    if (compiler.debug) {
                        e.printStackTrace(System.err);
                    } else{
                        System.err.println("ERROR: Re-run with --debug for more info.");
                    }
                    System.exit(1);
                }

            }
        } catch (ParameterException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

}