/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions;

import net.sf.eclipsensis.installoptions.model.DialogSize;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Color;

public interface IInstallOptionsConstants
{
    public static final Point EMPTY_POINT = new Point(0,0);
    public static final Dimension EMPTY_DIMENSION = new Dimension(0,0);

    public static final String PLUGIN_ID = InstallOptionsPlugin.getDefault().getBundle().getSymbolicName();
    public static final String PLUGIN_CONTEXT_PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

    public static final String RESOURCE_BUNDLE = "net.sf.eclipsensis.installoptions.InstallOptionsPluginResources"; //$NON-NLS-1$
    public static final String MESSAGE_BUNDLE = "net.sf.eclipsensis.installoptions.InstallOptionsPluginMessages"; //$NON-NLS-1$

    public static final String[] INI_EXTENSIONS = Common.tokenize(InstallOptionsPlugin.getBundleResourceString("%ini.extensions"),','); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_DESIGN_EDITOR_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.design.editor.id"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_SOURCE_EDITOR_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.source.editor.id"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_PREFERENCE_PAGE_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.preference.page.id"); //$NON-NLS-1$

    public static final String SWITCH_EDITOR_COMMAND_ID = InstallOptionsPlugin.getBundleResourceString("%switch.editor.command.id"); //$NON-NLS-1$
    public static final String EDITING_INSTALLOPTIONS_SOURCE_CONTEXT_ID = InstallOptionsPlugin.getBundleResourceString("%editing.installoptions.source.id"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_SOURCE_OUTLINE_CONTEXT_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.source.outline.id"); //$NON-NLS-1$
    public static final String EDITING_INSTALLOPTIONS_DESIGN_CONTEXT_ID = InstallOptionsPlugin.getBundleResourceString("%editing.installoptions.design.id"); //$NON-NLS-1$

    public static final String INSTALLOPTIONS_PROBLEM_MARKER_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.problem.marker.id"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_ERROR_ANNOTATION_NAME = InstallOptionsPlugin.getBundleResourceString("%installoptions.error.annotation.name"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_WARNING_ANNOTATION_NAME = InstallOptionsPlugin.getBundleResourceString("%installoptions.warning.annotation.name"); //$NON-NLS-1$

    public static final String INSTALLOPTIONS_NATURE_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.nature.id"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_BUILDER_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.builder.id"); //$NON-NLS-1$

    public static final String FILE_ASSOCIATION_ID = InstallOptionsPlugin.getBundleResourceString("%file.association.id"); //$NON-NLS-1$

    public static final String GRID_STYLE_LINES="GridStyleLines"; //$NON-NLS-1$
    public static final String GRID_STYLE_DOTS="GridStyleDots"; //$NON-NLS-1$

    public static final Boolean SHOW_GRID_DEFAULT = Boolean.FALSE;
    public static final Boolean SHOW_RULERS_DEFAULT = Boolean.FALSE;
    public static final Boolean SHOW_GUIDES_DEFAULT = Boolean.TRUE;
    public static final Boolean SHOW_DIALOG_SIZE_DEFAULT = Boolean.TRUE;
    public static final Boolean SNAP_TO_GRID_DEFAULT = Boolean.TRUE;
    public static final Boolean SNAP_TO_GEOMETRY_DEFAULT = Boolean.TRUE;
    public static final Boolean SNAP_TO_GUIDES_DEFAULT = Boolean.TRUE;
    public static final Boolean GLUE_TO_GUIDES_DEFAULT = Boolean.TRUE;
    public static final Dimension GRID_SPACING_DEFAULT = new Dimension(10,10);
    public static final Point GRID_ORIGIN_DEFAULT = EMPTY_POINT;
    public static final String GRID_STYLE_DEFAULT = GRID_STYLE_LINES;
    public static final boolean CHECK_FILE_ASSOCIATION_DEFAULT = true;

    public static final String PREFERENCE_SHOW_GRID = "ShowGrid"; //$NON-NLS-1$
    public static final String PREFERENCE_SHOW_RULERS = "ShowRulers"; //$NON-NLS-1$
    public static final String PREFERENCE_SHOW_GUIDES = "ShowGuides"; //$NON-NLS-1$
    public static final String PREFERENCE_SHOW_DIALOG_SIZE = "ShowDialogSize"; //$NON-NLS-1$
    public static final String PREFERENCE_SNAP_TO_GRID = "SnapToGrid"; //$NON-NLS-1$
    public static final String PREFERENCE_SNAP_TO_GEOMETRY = "SnapToGeometry"; //$NON-NLS-1$
    public static final String PREFERENCE_SNAP_TO_GUIDES = "SnapToGuides"; //$NON-NLS-1$
    public static final String PREFERENCE_GLUE_TO_GUIDES = "GlueToGuides"; //$NON-NLS-1$
    public static final String PREFERENCE_GRID_SPACING = "GridSpacing"; //$NON-NLS-1$
    public static final String PREFERENCE_GRID_ORIGIN = "GridOrigin"; //$NON-NLS-1$
    public static final String PREFERENCE_GRID_STYLE = "GridStyle"; //$NON-NLS-1$
    public static final String PREFERENCE_CHECK_EDITOR_ASSOCIATION = "CheckEditorAssociation"; //$NON-NLS-1$
    public static final String PREFERENCE_PALETTE_VIEWER_PREFS_INIT = "PaletteViewerPrefsInit"; //$NON-NLS-1$
    public static final String PREFERENCE_UNLOAD_CREATION_TOOL_WHEN_FINISHED = "UnloadCreationToolWhenFinished"; //$NON-NLS-1$
    public static final String PREFERENCE_PREVIEW_LANG = "PreviewLang"; //$NON-NLS-1$
    public static final String PREFERENCE_DELETE_CONTROL_WARNING = "DeleteControlWarning"; //$NON-NLS-1$
    public static final String PREFERENCE_AUTOSAVE_BEFORE_PREVIEW = "AutosaveBeforePreview"; //$NON-NLS-1$

    public static final String PREFERENCE_SYNTAX_STYLES = "SyntaxStyles"; //$NON-NLS-1$
    public static final String SECTION_STYLE = "SectionStyle"; //$NON-NLS-1$
    public static final String COMMENT_STYLE = "CommentStyle";     //$NON-NLS-1$
    public static final String KEY_STYLE = "KeyStyle";     //$NON-NLS-1$
    public static final String KEY_VALUE_DELIM_STYLE = "KeyValueDelimStyle";     //$NON-NLS-1$
    public static final String NUMBER_STYLE = "NumberStyle";     //$NON-NLS-1$

    public static final String QUALIFIED_NAME_PREFIX = PLUGIN_ID;

    public static final QualifiedName FILEPROPERTY_SHOW_GRID = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_GRID);
    public static final QualifiedName FILEPROPERTY_SHOW_RULERS = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_RULERS);
    public static final QualifiedName FILEPROPERTY_SHOW_GUIDES = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_GUIDES);
    public static final QualifiedName FILEPROPERTY_SHOW_DIALOG_SIZE = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_DIALOG_SIZE);
    public static final QualifiedName FILEPROPERTY_SNAP_TO_GRID = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SNAP_TO_GRID);
    public static final QualifiedName FILEPROPERTY_SNAP_TO_GEOMETRY = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SNAP_TO_GEOMETRY);
    public static final QualifiedName FILEPROPERTY_SNAP_TO_GUIDES = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SNAP_TO_GUIDES);
    public static final QualifiedName FILEPROPERTY_GLUE_TO_GUIDES = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GLUE_TO_GUIDES);
    public static final QualifiedName FILEPROPERTY_GRID_SPACING = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GRID_SPACING);
    public static final QualifiedName FILEPROPERTY_GRID_ORIGIN = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GRID_ORIGIN);
    public static final QualifiedName FILEPROPERTY_GRID_STYLE = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GRID_STYLE);
    public static final QualifiedName FILEPROPERTY_DIALOG_SIZE = new QualifiedName(QUALIFIED_NAME_PREFIX,"DialogSize"); //$NON-NLS-1$
    public static final QualifiedName FILEPROPERTY_DIALOG_SIZE_NAME = new QualifiedName(QUALIFIED_NAME_PREFIX,"DialogSizeName"); //$NON-NLS-1$
    public static final QualifiedName RESOURCEPROPERTY_BUILD_TIMESTAMP = new QualifiedName(QUALIFIED_NAME_PREFIX,"BuildTimestamp"); //$NON-NLS-1$
    public static final QualifiedName PROJECTPROPERTY_BUILDER_VERSION = new QualifiedName(QUALIFIED_NAME_PREFIX,"BuilderVersion"); //$NON-NLS-1$
    public static final QualifiedName PROJECTPROPERTY_NSIS_VERSION = new QualifiedName(QUALIFIED_NAME_PREFIX,"NSISVersion"); //$NON-NLS-1$

    public static final String PROPERTY_SNAP_TO_GUIDES = "net.sf.eclipsensis.installoptions.snap_to_guides"; //$NON-NLS-1$
    public static final String PROPERTY_GLUE_TO_GUIDES = "net.sf.eclipsensis.installoptions.glue_to_guides"; //$NON-NLS-1$
    public static final String PROPERTY_DIALOG_SIZE = "net.sf.eclipsensis.installoptions.dialog_size"; //$NON-NLS-1$
    public static final String PROPERTY_SHOW_DIALOG_SIZE = "net.sf.eclipsensis.installoptions.show_dialog_size"; //$NON-NLS-1$

    public static final String GRID_SNAP_GLUE_SETTINGS_ACTION_ID = "net.sf.eclipsensis.installoptions.grid_snap_glue_settings"; //$NON-NLS-1$

    public static final Color GHOST_FILL_COLOR = new Color(null, 31, 31, 31);

    public static final String REQ_REORDER_PART="reorder part"; //$NON-NLS-1$
    public static final String REQ_EXTENDED_EDIT="extended edit"; //$NON-NLS-1$
    public static final String REQ_CREATE_FROM_TEMPLATE="create from template"; //$NON-NLS-1$

    public static final int ARRANGE_SEND_BACKWARD = 1;
    public static final int ARRANGE_SEND_TO_BACK = 2;
    public static final int ARRANGE_BRING_FORWARD = 3;
    public static final int ARRANGE_BRING_TO_FRONT = 4;

    public static final int DISTRIBUTE_HORIZONTAL_LEFT_EDGE=1;
    public static final int DISTRIBUTE_HORIZONTAL_CENTER=2;
    public static final int DISTRIBUTE_HORIZONTAL_RIGHT_EDGE=3;
    public static final int DISTRIBUTE_HORIZONTAL_BETWEEN=4;
    public static final int DISTRIBUTE_VERTICAL_TOP_EDGE=5;
    public static final int DISTRIBUTE_VERTICAL_CENTER=6;
    public static final int DISTRIBUTE_VERTICAL_BOTTOM_EDGE=7;
    public static final int DISTRIBUTE_VERTICAL_BETWEEN=8;

    public static final int PREVIEW_CLASSIC = 1;
    public static final int PREVIEW_MUI = 2;

    public static final char LIST_SEPARATOR = '|';
    public static final String MATCHSIZE_GROUP = "net.sf.eclipsensis.installoptions.matchsize"; //$NON-NLS-1$
    public static final String DISTRIBUTE_GROUP = "net.sf.eclipsensis.installoptions.distribute"; //$NON-NLS-1$
    public static final String ALIGN_GROUP = "net.sf.eclipsensis.installoptions.align"; //$NON-NLS-1$
    public static final String ARRANGE_GROUP = "net.sf.eclipsensis.installoptions.arrange"; //$NON-NLS-1$
    public static final String PREVIEW_GROUP = "net.sf.eclipsensis.installoptions.preview"; //$NON-NLS-1$
    public static final String FIX_PROBLEMS_GROUP = "net.sf.eclipsensis.installoptions.fix_problems"; //$NON-NLS-1$

    public static final DialogSize DEFAULT_DIALOG_SIZE = new DialogSize(InstallOptionsPlugin.getResourceString("default.dialog.size.name"),true,new Dimension(300,140)); //$NON-NLS-1$
    public static final Position MAX_POSITION = new Position(Integer.MAX_VALUE,Integer.MAX_VALUE);

    public static final String CREATE_CONTROL_COMMAND_ID = InstallOptionsPlugin.getBundleResourceString("%create.control.command.id"); //$NON-NLS-1$
    public static final String EDIT_CONTROL_COMMAND_ID = InstallOptionsPlugin.getBundleResourceString("%edit.control.command.id"); //$NON-NLS-1$
    public static final String DELETE_CONTROL_COMMAND_ID = InstallOptionsPlugin.getBundleResourceString("%delete.control.command.id"); //$NON-NLS-1$
    public static final String DELETE_CONTROL_COMMAND_ID2 = InstallOptionsPlugin.getBundleResourceString("%delete.control.command.id2"); //$NON-NLS-1$

    public static final String TABBED_PROPERTIES_CONTRIBUTOR_ID = InstallOptionsPlugin.getBundleResourceString("%tabbed.properties.contributor.id"); //$NON-NLS-1$
}
