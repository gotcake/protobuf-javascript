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

package com.gotcake.protobuf.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * A class which allows the generation of sections of code to be out of order and acts as a code builder
 * @author Aaron Cake
 */
public class SectionBuffer implements LineWritable {

    private final TreeMap<Integer, ArrayList<LineWritable>> contents;
    private final boolean indented;

    public SectionBuffer() {
        this(false);
    }

    public SectionBuffer(final boolean indented) {
        this.indented = indented;
        this.contents = new TreeMap<>();
    }

    public SectionBuffer section(final int section, final LineWritable content) {
        if (content != null) {
            ArrayList<LineWritable> lineWritables = contents.get(section);
            if (lineWritables == null) {
                lineWritables = new ArrayList<>();
                contents.put(section, lineWritables);
            }
            lineWritables.add(content);
        }
        return this;
    }

    public SectionBuffer section(final Enum<?> section, final LineWritable content) {
        return this.section(section.ordinal(), content);
    }

    public SectionBuffer stringSection(final int section, final String string) {
        this.section(section, new StringLineWritable(string));
        return this;
    }

    public SectionBuffer stringSection(final Enum<?> section, final String string) {
        return this.stringSection(section.ordinal(), string);
    }

    public SectionBuffer stringSection(final int section, final Object... strings) {
        this.section(section, new StringLineWritable(strings));
        return this;
    }

    public SectionBuffer stringSection(final Enum<?> section, final Object... strings) {
        return this.stringSection(section.ordinal(), strings);
    }

    public IndentedLineBuffer lineBufferSection(final int section) {
        final IndentedLineBuffer buffer = new IndentedLineBuffer();
        this.section(section, buffer);
        return buffer;
    }

    public IndentedLineBuffer lineBufferSection(final Enum<?> section) {
        return this.lineBufferSection(section.ordinal());
    }

    public FunctionBuilder functionSection(final int section) {
        final FunctionBuilder builder = new FunctionBuilder();
        this.section(section, builder);
        return builder;
    }

    public FunctionBuilder functionSection(final Enum<?> section) {
        return this.functionSection(section.ordinal());
    }

    public DocBuilder docSection(final int section) {
        final DocBuilder builder = new DocBuilder();
        this.section(section, builder);
        return builder;
    }

    public DocBuilder docSection(final Enum<?> section) {
        return this.docSection(section.ordinal());
    }


    public SectionBuffer childSection(final int section) {
        final SectionBuffer child = new SectionBuffer();
        this.section(section, child);
        return child;
    }

    public SectionBuffer childSection(final Enum<?> section) {
        return this.childSection(section.ordinal());
    }

    public void clearSection(final int section) {
        if (this.contents.containsKey(section)) {
            this.contents.remove(section);
        }
    }

    public SectionBuffer indentedChildSection(final int section) {
        final SectionBuffer child = new SectionBuffer(true);
        this.section(section, child);
        return child;
    }

    public SectionBuffer indentedChildSection(final Enum<?> section) {
        return this.indentedChildSection(section.ordinal());
    }

    public void clearSection(final Enum<?> section) {
        this.clearSection(section.ordinal());
    }

    public void clearAll() {
        this.contents.clear();
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public void writeTo(final IndentedLineBuffer buffer) throws IOException {
        if (indented)
            buffer.in();
        for (final int section: contents.keySet()) {
            for (final LineWritable writable: contents.get(section)) {
                writable.writeTo(buffer);
            }
        }
        if (indented)
            buffer.out();
    }

    public void writeTo(final Writer writer) throws IOException {
        final IndentedLineBuffer buffer = new IndentedLineBuffer();
        writeTo(buffer);
        buffer.writeTo(writer);
    }



}
