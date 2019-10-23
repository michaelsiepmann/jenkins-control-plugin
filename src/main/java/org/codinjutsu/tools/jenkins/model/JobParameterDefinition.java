/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.jenkins.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.codinjutsu.tools.jenkins.logic.json.ParsedParameterDefinitionDefault;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_CHOICE;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DEFAULT_PARAM;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_DESCRIPTION;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_NAME;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.PARAMETER_TYPE;

public class JobParameterDefinition {

    public enum JobParameterType {
        ChoiceParameterDefinition,
        BooleanParameterDefinition,
        StringParameterDefinition,
        PasswordParameterDefinition,
        FileParameterDefinition,
        TextParameterDefinition,
        RunParameterDefinition,
        ListSubversionTagsParameterDefinition
    }

    private final String name;
    private final String description;
    private final JobParameterType jobParameterType;
    private final String defaultValue;
    private final List<String> values = new LinkedList<>();

    @SuppressWarnings("unused")
    @JsonCreator
    public JobParameterDefinition(
            @JsonProperty(PARAMETER_NAME)
                    String name,
            @JsonProperty(PARAMETER_DESCRIPTION)
                    String description,
            @JsonProperty(PARAMETER_TYPE)
                    String jobParameterType,
            @JsonProperty(PARAMETER_DEFAULT_PARAM)
                    ParsedParameterDefinitionDefault defaultValue,
            @JsonProperty(PARAMETER_CHOICE)
                    Collection<String> choices
    ) {
        this(name, description, jobParameterType, defaultValue != null ? defaultValue.getValue() : null, choices);
    }

    private JobParameterDefinition(String name, String description, String jobParameterType, String defaultValue, Collection<String> choices) {
        this.name = name;
        this.description = description;
        this.jobParameterType = evaluate(jobParameterType);
        this.defaultValue = defaultValue;
        if (choices != null) {
            values.addAll(choices);
        }
    }

    public static JobParameterDefinition create(String paramName, String paramType, String defaultValue, String... choices) {
        return new JobParameterDefinition(paramName, null, paramType, defaultValue, asList(choices));
    }

    public String getName() {
        return name;
    }

    public JobParameterType getJobParameterType() {
        return jobParameterType;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public List<String> getValues() {
        return values;
    }

    public String getDescription() {
        return description;
    }

    private static JobParameterType evaluate(String paramTypeToEvaluate) {
        return Arrays.stream(JobParameterType.values())
                     .filter(parameterType -> parameterType.name().equals(paramTypeToEvaluate))
                     .findFirst()
                     .orElse(null);
    }
}
