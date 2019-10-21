package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DEFINITIONS

internal data class ParsedParameter(
        @JsonProperty(PARAMETER_DEFINITIONS)
        val definitions: Collection<ParsedParameterDefinition>?
)