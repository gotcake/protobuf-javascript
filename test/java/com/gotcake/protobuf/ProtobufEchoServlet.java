package com.gotcake.protobuf;

import com.google.protobuf.GeneratedMessage;
import com.gotcake.json.JSONDecoder;
import com.gotcake.protobuf.javascript.protos.FooBarProtos;
import com.gotcake.protobuf.javascript.protos.PackedThingyProtos;
import com.gotcake.protobuf.javascript.protos.StuffProtos;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by aaron on 1/24/15.
 */
public class ProtobufEchoServlet extends HttpServlet  {


    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        try {
            final String type = request.getParameter("type");
            final GeneratedMessage message;
            switch (type) {
                case "Stuff":
                    message = StuffProtos.Stuff.parseFrom(request.getInputStream());
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


}
