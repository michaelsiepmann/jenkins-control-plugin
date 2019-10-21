package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH_DESCRIPTION
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH_ICON

internal data class ParsedHealth(
        @JsonProperty(JOB_HEALTH_DESCRIPTION)
        val description: String,
        @JsonProperty(JOB_HEALTH_ICON)
        val icon: String?
)