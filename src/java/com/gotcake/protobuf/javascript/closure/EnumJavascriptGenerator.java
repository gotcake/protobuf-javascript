package com.gotcake.protobuf.javascript.closure;

import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.javascript.Utils;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;

import java.io.IOException;

/**
 * Created by aaron on 12/3/14.
 */
public class EnumJavascriptGenerator {

    public void processEnum(final DescriptorProtos.EnumDescriptorProto descriptor,
                             final String javascriptName,
                             final SectionBuffer globalBuffer,
                             final SectionBuffer buffer) throws IOException {

        globalBuffer.lineBufferSection(GlobalSection.Provides)
                .line("goog.provide('", javascriptName, "');");


        buffer.lineBufferSection(EnumSecton.Body).line("// enum ", javascriptName, " goes here");

    }
}
