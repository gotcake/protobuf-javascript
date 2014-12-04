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

package com.gotcake.protobuf.javascript.closure;

import com.google.protobuf.DescriptorProtos;
import com.gotcake.protobuf.javascript.Utils;
import com.gotcake.protobuf.javascript.builder.SectionBuffer;

import java.io.IOException;

/**
 * A delegate class which processes enum definitions and generates the appropriate javascript
 * @author Aaron Cake
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
