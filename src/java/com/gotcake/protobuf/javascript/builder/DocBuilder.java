package com.gotcake.protobuf.javascript.builder;

import com.beust.jcommander.Parameter;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

/**
 * Created by aaron on 12/1/14.
 */
public class DocBuilder implements LineWritable {

    private static final int ACCESS_PUBLIC = 0;
    private static final int ACCESS_PROTECTED = 1;
    private static final int ACCESS_PRIVATE = 2;

    private ArrayList<Parameter> parameterList = new ArrayList<>();
    private int accessModifier = ACCESS_PUBLIC;
    private String description = null;
    private String enumType = null;
    private String type = null;
    private String returnType = null;
    private String extendsType = null;
    private boolean constructor = false;

    public DocBuilder setPublic() {
        this.accessModifier = ACCESS_PUBLIC;
        return this;
    }

    public DocBuilder setProtected() {
        this.accessModifier = ACCESS_PROTECTED;
        return this;
    }

    public DocBuilder setPrivate() {
        this.accessModifier = ACCESS_PRIVATE;
        return this;
    }

    public DocBuilder setExtendsType(final String extendsType) {
        this.extendsType = extendsType;
        return this;
    }

    public DocBuilder setDescription(final String description) {
        this.description = description;
        return this;
    }

    public DocBuilder setEnumType(final String enumType) {
        this.enumType = enumType;
        return this;
    }

    public DocBuilder setType(final String type) {
        this.type = type;
        return this;
    }

    public DocBuilder setReturnType(final String returnType) {
        this.returnType = returnType;
        return this;
    }

    public DocBuilder setConstructor(final boolean constructor) {
        this.constructor = constructor;
        return this;
    }

    public DocBuilder addParameter(final String name, final String type, final String description) {
        parameterList.add(new Parameter(name, type, description));
        return this;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void writeTo(final IndentedLineBuffer buffer) throws IOException {
        buffer.line("/**");
        if (description != null)
            buffer.line(" * ", description);
        for (Parameter p: parameterList) {
            buffer.write(" * ");
            if (p.type != null)
                buffer.write("{", type, "} ");
            buffer.write(p.name);
            if (p.description != null)
                buffer.write(" ", description);
            buffer.line();
        }
        if (type != null)
            buffer.line(" * @type {", type, "}");
        if (enumType != null)
            buffer.line(" * @enum {", enumType, "}");
        if (returnType != null)
            buffer.line(" * @return {", returnType, "}");
        if (constructor)
            buffer.line(" * @constructor");
        if (accessModifier == ACCESS_PRIVATE)
            buffer.line(" * @private");
        else if (accessModifier == ACCESS_PROTECTED)
            buffer.line(" * @protected");
        if (extendsType != null)
            buffer.line(" * @extends {", extendsType, "}");
        buffer.line(" */");
    }


    private static class Parameter {

        public final String name;
        public final String type;
        public final String description;

        public Parameter(final String name, final String type, final String description) {
            this.name = name;
            this.type = type;
            this.description = description;
        }

    }
}
