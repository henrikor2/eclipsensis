/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import net.sf.eclipsensis.util.winapi.WinAPI.HKEY;

import org.eclipse.core.runtime.QualifiedName;

public interface INSISConstants
{
    public static final String PLUGIN_ID = EclipseNSISPlugin.getDefault().getBundle().getSymbolicName();

    public static final String MAKENSIS_EXE = "makensis.exe"; //$NON-NLS-1$
    public static final String NSISCONF_NSH = "nsisconf.nsh"; //$NON-NLS-1$
    public static final String NSI_EXTENSION = "nsi"; //$NON-NLS-1$
    public static final String NSH_EXTENSION = "nsh"; //$NON-NLS-1$
    public static final String NSI_WILDCARD_EXTENSION = "ns[hi]"; //$NON-NLS-1$

    public static final String PLUGIN_CONTEXT_PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String FILE_ASSOCIATION_ID = EclipseNSISPlugin.getBundleResourceString("%file.association.id"); //$NON-NLS-1$
    public static final String EDITOR_ID = EclipseNSISPlugin.getBundleResourceString("%editor.id"); //$NON-NLS-1$
    public static final String PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%preference.page.id"); //$NON-NLS-1$
    public static final String EDITOR_PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%editor.preference.page.id"); //$NON-NLS-1$
    public static final String TEMPLATES_PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%template.preference.page.id"); //$NON-NLS-1$
    public static final String TASKTAGS_PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%task.tags.preference.page.id"); //$NON-NLS-1$
    public static final String HTMLHELP_ID = EclipseNSISPlugin.getBundleResourceString("%htmlhelp.id"); //$NON-NLS-1$
    public static final String COMMANDS_VIEW_ID = EclipseNSISPlugin.getBundleResourceString("%commands.view.id"); //$NON-NLS-1$
    public static final String PROBLEM_MARKER_ID = EclipseNSISPlugin.getBundleResourceString("%compile.problem.marker.id"); //$NON-NLS-1$
    public static final String ERROR_ANNOTATION_NAME = EclipseNSISPlugin.getBundleResourceString("%nsis.error.annotation.name"); //$NON-NLS-1$
    public static final String WARNING_ANNOTATION_NAME = EclipseNSISPlugin.getBundleResourceString("%nsis.warning.annotation.name"); //$NON-NLS-1$
    public static final String TASK_MARKER_ID = EclipseNSISPlugin.getBundleResourceString("%task.marker.id"); //$NON-NLS-1$
    public static final String NSIS_EDITOR_CONTEXT_ID = EclipseNSISPlugin.getBundleResourceString("%context.editingNSISSource.id"); //$NON-NLS-1$
    public static final String COMPILE_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%compile.action.id"); //$NON-NLS-1$
    public static final String COMPILE_TEST_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%compile.test.action.id"); //$NON-NLS-1$
    public static final String TEST_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%test.action.id"); //$NON-NLS-1$
    public static final String CLEAR_MARKERS_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%clear.markers.action.id"); //$NON-NLS-1$
    public static final String INSERT_TEMPLATE_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.template.command.id"); //$NON-NLS-1$
    public static final String GOTO_HELP_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%goto.help.command.id"); //$NON-NLS-1$
    public static final String STICKY_HELP_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%sticky.help.command.id"); //$NON-NLS-1$
    public static final String INSERT_FILE_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.file.command.id"); //$NON-NLS-1$
    public static final String INSERT_DIRECTORY_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.directory.command.id"); //$NON-NLS-1$
    public static final String INSERT_COLOR_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.color.command.id"); //$NON-NLS-1$
    public static final String INSERT_REGFILE_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.regfile.command.id"); //$NON-NLS-1$
    public static final String INSERT_REGKEY_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.regkey.command.id"); //$NON-NLS-1$
    public static final String INSERT_REGVAL_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.regval.command.id"); //$NON-NLS-1$
    public static final String TABS_TO_SPACES_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%tabs.to.spaces.command.id"); //$NON-NLS-1$
    public static final String TOGGLE_COMMENT_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%toggle.comment.command.id"); //$NON-NLS-1$
    public static final String ADD_BLOCK_COMMENT_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%add.block.comment.command.id"); //$NON-NLS-1$
    public static final String REMOVE_BLOCK_COMMENT_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%remove.block.comment.command.id"); //$NON-NLS-1$
    public static final String OPEN_ASSOCIATED_SCRIPT_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%open.associated.script.action.id"); //$NON-NLS-1$
    public static final String OPEN_ASSOCIATED_HEADERS_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%open.associated.headers.action.id"); //$NON-NLS-1$
    public static final String OPEN_ASSOCIATED_SCRIPT_POPUP_MENU_ID = EclipseNSISPlugin.getBundleResourceString("%open.associated.script.popup.menu.id"); //$NON-NLS-1$
    public static final String OPEN_ASSOCIATED_HEADERS_POPUP_MENU_ID = EclipseNSISPlugin.getBundleResourceString("%open.associated.headers.popup.menu.id"); //$NON-NLS-1$

    public static final String PLUGIN_HELP_LOCATION_PREFIX = "help/"; //$NON-NLS-1$
    public static final String NSISCONTRIB_JS_LOCATION = PLUGIN_HELP_LOCATION_PREFIX + "nsiscontrib.js"; //$NON-NLS-1$
    public static final String DOCS_LOCATION_PREFIX = "Docs/"; //$NON-NLS-1$
    public static final String KEYWORD_PREFIX = "keyword/"; //$NON-NLS-1$
    public static final String CONTRIB_LOCATION_PREFIX = "Contrib/"; //$NON-NLS-1$
    public static final String PLUGIN_HELP_DOCS_LOCATION_PREFIX = PLUGIN_HELP_LOCATION_PREFIX+DOCS_LOCATION_PREFIX;
    public static final String NSIS_PLATFORM_HELP_PREFIX = PLUGIN_HELP_LOCATION_PREFIX+"NSIS/"; //$NON-NLS-1$
    public static final String NSIS_PLATFORM_HELP_DOCS_PREFIX = NSIS_PLATFORM_HELP_PREFIX+DOCS_LOCATION_PREFIX;
    public static final String NSIS_CHM_HELP_FILE = "NSIS.chm"; //$NON-NLS-1$
    public static final String LANGUAGE_FILES_LOCATION = "Contrib\\Language files"; //$NON-NLS-1$
    public static final String MUI_LANGUAGE_FILES_LOCATION = "Contrib\\Modern UI\\Language files"; //$NON-NLS-1$
    public static final String LANGUAGE_FILES_EXTENSION = ".nlf"; //$NON-NLS-1$
    public static final String MUI_LANGUAGE_FILES_EXTENSION = ".nsh"; //$NON-NLS-1$
    public static final String RESOURCE_BUNDLE = "net.sf.eclipsensis.EclipseNSISPluginResources"; //$NON-NLS-1$
    public static final String MESSAGE_BUNDLE = "net.sf.eclipsensis.EclipseNSISPluginMessages"; //$NON-NLS-1$

    public static final QualifiedName NSIS_COMPILE_TIMESTAMP = new QualifiedName(PLUGIN_ID,"nsisCompileTimestamp"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_NAME = new QualifiedName(PLUGIN_ID,"nsisEXEName"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_TIMESTAMP = new QualifiedName(PLUGIN_ID,"nsisEXETimestamp"); //$NON-NLS-1$

    public static final char LINE_CONTINUATION_CHAR = '\\';
    public static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    public static final char[][] QUOTE_ESCAPE_SEQUENCES = {{'$','\\','"'},{'$','\\','\''},{'$','\\','`'}};
    public static final char[][] WHITESPACE_ESCAPE_SEQUENCES = {{'$','\\','r'},{'$','\\','n'},{'$','\\','t'}};

    public static final int DIALOG_TEXT_LIMIT = 100;
    public static final int DEFAULT_NSIS_TEXT_LIMIT = 1024;

    public static final String UNINSTALL_SECTION_NAME = "Uninstall"; //$NON-NLS-1$

    public static final String NSIS_PLUGINS_LOCATION = "Plugins"; //$NON-NLS-1$
    public static final String NSIS_PLUGINS_EXTENSION = ".dll"; //$NON-NLS-1$

    public static final HKEY NSIS_REG_ROOTKEY = HKEY.HKEY_LOCAL_MACHINE;

    public static final String NSIS_REG_SUBKEY = "SOFTWARE\\NSIS"; //$NON-NLS-1$

    public static final String NSIS_REG_VALUE = ""; //$NON-NLS-1$

    public static final String REG_FILE_EXTENSION = ".reg"; //$NON-NLS-1$
}
