package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILDS

internal data class ParsedBuilds(
        @JsonProperty(BUILDS)
        val builds: Collection<ParsedBuild>
)