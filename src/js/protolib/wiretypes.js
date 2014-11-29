goog.provide('protolib.WireTypes');

/**
 * An enum of wire types
 * @enum {number}
 */
protolib.WireTypes = {
    FIXED32: 1,
    FIXED64: 2,
    VARINT: 3,
    LENGTH_DELIMITED: 4,
    GROUP_END: 5,
    GROUP_START: 6
};