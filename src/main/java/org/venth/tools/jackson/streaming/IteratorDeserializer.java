package org.venth.tools.jackson.streaming;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.rits.cloning.Cloner;

/**
 * @author Venth on 03/10/2016
 */
public class IteratorDeserializer extends JsonDeserializer<Iterator<Object>> {

    private JsonDeserializer<?> objectDeserializer;

    public IteratorDeserializer() {
        this(null);
    }

    public IteratorDeserializer(JsonDeserializer<?> objectDeserializer) {
        this.objectDeserializer = objectDeserializer;
    }

    @Override
    public Iterator<Object> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Cloner cloner = new Cloner();

        //XXX create or use iterator that will close in case is exhausted
        DeserializationContext clonedContext = cloner.deepClone(ctxt);
        JsonParser clonedParser = cloner.deepClone(p);

        p.skipChildren();

        return new DeserializingIterator(clonedParser, clonedContext, objectDeserializer);
    }
}
