package org.venth.tools.jackson.streaming

import java.util.function.Supplier

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import spock.lang.Specification
import spock.lang.Unroll

/**
 * @author Venth on 02/10/2016
 */
class DeserializingIteratorTest extends Specification {

    def "iterator isn't created if deserialization content is empty"() {
        given: 'content is empty'
            jsonParser.hasCurrentToken() >> false

        when: 'iterator is constructed'
            createIterator()

        then: "iterator isn't created"
            thrown(AssertionError)
    }

    def "delegates deserialization of array element to token value handler"() {
        given: 'array containing one element'
            jsonParser.currentToken() >> JsonToken.START_ARRAY
            jsonParser.hasCurrentToken() >> true
            def elementType = JsonToken.VALUE_TRUE
            def element = true
            jsonParser.nextToken() >>> [elementType, null ]

            def deserializingIterator = createIterator()
            deserializingIterator.valueHandlers.put( elementType, [ get: { element } ] as Supplier<Object>)

        when: 'iterator approaches element'
            def deserialized = ++deserializingIterator

        then: 'element is deserialized'
            deserialized == element
    }

    def "deserializes objects via delegated object deserializer"() {
        given: 'content contains array with one object within'
            jsonParser.currentToken() >> JsonToken.START_ARRAY
            jsonParser.hasCurrentToken() >> true
            def elementType = JsonToken.START_OBJECT
            def objectElement = new Object()
            jsonParser.nextToken() >>> [elementType, null ]

        and: 'object type is ObjectToDeserialize'
            def objectType = Mock(JavaType)

            deserializationContext.readValue(jsonParser, objectType)  >> objectElement

        and: 'deserializng iterator is created'
            def deserializingIterator = createIterator(objectType)

        when: 'iterator approaches object element'
            def deserialized = ++deserializingIterator

        then: 'object is deserialized'
            deserialized == objectElement
    }

    def "treats embedded array as yet another object to deserialize"() {
        given: 'content contains array with one array within'
            jsonParser.currentToken() >> JsonToken.START_ARRAY
            jsonParser.hasCurrentToken() >> true
            def elementType = JsonToken.START_ARRAY
            def arrayElement = Mock(List)
            jsonParser.nextToken() >>> [elementType, null ]

        and: 'array type is List'
            def arrayType = Mock(JavaType)

            deserializationContext.readValue(jsonParser, arrayType)  >> arrayElement

        and: 'deserializng iterator is created'
            def deserializingIterator = createIterator(arrayType)

        when: 'iterator approaches object element'
            def deserialized = ++deserializingIterator

        then: 'object is deserialized'
            deserialized == arrayElement
    }

    @Unroll
    def "iterator isn't created if starting element is: #notArrayStartToken"() {
        given: 'starting token anything but array start'
            jsonParser.hasCurrentToken() >> true
            jsonParser.currentToken() >> notArrayStartToken

        when: 'iterator is constructed'
            createIterator()

        then: "iterator isn't created"
            thrown(AssertionError)

        where: "starting tokens which aren't arrays"
            notArrayStartToken << allTokensBut(JsonToken.START_ARRAY)
    }

    def "empty collection is deserialized as iterator with no next element"() {
        given: 'empty collection'
            jsonParser.currentToken() >> JsonToken.START_ARRAY
            jsonParser.hasCurrentToken() >> true
            jsonParser.nextToken() >>> [JsonToken.END_ARRAY, null]

        when: 'iterator initializes'
            def deserializer = createIterator()

        then: 'nothing to iterate'
            !deserializer.hasNext()
    }

    @Unroll
    def "unexpected #unexpectedToken causes parsing error"() {
        given: 'empty collection'
            jsonParser.currentToken() >> JsonToken.START_ARRAY
            jsonParser.hasCurrentToken() >> true
            jsonParser.nextToken() >>> [unexpectedToken, null]

        and: 'iterator is initialized'
            def deserializer = createIterator()

        when: 'iterator approaches element'
            ++deserializer

        then: 'throws parsing error'
            thrown(RuntimeException)

        where: 'provides unexpected tokens'
            unexpectedToken << [ JsonToken.END_ARRAY, JsonToken.END_OBJECT, JsonToken.FIELD_NAME ]
    }

    def jsonParser = Mock(JsonParser)

    def deserializationContext = Mock(DeserializationContext)

    private static List<JsonToken> allTokensBut(JsonToken ... exceptionTokens) {
        exceptionTokens = exceptionTokens ?: [] as JsonToken[]
        return JsonToken.values().findAll { token -> !exceptionTokens.contains(token) }
    }

    private DeserializingIterator createIterator(JavaType objectType) {
        new DeserializingIterator(jsonParser, deserializationContext, objectType)
    }

    private DeserializingIterator createIterator() {
        createIterator(null)
    }
}
