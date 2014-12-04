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

