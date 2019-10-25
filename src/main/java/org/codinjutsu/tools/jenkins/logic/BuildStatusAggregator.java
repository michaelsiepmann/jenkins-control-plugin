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

package org.codinjutsu.tools.jenkins.logic;

public class BuildStatusAggregator implements BuildStatusVisitor {

    private int nbBrokenBuilds = 0;

    private int nbSucceededBuilds = 0;

    private int nbUnstableBuilds = 0;

    private int nbAbortedBuilds = 0;

    private int nbJobs;

    public static BuildStatusAggregator EMPTY = new BuildStatusAggregator(0);

    public BuildStatusAggregator(int nbJobs) {
        this.nbJobs = nbJobs;
    }

    @Override
    public void visitFailed() {
        nbBrokenBuilds++;
    }

    @Override
    public void visitSuccess() {
        nbSucceededBuilds++;
    }

    @Override
    public void visitUnstable() {
        nbUnstableBuilds++;
    }

    @Override
    public void visitUnknown() {
    }

    @Override
    public void visitAborted() {
        nbAbortedBuilds++;
    }

    public int getNbBrokenBuilds() {
        return nbBrokenBuilds;
    }

    public int getNbUnstableBuilds() {
        return nbUnstableBuilds;
    }

    public boolean hasNoResults() {
        return nbJobs == 0 || sumAll() == 0;
    }

    public int sumAll() {
        return nbSucceededBuilds + nbUnstableBuilds + nbBrokenBuilds + nbAbortedBuilds;
    }
}
