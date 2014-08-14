/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

public interface INSISEditorPreferenceConstants extends INSISPreferenceConstants
{
    public static final String CUSTOM_TEMPLATES = "customTemplates"; //$NON-NLS-1$

    public static final String TASK_TAGS = "taskTags"; //$NON-NLS-1$
    public static final String CASE_SENSITIVE_TASK_TAGS = "caseSensitiveTaskTags"; //$NON-NLS-1$

    public static final String USE_SPACES_FOR_TABS = "useSpacesForTabs"; //$NON-NLS-1$

    public final static String MATCHING_DELIMITERS = "matchingDelimiters"; //$NON-NLS-1$
    public final static String MATCHING_DELIMITERS_COLOR = "matchingDelimitersColor"; //$NON-NLS-1$

    public static final String COMMENTS_STYLE = "commentsStyle"; //$NON-NLS-1$
    public static final String COMPILETIME_COMMANDS_STYLE = "compiletimeCommandsStyle"; //$NON-NLS-1$
    public static final String INSTALLER_ATTRIBUTES_STYLE = "installerAttributesStyle"; //$NON-NLS-1$
    public static final String COMMANDS_STYLE = "commandsStyle"; //$NON-NLS-1$
    public static final String INSTRUCTIONS_STYLE = "instructionsStyle"; //$NON-NLS-1$
    public static final String INSTRUCTION_PARAMETERS_STYLE = "instructionParametersStyle"; //$NON-NLS-1$
    public static final String INSTRUCTION_OPTIONS_STYLE = "instructionOptionsStyle"; //$NON-NLS-1$
    public static final String PREDEFINED_VARIABLES_STYLE = "predefinedVariablesStyle"; //$NON-NLS-1$
    public static final String USERDEFINED_VARIABLES_STYLE = "userdefinedVariablesStyle"; //$NON-NLS-1$
    public static final String SYMBOLS_STYLE = "symbolsStyle"; //$NON-NLS-1$
    public static final String CALLBACKS_STYLE = "callbacksStyle"; //$NON-NLS-1$
    public static final String STRINGS_STYLE = "stringsStyle"; //$NON-NLS-1$
    public static final String NUMBERS_STYLE = "numbersStyle"; //$NON-NLS-1$
    public static final String LANGSTRINGS_STYLE = "langstringsStyle"; //$NON-NLS-1$
    public static final String TASK_TAGS_STYLE = "taskTagsStyle"; //$NON-NLS-1$
    public static final String PLUGINS_STYLE = "pluginsStyle"; //$NON-NLS-1$

    public static final String DROP_EXTERNAL_FILES_ACTION = "dropExternalFilesAction"; //$NON-NLS-1$

    public static final int DROP_EXTERNAL_FILES_INSERT_AS_NSIS_COMMANDS = 0;
    public static final int DROP_EXTERNAL_FILES_OPEN_IN_EDITORS = 1;
    public static final int DROP_EXTERNAL_FILES_ASK = 2;

    public static final int DROP_EXTERNAL_FILES_DEFAULT = DROP_EXTERNAL_FILES_INSERT_AS_NSIS_COMMANDS;
}
