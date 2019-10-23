package org.codinjutsu.tools.jenkins.model

import com.fasterxml.jackson.annotation.JsonProperty

internal data class TestSuites(
        @JsonProperty("name")
        val name: String,
        @JsonProperty("cases")
        val cases: Collection<TestCase>
)