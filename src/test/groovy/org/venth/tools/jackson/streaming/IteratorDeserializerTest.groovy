package org.venth.tools.jackson.streaming

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
}
