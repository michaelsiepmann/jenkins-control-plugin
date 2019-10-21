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

package org.codinjutsu.tools.jenkins.view;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.codinjutsu.tools.jenkins.model.FavoriteView;
import org.codinjutsu.tools.jenkins.model.View;
import org.codinjutsu.tools.jenkins.util.GuiUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;
import javax.swing.JList;

public class JenkinsNestedViewComboRenderer extends ColoredListCellRenderer<View> {

    private static final Icon FAVORITE_ICON = GuiUtil.loadIcon("star.png");

    @Override
    protected void customizeCellRenderer(@NotNull JList list, View value, int index, boolean selected, boolean hasFocus) {

        if (value != null) {
            if (value.hasNestedView()) {
                setEnabled(false);
                setFocusable(false);
                setBackground(JBColor.LIGHT_GRAY);
                append(value.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            } else {
                String viewName = value.getName();
                if (value.isNested()) {
                    append("   " + viewName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                } else {
                    append(viewName, SimpleTextAttributes.REGULAR_ATTRIBUTES);
                }
            }

            if (value instanceof FavoriteView) {
                setIcon(FAVORITE_ICON);
            }
        }

    }
}
