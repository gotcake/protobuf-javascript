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

goog.provide("gotcake.proto.Buffer");

// see https://github.com/dcodeIO/ByteBuffer.js/blob/master/dist/ByteBufferAB.js

/**
 * The
 * @param opt_arrayBufferOrSize
 * @param opt_byteOffset
 * @constructor
 */
gotcake.proto.Buffer = function(opt_arrayBufferOrSize, opt_byteOffset) {
    /**
     * The underlying ArrayBuffer
     * @type {ArrayBuffer}
     */
    this.buffer = null;

    if (typeof opt_arrayBufferOrSize === 'number') {
        this.buffer = new ArrayBuffer(opt_arrayBufferOrSize);
    } else {
        this.buffer = opt_arrayBufferOrSize || new ArrayBuffer(gotcake.proto.Buffer.DEFAULT_SIZE);
    }

    /**
     * The dataview for the buffer
     * @type {DataView}
     */
    this.view = new DataView(this.buffer);

    /**
     * The read/write offset for this buffer
     * @type {number}
     */
    this.offset = opt_byteOffset || 0;

    /**
     * The end offset for this buffer
     * @type {number}
     */
    this.end = this.buffer.byteLength;
};


/**
 * The default initial size for buffers created without a specified size
 * @type {number}
 */
gotcake.proto.Buffer.DEFAULT_SIZE = 1024;


/**
 * Decodes a zig-zag encoded 32 bit integer to a normal 32 bit integer
 * @param {number} n the number to decode
 * @returns {number} the decoded number
 * @private
 */
gotcake.proto.Buffer.decodeZigZag32_ = function(n) {
    return ((n >>> 1) ^ -(n & 1)) | 0; // ref: src/google/protobuf/wire_format_lite.h
};


/**
 * Reads a 32-bit varint from the buffer and advances the offset accordingly
 * @returns {number}
 */
gotcake.proto.Buffer.prototype.readVarint32 = function() {

    var value = 0 >>> 0;
    var temp;
    var size = 0;

    do {
        temp = this.view.getUint8(this.offset + size);
        if (size < 5) {
            value |= ((temp & 0x7F) << (7 * size)) >>> 0;
        }
        ++size;
    } while ((temp & 0x80) === 0x80);

    this.offset += size;
    return value | 0;

};


/**
 * Reads a 32-bit zig-zag encoded varint from the buffer and advances the offset accordingly
 * @returns {number}
 */
gotcake.proto.Buffer.prototype.readVarint32ZigZag = function() {
    return gotcake.proto.Buffer.decodeZigZag32_(this.readVarint32());
};


/**
 * Reads a 32-bit fixed-width unsigned integer from the buffer and advances the offset accordingly
 * @returns {number}
 */
gotcake.proto.Buffer.prototype.readUint32 = function() {
    var value = this.view.getUint32(this.offset, true);
    this.offset += 4;
    return value;
};


/**
 * Reads a 32-bit fixed-width signed integer from the buffer and advances the offset accordingly
 * @returns {number}
 */
gotcake.proto.Buffer.prototype.readInt32 = function() {
    var value = this.view.getInt32(this.offset, true);
    this.offset += 4;
    return value;
};


/**
 * Reads a 32-bit fixed-width float from the buffer and advances the offset by 4 bytes
 * @returns {number}
 */
gotcake.proto.Buffer.prototype.readFloat32 = function() {
    var value = this.view.getFloat32(this.offset, true);
    this.offset += 4;
    return value;
};


/**
 * Reads a 64-bit fixed-width float from the buffer and advances the offset by 8 bytes
 * @returns {number}
 */
gotcake.proto.Buffer.prototype.readFloat64 = function() {
    var value = this.view.getFloat64(this.offset, true);
    this.offset += 8;
    return value;
};


/**
 * Reads a variable number of bytes as an array buffer and advances the offset accordingly.
 * @returns {ArrayBuffer}
 */
gotcake.proto.Buffer.prototype.readVBytes = function() {
    var numBytes = this.readVarint32();
    var value = new ArrayBuffer(numBytes);
    gotcake.proto.Buffer.copyArrayBufferContents_(this.buffer, this.offset, value, 0, numBytes);
    this.offset += numBytes;
    return value;
};

/**
 * Reads a variable length UTF-8 string from the buffer
 * @returns {string}
 */
gotcake.proto.Buffer.prototype.readVString = function() {
    //see http://stackoverflow.com/questions/17191945/conversion-between-utf-8-arraybuffer-and-string
    var numBytes = this.readVarint32();
    var bytes = gotcake.proto.Buffer.copyArrayBuffToArray_(this.buffer, this.offset, numBytes);
    this.offset += numBytes;
    var encodedString = String.fromCharCode.apply(null, bytes);
    return decodeURIComponent(escape(encodedString));
};


/**
 * Skips n bytes in the buffer by advancing the offset.
 * @param n
 */
gotcake.proto.Buffer.prototype.skip = function(n) {
    this.offset += n;
};


/**
 * Copies data from a ArrayBuffer to to an array
 * @param {ArrayBuffer} source the source buffer
 * @param {number} offset the offset to start copying at
 * @param {number} length the length to copy
 * @private
 */
gotcake.proto.Buffer.copyArrayBuffToArray_ = function(source, offset, length) {
    return Array.prototype.slice.call(new Uint8Array(source, offset, length));
};

/**
 * Copies data from one ArrayBuffer to another
 * @param {ArrayBuffer} source the source buffer
 * @param {number} sourceOffset the offset to start copying at in the source buffer
 * @param {ArrayBuffer} target the target buffer
 * @param {number} targetOffset the offset of
 * @param {number} length
 * @private
 */
gotcake.proto.Buffer.copyArrayBufferContents_ = function(source, sourceOffset, target, targetOffset, length) {
    new Uint8Array(target).set(new Uint8Array(source, sourceOffset, length), targetOffset);
};






