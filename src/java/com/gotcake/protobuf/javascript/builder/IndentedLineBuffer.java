package com.gotcake.protobuf.javascript.builder;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by aaron on 12/1/14.
 */
public class IndentedLineBuffer implements LineWritable {

    private final StringBuilder indentString;
    private ArrayList<String> lines;
    private StringBuilder currentLine;
    private final int indentSize;

    public IndentedLineBuffer(final int indentSize) {
        indentString = new StringBuilder();
        currentLine = new StringBuilder();
        lines = new ArrayList<>();
        this.indentSize = Math.max(0, indentSize);
    }

    public IndentedLineBuffer() {
        this(4);
    }

    public IndentedLineBuffer in() {
        for (int i = 0; i < indentSize; i++) {
            indentString.append(' ');
        }
        return this;
    }

    public IndentedLineBuffer out() {
        final int newSize = Math.max(0, indentString.length() - indentSize);
        indentString.setLength(newSize);
        return this;
    }

    public IndentedLineBuffer resetIndent() {
        indentString.setLength(0);
        return this;
    }

    public IndentedLineBuffer write(final Object... values) throws IOException {
        if (values.length > 0 && currentLine.length() == 0 && indentString.length() > 0) {
            currentLine.append(indentString);
        }
        for (final Object value: values) {
            currentLine.append(value.toString());
        }
        return this;
    }

    public IndentedLineBuffer line(final Object... values) throws IOException {
        write(values);
        currentLine.append("\n");
        lines.add(currentLine.toString());
        currentLine.setLength(0);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return lines.isEmpty() && currentLine.length() == 0;
    }

    public void writeTo(final IndentedLineBuffer buffer) throws IOException {
        for (final String line: lines) {
            buffer.line(line);
        }
        if (currentLine.length() > 0) {
            buffer.write(currentLine.toString());
        }
    }

    public void writeTo(final Writer writer) throws IOException {
        for (final String line: lines) {
            writer.write(line);
        }
        if (currentLine.length() > 0) {
            writer.write(currentLine.toString());
        }
    }

}
