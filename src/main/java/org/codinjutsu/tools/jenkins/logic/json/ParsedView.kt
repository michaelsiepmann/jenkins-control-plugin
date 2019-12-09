package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOBS
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEWS
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEW_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEW_URL
import org.codinjutsu.tools.jenkins.model.ViewElement
import org.codinjutsu.tools.jenkins.model.ViewElementDeserializer

internal data class ParsedView(
        @JsonProperty(VIEW_NAME)
        val name: String?,
        @JsonProperty(VIEW_URL)
        val url: String,
        @JsonProperty(VIEWS)
        val subViews: Collection<ParsedView>?,
        @JsonProperty(JOBS)
        @JsonDeserialize(contentUsing = ViewElementDeserializer::class)
        val views: Collection<ViewElement>?
)