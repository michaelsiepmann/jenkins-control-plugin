package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOBS

internal data class ParsedJobs(
        @JsonProperty(JOBS)
        val jobs: Collection<ParsedJob>
)