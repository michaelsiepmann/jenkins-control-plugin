package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOBS
import org.codinjutsu.tools.jenkins.model.Job

internal data class ParsedJobs(
        @JsonProperty(JOBS)
        val jobs: Collection<Job>
)