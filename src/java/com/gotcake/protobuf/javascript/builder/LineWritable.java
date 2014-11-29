package com.gotcake.protobuf.javascript.builder;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by aaron on 12/1/14.
 */
public interface LineWritable {

    public boolean isEmpty();

    public void writeTo(final IndentedLineBuffer buffer) throws IOException;

}
