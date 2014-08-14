###############################################################################
# Copyright (c) 2005-2009 Sunil Kamath (IcemanK).
# All rights reserved.
# This program is made available under the terms of the Common Public License
# v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
#
# Contributors:
#     Sunil Kamath (IcemanK) - initial API and implementation
###############################################################################
!ifdef PREVIEW_MUI
!include "MUI.nsh"
!endif
XPStyle on
!ifdef WINDOWS_VISTA
RequestExecutionLevel user
!endif
!ifndef PREVIEW_NAME
!define PREVIEW_NAME "InstallOptions Preview"
!endif
Name "${PREVIEW_NAME}"
OutFile "preview.exe"

ReserveFile "${NSISDIR}\Plugins\InstallOptions.dll"
ReserveFile "${PREVIEW_INI}"
!ifndef PREVIEW_LANG
!define PREVIEW_LANG "English"
!endif
!ifdef PREVIEW_MUI
!insertmacro MUI_LANGUAGE "${PREVIEW_LANG}"
!else
LoadLanguageFile "${NSISDIR}\Contrib\Language files\${PREVIEW_LANG}.nlf"
!endif

;Order of pages
Page custom dummy1 "" ""
Page custom Preview LeavePreview ""
Page custom dummy2 "" ""

!ifndef PREVIEW_MUI
!ifndef PREVIEW_BRANDING
!define PREVIEW_BRANDING "Click any button to close"
!endif
BrandingText "${PREVIEW_BRANDING}"

Function .onGUIInit
  GetDlgItem $0 $HWNDPARENT 1028
  EnableWindow $0 1
FunctionEnd
!endif

Section
SectionEnd

Function LeavePreview
  Push $R0
  ReadINIStr $R0 "$PLUGINSDIR\preview.ini" "Settings" "State"
  StrCmp $R0 0 done
  Pop $R0
  Abort

done:
  Pop $R0
FunctionEnd

Function Preview
  BringToFront
  InitPluginsDir
  Push $R0
  File /oname=$PLUGINSDIR\preview.ini "${PREVIEW_INI}"
!ifdef PREVIEW_MUI
!ifndef PREVIEW_TITLE
  !define PREVIEW_TITLE "InstallOptions Preview"
!endif
!ifndef PREVIEW_SUBTITLE
  !define PREVIEW_SUBTITLE "Click any button to close"
!endif
  !insertmacro MUI_HEADER_TEXT "${PREVIEW_TITLE}" "${PREVIEW_SUBTITLE}"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY_RETURN "preview.ini"
!else
  InstallOptions::dialog "$PLUGINSDIR\preview.ini"
!endif
  Pop $R0
  StrCmp $R0 "success" done
  StrCmp $R0 "back" done
  StrCmp $R0 "cancel" done
  MessageBox MB_OK $R0
done:
  Pop $R0
FunctionEnd

Function dummy1
FunctionEnd
Function dummy2
FunctionEnd
