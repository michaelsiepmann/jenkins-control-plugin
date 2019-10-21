package org.codinjutsu.tools.jenkins.logic.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_DURATION
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_ID
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_IS_BUILDING
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_NUMBER
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_RESULT
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_TIMESTAMP
import org.codinjutsu.tools.jenkins.logic.JenkinsParser.BUILD_URL

internal data class ParsedBuild(
        @JsonProperty(BUILD_ID)
        val id: String,
        @JsonProperty(BUILD_IS_BUILDING)
        val building: Boolean,
        @JsonProperty(BUILD_NUMBER)
        val number: Int,
        @JsonProperty(BUILD_RESULT)
        val status: String?,
        @JsonProperty(BUILD_URL)
        val url: String,
        @JsonProperty(BUILD_TIMESTAMP)
        val timestamp: Long?,
        @JsonProperty(BUILD_DURATION)
        val duration: Long?
)