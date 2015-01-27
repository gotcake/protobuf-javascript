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

goog.require('gotcake.proto.Buffer');
goog.require('gotcake.proto.WireTypes');

goog.provide('gotcake.proto.Message');

/**
 * The base class for all message types
 * @constructor
 */
gotcake.proto.Message = function() {};

/**
 * Re-initializes the Message to default values by calling the constructor
 */
gotcake.proto.Message.prototype.init = function() {
    this.constructor.call(this);
};

/**
 * A method to be overridden by subclasses to decode individual fields from the source buffer
 * @param {number} tag the tag of the field to decode
 * @param {gotcake.proto.Buffer} buffer the source buffer
 * @return {number|boolean|undefined} a number if the type is length-delimited,
 *      false if it is unrecognized, or undefined if was decoded successfully
 * @protected
 */
gotcake.proto.Message.prototype.decodeFieldCallback = function(tag, buffer) {
    throw Error("decoding not supported");
};

/**
 * A method to be overridden by subclasses to encode their data to a buffer
 * @param {gotcake.proto.Buffer} the buffer to write encoded data to
 */
gotcake.proto.Message.prototype.encode = function(buffer) {
    throw Error("encoding not supported");
};

/**
 * A method to be overridden by subclasses with required properties to check if all required properties are set
 * @return {boolean} true if all required properties are set, false otherwise
 */
gotcake.proto.Message.prototype.isInitialized = function() {
    return true;
};

/**
 * Decodes a message from the given source
 * @param {string|ArrayBuffer|gotcake.proto.Buffer} source
 * @param {boolean=} opt_lengthDelimited specify true to decode a length-delimited message
 */
gotcake.proto.Message.prototype.decode = function(source, opt_lengthDelimited) {
    if (!(source instanceof gotcake.proto.Buffer)) {
        source = new gotcake.proto.Buffer(source);
    }
    var end = opt_lengthDelimited ? source.offset + source.readVarint32() : source.end;
    while (source.offset < end) {
        var tag = source.readVarint32();
        var ret = this.decodeFieldCallback(tag, source);
        if (ret === false) {
            gotcake.proto.Message.skipField_(tag & 0x07, source);
        } else if (typeof ret === 'number') {
            this.decodePackedField_(ret, source);
        }
    }
    return this;
};

/**
 * Decodes a packed field
 * @param {number} tag the field tag
 * @param {gotcake.proto.Buffer} source the source buffer
 * @private
 */
gotcake.proto.Message.prototype.decodePackedField_ = function(tag, source) {
    var end = source.readVarint32() + source.offset; // order matters here since readVarint32 updates the offset
    while (source.offset < end) {
        this.decodeFieldCallback(tag, source);
    }
    if (source.offset > end) {
        throw Error("packed fields too long");
    }
};

/**
 * Skips over the next field, given the tag of the field
 * @param {number} wireType the wireType of the field
 * @param {gotcake.proto.Buffer} source the source buffer
 * @private
 */
gotcake.proto.Message.skipField_ = function(wireType, source) {
    switch (wireType) {
        case gotcake.proto.WireTypes.VARINT:
            source.readVarint32(); break;
        case gotcake.proto.WireTypes.FIXED32:
            source.skip(4); break;
        case gotcake.proto.WireTypes.FIXED64:
            source.skip(8); break;
        case gotcake.proto.WireTypes.LENGTH_DELIMITED:
            source.skip(source.readVarint32()); break;
        case gotcake.proto.WireTypes.GROUP_START:
            gotcake.proto.Message.skipGroup_(source); break;
        default:
            throw Error("unrecognized wire type " + wireType);
    }
};

/**
 * Skips over the next field, given the tag of the field
 * @param {gotcake.proto.Buffer} source the source buffer
 * @private
 */
gotcake.proto.Message.skipGroup_ = function(source) {
    var wireType;
    while((wireType = source.readVarint32() & 0x07) !== gotcake.proto.WireTypes.GROUP_END) {
        gotcake.proto.Message.skipField_(wireType, source);
    }
};


// elizabeth



