package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class TestCase(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("className")
        val className: String,
        @JsonProperty("errorDetails")
        val errorDetails: String?,
        @JsonProperty("errorStackTrace")
        val errorStackTrace: String?,
        @JsonProperty("skipped")
        val skipped: Boolean,
        @JsonProperty("duration")
        val duration: Double
) {
        fun getDurationInMS() = (duration * 1000).toLong()
}