package com.gotcake.protobuf.javascript.builder;

import java.io.IOException;

/**
 * Created by aaron on 12/2/14.
 */
public class StringLineWritable implements LineWritable {

    private final String string;

    public StringLineWritable(final String string) {
        this.string = string;
    }

    @Override
    public boolean isEmpty() {
        return string.isEmpty();
    }

    @Override
    public void writeTo(final IndentedLineBuffer buffer) throws IOException {
        buffer.write(string);
    }
}
