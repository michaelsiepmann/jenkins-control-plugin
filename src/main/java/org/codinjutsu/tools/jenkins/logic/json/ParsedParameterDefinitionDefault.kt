package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DEFAULT_PARAM_VALUE

internal data class ParsedParameterDefinitionDefault(
        @JsonProperty(PARAMETER_DEFAULT_PARAM_VALUE)
        val value: String?
)