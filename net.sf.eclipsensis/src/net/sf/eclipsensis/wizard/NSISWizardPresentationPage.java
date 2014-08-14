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

import java.io.File;
import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.util.*;
import net.sf.eclipsensis.util.winapi.*;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;
import net.sf.eclipsensis.wizard.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

public class NSISWizardPresentationPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardPresentation"; //$NON-NLS-1$

    private static final long DEFAULT_RESOLUTION = 32; //32 FPS
    private static final double OS_VERSION;

    private static final int LICDATA_CHECK=0x1;
    private static final int SPLIMG_CHECK=0x10;
    private static final int SPLWAV_CHECK=0x100;
    private static final int SPLDLY_CHECK=0x1000;
    private static final int BGIMG_CHECK=0x10000;
    private static final int BGWAV_CHECK=0x100000;

    private static final String[] cLicFileErrors = {"empty.license.file.error"}; //$NON-NLS-1$
    private static final String[] cSplashImageErrors = {"empty.splash.image.error"}; //$NON-NLS-1$
    private static final String[] cSplashDelayErrors = {"zero.splash.delay.error"}; //$NON-NLS-1$

    private FontData mBGPreviewFontData;
    private FontData mBGPreviewEscapeFontData;
    private Point mBGPreviewTextLocation;
    private int mBGPreviewGradientHeight;

    static {
        double version;
        try {
            version = Double.parseDouble(System.getProperty("os.version")); //$NON-NLS-1$
        }
        catch(Exception ex) {
            version = 0;
        }
        OS_VERSION = version;
    }

    /**
     * @param pageName
     * @param title
     */
    public NSISWizardPresentationPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.presentation.title"), //$NON-NLS-1$
                        EclipseNSISPlugin.getResourceString("wizard.presentation.description")); //$NON-NLS-1$
        mBGPreviewFontData = new FontData(EclipseNSISPlugin.getResourceString("background.preview.font","1|Times New Roman|24|3|WINDOWS|1|-53|0|0|0|700|1|0|0|1|0|0|0|0|Times New Roman")); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewEscapeFontData = new FontData(EclipseNSISPlugin.getResourceString("background.preview.escape.font","1|Times New Roman|12|1|WINDOWS|1|0|0|0|0|700|0|0|0|1|0|0|0|0|Times New Roman")); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewTextLocation = new Point(Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.text.x","16")), //$NON-NLS-1$ //$NON-NLS-2$
                        Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.text.y","8"))); //$NON-NLS-1$ //$NON-NLS-2$
        mBGPreviewGradientHeight = Integer.parseInt(EclipseNSISPlugin.getResourceString("background.preview.gradient.height","4")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected boolean hasRequiredFields()
    {
        return isScriptWizard();
    }

    @Override
    protected String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizpresentation_context"; //$NON-NLS-1$
    }

    @Override
    protected Control createPageControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();

        createLicenseGroup(composite, bundle);
        createSplashGroup(composite, bundle);
        createBackgroundGroup(composite, bundle);

        setPageComplete(validatePage(VALIDATE_ALL));

        return composite;
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createBackgroundGroup(Composite parent, ResourceBundle bundle)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group backgroundGroup = NSISWizardDialogUtil.createGroup(parent, 1, "background.group.label", null, false);  //$NON-NLS-1$

        final Button showBackground = NSISWizardDialogUtil.createCheckBox(backgroundGroup,"show.background.label",settings.isShowBackground(), //$NON-NLS-1$
                        true, null, false);

        final MasterSlaveController m = new MasterSlaveController(showBackground);

        Composite composite = new Composite(backgroundGroup,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Text backgroundImage = NSISWizardDialogUtil.createFileBrowser(composite, settings.getBackgroundBMP(), false,
                        Common.loadArrayProperty(bundle,"background.image.filternames"),  //$NON-NLS-1$
                        Common.loadArrayProperty(bundle,"background.image.filters"), "background.image.label", //$NON-NLS-1$ //$NON-NLS-2$
                        true,m, false);

        final Text backgroundSound = NSISWizardDialogUtil.createFileBrowser(composite, settings.getBackgroundWAV(), false,
                        Common.loadArrayProperty(bundle,"background.sound.filternames"),  //$NON-NLS-1$
                        Common.loadArrayProperty(bundle,"background.sound.filters"), "background.sound.label", //$NON-NLS-1$ //$NON-NLS-2$
                        true, m, false);

        composite = new Composite(backgroundGroup,SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Group backgroundColorGroup = NSISWizardDialogUtil.createGroup(composite, 3, "background.colors.label", m, false); //$NON-NLS-1$
        ((GridLayout)backgroundColorGroup.getLayout()).makeColumnsEqualWidth = true;
        ((GridData)backgroundColorGroup.getLayoutData()).horizontalSpan = 1;

        String[] labels = {"background.topcolor.label","background.bottomcolor.label","background.textcolor.label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        RGB[] values = {settings.getBGTopColor(),settings.getBGBottomColor(),settings.getBGTextColor()};
        final ColorEditor[] backgroundColorEditors = new ColorEditor[labels.length];

        SelectionAdapter sa = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                NSISWizardSettings settings = mWizard.getSettings();

                Button b = (Button)e.widget;
                int index = ((Integer)b.getData()).intValue();
                RGB rgb = backgroundColorEditors[index].getRGB();
                switch(index) {
                    case 0:
                        settings.setBGTopColor(rgb);
                        break;
                    case 1:
                        settings.setBGBottomColor(rgb);
                        break;
                    case 2:
                        settings.setBGTextColor(rgb);
                        break;
                }
            }
        };

        for (int i = 0; i < labels.length; i++) {
            Composite composite3 = new Composite(backgroundColorGroup, SWT.NONE);
            composite3.setLayoutData(new GridData());
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite3.setLayout(layout);
            backgroundColorEditors[i] = NSISWizardDialogUtil.createColorEditor(composite3, values[i], labels[i], true, null, false);
            Button b2 = backgroundColorEditors[i].getButton();
            b2.setData(new Integer(i));
            b2.addSelectionListener(sa);
        }

        final Button preview = new Button(composite, SWT.PUSH | SWT.CENTER);
        preview.setText(bundle.getString("preview.label")); //$NON-NLS-1$
        preview.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        preview.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                previewBackground();
            }
        });

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag) { }

            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(control == preview) {
                    return settings.isShowBackground() &&
                    validateEmptyOrValidFile(IOUtility.decodePath(settings.getBackgroundBMP()),null) &&
                    validateEmptyOrValidFile(IOUtility.decodePath(settings.getBackgroundWAV()),null);
                }
                else {
                    return true;
                }
            }
        };

        m.addSlave(preview, mse);
        showBackground.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = showBackground.getSelection();
                mWizard.getSettings().setShowBackground(selection);
                setPageComplete(validateField(BGIMG_CHECK | BGWAV_CHECK));
                preview.setEnabled(mse.canEnable(preview));
            }
        });
        backgroundImage.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setBackgroundBMP(text);
                setPageComplete(validateField(BGIMG_CHECK));
                preview.setEnabled(mse.canEnable(preview));
            }
        });
        backgroundSound.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setBackgroundWAV(text);
                setPageComplete(validateField(BGWAV_CHECK));
                preview.setEnabled(mse.canEnable(preview));
            }
        });
        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                showBackground.setSelection(newSettings.isShowBackground());
                backgroundImage.setText(newSettings.getBackgroundBMP());
                backgroundSound.setText(newSettings.getBackgroundWAV());
                backgroundColorEditors[0].setRGB(newSettings.getBGTopColor());
                backgroundColorEditors[1].setRGB(newSettings.getBGBottomColor());
                backgroundColorEditors[2].setRGB(newSettings.getBGTextColor());
                m.updateSlaves();
            }});
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createLicenseGroup(Composite parent, ResourceBundle bundle)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group licenseGroup = NSISWizardDialogUtil.createGroup(parent, 3, "license.group.label", null, false); //$NON-NLS-1$

        final Button showLicense = NSISWizardDialogUtil.createCheckBox(licenseGroup,"show.license.label",settings.isShowLicense(),  //$NON-NLS-1$
                        true, null, false);
        showLicense.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = showLicense.getSelection();
                mWizard.getSettings().setShowLicense(selection);
                setPageComplete(validateField(LICDATA_CHECK));
            }
        });

        final MasterSlaveController m = new MasterSlaveController(showLicense);
        final Text licenseFile = NSISWizardDialogUtil.createFileBrowser(licenseGroup, settings.getLicenseData(), false,
                        Common.loadArrayProperty(bundle,"license.file.filternames"),  //$NON-NLS-1$
                        Common.loadArrayProperty(bundle,"license.file.filters"), "license.file.label", //$NON-NLS-1$ //$NON-NLS-2$
                        true, m, isScriptWizard());
        licenseFile.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setLicenseData(text);
                setPageComplete(validateField(LICDATA_CHECK));
            }
        });

        final Combo licenseButtons = NSISWizardDialogUtil.createCombo(licenseGroup, NSISWizardDisplayValues.LICENSE_BUTTON_TYPE_NAMES,
                        settings.getLicenseButtonType(),true,"license.button.label", //$NON-NLS-1$
                        (settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isShowLicense()), m, false);

        licenseButtons.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                mWizard.getSettings().setLicenseButtonType(((Combo)e.widget).getSelectionIndex());
            }
        });
        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag) { }

            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();
                return settings.getInstallerType() != INSTALLER_TYPE_SILENT && settings.isShowLicense();
            }
        };
        m.setEnabler(licenseButtons,mse);

        addPageChangedRunnable(new Runnable() {
            public void run()
            {
                m.updateSlaves();
            }
        });
        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                showLicense.setSelection(newSettings.isShowLicense());
                licenseFile.setText(newSettings.getLicenseData());
                int n = newSettings.getLicenseButtonType();
                if(n >= 0 && n < NSISWizardDisplayValues.LICENSE_BUTTON_TYPE_NAMES.length) {
                    licenseButtons.setText(NSISWizardDisplayValues.LICENSE_BUTTON_TYPE_NAMES[n]);
                }
                else {
                    licenseButtons.clearSelection();
                    licenseButtons.setText(""); //$NON-NLS-1$
                }
                licenseButtons.setEnabled(newSettings.getInstallerType() != INSTALLER_TYPE_SILENT && newSettings.isShowLicense());

                m.updateSlaves();
            }}
        );
    }

    /**
     * @param composite
     * @param bundle
     */
    private void createSplashGroup(Composite parent, ResourceBundle bundle)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        Group splashGroup = NSISWizardDialogUtil.createGroup(parent, 1, "splash.group.label", null, false); //$NON-NLS-1$

        final Button showSplash = NSISWizardDialogUtil.createCheckBox(splashGroup,"show.splash.label",settings.isShowSplash(), //$NON-NLS-1$
                        true, null, false);

        final MasterSlaveController m = new MasterSlaveController(showSplash);

        Composite composite = new Composite(splashGroup,SWT.NONE);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        final Text splashImage = NSISWizardDialogUtil.createFileBrowser(composite, settings.getSplashBMP(), false,
                        Common.loadArrayProperty(bundle,"splash.image.filternames"),  //$NON-NLS-1$
                        Common.loadArrayProperty(bundle,"splash.image.filters"), "splash.image.label", //$NON-NLS-1$ //$NON-NLS-2$
                        true,m, isScriptWizard());

        final Text splashSound = NSISWizardDialogUtil.createFileBrowser(composite, settings.getSplashWAV(), false,
                        Common.loadArrayProperty(bundle,"splash.sound.filternames"),  //$NON-NLS-1$
                        Common.loadArrayProperty(bundle,"splash.sound.filters"), "splash.sound.label", //$NON-NLS-1$ //$NON-NLS-2$
                        true, m, false);

        composite = new Composite(splashGroup,SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        composite.setLayoutData(gd);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Group delayGroup = NSISWizardDialogUtil.createGroup(composite, 3, "splash.delay.label", m, false); //$NON-NLS-1$
        ((GridData)delayGroup.getLayoutData()).horizontalSpan = 1;
        String[] labels = {"splash.display.label","splash.fadein.label","splash.fadeout.label"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        int[] values = {settings.getSplashDelay(),settings.getFadeInDelay(),settings.getFadeOutDelay()};
        boolean[] required = {isScriptWizard(), false, false};

        GC gc = new GC(delayGroup);
        gc.setFont(delayGroup.getFont());
        FontMetrics fm = gc.getFontMetrics();
        gc.dispose();
        int widthHint = fm.getAverageCharWidth()*5;
        final Text[] delays = new Text[labels.length];
        for (int i = 0; i < labels.length; i++) {
            Composite composite2 = new Composite(delayGroup, SWT.NONE);
            composite2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            layout = new GridLayout(2, false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite2.setLayout(layout);
            delays[i] = NSISWizardDialogUtil.createText(composite2, makeStringFromInt(values[i]), labels[i], true, null, required[i]);
            delays[i].setData(new Integer(i));
            delays[i].addVerifyListener(mNumberVerifyListener);
            ((GridData)delays[i].getLayoutData()).widthHint = widthHint;
        }

        final Button preview = new Button(composite, SWT.PUSH | SWT.CENTER);
        preview.setText(bundle.getString("preview.label")); //$NON-NLS-1$
        preview.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));

        final MasterSlaveEnabler mse = new MasterSlaveEnabler() {
            public void enabled(Control control, boolean flag) { }

            public boolean canEnable(Control control)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                if(control == preview) {
                    return settings.isShowSplash() &&
                    IOUtility.isValidFile(IOUtility.decodePath(settings.getSplashBMP())) &&
                    validateEmptyOrValidFile(IOUtility.decodePath(settings.getSplashWAV()),null) &&
                    settings.getSplashDelay() > 0;
                }
                else {
                    return true;
                }
            }
        };
        m.addSlave(preview, mse);
        showSplash.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                boolean selection = ((Button)e.widget).getSelection();
                mWizard.getSettings().setShowSplash(selection);
                setPageComplete(validateField(SPLIMG_CHECK | SPLWAV_CHECK | SPLDLY_CHECK));
                preview.setEnabled(mse.canEnable(preview));
            }
        });
        splashImage.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setSplashBMP(text);
                setPageComplete(validateField(SPLIMG_CHECK));
                preview.setEnabled(mse.canEnable(preview));
            }
        });
        splashSound.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                String text = ((Text)e.widget).getText();
                mWizard.getSettings().setSplashWAV(text);
                setPageComplete(validateField(SPLWAV_CHECK));
                preview.setEnabled(mse.canEnable(preview));
            }
        });
        ModifyListener delayListener = new ModifyListener() {
            public void modifyText(ModifyEvent e)
            {
                NSISWizardSettings settings = mWizard.getSettings();

                Text text = (Text)e.widget;
                String str = text.getText();
                int index = ((Integer)text.getData()).intValue();
                int value = Common.isEmpty(str)?0:Integer.parseInt(str);
                switch(index) {
                    case 0:
                        settings.setSplashDelay(value);
                        setPageComplete(validateField(SPLDLY_CHECK));
                        preview.setEnabled(mse.canEnable(preview));
                        break;
                    case 1:
                        settings.setFadeInDelay(value);
                        break;
                    case 2:
                        settings.setFadeOutDelay(value);
                        break;
                }
            }
        };
        for (int i = 0; i < delays.length; i++) {
            delays[i].addModifyListener(delayListener);
        }

        preview.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                previewSplash();
            }
        });
        m.updateSlaves();

        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged(NSISWizardSettings oldSettings, NSISWizardSettings newSettings)
            {
                showSplash.setSelection(newSettings.isShowSplash());
                splashImage.setText(newSettings.getSplashBMP());
                splashSound.setText(newSettings.getSplashWAV());
                delays[0].setText(makeStringFromInt(newSettings.getSplashDelay()));
                delays[1].setText(makeStringFromInt(newSettings.getFadeInDelay()));
                delays[2].setText(makeStringFromInt(newSettings.getFadeOutDelay()));
                m.updateSlaves();
            }}
        );
    }

    private String makeStringFromInt(int value)
    {
        if(value > 0) {
            return Integer.toString(value);
        }
        return ""; //$NON-NLS-1$
    }

    private void previewSplash()
    {
        if(IOUtility.isValidFile(IOUtility.decodePath(mWizard.getSettings().getSplashBMP()))) {
            SplashPreviewTask task = new SplashPreviewTask();
            new Timer().scheduleAtFixedRate(task,0,task.getResolution());
        }
    }

    private void previewBackground()
    {
        final NSISWizardSettings settings = mWizard.getSettings();

        final Shell shell = new Shell(getShell().getDisplay(), SWT.APPLICATION_MODAL|SWT.NO_TRIM);
        shell.setText(EclipseNSISPlugin.getResourceString("background.preview.title")); //$NON-NLS-1$
        final String previewText = EclipseNSISPlugin.getFormattedString("background.preview.text", new Object[]{settings.getName()});  //$NON-NLS-1$
        final Display display = shell.getDisplay();
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight=0;
        fillLayout.marginWidth=0;
        shell.setLayout(fillLayout);
        shell.setParent(getShell());
        final Font previewFont = new Font(display,mBGPreviewFontData);
        final Font messageFont = new Font(display,mBGPreviewEscapeFontData);
        final File wavFile;
        File file = new File(IOUtility.decodePath(settings.getBackgroundWAV()));
        if(IOUtility.isValidFile(file)) {
            wavFile = file;
        }
        else {
            wavFile = null;
        }
        final Shell parentShell = getShell();
        shell.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.character == SWT.ESC) {
                    shell.close();
                    shell.dispose();
                    previewFont.dispose();
                    messageFont.dispose();
                    if(wavFile != null) {
                        WinAPI.INSTANCE.playSound(null, WinAPI.ZERO_HANDLE, WinAPI.SND_PURGE);
                    }
                    if(parentShell != null && !parentShell.getMinimized()) {
                        parentShell.forceActive();
                    }
                }
            }
        });

        Canvas canvas = new Canvas(shell, SWT.NO_BACKGROUND);

        final Shell windowShell;
        Shell temp;
        try {
            temp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        }
        catch (NullPointerException e) {
            temp = null;
        }
        windowShell = temp;
        final boolean parentMinimized = parentShell != null && parentShell.getMinimized();
        if(parentShell != null && !parentMinimized) {
            parentShell.setMinimized(true);
        }

        final boolean windowMinimized = windowShell != null && windowShell.getMinimized();
        if(windowShell != null && !windowMinimized) {
            windowShell.setMinimized(true);
        }

        shell.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                if(windowShell != null && !windowMinimized && windowShell.getMinimized()) {
                    windowShell.setMinimized(false);
                }
                if(parentShell != null && !parentMinimized && parentShell.getMinimized()) {
                    parentShell.setMinimized(false);
                }
            }
        });
        final GC gc = new GC(canvas);
        shell.open();
        shell.setFullScreen(true);
        shell.forceActive();
        if (wavFile != null) {
            WinAPI.INSTANCE.playSound(wavFile.getAbsolutePath(), WinAPI.ZERO_HANDLE,
                            WinAPI.SND_ASYNC | WinAPI.SND_FILENAME | WinAPI.SND_NODEFAULT | WinAPI.SND_LOOP);
        }
        canvas.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e)
            {
                RGB topRGB = settings.getBGTopColor();
                RGB botRGB = settings.getBGBottomColor();
                long r = topRGB.red << 10;
                long g = topRGB.green << 10;
                long b = topRGB.blue << 10;
                Rectangle rect = display.getPrimaryMonitor().getBounds();
                long dr = ((botRGB.red << 10) - r) * 4 / rect.height;
                long dg = ((botRGB.green << 10) - g) * 4 / rect.height;
                long db = ((botRGB.blue << 10) - b) * 4 / rect.height;
                int ry = rect.y;

                //Save GC settings
                Color fgColor = gc.getForeground();
                Color bgColor = gc.getBackground();
                Font font = gc.getFont();

                while (ry < rect.y + rect.height) {
                    Color color = new Color(display, (int)(r >> 10), (int)(g >> 10), (int)(b >> 10));
                    gc.setBackground(color);
                    gc.fillRectangle(rect.x, ry, rect.width, mBGPreviewGradientHeight);
                    color.dispose();
                    ry += mBGPreviewGradientHeight;
                    r += dr;
                    g += dg;
                    b += db;
                }

                String backgroundBMP = IOUtility.decodePath(settings.getBackgroundBMP());
                if (IOUtility.isValidFile(backgroundBMP)) {
                    ImageData imageData = new ImageData(backgroundBMP);
                    Image image = new Image(display, imageData);
                    int x = rect.x + (rect.width - imageData.width) / 2;
                    int y = rect.y + (rect.height - imageData.height) / 2;
                    gc.drawImage(image, x, y);
                    image.dispose();
                }

                gc.setForeground(ColorManager.getColor(settings.getBGTextColor()));
                gc.setFont(previewFont);
                gc.drawString(previewText, mBGPreviewTextLocation.x, mBGPreviewTextLocation.y, true);

                gc.setForeground(ColorManager.getNegativeColor(botRGB));
                gc.setFont(messageFont);
                gc.drawString(EclipseNSISPlugin.getResourceString("background.preview.escape.message"), 10, rect.y + rect.height - 20, true); //$NON-NLS-1$

                //Restore GC settings
                gc.setForeground(fgColor);
                gc.setBackground(bgColor);
                gc.setFont(font);
            }
        });
        canvas.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                gc.dispose();
            }
        });
    }

    private boolean validateField(int flag)
    {
        if(validatePage(flag)) {
            return validatePage(VALIDATE_ALL & ~flag);
        }
        else {
            return false;
        }
    }

    private boolean validateDelay(int delay, String[] messageResource)
    {
        if(delay <= 0) {
            setErrorMessage(getArrayStringResource(messageResource,0,"zero.number.error")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    @Override
    public boolean validatePage(int flag)
    {
        if(isTemplateWizard()) {
            return true;
        }
        else {
            NSISWizardSettings settings = mWizard.getSettings();

            boolean b = (!settings.isShowLicense() || (flag & LICDATA_CHECK) == 0 || validateFile(IOUtility.decodePath(settings.getLicenseData()),cLicFileErrors)) &&
            (!settings.isShowSplash() || ((flag & SPLIMG_CHECK) == 0 || validateFile(IOUtility.decodePath(settings.getSplashBMP()), cSplashImageErrors)) &&
                            ((flag & SPLWAV_CHECK) == 0 || validateEmptyOrValidFile(IOUtility.decodePath(settings.getSplashWAV()),null)) &&
                            ((flag & SPLDLY_CHECK) == 0 || validateDelay(settings.getSplashDelay(),cSplashDelayErrors))) &&
                            (!settings.isShowBackground() || ((flag & BGIMG_CHECK) == 0 || validateEmptyOrValidFile(IOUtility.decodePath(settings.getBackgroundBMP()),null)) &&
                                            ((flag & BGWAV_CHECK) == 0 || validateEmptyOrValidFile(IOUtility.decodePath(settings.getBackgroundWAV()),null)));
            setPageComplete(b);
            if(b) {
                setErrorMessage(null);
            }
            return b;
        }
    }

    private class SplashPreviewTask extends TimerTask
    {
        public static final int STATE_FADE_IN = 0;
        public static final int STATE_DISPLAY = 1;
        public static final int STATE_FADE_OUT = 2;

        private int mState = STATE_FADE_IN;
        private int mTimeLeft = 0;
        private Image mImage = null;
        private int mFadeInDelay = 0;
        private int mDisplayDelay = 0;
        private int mFadeOutDelay = 0;
        private Display mDisplay;
        private Shell mShell = null;
        private int mAlpha;
        private long mResolution;
        private File mWavFile = null;
        private boolean mAdvSplash = true;

        public SplashPreviewTask()
        {
            init();
        }

        public long getResolution()
        {
            return mResolution;
        }

        public void init()
        {
            NSISWizardSettings settings = mWizard.getSettings();

            if(mShell != null) {
                mShell.close();
                mShell.dispose();
            }

            if(OS_VERSION >= 5.0) {
                mResolution = DEFAULT_RESOLUTION;
                mFadeInDelay = settings.getFadeInDelay() >> 5;
                mDisplayDelay = settings.getSplashDelay() >> 5;
                mFadeOutDelay = settings.getFadeOutDelay() >> 5;
            }
            else {
                mResolution = settings.getFadeInDelay() + settings.getSplashDelay() + settings.getFadeOutDelay();
                mFadeInDelay = 0;
                mDisplayDelay = 1;
                mFadeOutDelay = 0;
            }

            mAdvSplash = OS_VERSION >= 5.0 && (mFadeInDelay > 0 || mFadeOutDelay > 0);
            mShell = new Shell(getShell().getDisplay(), SWT.APPLICATION_MODAL | SWT.NO_TRIM | SWT.NO_BACKGROUND);
            mShell.setText(EclipseNSISPlugin.getResourceString("splash.preview.title")); //$NON-NLS-1$

            mDisplay = mShell.getDisplay();
            FillLayout fillLayout = new FillLayout();
            fillLayout.marginHeight=0;
            fillLayout.marginWidth=0;
            mShell.setLayout(fillLayout);

            ImageData imageData = new ImageData(IOUtility.decodePath(settings.getSplashBMP()));
            mShell.setSize(imageData.width, imageData.height);
            Rectangle rect = mDisplay.getPrimaryMonitor().getClientArea();
            int x = (rect.width-imageData.width)/2;
            int y = (rect.height-imageData.height)/2;
            mShell.setBounds(x,y,imageData.width,imageData.height);
            mState = STATE_FADE_IN;
            mTimeLeft = mFadeInDelay;

            mImage = new Image(mDisplay, imageData);
            Label l = new Label(mShell,SWT.NONE);
            l.setImage(mImage);
            if(mAdvSplash) {
                mShell.setAlpha(mFadeInDelay > 0?0:255);
            }

            mAlpha = -1;
            mShell.open();
            mWavFile = new File(IOUtility.decodePath(settings.getSplashWAV()));
            if(!IOUtility.isValidFile(mWavFile)) {
                mWavFile = null;
            }
            if(mWavFile != null) {
                new Thread(new Runnable() {
                    public void run() {
                        WinAPI.INSTANCE.playSound(mWavFile.getAbsolutePath(), WinAPI.ZERO_HANDLE,
                                        WinAPI.SND_ASYNC | WinAPI.SND_FILENAME | WinAPI.SND_NODEFAULT);
                        mWavFile = null;
                    }
                },EclipseNSISPlugin.getResourceString("splash.preview.audio.thread.name")).start(); //$NON-NLS-1$
            }
        }

        @Override
        public void run(){
            Thread.currentThread().setName(EclipseNSISPlugin.getResourceString("splash.preview.thread.name")); //$NON-NLS-1$
            final int newAlpha;
            switch(mState) {
                case STATE_FADE_IN:
                    if(mTimeLeft == 0) {
                        mTimeLeft = mDisplayDelay;
                        mState = STATE_DISPLAY;
                    }
                    else {
                        newAlpha = (mFadeInDelay - mTimeLeft)*255/mFadeInDelay;
                        break;
                    }
                    //$FALL-THROUGH$
                case STATE_DISPLAY:
                    if(mTimeLeft == 0) {
                        mTimeLeft = mFadeOutDelay;
                        mState = STATE_FADE_OUT;
                    }
                    else {
                        newAlpha = 255;
                        break;
                    }
                    //$FALL-THROUGH$
                case STATE_FADE_OUT:
                    if(mTimeLeft == 0) {
                        mDisplay.asyncExec(new Runnable(){
                            public void run(){
                                if(mWavFile != null) {
                                    WinAPI.INSTANCE.playSound(null, WinAPI.ZERO_HANDLE, WinAPI.SND_PURGE);
                                    mWavFile = null;
                                }
                                if(mImage != null) {
                                    mImage.dispose();
                                }
                                mShell.close();
                                mShell.dispose();
                            }
                        });
                        cancel();
                        return;
                    }
                    else {
                        newAlpha = mTimeLeft*255/mFadeOutDelay;
                        break;
                    }
                default:
                    newAlpha = 255;
            }

            if(mAdvSplash) {
                mDisplay.asyncExec(new Runnable(){
                    public void run(){
                        if(mAlpha != newAlpha) {
                            mAlpha = newAlpha;
                            mShell.setAlpha(mAlpha);
                        }
                    }
                });
            }
            mTimeLeft--;
        }
    }
}
