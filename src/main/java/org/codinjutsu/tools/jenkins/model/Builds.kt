package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILDS

internal data class Builds(
        @JsonProperty(BUILDS)
        val builds: Collection<Build>
)