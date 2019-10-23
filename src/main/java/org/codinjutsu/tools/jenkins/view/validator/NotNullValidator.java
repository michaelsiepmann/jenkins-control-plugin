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

package org.codinjutsu.tools.jenkins.view.validator;

import org.apache.commons.lang3.StringUtils;
import org.codinjutsu.tools.jenkins.exception.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTextField;

public class NotNullValidator implements UIValidator<JTextField> {
    @Override
    public void validate(@NotNull JTextField component) throws ConfigurationException {
        if (component.isEnabled()) {    //TODO a revoir
            String value = component.getText();
            if (StringUtils.isEmpty(value)) {
                throw new ConfigurationException(String.format("'%s' must be set", component.getName()));
            }
        }
    }
}
