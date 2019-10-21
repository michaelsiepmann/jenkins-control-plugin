package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOBS

internal data class Jobs(
        @JsonProperty(JOBS)
        val jobs: Collection<Job>
)