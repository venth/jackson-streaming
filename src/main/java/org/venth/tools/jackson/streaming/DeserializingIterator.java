package org.venth.tools.jackson.streaming;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;

/**
 * @author Venth on 02/10/2016
 */
public class DeserializingIterator implements Iterator<Object> {

    private final JsonParser parser;

    private final DeserializationContext ctx;

    private JsonToken currentToken;

    Map<JsonToken, Supplier<Object>> valueHandlers;

    public DeserializingIterator(
            JsonParser parser,
            DeserializationContext ctx
    ) {
        this(parser, ctx, null);
    }

    public DeserializingIterator(
            JsonParser parser,
            DeserializationContext ctx,
            JavaType expectedObjectType
    ) {
        this.parser = parser;
        this.ctx = ctx;

        verifyIfCurrentTokenIsArrayStart(this.parser, this.ctx);

        valueHandlers = new TokenValueHandlerFactory().create(this.parser, this.ctx, expectedObjectType);
        currentToken = nextToken(this.parser);
    }

    private JsonToken nextToken(JsonParser parser) {
        JsonToken nextToken;
        try {
            nextToken = parser.nextToken();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return nextToken;
    }

    private void verifyIfCurrentTokenIsArrayStart(JsonParser parser, DeserializationContext ctx) {
        assert (
                parser.hasCurrentToken() && JsonToken.START_ARRAY == parser.currentToken()
        ) : "Current token has to be Array Start, but is: " + parser.currentToken();
    }

    @Override
    public boolean hasNext() {
        return currentToken != null && currentToken != JsonToken.END_ARRAY;
    }

    @Override
    public Object next() {
        Object value = value(currentToken);

        currentToken = nextToken(parser);

        return value;
    }

    private Object value(JsonToken token) {
        return Optional.ofNullable(valueHandlers.get(token))
                .map(Supplier::get)
                .orElse(null);
    }

    public static class TokenValueHandlerFactory {

        Map<JsonToken, Supplier<Object>> create(
                JsonParser parser,
                DeserializationContext context,
                JavaType expectedObjectType
        ) {
            HashMap<JsonToken, Supplier<Object>> handlers = new HashMap<>();

            handlers.put(JsonToken.VALUE_STRING, () -> {
                try {
                    return parser.getText();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            handlers.put(JsonToken.VALUE_NUMBER_INT, () -> {
                try {
                    return parser.getLongValue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            handlers.put(JsonToken.VALUE_NUMBER_FLOAT, () -> {
                try {
                    return parser.getDoubleValue();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            handlers.put(JsonToken.VALUE_TRUE, () -> true);
            handlers.put(JsonToken.VALUE_FALSE, () -> true);
            handlers.put(JsonToken.VALUE_NULL, () -> null);

            handlers.put(JsonToken.START_OBJECT, () -> {
                try {
                    return context.readValue(parser, expectedObjectType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            handlers.put(JsonToken.START_ARRAY, () -> {
                try {
                    return context.readValue(parser, expectedObjectType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            handlers.put(JsonToken.END_ARRAY, () -> {
                throw new RuntimeException("Unexpected token: " + JsonToken.END_ARRAY);
            });
            handlers.put(JsonToken.END_OBJECT, () -> {
                throw new RuntimeException("Unexpected token: " + JsonToken.END_OBJECT);
            });
            handlers.put(JsonToken.FIELD_NAME, () -> {
                throw new RuntimeException("Unexpected token: " + JsonToken.FIELD_NAME);
            });

            return handlers;
        }
    }
}
