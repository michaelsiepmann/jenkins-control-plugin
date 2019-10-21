package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_COLOR
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_DISPLAY_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_FULLDISPLAY_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_IS_BUILDABLE
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_IS_IN_QUEUE
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_LAST_BUILD
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_URL
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_PROPERTY
import org.codinjutsu.tools.jenkins.model.Health
import org.codinjutsu.tools.jenkins.model.JobParameterDefinition
import org.codinjutsu.tools.jenkins.model.JobParameter

internal data class ParsedJob(
        @JsonProperty(JOB_NAME)
        val name: String,
        @JsonProperty(JOB_DISPLAY_NAME)
        val displayName: String,
        @JsonProperty(JOB_FULLDISPLAY_NAME)
        val fullDisplayName: String?,
        @JsonProperty(JOB_URL)
        val url: String?,
        @JsonProperty(JOB_COLOR)
        val color: String,
        @JsonProperty(JOB_IS_BUILDABLE)
        val buildable: Boolean,
        @JsonProperty(JOB_IS_IN_QUEUE)
        val inQueue: Boolean,
        @JsonProperty(JOB_LAST_BUILD)
        val lastBuild: ParsedBuild?,
        @JsonProperty(JOB_HEALTH)
        val healths: Collection<Health>?,
        @JsonProperty(PARAMETER_PROPERTY)
        private val parameters: Collection<JobParameter>?
) {
    fun getParameters(): Collection<JobParameterDefinition> {
        if (parameters == null) {
            return emptyList()
        }
        return parameters.mapNotNull { it.definitions }.flatten()
    }
}