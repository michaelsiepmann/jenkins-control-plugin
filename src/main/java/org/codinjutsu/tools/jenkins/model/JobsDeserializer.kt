package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

internal class JobsDeserializer(vc : Class<*>) : StdDeserializer<Jobs>(vc) {
    override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): Jobs {
        return Jobs(emptyList())
    }
}