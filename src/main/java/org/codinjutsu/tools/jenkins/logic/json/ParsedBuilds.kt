package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILDS
import org.codinjutsu.tools.jenkins.model.Build

internal data class ParsedBuilds(
        @JsonProperty(BUILDS)
        val builds: Collection<Build>
)