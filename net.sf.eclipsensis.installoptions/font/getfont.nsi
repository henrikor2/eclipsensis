!include "WinMessages.nsh"
Name GetFont
OutFile getfont.exe
!ifdef WINDOWS_VISTA
RequestExecutionLevel user
!endif

LoadLanguageFile "${NSISDIR}\Contrib\Language Files\${LANGUAGE}.nlf"

Function .onGuiInit
    Push $0 ;HFONT
    Push $1 ;LOGFONT
    Push $2 ;sizeof(LOGFONT)
    Push $3 ;return value

    Push $4 ;File handle

    Push $R0 ;lfHeight
    Push $R1 ;lfWidth
    Push $R2 ;lfEscapement
    Push $R3 ;lfOrientation
    Push $R4 ;lfWeight
    Push $R5 ;lfItalic/lfUnderline/lfStrikeOut/lfCharSet
    Push $R6 ;lfOutPrecision/lfClipPrecision/lfQuality/lfPitchAndFamily
    Push $R7 ;lfFaceName

    SendMessage $HWNDPARENT ${WM_GETFONT} 0 0 $0
    ReadRegStr $1 HKLM "SOFTWARE\Microsoft\Windows NT\CurrentVersion" CurrentVersion
    IfErrors 0 winnt
    StrCpy $2 60
    GoTo next
winnt:
    StrCpy $2 92

next:
    System::Alloc $2
    Pop $1

    System::Call "gdi32::GetObjectA(i $0, i $2, i $1)i .r3"

    IntOp $2 $2 - 32
    System::Alloc $2
    Pop $R7
    System::Call "*$1(i .R0, i .R1, i .R2, i .R3, i .R4, i .R5, i .R6, &t$2 .R7)"

    FileOpen $4 "${PROPERTIES_FILE}" "w"
    FileWrite $4 "name=$R7$\r$\n"
    FileWrite $4 "size=$(^FontSize)$\r$\n"
    FileClose $4
    System::Free $R7
    System::Free $1
    Quit
FunctionEnd

Section
SectionEnd
