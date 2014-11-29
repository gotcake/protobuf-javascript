goog.require('protolib.Buffer');
good.require('protolib.WireTypes');

goog.provide('protolib.Message');

protolib.Message = function() {};

protolib.Message.prototype.init = function() {
    this.constructor.call(this);
};

protolib.Message.prototype.decodeFieldCallback = function() {
    throw Error("must override");
};

protolib.Message.prototype.decode = function(source, opt_lengthDelimited) {
    if (!(source instanceof protolib.Buffer)) {
        source = new protolib.Buffer(source);
    }
    var end = opt_lengthDelimited ? source.offset + (source.readVarint32() | 0) : source.end;
    while (source.offset < end) {
        var tag = source.readVarint32() | 0;
        var ret = this.decodeFieldCallback(tag, source);
        if (ret === false) {
            protolib.Message.skipField_(tag, source);
        } else if (typeof ret === 'number') {
            this.decodePackedField_(ret, source);
        }
    }
    if (source.offset > end) {
        throw Error("message contents too long");
    }
};

protolib.Message.prototype.decodePackedField_ = function(tag, source) {
    var end = source.offset + (source.readVarint32() | 0);
    while (source.offset < end) {
        this.decodeFieldCallback(tag, source);
    }
    if (source.offset > end) {
        throw Error("packed fields too long");
    }
};

protolib.Message.skipField_ = function(tag, source) {
    var wireType = tag & 0x07;
    switch (wireType) {

    };
};

