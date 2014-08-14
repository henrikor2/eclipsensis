/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import net.sf.eclipsensis.editor.codeassist.NSISAnnotationHover.NSISInformation;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.*;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class NSISInformationControl implements IInformationControl, IInformationControlExtension, IInformationControlExtension2, IInformationControlExtension3, IInformationControlExtension4, IInformationControlExtension5
{
    private static final int INNER_BORDER = 1;

    private final Shell mShell;

    private final Composite mContentComposite;

    private Composite mStatusComposite;

    private Label mSeparator;

    private Label mStatusLabel;

    private Listener mShellListener;

    private ListenerList mFocusListeners = new ListenerList(ListenerList.IDENTITY);

    private Point mSizeConstraints;

    private StyledText mText;

    private final IInformationPresenter mPresenter;

    private final TextPresentation mPresentation = new TextPresentation();

    private final int mTextStyles;

    private IInformationControlCreator mCreator = null;

    public NSISInformationControl(Shell parent, IInformationPresenter presenter)
    {
        this(parent, SWT.NONE, presenter);
    }

    public NSISInformationControl(Shell parent, int textStyles, IInformationPresenter presenter)
    {
        this(parent, textStyles, presenter, null);
    }

    public NSISInformationControl(Shell parent, int textStyles, IInformationPresenter presenter, String statusFieldText)
    {
        //int shellStyle = (SWT.NO_FOCUS | SWT.ON_TOP) & ~(SWT.NO_TRIM | SWT.SHELL_TRIM);
        int shellStyle = (SWT.NO_FOCUS | SWT.TOOL ) & ~(SWT.NO_TRIM | SWT.SHELL_TRIM);

        mShell = new Shell(parent, shellStyle);
        Display display = mShell.getDisplay();
        Color foreground = display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
        Color background = display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
        setColor(mShell, foreground, background);

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        mShell.setLayout(layout);

        mContentComposite = new Composite(mShell, SWT.NONE);
        mContentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        mContentComposite.setLayout(new FillLayout());
        setColor(mContentComposite, foreground, background);

        createStatusComposite(statusFieldText, foreground, background);
        mPresenter = presenter;
        mTextStyles = textStyles;
        create();
    }

    private void createStatusComposite(final String statusFieldText, Color foreground, Color background)
    {
        if (statusFieldText == null) {
            return;
        }

        mStatusComposite = new Composite(mShell, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.BOTTOM, true, false);
        mStatusComposite.setLayoutData(gridData);
        GridLayout statusLayout = new GridLayout(1, false);
        statusLayout.marginHeight = 0;
        statusLayout.marginWidth = 0;
        statusLayout.verticalSpacing = 1;
        mStatusComposite.setLayout(statusLayout);

        mSeparator = new Label(mStatusComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        mSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createStatusLabel(statusFieldText, foreground, background);
    }

    private void createStatusLabel(final String statusFieldText, Color foreground, Color background)
    {
        mStatusLabel = new Label(mStatusComposite, SWT.RIGHT);
        mStatusLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mStatusLabel.setText(statusFieldText);

        FontData[] fontDatas = JFaceResources.getDialogFont().getFontData();
        for (int i = 0; i < fontDatas.length; i++) {
            fontDatas[i].setHeight(fontDatas[i].getHeight() * 9 / 10);
        }
        mStatusLabel.setFont(new Font(mStatusLabel.getDisplay(), fontDatas));

        mStatusLabel.setForeground(mStatusLabel.getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
        mStatusLabel.setBackground(background);
        setColor(mStatusComposite, foreground, background);
    }

    private static void setColor(Control control, Color foreground, Color background)
    {
        control.setForeground(foreground);
        control.setBackground(background);
    }

    private Shell getShell()
    {
        return mShell;
    }

    private void create()
    {
        mText = new StyledText(mContentComposite, SWT.MULTI | SWT.READ_ONLY | mTextStyles);
        mText.setForeground(mContentComposite.getForeground());
        mText.setBackground(mContentComposite.getBackground());
        mText.setFont(JFaceResources.getDialogFont());
        FillLayout layout = (FillLayout)mContentComposite.getLayout();
        if (mText.getWordWrap()) {
            layout.marginHeight = INNER_BORDER;
            layout.marginWidth = INNER_BORDER;
        }
        else {
            mText.setIndent(INNER_BORDER);
        }
    }

    public void dispose()
    {
        if (mShell != null && !mShell.isDisposed())
            mShell.dispose();
    }

    public void setSize(int width, int height)
    {
        mShell.setSize(width, height);
    }

    public void setLocation(Point location)
    {
        mShell.setLocation(location);
    }

    public void setSizeConstraints(int maxWidth, int maxHeight)
    {
        mSizeConstraints = new Point(maxWidth, maxHeight);
    }

    protected final Point getSizeConstraints()
    {
        return mSizeConstraints != null?Geometry.copy(mSizeConstraints):null;
    }

    public Rectangle getBounds()
    {
        return mShell.getBounds();
    }

    public boolean restoresLocation()
    {
        return false;
    }

    public boolean restoresSize()
    {
        return false;
    }

    public void addDisposeListener(DisposeListener listener)
    {
        mShell.addDisposeListener(listener);
    }

    public void removeDisposeListener(DisposeListener listener)
    {
        mShell.removeDisposeListener(listener);
    }

    public boolean isFocusControl()
    {
        return mShell.getDisplay().getActiveShell() == mShell;
    }

    public void setFocus()
    {
        boolean focusTaken = mShell.setFocus();
        if (!focusTaken) {
            mShell.forceFocus();
        }
    }

    public void addFocusListener(final FocusListener listener)
    {
        if (mFocusListeners.isEmpty()) {
            mShellListener = new Listener() {

                public void handleEvent(Event event)
                {
                    Object[] listeners = mFocusListeners.getListeners();
                    for (int i = 0; i < listeners.length; i++) {
                        FocusListener focusListener = (FocusListener)listeners[i];
                        if (event.type == SWT.Activate) {
                            focusListener.focusGained(new FocusEvent(event));
                        }
                        else {
                            focusListener.focusLost(new FocusEvent(event));
                        }
                    }
                }
            };
            mShell.addListener(SWT.Deactivate, mShellListener);
            mShell.addListener(SWT.Activate, mShellListener);
        }
        mFocusListeners.add(listener);
    }

    public void removeFocusListener(FocusListener listener)
    {
        mFocusListeners.remove(listener);
        if (mFocusListeners.isEmpty()) {
            mShell.removeListener(SWT.Activate, mShellListener);
            mShell.removeListener(SWT.Deactivate, mShellListener);
            mShellListener = null;
        }
    }

    public void setStatusText(String statusFieldText)
    {
        if (mStatusLabel != null && !getShell().isVisible()) {
            if (statusFieldText == null) {
                mStatusComposite.setVisible(false);
            }
            else {
                mStatusLabel.setText(statusFieldText);
                mStatusComposite.setVisible(true);
            }
        }
    }

    public boolean containsControl(Control control)
    {
        Control control2 = control;
        do {
            if (control2 == mShell) {
                return true;
            }
            if (control2 instanceof Shell) {
                return false;
            }
            control2 = control2.getParent();
        } while (control2 != null);
        return false;
    }

    public boolean isVisible()
    {
        return mShell != null && !mShell.isDisposed() && mShell.isVisible();
    }

    public Point computeSizeConstraints(int widthInChars, int heightInChars)
    {
        GC gc = new GC(mContentComposite);
        gc.setFont(JFaceResources.getDialogFont());
        int width = gc.getFontMetrics().getAverageCharWidth();
        int height = gc.getFontMetrics().getHeight();
        gc.dispose();

        return new Point(widthInChars * width, heightInChars * height);
    }

    public void setInformation(String content)
    {
        setInput(new NSISInformation(content==null?"":content)); //$NON-NLS-1$
    }

    public void setVisible(boolean visible)
    {
        if (visible) {
            if (mText.getWordWrap()) {
                Point currentSize = getShell().getSize();
                getShell().pack(true);
                Point newSize = getShell().getSize();
                if (newSize.x > currentSize.x || newSize.y > currentSize.y) {
                    setSize(currentSize.x, currentSize.y);
                }
            }
        }

        if (mShell.isVisible() == visible) {
            return;
        }

        mShell.setVisible(visible);
    }

    public Point computeSizeHint()
    {
        int widthHint = SWT.DEFAULT;
        Point constraints = getSizeConstraints();
        if (constraints != null && mText.getWordWrap())
            widthHint = constraints.x;

        return getShell().computeSize(widthHint, SWT.DEFAULT, true);
    }

    public Rectangle computeTrim()
    {
        Rectangle trim = mShell.computeTrim(0, 0, 0, 0);

        if (mStatusComposite != null)
            trim.height += mStatusComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;

        return Geometry.add(trim, mText.computeTrim(0, 0, 0, 0));
    }

    public void setForegroundColor(Color foreground)
    {
        mContentComposite.setForeground(foreground);
        mText.setForeground(foreground);
    }

    public void setBackgroundColor(Color background)
    {
        mContentComposite.setBackground(background);
        mText.setBackground(background);
    }

    public boolean hasContents()
    {
        return mText.getCharCount() > 0;
    }

    public IInformationControlCreator getInformationPresenterControlCreator()
    {
        return mCreator;
    }

    void setInformationPresenterControlCreator(IInformationControlCreator creator)
    {
        mCreator = creator;
    }

    public interface IInformationPresenter
    {
        String updatePresentation(Display display, String hoverInfo, TextPresentation presentation, int maxWidth, int maxHeight);
    }

    public void setInput(Object input)
    {
        String content = input != null?String.valueOf(input):""; //$NON-NLS-1$
        if (mPresenter == null) {
            mText.setText(content);
        }
        else {
            mPresentation.clear();

            int maxWidth = -1;
            int maxHeight = -1;
            Point constraints = getSizeConstraints();
            if (constraints != null) {
                maxWidth = constraints.x;
                maxHeight = constraints.y;
                if (mText.getWordWrap()) {
                    maxWidth -= INNER_BORDER * 2;
                    maxHeight -= INNER_BORDER * 2;
                }
                else {
                    maxWidth -= INNER_BORDER; // indent
                }
                Rectangle trim = computeTrim();
                maxWidth -= trim.width;
                maxHeight -= trim.height;
                maxWidth -= mText.getCaret().getSize().x; // StyledText adds a
                                                          // border at the end
                                                          // of the line for the
                                                          // caret.
            }

            content = mPresenter.updatePresentation(getShell().getDisplay(), content, mPresentation, maxWidth, maxHeight);

            if (content != null) {
                mText.setText(content);
                TextPresentation.applyTextPresentation(mPresentation, mText);
            }
            else {
                mText.setText(""); //$NON-NLS-1$
            }
        }
    }
}
