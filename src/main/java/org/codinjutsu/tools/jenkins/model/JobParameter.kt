package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DEFINITIONS

internal data class JobParameter(
        @JsonProperty(PARAMETER_DEFINITIONS)
        val definitions: Collection<JobParameterDefinition>?
)