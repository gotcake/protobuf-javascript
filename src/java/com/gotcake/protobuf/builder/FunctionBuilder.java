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
import java.util.ArrayList;
import java.util.List;

/**
 * A builder to generate functions
 * @author Aaron Cake
 */
public class FunctionBuilder implements LineWritable {

    private final SectionBuffer body = new SectionBuffer();

    private String name = null;
    private ArrayList<String> parameters = new ArrayList<>();
    private boolean endWithNewline = false;

    public SectionBuffer body() {
        return body;
    }

    public FunctionBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public FunctionBuilder setEndWithNewline(final boolean endWithNewline) {
        this.endWithNewline = endWithNewline;
        return this;
    }

    public FunctionBuilder addParameter(final String name) {
        this.parameters.add(name);
        return this;
    }

    public FunctionBuilder addParameters(final String... names) {
        for (final String name: names) {
            this.parameters.add(name);
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void writeTo(IndentedLineBuffer buffer) throws IOException {
        if (name != null) {
            buffer.write("function ", name, "(");
            for (int i = 0; i < parameters.size(); i++) {
                if (i > 0) {
                    buffer.write(", ");
                }
                buffer.write(parameters.get(i));
            }
        } else {
            buffer.write("function(");
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                buffer.write(", ");
            }
            buffer.write(parameters.get(i));
        }
        buffer.line(") {").in();
        body.writeTo(buffer);
        buffer.out().write('}');
        if (endWithNewline) {
            buffer.line();
        }

    }
}
