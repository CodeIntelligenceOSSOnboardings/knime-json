/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created on 2013.04.25. by Gabor
 */
package org.knime.json.node.patch.apply;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JComponent;
import javax.swing.text.BadLocationException;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.knime.base.node.preproc.stringmanipulation.manipulator.Manipulator;
import org.knime.base.node.util.JSnippetPanel;
import org.knime.base.node.util.JavaScriptingCompletionProvider;
import org.knime.base.node.util.KnimeSyntaxTextArea;
import org.knime.base.node.util.ManipulatorProvider;
import org.knime.core.node.NodeLogger;
import org.knime.json.node.patch.apply.JsonPatchManipulator.FromManipulator;
import org.knime.json.node.patch.apply.JsonPatchManipulator.RemoveManipulator;
import org.knime.json.node.patch.apply.JsonPatchManipulator.ValueManipulator;

/**
 * The main panel (manipulators, columns, flow variables and the editor) of the JSON Patch Apply node dialog.
 *
 * @author Gabor Bakos
 */
@SuppressWarnings("serial")
class JsonPatchMainPanel extends JSnippetPanel {
    private static final NodeLogger LOGGER = NodeLogger.getLogger(JsonPatchMainPanel.class);

    private KnimeSyntaxTextArea m_textEditor;

    /**
     * Constucts the main panel.
     */
    JsonPatchMainPanel() {
        super(new ManipulatorProvider() {

            @Override
            public Collection<? extends Manipulator> getManipulators(final String category) {
                return Arrays.asList(new ValueManipulator("add"), new ValueManipulator("replace"),
                    new RemoveManipulator(), new FromManipulator("copy"), new FromManipulator("move"),
                    new ValueManipulator("test"));
            }

            @Override
            public Collection<String> getCategories() {
                return Collections.singleton(JsonPatchManipulator.JSON_PATCH_CATEGORY);
            }
        }, new JavaScriptingCompletionProvider());
        m_textEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JComponent createEditorComponent() {
        m_textEditor = new KnimeSyntaxTextArea(20, 60);
        final RSyntaxTextArea textArea = m_textEditor;
        // An AutoCompletion acts as a "middle-man" between a text component
        // and a CompletionProvider. It manages any options associated with
        // the auto-completion (the popup trigger key, whether to display a
        // documentation window along with completion choices, etc.). Unlike
        // CompletionProviders, instances of AutoCompletion cannot be shared
        // among multiple text components.
        AutoCompletion ac = new AutoCompletion(getCompletionProvider());
        ac.setShowDescWindow(true);

        ac.install(textArea);
        setExpEdit(textArea);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);

        RTextScrollPane textScrollPane = new RTextScrollPane(textArea);
        textScrollPane.setLineNumbersEnabled(true);
        textScrollPane.setIconRowHeaderEnabled(true);
        return textScrollPane;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSelectionInManipulatorList(final Object selected) {
        if (selected instanceof JsonPatchManipulator) {
            JsonPatchManipulator patch = (JsonPatchManipulator)selected;
            if (m_textEditor.getText().trim().isEmpty()) {
                m_textEditor.setText("[\n]\n");
                m_textEditor.setCaretPosition("[\n".length());
            }
            final int afterFirstBracket = m_textEditor.getText().indexOf('[') + 1;
            if (m_textEditor.getCaretPosition() <= afterFirstBracket) {
                m_textEditor.setCaretPosition(afterFirstBracket);
            }
            int caretPosition = m_textEditor.getCaretPosition();
            boolean isLast = true, isFirst = true;
            try {
                isFirst = m_textEditor.getText().indexOf(',') < 0
                    || m_textEditor.getCaretPosition() < m_textEditor.getText().indexOf(',') + 1;
                isLast = m_textEditor.getText(caretPosition, m_textEditor.getText().length() - caretPosition).trim()
                    .startsWith("]");
            } catch (final BadLocationException e) {
                LOGGER.coding("Not fatal error, but should not happen, requires no action.", e);
            }
            String selectedString = m_textEditor.getSelectedText();
            boolean selectionIsPath = selectedIsPath(selectedString);
            if (selectionIsPath) {
                selectedString = fixQuotes(selectedString);
            }
            String textToInsert = patch.getDisplayName();
            int position = textToInsert.length();
            if (patch instanceof ValueManipulator) {
                if (selectionIsPath) {
                    //Selection is already fixed
                    textToInsert =
                        "{ \"op\": \"" + patch.getName() + "\", \"path\": " + selectedString + ", \"value\":  }";
                    position = textToInsert.length() - 2;
                } else {
                    textToInsert = "{ \"op\": \"" + patch.getName() + "\", \"path\": \"\", \"value\": "
                        + valueOrEmpty(selectedString) + " }";
                    position = ("{ \"op\": \"" + patch.getName() + "\", \"path\": \"").length();
                }
            } else if (patch instanceof RemoveManipulator) {
                textToInsert = "{ \"op\": \"remove\", \"path\": " + valueOrEmpty(selectedString) + " }";
                position = textToInsert.length() - 2;
            } else if (patch instanceof FromManipulator) {
                textToInsert = "{ \"op\": \"" + patch.getName() + "\", \"from\": " + valueOrEmpty(selectedString)
                    + ", \"path\":  }";
                position =
                    selectionIsPath ? textToInsert.length() - 2 : textToInsert.length() - ", \"path\":  }".length();
            }
            if (isLast) {
                if (isFirst) {
                    textToInsert += "\n";
                } else {
                    textToInsert = ",\n" + textToInsert + "\n";
                    position += ",\n".length();
                }
            } else {
                textToInsert += ",\n";
            }
            int origPosition = m_textEditor.getCaretPosition();
            m_textEditor.replaceSelection(textToInsert);
            m_textEditor.setCaretPosition(origPosition + position);
            m_textEditor.requestFocus();

        } else {
            super.onSelectionInManipulatorList(selected);
        }
    }

    /**
     * @param selectedString
     * @return
     */
    private static String valueOrEmpty(final String selectedString) {
        return selectedString == null ? "" : selectedString;
    }

    /**
     * @param selectedString
     * @return
     */
    private static String fixQuotes(final String selectedString) {
        int length = selectedString.length();
        return selectedString.isEmpty() ? selectedString
            : selectedString.charAt(0) == '"' && length > 1 && selectedString.charAt(length - 1) != '"'
                ? selectedString + '"'
                : selectedString.charAt(0) == '/' ? fixQuotes('"' + selectedString) : selectedString;
    }

    private static boolean selectedIsPath(final String selection) {
        return selection != null && (selection.startsWith("\"/") || selection.startsWith("/"));
    }

    /**
     * @return the textEditor
     */
    public KnimeSyntaxTextArea getTextEditor() {
        return m_textEditor;
    }
}
