package com.gotcake.protobuf;

import com.google.protobuf.ByteString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by aaron on 1/10/15.
 */
public class Conversions {

    private static final byte[] EMPTY_BYTES = new byte[0];

    public static long asLong(final Object val) {
        if (val instanceof Number) {
            return ((Number)val).longValue();
        }
        return Long.parseLong(val.toString());
    }

    public static long asUnsignedLong(final Object val) {
        if (val instanceof Number) {
            return ((Number)val).longValue();
        }
        return Long.parseUnsignedLong(val.toString());
    }

    public static int asInt(final Object val) {
        if (val instanceof Number) {
            return ((Number)val).intValue();
        }
        return Integer.parseInt(val.toString());
    }

    public static int asUnsignedInt(final Object val) {
        if (val instanceof Number) {
            return ((Number)val).intValue();
        }
        return Integer.parseUnsignedInt(val.toString());
    }

    public static String asString(final Object val) {
        return val.toString();
    }

    public static double asDouble(final Object val) {
        if (val instanceof Number) {
            return ((Number)val).doubleValue();
        }
        return Double.parseDouble(val.toString());
    }

    public static float asFloat(final Object val) {
        if (val instanceof Number) {
            return ((Number)val).floatValue();
        }
        return Float.parseFloat(val.toString());
    }

    public static boolean asBoolean(final Object val) {
        return Boolean.parseBoolean(val.toString());
    }

    public static ByteString asByteString(final Object val) {
        if (val instanceof List) {
            final List list = (List) val;
            final byte[] bytes = new byte[list.size()];
            for (int i = 0; i < list.size(); ++i) {
                bytes[i] = (byte) asInt(list.get(i));
            }
            return ByteString.copyFrom(bytes);
        } else {
            return ByteString.copyFrom(EMPTY_BYTES);
        }
    }

    public static List asList(final Object val) {
        if (val instanceof List) {
            return (List)val;
        } else {
            return Collections.emptyList();
        }
    }

    public static void main(String[] args) {
        System.out.println(asFloat(Double.parseDouble(Float.toString(Float.MAX_VALUE))));
    }

}
