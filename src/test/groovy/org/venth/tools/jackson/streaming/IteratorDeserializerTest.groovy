package org.venth.tools.jackson.streaming

import java.util.stream.Collectors
import java.util.stream.StreamSupport

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Venth on 03/10/2016
 */
class IteratorDeserializerTest extends Specification {

    @Unroll
    def "deserializes #declaredJson"() {
        given: 'object mapper'
            def mapper = new ObjectMapper()

        and: 'deserializing iterator registered in object mapper'
            def module = new SimpleModule();
            module.addDeserializer(Iterator, new IteratorDeserializer())
            mapper.registerModule(module)

        when: 'collects all element from deserialized iterator'
            def deserialized = []
            Iterator iterator = mapper.readValue(declaredJson, Iterator)
            iterator.forEachRemaining({ el -> deserialized.add(el) })

        then: 'deserializes in declared sequence'
            deserialized == expected

        where: 'array is declared as'
            declaredJson    || expected
            '[1]'                   || [1]
            '[1, 2, 3]'             || [1, 2, 3]
            '[1, 2, 3, 4]'          || [1, 2, 3, 4]
            '[1, 2]'                || [1, 2]
            '[1.1]'                 || [1.1]
            '[1.2, 2.2, 3]'         || [1.2, 2.2, 3]
            '[1.4, 2.2, 3.1, 4.2]'  || [1.4, 2.2, 3.1, 4.2]
            '[1.1, 2.1]'            || [1.1, 2.1]
            '[null, null]'          || [null, null]
            '["1", "2"]'            || ["1", "2"]
            '[]'                    || []
    }

    @Unroll
    def "deserializes object: #declaredJson containing array"() {
        given: 'object mapper'
            def mapper = new ObjectMapper()

        and: 'deserializing iterator registered in object mapper'
            def module = new SimpleModule();
            module.addDeserializer(Iterator, new IteratorDeserializer())
            mapper.registerModule(module)

        when: 'collects all elements from deserialized object'
            DeserializedObject deserialized = mapper.readValue(declaredJson, DeserializedObject)

        then: 'deserializes object'
            expected == deserialized

        where: 'array is declared as'
        declaredJson                                                          || expected
            '{ "field": 21, "array": [1], "text": "text" }'                   || new DeserializedObject(21, [1], "text")
            '{ "field": 22, "array": [1, 2, 3], "text": "text" }'             || new DeserializedObject(22, [1, 2, 3], "text")
            '{ "field": 23, "array": [1, 2, 3, 4], "text": "text" }'          || new DeserializedObject(23, [1, 2, 3, 4], "text")
            '{ "field": 24, "array": [1.1], "text": "text" }'                 || new DeserializedObject(24, [1.1], "text")
            '{ "field": 25, "array": [1.2, 2.2, 3], "text": "text" }'         || new DeserializedObject(25, [1.2, 2.2, 3], "text")
            '{ "field": 26, "array": [1, 2], "text": "text" }'                || new DeserializedObject(26, [1, 2], "text")
            '{ "field": 27, "array": [1.4, 2.2, 3.1, 4.2], "text": "text" }'  || new DeserializedObject(27, [1.4, 2.2, 3.1, 4.2], "text")
            '{ "field": 28, "array": [1.1, 2.1], "text": "text" }'            || new DeserializedObject(28, [1.1, 2.1], "text")
            '{ "field": 29, "array": [null, null], "text": "text" }'          || new DeserializedObject(29, [null, null], "text")
            '{ "field": 30, "array": ["1", "2"], "text": "text" }'            || new DeserializedObject(30, ["1", "2"], "text")
            '{ "field": 31, "array": [], "text": "text" }'                    || new DeserializedObject(31, [], "text")
    }

    static class DeserializedObject {
        private int field
        private Iterator<?> array
        private String text

        DeserializedObject() {
        }

        DeserializedObject(int field, List array, String text) {
            this.field = field
            this.array = array.iterator()
            this.text = text
        }

        int getField() {
            return field
        }

        void setField(int field) {
            this.field = field
        }

        Iterator<?> getArray() {
            return array
        }

        void setArray(Iterator<?> array) {
            this.array = array
        }

        String getText() {
            return text
        }

        void setText(String text) {
            this.text = text
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            DeserializedObject that = (DeserializedObject) o

            if (field != that.field) return false
            if (text != that.text) return false

            def thisArray = StreamSupport.stream(Spliterators.spliteratorUnknownSize(array, Spliterator.ORDERED), false).collect(Collectors.toList())
            def thatArray = StreamSupport.stream(Spliterators.spliteratorUnknownSize(that.array, Spliterator.ORDERED), false).collect(Collectors.toList())

            if (thisArray != thatArray) return false

            return true
        }

        int hashCode() {
            int result
            result = field
            result = 31 * result + (text != null ? text.hashCode() : 0)
            return result
        }


        @Override
        public String toString() {
            return "DeserializedObject{" +
                    "field=" + field +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}
