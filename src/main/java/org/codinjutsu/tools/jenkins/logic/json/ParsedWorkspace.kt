package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PRIMARY_VIEW
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEWS

internal data class ParsedWorkspace(
        @JsonProperty(PRIMARY_VIEW)
        val primaryView: ParsedView?,
        @JsonProperty(VIEWS)
        val views: Collection<ParsedView>?
)