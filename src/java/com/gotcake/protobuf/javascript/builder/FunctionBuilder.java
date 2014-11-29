package com.gotcake.protobuf.javascript.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by aaron on 12/1/14.
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
