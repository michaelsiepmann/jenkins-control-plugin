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

import org.codinjutsu.tools.jenkins.logic.BuildStatusVisitor;

public enum BuildStatusEnum {

    FAILURE("Failure", "red") {
        @Override
        public void visit(BuildStatusVisitor visitor) {
            visitor.visitFailed();
        }
    },
    UNSTABLE("Unstable", "yellow") {
        @Override
        public void visit(BuildStatusVisitor visitor) {
            visitor.visitUnstable();
        }
    },
    ABORTED("Aborted", "aborted") {
        @Override
        public void visit(BuildStatusVisitor visitor) {
            visitor.visitAborted();
        }
    },
    SUCCESS("Success", "blue") {
        @Override
        public void visit(BuildStatusVisitor visitor) {
            visitor.visitSuccess();
        }
    },
    STABLE("Stable", "blue"){
        @Override
        public void visit(BuildStatusVisitor visitor) {
        }
    },
    NULL("Null", "disabled"){
        @Override
        public void visit(BuildStatusVisitor visitor) {
            visitor.visitUnknown();
        }
    },
    // TODO: handle the folder-case explicitly
    // instead of simply making it a BuildStatusEnum so that the icon renders
    FOLDER("Folder", "disabled") {
        @Override
        public void visit(BuildStatusVisitor visitor) {
        }
    };

    private final String status;
    private final String color;

    BuildStatusEnum(String status, String color) {
        this.status = status;
        this.color = color;
    }

    public static BuildStatusEnum parseStatus(String status) {
        try {
            if (status == null || "null".equals(status)) {
                status = "NULL";
            }
            return valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.out.println("Unsupported status : " + status);
            return NULL;
        }
    }

    /**
     * Parse status from color
     */
    public static BuildStatusEnum getStatus(String jobColor) {
        if (null == jobColor) {
            return NULL;
        }
        BuildStatusEnum[] jobStates = values();
        for (BuildStatusEnum jobStatus : jobStates) {
            String stateName = jobStatus.getColor();
            if (jobColor.startsWith(stateName)) {
                return jobStatus;
            }
        }

        return NULL;
    }

    public String getStatus() {
        return status;
    }

    public String getColor() {
        return color;
    }

    public abstract void visit(BuildStatusVisitor visitor);
}
