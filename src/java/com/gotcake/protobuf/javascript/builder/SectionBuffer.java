package com.gotcake.protobuf.javascript.builder;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by aaron on 12/1/14.
 */
public class SectionBuffer implements LineWritable {

    private final TreeMap<Integer, ArrayList<LineWritable>> contents = new TreeMap<>();

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

    public void clearSection(final Enum<?> section) {
        this.clearSection(section.ordinal());
    }

    public void clearAll(final int section) {
        this.contents.clear();
    }

    @Override
    public boolean isEmpty() {
        return contents.isEmpty();
    }

    @Override
    public void writeTo(final IndentedLineBuffer buffer) throws IOException {
        for (final int section: contents.keySet()) {
            for (final LineWritable writable: contents.get(section)) {
                writable.writeTo(buffer);
            }
        }
    }

    public void writeTo(final Writer writer) throws IOException {
        final IndentedLineBuffer buffer = new IndentedLineBuffer();
        writeTo(buffer);
        buffer.writeTo(writer);
    }



}
