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

package com.gotcake.protobuf.javascript.builder;


import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * A class that provides the ability to create nested, indented sections of code easily.
 * @author Aaron Cake
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
