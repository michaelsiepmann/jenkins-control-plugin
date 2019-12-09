package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOBS

internal data class Jobs(
        @JsonProperty(JOBS)
        @JsonDeserialize(contentAs = ViewElement::class, contentUsing = ViewElementDeserializer::class)
        val views: Collection<ViewElement>
)