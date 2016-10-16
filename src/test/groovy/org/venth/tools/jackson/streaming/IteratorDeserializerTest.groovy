package org.venth.tools.jackson.streaming

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.TypeFactory
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
            DeserializedObjectWithArray deserialized = mapper.readValue(declaredJson, DeserializedObjectWithArray)

        then: 'deserializes object'
            expected == deserialized

        where: 'array is declared as'
        declaredJson                                                          || expected
            '{ "field": 21, "array": [1], "text": "text" }'                   || new DeserializedObjectWithArray(21, [1], "text")
            '{ "field": 22, "array": [1, 2, 3], "text": "text" }'             || new DeserializedObjectWithArray(22, [1, 2, 3], "text")
            '{ "field": 23, "array": [1, 2, 3, 4], "text": "text" }'          || new DeserializedObjectWithArray(23, [1, 2, 3, 4], "text")
            '{ "field": 24, "array": [1.1], "text": "text" }'                 || new DeserializedObjectWithArray(24, [1.1], "text")
            '{ "field": 25, "array": [1.2, 2.2, 3], "text": "text" }'         || new DeserializedObjectWithArray(25, [1.2, 2.2, 3], "text")
            '{ "field": 26, "array": [1, 2], "text": "text" }'                || new DeserializedObjectWithArray(26, [1, 2], "text")
            '{ "field": 27, "array": [1.4, 2.2, 3.1, 4.2], "text": "text" }'  || new DeserializedObjectWithArray(27, [1.4, 2.2, 3.1, 4.2], "text")
            '{ "field": 28, "array": [1.1, 2.1], "text": "text" }'            || new DeserializedObjectWithArray(28, [1.1, 2.1], "text")
            '{ "field": 29, "array": [null, null], "text": "text" }'          || new DeserializedObjectWithArray(29, [null, null], "text")
            '{ "field": 30, "array": ["1", "2"], "text": "text" }'            || new DeserializedObjectWithArray(30, ["1", "2"], "text")
            '{ "field": 31, "array": [], "text": "text" }'                    || new DeserializedObjectWithArray(31, [], "text")
    }

    @Unroll
    def "deserializes object: #declaredJson containing array that contains an array of simple objects"() {
        given: 'object mapper'
            def mapper = new ObjectMapper()

        and: 'deserializing iterator registered in object mapper'
            def module = new SimpleModule();


            module.addDeserializer(GenericTypeReference.simpleObjectIteratorClass(), new IteratorDeserializer(new TypeReference<SimpleObject>() {}))
            module.addDeserializer(GenericTypeReference.deserializedObjectWithArrayIteratorClass(), new IteratorDeserializer(new TypeReference<DeserializedObjectWithArray>() {}))
            mapper.registerModule(module)

        when: 'collects all elements from deserialized object'
            mapper.readValue(
                    declaredJson,
                    TypeFactory.defaultInstance().constructParametricType(Iterator, DeserializedObjectWithArray)
            )
            Iterator deserialized = mapper.readValue(declaredJson, new TypeReference<Iterator<DeserializedObjectWithArray>>() {})

        then: 'deserializes object'
            def deserializedCollection = new ArrayList()
            deserialized.forEachRemaining({ deserializedCollection.add(it) })

            [expected].eachWithIndex { DeserializedObjectWithArray entry, int index ->
                entry == deserializedCollection.get(index)
            }

        where: 'array is declared as'
        declaredJson                                                          || expected
//            '{ "field": 21, "array": [], "text": "text" }'                    || new DeserializedObjectWithArray(21, [], "text")
            '[{ "field": 21, "array": ' +
                    '[{"text": "text", "integer": 1}]' +
                    ', "text": "text" }]'                                      || new DeserializedObjectWithArray(21, [new SimpleObject("text", 1)], "text")
    }

    def "deserializes object inside array"() {
        given: 'object mapper'
            def mapper = new ObjectMapper()

        and: 'deserializing iterator registered in object mapper'
            def module = new SimpleModule();
            module.addDeserializer(
                    Iterator,
                    new IteratorDeserializer(new TypeReference<SimpleObject>() {})
            )
            mapper.registerModule(module)

        and: 'json to deserializes is an array with one object within'
            def objectWithinArray = new SimpleObject("text", 12312)
            def declaredJson = "[{ \"text\": \"${objectWithinArray.text}\", \"integer\": ${objectWithinArray.integer}}]"

        and: 'object mapper deserializes the collection to an iterator'
            def deserializedIterator = mapper.readValue(declaredJson, Iterator)

        when: 'iterator returns an element'
            def element = ++deserializedIterator

        then: 'the element is deserialized object from array'
            element == objectWithinArray
    }
}
