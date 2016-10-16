package org.venth.tools.jackson.streaming;

import java.util.Objects;

/**
 * @author Venth on 08/10/2016
 */
public class SimpleObject {
    private String text;

    private Integer integer;

    public SimpleObject() {
    }

    public SimpleObject(String text, Integer integer) {
        this.text = text;
        this.integer = integer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleObject that = (SimpleObject) o;
        return Objects.equals(text, that.text) &&
                Objects.equals(integer, that.integer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, integer);
    }

    public String getText() {

        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }
}
