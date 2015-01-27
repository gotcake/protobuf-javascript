package com.gotcake.protobuf;

import com.google.protobuf.GeneratedMessage;
import com.gotcake.json.JSONDecoder;
import com.gotcake.json.parser.JSONParserException;
import com.gotcake.protobuf.javascript.protos.FooBarProtos;
import com.gotcake.protobuf.javascript.protos.PackedThingyProtos;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Map;

/**
 * Created by aaron on 12/21/14.
 */
public class ProtobufProducingServlet extends HttpServlet {

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        try {
            final Map<String, Object> data = JSONDecoder.createDefaultDecoder(request.getReader()).parseObject();
            final String type = request.getParameter("type");
            final GeneratedMessage message;
            switch (type) {
                case "FooBar":
                    message = createFooBar(data);
                    break;
                case "PackedThingy":
                    message = createPackedThingy(data);
                    break;
                default:
                    throw new RuntimeException("Unrecognized type: " + type);
            }
            response.setHeader("Content-Type", "application/x-protobuf");
            response.setStatus(200);
            message.writeTo(response.getOutputStream());
        } catch (final Exception e) {
            response.setStatus(400);
            response.getOutputStream().println(e.getMessage());
        }

    }

    private static FooBarProtos.FooBar createFooBar(final Map<String, Object> data) {
        final FooBarProtos.FooBar.Builder builder = FooBarProtos.FooBar.newBuilder();
        for (final Map.Entry<String, Object> entry: data.entrySet()) {
            for (final Object value : Conversions.asList(entry.getValue())) {
                switch (entry.getKey()) {
                    case "int64":
                        builder.addInt64(Conversions.asLong(value));
                        break;
                    case "int32":
                        builder.addInt32(Conversions.asInt(value));
                        break;
                    case "flag":
                        builder.addFlag(Conversions.asBoolean(value));
                        break;
                    case "fixed32":
                        builder.addFixed32(Conversions.asUnsignedInt(value));
                        break;
                    case "sfixed32":
                        builder.addSfixed32(Conversions.asInt(value));
                        break;
                    case "fixed64":
                        builder.addFixed64(Conversions.asUnsignedLong(value));
                        break;
                    case "sfixed64":
                        builder.addSfixed64(Conversions.asLong(value));
                        break;
                    case "rawbytes":
                        builder.addRawbytes(Conversions.asByteString(value));
                        break;
                    case "strings":
                        builder.addStrings(Conversions.asString(value));
                        break;
                    case "sint32":
                        builder.addSint32(Conversions.asInt(value));
                        break;
                    case "sint64":
                        builder.addSint64(Conversions.asLong(value));
                        break;
                    case "uint32":
                        builder.addUint32(Conversions.asUnsignedInt(value));
                        break;
                    case "uint64":
                        builder.addUint64(Conversions.asUnsignedLong(value));
                        break;
                    case "float32":
                        builder.addFloat32(Conversions.asFloat(value));
                        break;
                    case "float64":
                        builder.addFloat64(Conversions.asDouble(value));
                        break;
                    default:
                        throw new RuntimeException("Unsupported key: " + entry.getKey());
                }
            }
        }
        return builder.build();
    }

    private static PackedThingyProtos.PackedThingy createPackedThingy(final Map<String, Object> data) {
        final PackedThingyProtos.PackedThingy.Builder builder = PackedThingyProtos.PackedThingy.newBuilder();
        for (final Map.Entry<String, Object> entry: data.entrySet()) {
            for (final Object value : Conversions.asList(entry.getValue())) {
                switch (entry.getKey()) {
                    case "int64":
                        builder.addInt64(Conversions.asLong(value));
                        break;
                    case "int32":
                        builder.addInt32(Conversions.asInt(value));
                        break;
                    case "flag":
                        builder.addFlag(Conversions.asBoolean(value));
                        break;
                    case "fixed32":
                        builder.addFixed32(Conversions.asUnsignedInt(value));
                        break;
                    case "sfixed32":
                        builder.addSfixed32(Conversions.asInt(value));
                        break;
                    case "fixed64":
                        builder.addFixed64(Conversions.asUnsignedLong(value));
                        break;
                    case "sfixed64":
                        builder.addSfixed64(Conversions.asLong(value));
                        break;
                    case "sint32":
                        builder.addSint32(Conversions.asInt(value));
                        break;
                    case "sint64":
                        builder.addSint64(Conversions.asLong(value));
                        break;
                    case "uint32":
                        builder.addUint32(Conversions.asUnsignedInt(value));
                        break;
                    case "uint64":
                        builder.addUint64(Conversions.asUnsignedLong(value));
                        break;
                    case "float32":
                        builder.addFloat32(Conversions.asFloat(value));
                        break;
                    case "float64":
                        builder.addFloat64(Conversions.asDouble(value));
                        break;
                    default:
                        throw new RuntimeException("Unsupported key: " + entry.getKey());
                }
            }
        }
        return builder.buildPartial();
    }

}
