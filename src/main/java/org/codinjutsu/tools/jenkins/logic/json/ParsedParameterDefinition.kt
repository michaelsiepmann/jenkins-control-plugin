package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_CHOICE
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DEFAULT_PARAM
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DESCRIPTION
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_TYPE

internal data class ParsedParameterDefinition(
        @JsonProperty(PARAMETER_NAME)
        val name: String,
        @JsonProperty(PARAMETER_TYPE)
        val type: String,
        @JsonProperty(PARAMETER_DESCRIPTION)
        val description: String?,
        @JsonProperty(PARAMETER_CHOICE)
        val choices: Collection<String>?,
        @JsonProperty(PARAMETER_DEFAULT_PARAM)
        val defaultValue : ParsedParameterDefinitionDefault?
)