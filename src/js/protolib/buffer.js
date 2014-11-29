goog.provide("protolib.Buffer");

// see https://github.com/dcodeIO/ByteBuffer.js/blob/master/dist/ByteBufferAB.js

protolib.Buffer = function(opt_arrayBufferOrSize, opt_byteOffset) {
    if (typeof opt_arrayBufferOrSize === 'number') {
        this.buffer = new ArrayBuffer(opt_arrayBufferOrSize);
    } else {
        this.buffer = opt_arrayBuffer || new ArrayBuffer(protolib.Buffer.DEFAULT_SIZE);
    }
    this.view = new DataView(this.buffer);
    this.offset = this.buffer.byteOffset + (opt_byteOffset || 0);
};


protolib.Buffer.decodeZigZag32_ = function(n) {
    return ((n >>> 1) ^ -(n & 1)) | 0; // // ref: src/google/protobuf/wire_format_lite.h
};

protolib.Buffer.prototype.readVarint32 = function() {

    var value = 0 >>> 0;
    var ioffset;
    var temp;
    var size = 0;

    do {
        ioffset = this.offset + size;
        if (ioffset > this.limit) {
            throw Error("truncated");
        }
        temp = this.view.getUint8(ioffset);
        if (size < 5) {
            value |= ((temp & 0x7F) << (7 * size)) >>> 0;
        }
        ++size;
    } while ((temp & 0x80) === 0x80);

    this.offset += size;
    return value | 0;

};

protolib.Buffer.prototype.readVarint32ZigZag = function() {
    return protolib.Buffer.decodeZigZag32(this.readVarint32());
};

protolib.Buffer.prototype.readUint32 = function() {
    if (this.offset + 4 > this.buffer.byteLength) {
        throw Error("truncated");
    }
    var value = this.view.getUint32(this.offset);
    this.offset += 4;
    return value;
};

protolib.Buffer.prototype.readInt32 = function() {
    if (this.offset + 4 > this.buffer.byteLength) {
        throw Error("truncated");
    }
    var value = this.view.getInt32(this.offset);
    this.offset += 4;
    return value;
};

protolib.Buffer.prototype.readFloat32 = function() {
    if (this.offset + 4 > this.buffer.byteLength) {
        throw Error("truncated");
    }
    var value = this.view.getFloat32(this.offset);
    this.offset += 4;
    return value;
};

protolib.Buffer.prototype.readFloat64 = function() {
    if (this.offset + 8 > this.buffer.byteLength) {
        throw Error("truncated");
    }
    var value = this.view.getFloat64(this.offset);
    this.offset += 4;
    return value;
};

protolib.Buffer.prototype.readVBytes = function() {
    var numBytes = this.readVarint32();
    var end = this.offset + numBytes;
    if (end > this.buffer.byteLength) {
        throw Error("truncated");
    }
    var value = new ArrayBuffer(numBytes);
    new Uint8Array(value).set(new Uint8Array(this.buffer).subarray(this.offset, end));
    this.offset = end;
    return value;
};

protolib.Buffer.prototype.getOffset = function() {
    return this.offset;
};





