package com.gotcake.protobuf.javascript.closure;

import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.javascript.Utils;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;

import java.io.IOException;

/**
 * Created by aaron on 12/3/14.
 */
public class MessageJavascriptGenerator {

    public void processMessage(final DescriptorProtos.DescriptorProto descriptor,
                                final String javascriptName,
                                final SectionBuffer globalBuffer,
                                final SectionBuffer buffer) throws IOException {

        globalBuffer.lineBufferSection(GlobalSection.Provides)
                .line("goog.provide('", javascriptName, "');");

        buffer.lineBufferSection(MessageSection.Constructor).line("// message ", javascriptName, " goes here");

    }

}
