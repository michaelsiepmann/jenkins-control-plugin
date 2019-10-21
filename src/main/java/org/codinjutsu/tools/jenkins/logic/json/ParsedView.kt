package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOBS
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEWS
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEW_NAME
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.VIEW_URL

internal data class ParsedView(
        @JsonProperty(VIEW_NAME)
        val name: String?,
        @JsonProperty(VIEW_URL)
        val url: String,
        @JsonProperty(VIEWS)
        val subViews: Collection<ParsedView>?,
        @JsonProperty(JOBS)
        val jobs: Collection<ParsedJob>?
)