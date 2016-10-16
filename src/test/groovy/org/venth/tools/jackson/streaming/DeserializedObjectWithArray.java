package org.venth.tools.jackson.streaming;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Venth on 08/10/2016
 */
public class DeserializedObjectWithArray {
    private int field;
    private Iterator<SimpleObject> array;
    private String text;

    public DeserializedObjectWithArray() {
    }

    public DeserializedObjectWithArray(int field, List<SimpleObject> array, String text) {
        this.field = field;
        this.array = array.iterator();
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeserializedObjectWithArray that = (DeserializedObjectWithArray) o;
        return field == that.field &&
                Objects.equals(array, that.array) &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, array, text);
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public Iterator<SimpleObject> getArray() {
        return array;
    }

    public void setArray(Iterator<SimpleObject> array) {
        this.array = array;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
