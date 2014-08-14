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

import net.sf.eclipsensis.console.NSISConsoleLine;


public interface INSISPreferenceConstants extends INSISSettingsConstants
{
    public static final String NSIS_HOME = "nsisHome"; //$NON-NLS-1$
    public static final String NOTIFY_MAKENSIS_CHANGED = "notifyMakeNSISChanged"; //$NON-NLS-1$
    public static final String USE_ECLIPSE_HELP = "useEclipseHelp"; //$NON-NLS-1$

    public static final String WARN_PROCESS_PRIORITY = "warnProcessPriority"; //$NON-NLS-1$
    public static final String WARN_REASSOCIATE_HEADER = "warnReassociateHeader"; //$NON-NLS-1$

    public static final String AUTO_SHOW_CONSOLE = "autoShowConsole"; //$NON-NLS-1$
    public static final int AUTO_SHOW_CONSOLE_NEVER = 0;
    public static final int AUTO_SHOW_CONSOLE_ERROR = NSISConsoleLine.TYPE_ERROR;
    public static final int AUTO_SHOW_CONSOLE_WARNING = AUTO_SHOW_CONSOLE_ERROR|NSISConsoleLine.TYPE_WARNING;
    public static final int AUTO_SHOW_CONSOLE_ALWAYS = AUTO_SHOW_CONSOLE_WARNING|NSISConsoleLine.TYPE_INFO;
    public static final int AUTO_SHOW_CONSOLE_DEFAULT = AUTO_SHOW_CONSOLE_ALWAYS;

    public static final int[] AUTO_SHOW_CONSOLE_ARRAY = {AUTO_SHOW_CONSOLE_ALWAYS, AUTO_SHOW_CONSOLE_WARNING,
                                                         AUTO_SHOW_CONSOLE_ERROR,AUTO_SHOW_CONSOLE_NEVER};
    public static final String CONSOLE_FONT = "net.sf.eclipsensis.console.Font"; //$NON-NLS-1$
    public static final String CONSOLE_INFO_COLOR = "net.sf.eclipsensis.console.InfoColor"; //$NON-NLS-1$
    public static final String CONSOLE_WARNING_COLOR = "net.sf.eclipsensis.console.WarningColor"; //$NON-NLS-1$
    public static final String CONSOLE_ERROR_COLOR = "net.sf.eclipsensis.console.ErrorColor"; //$NON-NLS-1$
    public static final String TEMPLATE_VARIABLE_COLOR = "net.sf.eclipsensis.template.TemplateVariableColor"; //$NON-NLS-1$

    public static final String REG_EXE_LOCATION = "regExeLocation"; //$NON-NLS-1$
    public static final String NSIS_COMMAND_VIEW_FLAT_MODE = "nsisCommandViewFlatMode"; //$NON-NLS-1$
    public static final String NSIS_HELP_VIEW_SHOW_NAV = "nsisHelpViewShowNav"; //$NON-NLS-1$
    public static final String NSIS_HELP_VIEW_SYNCHED = "nsisHelpViewSynched"; //$NON-NLS-1$

    public static final String BEFORE_COMPILE_SAVE = "beforeCompileSave"; //$NON-NLS-1$
    public static final int BEFORE_COMPILE_SAVE_AUTO_FLAG = 0x10;
    public static final int BEFORE_COMPILE_SAVE_CURRENT_CONFIRM = 0x0;
    public static final int BEFORE_COMPILE_SAVE_ASSOCIATED_CONFIRM = 0x1;
    public static final int BEFORE_COMPILE_SAVE_ALL_CONFIRM = 0x2;
    public static final int BEFORE_COMPILE_SAVE_CURRENT_AUTO = BEFORE_COMPILE_SAVE_AUTO_FLAG | BEFORE_COMPILE_SAVE_CURRENT_CONFIRM;
    public static final int BEFORE_COMPILE_SAVE_ASSOCIATED_AUTO = BEFORE_COMPILE_SAVE_AUTO_FLAG | BEFORE_COMPILE_SAVE_ASSOCIATED_CONFIRM;
    public static final int BEFORE_COMPILE_SAVE_ALL_AUTO = BEFORE_COMPILE_SAVE_AUTO_FLAG | BEFORE_COMPILE_SAVE_ALL_CONFIRM;
    public static final int BEFORE_COMPILE_SAVE_DEFAULT = BEFORE_COMPILE_SAVE_CURRENT_CONFIRM;
    public static final int[] BEFORE_COMPILE_SAVE_ARRAY = {BEFORE_COMPILE_SAVE_CURRENT_CONFIRM, BEFORE_COMPILE_SAVE_ASSOCIATED_CONFIRM, BEFORE_COMPILE_SAVE_ALL_CONFIRM,
                                                           BEFORE_COMPILE_SAVE_CURRENT_AUTO, BEFORE_COMPILE_SAVE_ASSOCIATED_AUTO, BEFORE_COMPILE_SAVE_ALL_AUTO};
}
