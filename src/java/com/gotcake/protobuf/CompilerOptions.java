package com.gotcake.protobuf;

import com.beust.jcommander.DynamicParameter;
import com.beust.jcommander.Parameter;
import com.gotcake.protobuf.closure.ClosureJavascriptGenerator;
import com.gotcake.protobuf.closure.ClosureJavascriptGenerator;
import com.gotcake.protobuf.closure.ClosureJavascriptGeneratorFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * A programmatic interface for the compiler command line options.
 */
public class CompilerOptions {

    @Parameter(names = { "-i", "--input" }, variableArity = true, description = "Specify one or more input proto files.")
    private List<String> inputFiles = new ArrayList<>();

    @Parameter(names = { "-I", "--searchDir" }, variableArity = true, description = "Specify one or more search dirs for imported proto files.")
    private List<String> searchDirs = new ArrayList<>();

    @Parameter(names = { "-p", "--inputDescriptor" }, description = "Specify a file to read descriptors from.")
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

    @Parameter(names = { "--debug" }, description = "Enable debugging output")
    private boolean debug = false;

    @Parameter(names = { "-h", "--help" }, description = "Display usage message.", help = true)
    private boolean help = false;

    @Parameter(names = { "-f", "--outputFormat" }, description = "Specify an output format by class name. Defaults to the closure output format.")
    private String outputFormat = ClosureJavascriptGeneratorFactory.class.getCanonicalName();

    @Parameter(names = { "--lang" }, description = "Specify the output language (using native protoc)")
    private String outputLang = null;

    @DynamicParameter(names = { "-E" }, description = "Specify an extra parameter for the output format. -Ekey=value")
    private Map<String, String> extraArgs = new HashMap<>();

    @Parameter(names = { "--support" }, variableArity = true, description = "Specify one or more features to support")
    private List<String> support;

    private InputStream customInputStream;
    private boolean customInputStreamIsDescriptor;

    /**
     * Validates the current options, checking for required and mutually-exclusive options.
     * @return null if the options are valid, or a String containing an error message if the options are invalid.
     */
    public String validate() {

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
        if (customInputStream != null)
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

        if (outputLang != null) {
            if (outputFile != null)
                return "Native java/python compilation cannot be used with the --outputFile option";
            if (stdout)
                return "Native java/python compilation cannot be used with the --stdout option";
        }

        return null;
    }

    public void setInputProtoFiles(final List<String> inputProtoFiles) {
        this.inputFiles = inputProtoFiles;
    }

    public void setInputProtoFiles(final String... inputProtoFiles) {
        setInputProtoFiles(Arrays.asList(inputProtoFiles));
    }

    public void setInputDescriptorFile(final String inputDescriptorFile) {
        this.inputDescriptor = inputDescriptorFile;
    }

    public void setOutputFile(final String outputFile) {
        this.outputFile = outputFile;
    }

    public void setOutputDir(final String outputDir) {
        this.outputDir = outputDir;
    }

    public void setCustomInputProtoStream(final InputStream is) {
        this.customInputStreamIsDescriptor = false;
        this.customInputStream = is;
    }

    public void setCustomInputDescriptorStream(final InputStream is) {
        this.customInputStreamIsDescriptor = true;
        this.customInputStream = is;
    }

    public void setOutputLanguage(final String nativeLang) {
        this.outputLang = nativeLang;
    }

    public void setOutputGeneratorClass(final Class<? extends CodeGenerator> generatorClass) {
        this.outputFormat = generatorClass.getCanonicalName();
    }

    public void setOutputGeneratorClassName(final String generatorClassName) {
        this.outputFormat = generatorClassName;
    }

    public void setReadDescriptorFromStdin(final boolean readDescriptorFromStdin) {
        this.stdinDescriptor = readDescriptorFromStdin;
    }

    public void setReadProtoFileFromStdin(final boolean readProtoFileFromStdin) {
        this.stdin = readProtoFileFromStdin;
    }

    public void showArgumentHelp(boolean showArgumentHelp) {
        this.help = showArgumentHelp;
    }

    public void setExtraArgs(final Map<String, String> extraArgs) {
        this.extraArgs.clear();
        this.extraArgs.putAll(extraArgs);
    }

    public void setExtraArg(final String key, final String value) {
        this.extraArgs.put(key, value);
    }

    public void setProtoImportSearchDirs(final List<String> protoImportSearchDirs) {
        this.searchDirs = protoImportSearchDirs;
    }

    public void setProtoImportSearchDirs(final String... protoImportSearchDirs) {
        setProtoImportSearchDirs(Arrays.asList(protoImportSearchDirs));
    }

    public Map<String, String> getExtraArgs() {
        return this.extraArgs;
    }

    public boolean getShowArgumentHelp() {
        return help;
    }

    @SuppressWarnings("unchecked")
    public Class<? extends CodeGeneratorFactory> getCodeGeneratorFactoryClass() throws ClassNotFoundException, ClassCastException {
        if (outputFormat == null)
            return ClosureJavascriptGeneratorFactory.class;
        Class<?> clazz = Class.forName(outputFormat);
        if (!CodeGeneratorFactory.class.isAssignableFrom(clazz))
            throw new ClassCastException("Class "+clazz.getCanonicalName()+" is not a subclass of "+CodeGeneratorFactory.class.getCanonicalName());
        return (Class<? extends CodeGeneratorFactory>) clazz;
    }

    public InputStream getProtoInputStream() throws IOException {
        if (stdin) {
            return System.in;
        } else if (customInputStream != null && !customInputStreamIsDescriptor) {
            return customInputStream;
        } else {
            return null;
        }
    }

    public InputStream getDescriptorInputStream() throws IOException {
        if (stdinDescriptor) {
            return System.in;
        } else if (customInputStream != null && customInputStreamIsDescriptor) {
            return customInputStream;
        } else if (inputDescriptor != null) {
            return new FileInputStream(inputDescriptor);
        } else {
            return null;
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean getDebug() {
        return debug;
    }

    public OutputStream getOutputStream() throws IOException {
        if (stdout) {
            return System.out;
        } else if (outputFile != null) {
            final File f = new File(outputFile);
            f.getParentFile().mkdirs();
            return new FileOutputStream(f);
        } else {
            return null;
        }
    }

    public Path getOutputDir() {
        return Paths.get(outputDir);
    }

    public String getOutputLanguage() {
        return outputLang;
    }

    public List<String> getInputProtoFiles() {
        return inputFiles;
    }

    public List<String> getProtoImportSearhDirs() {
        return searchDirs;
    }

    public boolean isModeOutputDir() {
        return outputDir != null;
    }

    public boolean isModeReadProtoStream() {
        return stdin || (customInputStream != null && !customInputStreamIsDescriptor);
    }

    public boolean isModeReadDescriptorStream() {
        return stdinDescriptor || inputDescriptor != null || (customInputStream != null && customInputStreamIsDescriptor);
    }

    public boolean isModeReadProtoFiles() {
        return !inputFiles.isEmpty();
    }

    public boolean isModeOutputLanguageNative() {
        return outputLang != null;
    }
    

}
