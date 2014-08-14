/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.makensis.MakeNSISRunner;

public class NSISWizardDisplayValues implements INSISWizardConstants
{
    public static final String[] INSTALLER_TYPE_NAMES = new String[INSTALLER_TYPE_MUI2+1];
    public static final String[] LICENSE_BUTTON_TYPE_NAMES = new String[LICENSE_BUTTON_RADIO+1];
    public static final String[] COMPRESSOR_TYPE_NAMES = new String[MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY.length-1];
    public static final String[] HKEY_NAMES = new String[HKPD+1];
    public static final String[] SHORTCUT_TYPE_NAMES = new String[SHORTCUT_INSTALLELEMENT+1];
    public static final String[] OVERWRITE_MODE_NAMES = new String[OVERWRITE_IFDIFF+1];
    public static final String[] REG_VALUE_TYPES = new String[REG_BIN+1];
    public static final String[] LIBTYPES = new String[LIBTYPE_REGEXE+1];
    public static final String[] EXECUTION_LEVELS = new String[EXECUTION_LEVEL_HIGHEST+1];
    public static final String[] TARGET_PLATFORMS = new String[TARGET_PLATFORM_X64+1];
    public static final String[] MULTIUSER_EXEC_LEVELS = new String[MULTIUSER_EXEC_LEVEL_HIGHEST+1];
    public static final String[] MULTIUSER_INSTALL_MODES = new String[MULTIUSER_INSTALL_MODE_CURRENTUSER+1];

    static {
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_SILENT] = EclipseNSISPlugin.getResourceString("installer.type.silent"); //$NON-NLS-1$
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_CLASSIC] = EclipseNSISPlugin.getResourceString("installer.type.classic"); //$NON-NLS-1$
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_MUI] = EclipseNSISPlugin.getResourceString("installer.type.mui"); //$NON-NLS-1$
        INSTALLER_TYPE_NAMES[INSTALLER_TYPE_MUI2] = EclipseNSISPlugin.getResourceString("installer.type.mui2"); //$NON-NLS-1$

        LICENSE_BUTTON_TYPE_NAMES[LICENSE_BUTTON_CLASSIC] = EclipseNSISPlugin.getResourceString("license.button.classic"); //$NON-NLS-1$
        LICENSE_BUTTON_TYPE_NAMES[LICENSE_BUTTON_CHECKED] = EclipseNSISPlugin.getResourceString("license.button.checked"); //$NON-NLS-1$
        LICENSE_BUTTON_TYPE_NAMES[LICENSE_BUTTON_RADIO] = EclipseNSISPlugin.getResourceString("license.button.radio"); //$NON-NLS-1$

        System.arraycopy(MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY,0,COMPRESSOR_TYPE_NAMES,0,COMPRESSOR_TYPE_NAMES.length);
        COMPRESSOR_TYPE_NAMES[MakeNSISRunner.COMPRESSOR_DEFAULT] = EclipseNSISPlugin.getResourceString("default.compressor.label"); //$NON-NLS-1$

        SHORTCUT_TYPE_NAMES[SHORTCUT_URL] = EclipseNSISPlugin.getResourceString("shortcut.type.url"); //$NON-NLS-1$
        SHORTCUT_TYPE_NAMES[SHORTCUT_INSTALLELEMENT] = EclipseNSISPlugin.getResourceString("shortcut.type.installelement"); //$NON-NLS-1$

        OVERWRITE_MODE_NAMES[OVERWRITE_ON] = EclipseNSISPlugin.getResourceString("overwrite.on"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_OFF] = EclipseNSISPlugin.getResourceString("overwrite.off"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_TRY] = EclipseNSISPlugin.getResourceString("overwrite.try"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_NEWER] = EclipseNSISPlugin.getResourceString("overwrite.newer"); //$NON-NLS-1$;
        OVERWRITE_MODE_NAMES[OVERWRITE_IFDIFF] = EclipseNSISPlugin.getResourceString("overwrite.ifdiff"); //$NON-NLS-1$;

        REG_VALUE_TYPES[REG_SZ] = EclipseNSISPlugin.getResourceString("reg.value.string"); //$NON-NLS-1$;
        REG_VALUE_TYPES[REG_DWORD] = EclipseNSISPlugin.getResourceString("reg.value.dword"); //$NON-NLS-1$;
        REG_VALUE_TYPES[REG_EXPAND_SZ] = EclipseNSISPlugin.getResourceString("reg.value.expandstring"); //$NON-NLS-1$;
        REG_VALUE_TYPES[REG_BIN] = EclipseNSISPlugin.getResourceString("reg.value.bin"); //$NON-NLS-1$;

        LIBTYPES[LIBTYPE_DLL] = EclipseNSISPlugin.getResourceString("lib.type.dll"); //$NON-NLS-1$;
        LIBTYPES[LIBTYPE_REGDLL] = EclipseNSISPlugin.getResourceString("lib.type.regdll"); //$NON-NLS-1$;
        LIBTYPES[LIBTYPE_TLB] = EclipseNSISPlugin.getResourceString("lib.type.tlb"); //$NON-NLS-1$;
        LIBTYPES[LIBTYPE_REGDLLTLB] = EclipseNSISPlugin.getResourceString("lib.type.regdlltlb"); //$NON-NLS-1$;
        LIBTYPES[LIBTYPE_REGEXE] = EclipseNSISPlugin.getResourceString("lib.type.regexe"); //$NON-NLS-1$;

        EXECUTION_LEVELS[EXECUTION_LEVEL_NONE] = EclipseNSISPlugin.getResourceString("execution.level.none"); //$NON-NLS-1$;
        EXECUTION_LEVELS[EXECUTION_LEVEL_USER] = EclipseNSISPlugin.getResourceString("execution.level.user"); //$NON-NLS-1$;
        EXECUTION_LEVELS[EXECUTION_LEVEL_ADMIN] = EclipseNSISPlugin.getResourceString("execution.level.admin"); //$NON-NLS-1$;
        EXECUTION_LEVELS[EXECUTION_LEVEL_HIGHEST] = EclipseNSISPlugin.getResourceString("execution.level.highest"); //$NON-NLS-1$;

        HKEY_NAMES[HKCR] = "HKEY_CLASSES_ROOT"; //$NON-NLS-1$
        HKEY_NAMES[HKLM] = "HKEY_LOCAL_MACHINE"; //$NON-NLS-1$
        HKEY_NAMES[HKCU] = "HKEY_CURRENT_USER"; //$NON-NLS-1$
        HKEY_NAMES[HKU] = "HKEY_USERS"; //$NON-NLS-1$
        HKEY_NAMES[HKCC] = "HKEY_CURRENT_CONFIG"; //$NON-NLS-1$
        HKEY_NAMES[HKDD] = "HKEY_DYN_DATA"; //$NON-NLS-1$
        HKEY_NAMES[HKPD] = "HKEY_PERFORMANCE_DATA"; //$NON-NLS-1$

        TARGET_PLATFORMS[TARGET_PLATFORM_ANY] = EclipseNSISPlugin.getResourceString("target.platform.any"); //$NON-NLS-1$;
        TARGET_PLATFORMS[TARGET_PLATFORM_X86] = EclipseNSISPlugin.getResourceString("target.platform.x86"); //$NON-NLS-1$;
        TARGET_PLATFORMS[TARGET_PLATFORM_X64] = EclipseNSISPlugin.getResourceString("target.platform.x64"); //$NON-NLS-1$;

        MULTIUSER_EXEC_LEVELS[MULTIUSER_EXEC_LEVEL_STANDARD] = EclipseNSISPlugin.getResourceString("multiuser.exec.level.standard"); //$NON-NLS-1$
        MULTIUSER_EXEC_LEVELS[MULTIUSER_EXEC_LEVEL_POWER] = EclipseNSISPlugin.getResourceString("multiuser.exec.level.power"); //$NON-NLS-1$
        MULTIUSER_EXEC_LEVELS[MULTIUSER_EXEC_LEVEL_ADMIN] = EclipseNSISPlugin.getResourceString("multiuser.exec.level.admin"); //$NON-NLS-1$
        MULTIUSER_EXEC_LEVELS[MULTIUSER_EXEC_LEVEL_HIGHEST] = EclipseNSISPlugin.getResourceString("multiuser.exec.level.highest"); //$NON-NLS-1$

        MULTIUSER_INSTALL_MODES[MULTIUSER_INSTALL_MODE_ALLUSERS] = EclipseNSISPlugin.getResourceString("multiuser.install.mode.all.users"); //$NON-NLS-1$
        MULTIUSER_INSTALL_MODES[MULTIUSER_INSTALL_MODE_CURRENTUSER] = EclipseNSISPlugin.getResourceString("multiuser.install.mode.current.user"); //$NON-NLS-1$
    }

    public static int getHKeyIndex(String rootKey)
    {
        for (int i = 0; i < NSISWizardDisplayValues.HKEY_NAMES.length; i++) {
            if(NSISWizardDisplayValues.HKEY_NAMES[i].equalsIgnoreCase(rootKey)) {
                return i;
            }
        }
        return -1;
    }
}
