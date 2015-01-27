goog.provide('gotcake.closure.test');


goog.require('goog.net.XhrIo');
goog.require('gotcake.FooBar');
goog.require('gotcake.PackedThingy');


/**
 * Executes a test against gotcake.FooBar
 * @param {string} name
 * @param {Object.<string,*>} data
 * @param {!function(*, gotcake.FooBar, *)} cb
 */
gotcake.closure.test.testFooBar = function(name, data, cb) {
    QUnit.test(name,  function(assert) {
        var done = assert['async']();
        gotcake.closure.test.ajaxBinary('POST', '/get?type=FooBar', 'application/json; charset=UTF-8', JSON.stringify(data), function(xhr) {
            if (xhr.isSuccess()) {
                var arrBuff = xhr.getResponse();
                var msg = new gotcake.FooBar();
                msg.decode(arrBuff);
                cb(assert, msg, data);
                done();
            } else {
                throw Error('Request returned with status: ' + xhr.getStatus());
            }
        });
    });
};

/**
 * Executes a test against gotcake.FooBar
 * @param {string} name
 * @param {gotcake.Stuff} stuff
 * @param {!function(*, gotcake.Stuff, *)} cb
 */
gotcake.closure.test.stuffEchoTest = function(name, stuff, cb) {
    QUnit.test(name,  function(assert) {
        var done = assert['async']();
        gotcake.closure.test.ajaxBinary('POST', '/echo?type=Stuff', 'application/x-protobuf', stuff.encode(), function(xhr) {
            if (xhr.isSuccess()) {
                var arrBuff = xhr.getResponse();
                var msg = new gotcake.FooBar();
                msg.decode(arrBuff);
                cb(assert, msg, data);
                done();
            } else {
                throw Error('Request returned with status: ' + xhr.getStatus());
            }
        });
    });
};

/**
 * Executes a test against gotcake.PackedThingy
 * @param {string} name
 * @param {Object.<string,*>} data
 * @param {!function(*, gotcake.PackedThingy, *)} cb
 */
gotcake.closure.test.testPackedThingy = function(name, data, cb) {
    QUnit.test(name,  function(assert) {
        var done = assert['async']();
        gotcake.closure.test.ajaxBinary('POST', '/get?type=PackedThingy', 'application/json; charset=UTF-8', JSON.stringify(data), function(xhr) {
            if (xhr.isSuccess()) {
                var arrBuff = xhr.getResponse();
                var msg = new gotcake.PackedThingy();
                msg.decode(arrBuff);
                cb(assert, msg, data);
                done();
            } else {
                throw Error('Request returned with status: ' + xhr.getStatus());
            }
        });
    });
};


/**
 * Makes a binary request
 * @param {string} method the http method (GET, POST, etc.)
 * @param {string} url the
 * @param {string} contentType the content type
 * @param {*} data to send
 * @param {!function(*, goog.net.XhrIo)} cb the callback to call
 */
gotcake.closure.test.ajaxBinary = function(method, url, contentType, data, cb) {
    var xhr = new goog.net.XhrIo();
    goog.events.listen(xhr, goog.net.EventType.COMPLETE, function() {
        cb(xhr);
    });
    xhr.setResponseType(goog.net.XhrIo.ResponseType.ARRAY_BUFFER);
    xhr.send(url, method, data, {
        'Accept': 'application/x-protobuf',
        'Content-Type': contentType
    });
};


/**
 *
 * @param {ArrayBuffer[]} arr
 * @returns {number[][]}
 */
gotcake.closure.test.extractArrays_ = function(arr) {
    if (!(arr instanceof Array)) {
        return null;
    }
    var ret = [];
    for (var j = 0; j < arr.length; ++j) {
        var retj = ret[j] = [];
        var view = new DataView(arr[j]);
        for (var i = 0; i < arr[j].byteLength; ++i) {
            retj[i] = view.getUint8(i);
        }
    }
    return ret;
};


/**
 * Executes the closure quint tests
 */
gotcake.closure.test.runTests = function() {

    // test data
    var bytes1 = [[0, 127, 76], [], [1, 2]];
    var strings1 = ['Aaron', 'Cake', '(ノಠ益ಠ)ノ彡┻━┻', '', '\n\t\r', '\u1234'];
    var float32Literal = [0, 1, 7654321, 3.4028235e+38, 1.17549435E-38, 1.4e-45, 'NaN', 'Infinity', '-Infinity'];
    var float64Literal = [0, 1, 7654321, 1.7976931348623157e+308, 2.2250738585072014E-308, '4.9e-324', 'NaN', 'Infinity', '-Infinity'];
    // in the process of reading/writing the float values, they get converted to non-exact double values...
    var float32s = [0, 1, 7654321, 3.4028234663852886e+38, 1.1754943508222875e-38, 1.401298464324817e-45, 0/0, 1/0, -1/0];
    var float64s = [0, 1, 7654321, 1.7976931348623157e+308, 2.2250738585072014E-308, 4.9e-324, 0/0, 1/0, -1/0];
    var signedInts = [2147483647, -2147483648, 0, -75, 6, 123456, -1];
    var usignedInts = [2147483647, 4294967295, 0, 76, 2147483648, 123456, 1];

    // test unpacked fields

    gotcake.closure.test.testFooBar("decode bytes", {'rawbytes':bytes1}, function(assert, msg){
        assert['deepEqual'](gotcake.closure.test.extractArrays_(msg.rawbytes), bytes1, "All bytes must match");
    });
    gotcake.closure.test.testFooBar("decode strings", {'strings':strings1}, function(assert, msg){
        assert['deepEqual'](msg.strings, strings1, "All strings must match");
    });
    gotcake.closure.test.testFooBar("decode int32", {'int32':signedInts}, function(assert, msg){
        assert['deepEqual'](msg.int32, signedInts, "All ints must match");
    });
    gotcake.closure.test.testFooBar("decode sint32", {'sint32':signedInts}, function(assert, msg){
        assert['deepEqual'](msg.sint32, signedInts, "All ints must match");
    });
    gotcake.closure.test.testFooBar("decode uint32", {'uint32':usignedInts}, function(assert, msg){
        assert['deepEqual'](msg.uint32, usignedInts, "All ints must match");
    });
    gotcake.closure.test.testFooBar("decode fixed32", {'fixed32':usignedInts}, function(assert, msg){
        assert['deepEqual'](msg.fixed32, usignedInts, "All ints must match");
    });
    gotcake.closure.test.testFooBar("decode sfixed32", {'sfixed32':signedInts}, function(assert, msg){
        assert['deepEqual'](msg.sfixed32, signedInts, "All ints must match");
    });
    gotcake.closure.test.testFooBar("decode float32", {'float32':float32Literal}, function(assert, msg){
        assert['deepEqual'](msg.float32, float32s, "All floats must match");
    });
    gotcake.closure.test.testFooBar("decode float64", {'float64':float64Literal}, function(assert, msg){
        assert['deepEqual'](msg.float64, float64s, "All floats must match");
    });

    // test packed fields

    gotcake.closure.test.testPackedThingy("decode packed int32", {'int32':signedInts}, function(assert, msg){
        assert['deepEqual'](msg.int32, signedInts, "All ints must match");
    });
    gotcake.closure.test.testPackedThingy("decode packed sint32", {'sint32':signedInts}, function(assert, msg){
        assert['deepEqual'](msg.sint32, signedInts, "All ints must match");
    });
    gotcake.closure.test.testPackedThingy("decode packed uint32", {'uint32':usignedInts}, function(assert, msg){
        assert['deepEqual'](msg.uint32, usignedInts, "All ints must match");
    });
    gotcake.closure.test.testPackedThingy("decode packed fixed32", {'fixed32':usignedInts}, function(assert, msg){
        assert['deepEqual'](msg.fixed32, usignedInts, "All ints must match");
    });
    gotcake.closure.test.testPackedThingy("decode packed sfixed32", {'sfixed32':signedInts}, function(assert, msg){
        assert['deepEqual'](msg.sfixed32, signedInts, "All ints must match");
    });
    gotcake.closure.test.testPackedThingy("decode packed float32", {'float32':float32Literal}, function(assert, msg){
        assert['deepEqual'](msg.float32, float32s, "All floats must match");
    });
    gotcake.closure.test.testPackedThingy("decode packed float64", {'float64':float64Literal}, function(assert, msg){
        assert['deepEqual'](msg.float64, float64s, "All floats must match");
    });
};