package org.codinjutsu.tools.jenkins.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH_DESCRIPTION;
import static org.codinjutsu.tools.jenkins.logic.JenkinsParser.JOB_HEALTH_ICON;

public class Health {

    private static final Collection<String> SUFFIXES = Arrays.asList(".png", ".gif");

    private String healthLevel;
    private String description;

    @JsonCreator
    public Health(
            @JsonProperty(JOB_HEALTH_ICON)
                    String healthLevel,
            @JsonProperty(JOB_HEALTH_DESCRIPTION)
                    String description
    ) {
        this.healthLevel = getHealthLevel(healthLevel);
        this.description = description;
    }

    public String getLevel() {
        return healthLevel;
    }

    public void setLevel(String healthLevel) {
        this.healthLevel = healthLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static Health createHealth(String healthLevel, String healthDescription) {
        return new Health(healthLevel, healthDescription);
    }

    @Nullable
    private String getHealthLevel(String icon) {
        if (isNotEmpty(icon)) {
            return SUFFIXES.stream()
                           .filter(icon::endsWith)
                           .findFirst()
                           .map(suffix -> icon.substring(0, icon.lastIndexOf(suffix)))
                           .orElse(icon);
        }
        return icon;
    }
}
