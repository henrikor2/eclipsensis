/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK). All rights reserved. This
 * program is made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

/**
 * The Interface INSISInstallFileSystemObject.
 */
public interface INSISInstallFileSystemObject
{

    /**
     * Gets the destination.
     * 
     * @return Returns the destination.
     */
    public String getDestination();

    /**
     * Sets the destination.
     * 
     * @param destination
     *            The destination to set.
     */
    public void setDestination(String destination);

    /**
     * Gets the overwrite mode.
     * 
     * @return Returns the overwriteMode.
     */
    public int getOverwriteMode();

    /**
     * Sets the overwrite mode.
     * 
     * @param overwriteMode
     *            The overwriteMode to set.
     */
    public void setOverwriteMode(int overwriteMode);

    /**
     * Gets the non fatal.
     * 
     * @return the non fatal
     */
    public boolean getNonFatal();

    /**
     * Sets the non fatal.
     * 
     * @param nonFatal
     *            the new non fatal
     */
    public void setNonFatal(boolean nonFatal);

    /**
     * Gets the preserve attributes.
     * 
     * @return the preserve attributes
     */
    public boolean getPreserveAttributes();

    /**
     * Sets the preserve attributes.
     * 
     * @param preserveAttributes
     *            the new preserve attributes
     */
    public void setPreserveAttributes(boolean preserveAttributes);
}