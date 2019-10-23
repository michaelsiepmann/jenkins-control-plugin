package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class TestResult(
        @JsonProperty("suites")
        val suites: Collection<TestSuites>?
)