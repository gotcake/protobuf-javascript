package com.gotcake.protobuf.javascript;

import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;

import java.io.*;
import java.util.Map;

/**
 * Created by aaron on 12/1/14.
 */
public interface JavascriptGenerator {

    public boolean supportsMultipleOutputFiles();

    public void enableDebug(final PrintStream debugWriter);

    public void setExtraArgs(final Map<String, String> extraArgs);

    public void processProtoFile(final DescriptorProtos.FileDescriptorProto protoFile, final SectionBuffer outputBuffer) throws IOException;

    public String getRelativeOutputPathFor(final DescriptorProtos.FileDescriptorProto protoFile);

}
