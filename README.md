# Overview
Protobuf-javascript is a javascript code generator for [Google Protocol Buffers](https://developers.google.com/protocol-buffers/).

The project is currently in very early stages of development and isn't yet functional.
If you're interested in this project, I'd love to hear about it, but the project will not be open to other developers until
after the v1.0 release.

Google Protocol Buffers is an efficient data serialization format. It's used widely in many major companies and almost exclusively at Google
as a replacement for JSON or XML to store and transport data between servers and native applications. Currently, it is not widely used
in browsers because of a lack of an efficient, lightweight javascript implementation.

This full project is available via GitHub at https://github.com/gotcake/protobuf-javascript

## Other Implementations
Here is a list of all of the ones I am aware of. If one of them supports your particular use case better,
I encourage you to use their implementation.
* https://code.google.com/p/protobuf-js
* https://github.com/sirikata/protojs
* https://github.com/dcodeIO/ProtoBuf.js
* https://code.google.com/p/protobuf-for-node/

## Why protobuf-javscript?
Other implementations either rely on a proto file definition at runtime and then use the information in that definition
to encode or decode the data, which is inefficient and results in large library sizes, or are only for node.js and rely on native plugins.

Instead of relying on a definition at runtime, protobuf-javascript generates javascript code that inherently knows how to encode and
decode the data. This results in a boost in efficiency and a significantly reduced library size because much of the encoding and
decoding logic is moved out of the runtime and into the compiler.

## Target Features
* Version 1.0
    * Encoding/Decoding
    * Validation
    * Data Types
        * message
        * enum
        * float32, float64
        * int32, uint32, sint32, fixed32
        * string
        * bytes
    * Proto option extensions for configuring generator output
    * [Google Closure](https://developers.google.com/closure/) source compatibility
* Version 2.0
    * Reflection
    * Extensions
    * Data Types
        * oneof (maybe)
        * int64, uint64, sint64, fixed64
* Version 3.0 and Beyond
    * AMD / non-closure support
* Not Targeted
    * Services
    * Dynamic (runtime) extensions

## Browser Dependencies
The implementation relies on the HTML5 ArrayBuffer and DataView. A shim must be provided for older browsers which do not support these standards.

