package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEWS

internal data class ParsedViews(
        @JsonProperty(VIEWS)
        val views: Collection<ParsedView>?
)