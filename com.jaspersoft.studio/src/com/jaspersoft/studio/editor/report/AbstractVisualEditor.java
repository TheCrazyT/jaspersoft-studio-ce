/*******************************************************************************
 * Copyright © 2010-2023. Cloud Software Group, Inc. All rights reserved.
 *******************************************************************************/
package com.jaspersoft.studio.editor.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.dnd.AbstractTransferDropTargetListener;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.CopyTemplateAction;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.MatchHeightAction;
import org.eclipse.gef.ui.actions.MatchWidthAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite.FlyoutPreferences;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.jaspersoft.studio.JaspersoftStudioPlugin;
import com.jaspersoft.studio.background.action.BackgroundEndTransformationAction;
import com.jaspersoft.studio.background.action.BackgroundFitAction;
import com.jaspersoft.studio.background.action.BackgroundKeepRatioAction;
import com.jaspersoft.studio.background.action.BackgroundTransparencyAction;
import com.jaspersoft.studio.callout.action.CreatePinAction;
import com.jaspersoft.studio.editor.AbstractJRXMLEditor;
import com.jaspersoft.studio.editor.IGraphicalEditor;
import com.jaspersoft.studio.editor.ZoomActualAction;
import com.jaspersoft.studio.editor.action.BindElementsAction;
import com.jaspersoft.studio.editor.action.CustomDeleteAction;
import com.jaspersoft.studio.editor.action.EncloseIntoFrameAction;
import com.jaspersoft.studio.editor.action.HideElementsAction;
import com.jaspersoft.studio.editor.action.MoveDetailDownAction;
import com.jaspersoft.studio.editor.action.MoveDetailUpAction;
import com.jaspersoft.studio.editor.action.MoveGroupDownAction;
import com.jaspersoft.studio.editor.action.MoveGroupUpAction;
import com.jaspersoft.studio.editor.action.OpenEditorAction;
import com.jaspersoft.studio.editor.action.ShowPropertyViewAction;
import com.jaspersoft.studio.editor.action.UnBindElementsAction;
import com.jaspersoft.studio.editor.action.align.Align2BorderAction;
import com.jaspersoft.studio.editor.action.align.Align2Element;
import com.jaspersoft.studio.editor.action.band.MaximizeContainerAction;
import com.jaspersoft.studio.editor.action.band.StretchToContentAction;
import com.jaspersoft.studio.editor.action.copy.CopyAction;
import com.jaspersoft.studio.editor.action.copy.CopyFormatAction;
import com.jaspersoft.studio.editor.action.copy.CutAction;
import com.jaspersoft.studio.editor.action.copy.PasteAction;
import com.jaspersoft.studio.editor.action.copy.PasteFormatAction;
import com.jaspersoft.studio.editor.action.exporter.AddExporterPropertyAction;
import com.jaspersoft.studio.editor.action.image.ChangeImageExpression;
import com.jaspersoft.studio.editor.action.order.BringBackwardAction;
import com.jaspersoft.studio.editor.action.order.BringForwardAction;
import com.jaspersoft.studio.editor.action.order.BringToBackAction;
import com.jaspersoft.studio.editor.action.order.BringToFrontAction;
import com.jaspersoft.studio.editor.action.size.MatchSizeAction;
import com.jaspersoft.studio.editor.action.size.Size2BorderAction;
import com.jaspersoft.studio.editor.action.snap.KeepUnitsInReportAction;
import com.jaspersoft.studio.editor.action.snap.ShowGridAction;
import com.jaspersoft.studio.editor.action.snap.ShowRullersAction;
import com.jaspersoft.studio.editor.action.snap.SizeGridAction;
import com.jaspersoft.studio.editor.action.snap.SnapToGeometryAction;
import com.jaspersoft.studio.editor.action.snap.SnapToGridAction;
import com.jaspersoft.studio.editor.action.snap.SnapToGuidesAction;
import com.jaspersoft.studio.editor.action.text.AdjustTextFontSize;
import com.jaspersoft.studio.editor.action.text.BoldAction;
import com.jaspersoft.studio.editor.action.text.ConvertStaticIntoText;
import com.jaspersoft.studio.editor.action.text.ConvertTextIntoStatic;
import com.jaspersoft.studio.editor.action.text.ItalicAction;
import com.jaspersoft.studio.editor.action.text.StrikethroughAction;
import com.jaspersoft.studio.editor.action.text.UnderlineAction;
import com.jaspersoft.studio.editor.defaults.SetDefaultsAction;
import com.jaspersoft.studio.editor.dnd.ImageResourceDropTargetListener;
import com.jaspersoft.studio.editor.dnd.ImageURLTransfer;
import com.jaspersoft.studio.editor.dnd.JSSTemplateTransferDropTargetListener;
import com.jaspersoft.studio.editor.gef.rulers.component.JDRulerComposite;
import com.jaspersoft.studio.editor.gef.ui.actions.RZoomComboContributionItem;
import com.jaspersoft.studio.editor.gef.ui.actions.ViewSettingsDropDownAction;
import com.jaspersoft.studio.editor.java2d.J2DGraphicalEditorWithFlyoutPalette;
import com.jaspersoft.studio.editor.layout.LayoutManager;
import com.jaspersoft.studio.editor.menu.AppContextMenuProvider;
import com.jaspersoft.studio.editor.outline.JDReportOutlineView;
import com.jaspersoft.studio.editor.outline.actions.ConnectToDatasetAction;
import com.jaspersoft.studio.editor.outline.actions.CreateConditionalStyleAction;
import com.jaspersoft.studio.editor.outline.actions.CreateDatasetAction;
import com.jaspersoft.studio.editor.outline.actions.CreateGroupAction;
import com.jaspersoft.studio.editor.outline.actions.CreateParameterAction;
import com.jaspersoft.studio.editor.outline.actions.CreateParameterSetAction;
import com.jaspersoft.studio.editor.outline.actions.CreateScriptletAction;
import com.jaspersoft.studio.editor.outline.actions.CreateSortFieldAction;
import com.jaspersoft.studio.editor.outline.actions.CreateStyleAction;
import com.jaspersoft.studio.editor.outline.actions.CreateStyleTemplateAction;
import com.jaspersoft.studio.editor.outline.actions.CreateVariableAction;
import com.jaspersoft.studio.editor.outline.actions.HideDefaultVariablesAction;
import com.jaspersoft.studio.editor.outline.actions.HideDefaultsParametersAction;
import com.jaspersoft.studio.editor.outline.actions.RefreshImageAction;
import com.jaspersoft.studio.editor.outline.actions.RefreshTemplateStyleExpression;
import com.jaspersoft.studio.editor.outline.actions.RefreshTemplateStyleReference;
import com.jaspersoft.studio.editor.outline.actions.ResetStyleAction;
import com.jaspersoft.studio.editor.outline.actions.SaveStyleAsTemplateAction;
import com.jaspersoft.studio.editor.outline.actions.SortParametersAction;
import com.jaspersoft.studio.editor.outline.actions.SortVariablesAction;
import com.jaspersoft.studio.editor.outline.actions.field.CreateFieldAction;
import com.jaspersoft.studio.editor.outline.actions.field.CreateFieldsContainerAction;
import com.jaspersoft.studio.editor.outline.actions.field.DeleteFieldsAllGroupAction;
import com.jaspersoft.studio.editor.outline.actions.field.DeleteFieldsGroupAction;
import com.jaspersoft.studio.editor.outline.actions.field.ShowFieldsTreeAction;
import com.jaspersoft.studio.editor.outline.actions.field.SortFieldsAction;
import com.jaspersoft.studio.editor.outline.page.MultiOutlineView;
import com.jaspersoft.studio.editor.palette.JDPaletteFactory;
import com.jaspersoft.studio.editor.palette.JSSPaletteContextMenuProvider;
import com.jaspersoft.studio.editor.part.MultiPageToolbarEditorPart;
import com.jaspersoft.studio.editor.tools.CreateCompositeElementAction;
import com.jaspersoft.studio.formatting.actions.CenterInParentAction;
import com.jaspersoft.studio.formatting.actions.DecreaseHSpaceAction;
import com.jaspersoft.studio.formatting.actions.DecreaseVSpaceAction;
import com.jaspersoft.studio.formatting.actions.EqualsHSpaceAction;
import com.jaspersoft.studio.formatting.actions.EqualsVSpaceAction;
import com.jaspersoft.studio.formatting.actions.IncreaseHSpaceAction;
import com.jaspersoft.studio.formatting.actions.IncreaseVSpaceAction;
import com.jaspersoft.studio.formatting.actions.JoinLeftAction;
import com.jaspersoft.studio.formatting.actions.JoinRightAction;
import com.jaspersoft.studio.formatting.actions.OrganizeAsTableAction;
import com.jaspersoft.studio.formatting.actions.RemoveHSpaceAction;
import com.jaspersoft.studio.formatting.actions.RemoveVSpaceAction;
import com.jaspersoft.studio.formatting.actions.SameHeightMaxAction;
import com.jaspersoft.studio.formatting.actions.SameHeightMinAction;
import com.jaspersoft.studio.formatting.actions.SameWidthMaxAction;
import com.jaspersoft.studio.formatting.actions.SameWidthMinAction;
import com.jaspersoft.studio.model.INode;
import com.jaspersoft.studio.model.MPage;
import com.jaspersoft.studio.model.MReport;
import com.jaspersoft.studio.model.MRoot;
import com.jaspersoft.studio.preferences.DesignerPreferencePage;
import com.jaspersoft.studio.preferences.RulersGridPreferencePage;
import com.jaspersoft.studio.style.view.TemplateViewProvider;
import com.jaspersoft.studio.utils.SelectionHelper;
import com.jaspersoft.studio.utils.UIUtil;
import com.jaspersoft.studio.utils.jasper.JasperReportsConfiguration;

import net.sf.jasperreports.eclipse.ui.util.UIUtils;
import net.sf.jasperreports.eclipse.util.FileUtils;

/*
 * The Class AbstractVisualEditor.
 * 
 * @author Chicu Veaceslav
 */
public abstract class AbstractVisualEditor extends J2DGraphicalEditorWithFlyoutPalette
		implements IAdaptable, IGraphicalEditor, CachedSelectionProvider {

	private Image partImage = JaspersoftStudioPlugin.getInstance().getImage(MReport.getIconDescriptor().getIcon16());

	private FlyoutPreferences palettePreferences;

	protected JasperReportsConfiguration jrContext;

	/** The ruler composite. */
	private JDRulerComposite rulerComp;

	public JasperReportsConfiguration getJrContext() {
		return jrContext;
	}

	public Image getPartImage() {
		return partImage;
	}

	/**
	 * Instantiates a new abstract visual editor.
	 */
	public AbstractVisualEditor(JasperReportsConfiguration jrContext) {
		this.jrContext = jrContext;
		ScrollEditDomain ed = new ScrollEditDomain(this);
		setEditDomain(ed);
	}

	@Override
	public DefaultEditDomain getEditDomain() {
		return super.getEditDomain();
	}

	@Override
	public void setEditDomain(DefaultEditDomain ed) {
		super.setEditDomain(ed);
	}

	public void setPartImage(Image partImage) {
		this.partImage = partImage;
	}

	private INode model;

	/**
	 * Sets the model.
	 * 
	 * @param model the new model
	 */
	public void setModel(INode model) {
		this.model = model;
		getGraphicalViewer().setContents(model);
		if (outlinePage != null) {
			// The outline for the current editor maybe not available because it
			// was closed
			// and reopened into another editor. So when we try to set its
			// contents it is
			// better to check if it was disposed outside and in that case
			// recreated it.
			if (outlinePage.isDisposed()) {
				// If the outline is recreated by calling the getOutlineView
				// then the setContends it is already done so we need to do it
				// only in the else
				// case
				getOutlineView();
			} else
				outlinePage.setContents(model);
		}
	}

	/**
	 * Gets the model.
	 * 
	 * @return the model
	 */
	public INode getModel() {
		return model;
	}

	public ISelection getOutlineSelection() {
		if (outlinePage != null && !outlinePage.isDisposed()) {
			return outlinePage.getSelection();
		}
		return StructuredSelection.EMPTY;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#getActionRegistry()
	 */
	@Override
	public ActionRegistry getActionRegistry() {
		return super.getActionRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.jaspersoft.studio.editor.java2d.J2DGraphicalEditorWithFlyoutPalette#
	 * createGraphicalViewer(org.eclipse.swt. widgets .Composite)
	 */
	@Override
	protected void createGraphicalViewer(Composite parent) {
		// the rulerComp is the composite that will contain both the ruler and
		// the editor as child
		// this doens't change if the editor is visible or not, since it act
		// only as container
		rulerComp = new JDRulerComposite(parent, SWT.NONE, this);
		super.createGraphicalViewer(rulerComp);
		rulerComp.setGraphicalViewer((ScrollingGraphicalViewer) getGraphicalViewer());
	}

	/**
	 * Return the ruler composite, the ruler has the possibility to layout the
	 * complete editor area trough the layout() method
	 */
	public JDRulerComposite getRuler() {
		return rulerComp;
	}

	public void layout() {
		// this is a short running method so it can be executed synchronously
		UIUtils.getDisplay().syncExec(() -> {
			if (!rulerComp.isDisposed()) {
				UIUtil.safeRequestLayout(rulerComp);
				rulerComp.redraw();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getPaletteRoot(
	 * )
	 */
	@Override
	protected PaletteRoot getPaletteRoot() {
		return JDPaletteFactory.createPalette(getIgnorePalleteElements(), jrContext);
	}

	protected abstract List<String> getIgnorePalleteElements();

	// FIXME: something wrong, I should not do that, order in initialisation is
	// wrong

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * getGraphicalControl ()
	 */
	@Override
	protected Control getGraphicalControl() {
		if (rulerComp != null)
			return rulerComp;
		return super.getGraphicalControl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#getSelectionSynchronizer()
	 */
	@Override
	public SelectionSynchronizer getSelectionSynchronizer() {
		return super.getSelectionSynchronizer();
	}

	/**
	 * Gets the editor.
	 * 
	 * @return the editor
	 */
	public FigureCanvas getEditor() {
		return (FigureCanvas) getGraphicalViewer().getControl();
	}

	/** The shared key handler. */
	private KeyHandler sharedKeyHandler;

	/**
	 * Gets the common key handler.
	 * 
	 * @return the common key handler
	 */
	public KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
		}
		return sharedKeyHandler;
	}

	/**
	 * Creates the additional actions.
	 */
	protected void createAdditionalActions() {
		GraphicalViewer graphicalViewer = getGraphicalViewer();
		// Show Grid Action
		Boolean isGridVisible = jrContext.getPropertyBoolean(RulersGridPreferencePage.P_PAGE_RULERGRID_SHOWGRID, true);
		Boolean isSnapToGuides = jrContext.getPropertyBoolean(RulersGridPreferencePage.P_PAGE_RULERGRID_SNAPTOGUIDES,
				true);
		Boolean isSnapToGrid = jrContext.getPropertyBoolean(RulersGridPreferencePage.P_PAGE_RULERGRID_SNAPTOGRID, true);
		Boolean isSnapToGeometry = jrContext
				.getPropertyBoolean(RulersGridPreferencePage.P_PAGE_RULERGRID_SNAPTOGEOMETRY, true);

		int gspaceX = jrContext.getPropertyInteger(RulersGridPreferencePage.P_PAGE_RULERGRID_GRIDSPACEX, 10);
		int gspaceY = jrContext.getPropertyInteger(RulersGridPreferencePage.P_PAGE_RULERGRID_GRIDSPACEY, 10);

		graphicalViewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, isSnapToGrid);
		graphicalViewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, isGridVisible);
		graphicalViewer.setProperty(SnapToGrid.PROPERTY_GRID_ORIGIN, new Point(30, 30));
		graphicalViewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING, new Dimension(gspaceX, gspaceY));
		graphicalViewer.setProperty(SnapToGuidesAction.ID, isSnapToGuides);
		graphicalViewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, isSnapToGeometry);

		IAction showGrid = new ShowGridAction(jrContext);
		getActionRegistry().registerAction(showGrid);

		SnapToGridAction snapGridAction = new SnapToGridAction(jrContext);
		getActionRegistry().registerAction(snapGridAction);

		SizeGridAction sizeGridAction = new SizeGridAction(jrContext);
		getActionRegistry().registerAction(sizeGridAction);

		// snap to geometry
		IAction snapAction = new SnapToGeometryAction(jrContext);
		getActionRegistry().registerAction(snapAction);

		snapAction = new SnapToGuidesAction(jrContext);
		getActionRegistry().registerAction(snapAction);

		// show rullers
		IAction showRulers = new ShowRullersAction(jrContext);
		getActionRegistry().registerAction(showRulers);
		// zoom manager actions
		ZoomManager zoomManager = (ZoomManager) graphicalViewer.getProperty(ZoomManager.class.toString());

		getActionRegistry().registerAction(new ZoomInAction(zoomManager));
		getActionRegistry().registerAction(new ZoomOutAction(zoomManager));
		getActionRegistry().registerAction(new ZoomActualAction(zoomManager));
		graphicalViewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1), MouseWheelZoomHandler.SINGLETON);

		// set context menu
		graphicalViewer.setContextMenu(new AppContextMenuProvider(graphicalViewer, getActionRegistry()));

		graphicalViewer.setProperty("JRCONTEXT", jrContext);

		LayoutManager.addActions(getActionRegistry(), this, getSelectionActions());

		JaspersoftStudioPlugin.getDecoratorManager().registerActions(getActionRegistry(), getSelectionActions(),
				getGraphicalViewer(), this);
		JaspersoftStudioPlugin.getEditorSettingsManager().registerActions(getActionRegistry(), jrContext);

	}

	/**
	 * Force the refresh of the actions enablement and visibility state
	 */
	public void forceUpdateActions() {
		updateActions(getSelectionActions());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditor#selectionChanged(org.eclipse.ui.
	 * IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isSame(part)) {
			updateActions(getSelectionActions());
			//fix for community issue 12061, look at RedrawingEditPolicy for more informations
			if (Util.isLinux()) {
				layout();
			}
		}
	}

	private boolean isSame(IWorkbenchPart part) {
		if (part == getSite().getPart())
			return true;
		if (part instanceof MultiPageEditorPart) {
			Object spage = ((MultiPageEditorPart) part).getSelectedPage();
			if (spage instanceof IWorkbenchPart)
				return isSame((IWorkbenchPart) spage);
		} else if (part instanceof MultiPageToolbarEditorPart) {
			Object spage = ((MultiPageToolbarEditorPart) part).getSelectedPage();
			if (spage instanceof IWorkbenchPart)
				return isSame((IWorkbenchPart) spage);
		}
		if (part instanceof ContentOutline) {
			IContentOutlinePage outPage = (IContentOutlinePage)part.getAdapter(IContentOutlinePage.class);
			if (outPage instanceof MultiOutlineView)
				return isSame(((MultiOutlineView) outPage).getEditor());
			else if (outPage instanceof JDReportOutlineView) {
				JDReportOutlineView coPage = (JDReportOutlineView) outPage;
				return coPage == outlinePage;
			}
		}
		return false;
	}

	/** The outline page. */
	protected JDReportOutlineView outlinePage;
	private EditorContributor editorContributor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#getAdapter(
	 * java. lang.Class)
	 */
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class type) {
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		if (type == IContentOutlinePage.class) {
			return getOutlineView();
		}
		if (type == EditorContributor.class) {
			if (editorContributor == null)
				editorContributor = new EditorContributor(getEditDomain());
			return editorContributor;
		}
		return super.getAdapter(type);
	}

	protected JDReportOutlineView getOutlineView() {
		if (outlinePage == null || outlinePage.isDisposed()) {
			TreeViewer viewer = new TreeViewer();
			outlinePage = new JDReportOutlineView(this, viewer);
		}
		outlinePage.setContents(getModel());
		return outlinePage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.
	 * IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		getEditDomain().getCommandStack().markSaveLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * initializeGraphicalViewer()
	 */
	@Override
	protected void initializeGraphicalViewer() {
		super.initializeGraphicalViewer();
		initializeEditor();
	}

	@Override
	protected FlyoutPreferences getPalettePreferences() {
		// We cache the palette preferences for the open editor
		// Default implementation returns a new FlyoutPreferences object
		// every time the getPalettePreferences method is invoked.
		if (palettePreferences == null) {
			palettePreferences = super.getPalettePreferences();
			// Palette always opened
			palettePreferences.setPaletteState(FlyoutPaletteComposite.STATE_PINNED_OPEN);
		}
		return palettePreferences;
	}

	protected void initializeEditor() {
		GraphicalViewer graphicalViewer = getGraphicalViewer();
		graphicalViewer.addDropTargetListener(new JSSTemplateTransferDropTargetListener(graphicalViewer));
		graphicalViewer.addDropTargetListener(new ReportUnitDropTargetListener(graphicalViewer));
		graphicalViewer.addDropTargetListener(
				new ImageResourceDropTargetListener(graphicalViewer, ResourceTransfer.getInstance()));
		graphicalViewer.addDropTargetListener(
				new ImageResourceDropTargetListener(graphicalViewer, FileTransfer.getInstance()));
		graphicalViewer.addDropTargetListener(
				new ImageResourceDropTargetListener(graphicalViewer, ImageURLTransfer.getInstance()));

		// Load the contributed drop providers for the contributed template
		// styles
		List<TemplateViewProvider> dropProviders = JaspersoftStudioPlugin.getExtensionManager().getStylesViewProvider();
		for (TemplateViewProvider provider : dropProviders) {
			AbstractTransferDropTargetListener listener = provider.getDropListener(graphicalViewer);
			if (listener != null)
				graphicalViewer.addDropTargetListener(listener);
		}

		getEditorSite().getActionBarContributor();

		graphicalViewer.getControl().addFocusListener(new FocusListener() {
			protected IContextActivation context;

			@Override
			public void focusLost(FocusEvent e) {
				IContextService service = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
				if (context != null && service != null) {
					// it could be activated somewhere else, we don't know, so I
					// add this dirty :(
					for (int i = 0; i < 10; i++)
						service.deactivateContext(context);
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				IContextService service = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
				if (service != null)
					context = service.activateContext("com.jaspersoft.studio.context"); //$NON-NLS-1$
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#getGraphicalViewer()
	 */
	@Override
	public GraphicalViewer getGraphicalViewer() {
		return super.getGraphicalViewer();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * createPalettePage()
	 */
	@Override
	protected CustomPalettePage createPalettePage() {
		return new CustomPalettePage(getPaletteViewerProvider()) {
			@Override
			public void init(IPageSite pageSite) {
				super.init(pageSite);
				IAction copy = getActionRegistry().getAction(ActionFactory.COPY.getId());
				pageSite.getActionBars().setGlobalActionHandler(ActionFactory.COPY.getId(), copy);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette#
	 * createPaletteViewerProvider()
	 */
	@Override
	protected PaletteViewerProvider createPaletteViewerProvider() {
		return new PaletteViewerProvider(getEditDomain()) {

			@Override
			protected void configurePaletteViewer(PaletteViewer viewer) {
				viewer.setContextMenu(new JSSPaletteContextMenuProvider(viewer));
				viewer.addDragSourceListener(new TemplateTransferDragSourceListener(viewer));
				// set the selection tool into the palette
				viewer.getEditDomain().setDefaultTool(new JSSPaletteSelectionTool(getEditDomain()));
				viewer.getEditDomain().loadDefaultTool();
				// Uncomment these lines if you want to set as default a palette
				// with column layout and large icons.
				// // TODO: we should replace these default suggestions not
				// using the GEF
				// preference
				// // store explicitly. It would be better override the
				// PaletteViewer creation
				// in order
				// // to have a custom PaletteViewerPreferences
				// (#viewer.getPaletteViewerPreferences()).
				// // This way we could store the preferences in our preference
				// store (maybe the
				// JaspersoftStudio plugin one).
				// // For now we'll stay with this solution avoiding the user to
				// lose previous
				// saved preferences
				// // regarding the palette.
				// InternalGEFPlugin.getDefault().getPreferenceStore().setDefault(
				// PaletteViewerPreferences.PREFERENCE_LAYOUT,
				// PaletteViewerPreferences.LAYOUT_COLUMNS);
				// InternalGEFPlugin.getDefault().getPreferenceStore().setDefault(
				// PaletteViewerPreferences.PREFERENCE_COLUMNS_ICON_SIZE,true);
			}

			@Override
			protected void hookPaletteViewer(PaletteViewer viewer) {
				super.hookPaletteViewer(viewer);
				final CopyTemplateAction copy = new CopyTemplateAction(AbstractVisualEditor.this);
				viewer.addSelectionChangedListener(copy);
			}
		};
	}

	/**
	 * Return the selection cache extracting it from the current jr context
	 */
	public CommonSelectionCacheProvider getSelectionCache() {
		return (CommonSelectionCacheProvider) jrContext.get(ReportContainer.SELECTION_CACHE_KEY);
	}

	/**
	 * Create the contextual action to add stuff to the datasets (fields,
	 * variables) and to create styles.
	 */
	protected void createDatasetAndStyleActions(ActionRegistry registry) {
		List<String> selectionActions = getSelectionActions();

		IAction action = new CreateFieldAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateFieldAction.ID);

		action = new CreateFieldsContainerAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateFieldsContainerAction.ID);

		action = new CreateSortFieldAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateSortFieldAction.ID);

		action = new CreateVariableAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateVariableAction.ID);

		action = new SortVariablesAction(this);
		registry.registerAction(action);
		selectionActions.add(SortVariablesAction.ID);

		action = new SortParametersAction(this);
		registry.registerAction(action);
		selectionActions.add(SortParametersAction.ID);

		action = new SortFieldsAction(this);
		registry.registerAction(action);
		selectionActions.add(SortFieldsAction.ID);

		action = new ShowFieldsTreeAction(this);
		registry.registerAction(action);
		selectionActions.add(ShowFieldsTreeAction.ID);

		action = new DeleteFieldsGroupAction(this);
		registry.registerAction(action);
		selectionActions.add(DeleteFieldsGroupAction.ID);

		action = new DeleteFieldsAllGroupAction(this);
		registry.registerAction(action);
		selectionActions.add(DeleteFieldsAllGroupAction.ID);

		action = new HideDefaultsParametersAction(this);
		registry.registerAction(action);
		selectionActions.add(HideDefaultsParametersAction.ID);

		action = new HideDefaultVariablesAction(this);
		registry.registerAction(action);
		selectionActions.add(HideDefaultVariablesAction.ID);

		action = new CreateScriptletAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateScriptletAction.ID);

		action = new CreateParameterAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateParameterAction.ID);

		action = new CreateParameterSetAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateParameterSetAction.ID);

		action = new CreateGroupAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateGroupAction.ID);

		action = new CreateDatasetAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateDatasetAction.ID);

		action = new CreateStyleAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateStyleAction.ID);

		action = new CreateConditionalStyleAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateConditionalStyleAction.ID);

		action = new SaveStyleAsTemplateAction(this);
		registry.registerAction(action);
		selectionActions.add(SaveStyleAsTemplateAction.ID);

		action = new ResetStyleAction(this);
		registry.registerAction(action);
		selectionActions.add(ResetStyleAction.ID);

		action = new CreateStyleTemplateAction(this);
		registry.registerAction(action);
		selectionActions.add(CreateStyleTemplateAction.ID);

		action = new RefreshTemplateStyleExpression(this);
		registry.registerAction(action);
		selectionActions.add(RefreshTemplateStyleExpression.ID);

		action = new RefreshTemplateStyleReference(this);
		registry.registerAction(action);
		selectionActions.add(RefreshTemplateStyleReference.ID);

		action = new RefreshImageAction(this);
		registry.registerAction(action);
		selectionActions.add(RefreshImageAction.ID);
	}

	protected void createDeleteAction(ActionRegistry registry) {
		List<String> selectionActions = getSelectionActions();
		CustomDeleteAction deleteAction = new CustomDeleteAction(this);
		registry.registerAction(deleteAction);
		selectionActions.add(deleteAction.getId());
	}

	protected void createCopyAction(ActionRegistry registry) {
		List<String> selectionActions = getSelectionActions();
		IAction action = new CopyAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());
	}

	protected void createPasteAction(ActionRegistry registry) {
		List<String> selectionActions = getSelectionActions();
		IAction action = new PasteAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());
	}

	protected void createCutAction(ActionRegistry registry) {
		List<String> selectionActions = getSelectionActions();
		IAction action = new CutAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
	 */
	@Override
	protected void createActions() {
		super.createActions();

		ActionRegistry registry = getActionRegistry();

		IAction action = null;

		List<String> selectionActions = getSelectionActions();

		// Create the custom delete action that aggregate all the messages when
		// more
		// elements are deleted
		// the old default action is replaced
		createDeleteAction(registry);

		// Create the copy, paste and cut actions
		createCutAction(registry);
		createPasteAction(registry);
		createCopyAction(registry);

		action = new KeepUnitsInReportAction(jrContext);
		registry.registerAction(action);

		action = new HideElementsAction(this, true);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new HideElementsAction(this, false);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new CopyFormatAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new PasteFormatAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SetDefaultsAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MatchWidthAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MatchHeightAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());
		// create actions
		createEditorActions(registry);

		// ------------
		action = new DirectEditAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// ------------
		action = new BringForwardAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BringToFrontAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BringToBackAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BringBackwardAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// --Create image change path action --
		action = new ChangeImageExpression(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// --Create exporter properties action --
		action = new AddExporterPropertyAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// ------------
		action = new Align2Element(this.getSite().getPart(), PositionConstants.LEFT);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2Element(this.getSite().getPart(), PositionConstants.RIGHT);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2Element(this.getSite().getPart(), PositionConstants.TOP);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2Element(this.getSite().getPart(), PositionConstants.BOTTOM);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2Element(this.getSite().getPart(), PositionConstants.CENTER);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2Element(this.getSite().getPart(), PositionConstants.MIDDLE);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// ------------
		action = new Align2BorderAction(this, PositionConstants.LEFT);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2BorderAction(this, PositionConstants.RIGHT);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2BorderAction(this, PositionConstants.TOP);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2BorderAction(this, PositionConstants.BOTTOM);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2BorderAction(this, PositionConstants.CENTER);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Align2BorderAction(this, PositionConstants.MIDDLE);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new CenterInParentAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// ---------------------

		action = new MatchSizeAction(this, MatchSizeAction.TYPE.WIDTH);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MatchSizeAction(this, MatchSizeAction.TYPE.HEIGHT);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MatchSizeAction(this, MatchSizeAction.TYPE.BOTH);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SameHeightMaxAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SameHeightMinAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SameWidthMaxAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new SameWidthMinAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Horizontal Spacing Actions

		action = new IncreaseHSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new DecreaseHSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new RemoveHSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new EqualsHSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Vertical Spacing Actions

		action = new IncreaseVSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new DecreaseVSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new RemoveVSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new EqualsVSpaceAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Join Spacing Actions

		action = new JoinRightAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new JoinLeftAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// ---------------------

		action = new Size2BorderAction(this, Size2BorderAction.WIDTH);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Size2BorderAction(this, Size2BorderAction.HEIGHT);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new Size2BorderAction(this, Size2BorderAction.BOTH);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MaximizeContainerAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new OrganizeAsTableAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new StretchToContentAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// ------------------

		action = new ShowPropertyViewAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BoldAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new ItalicAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new UnderlineAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new StrikethroughAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new CreatePinAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Start of the convert action
		action = new ConvertTextIntoStatic(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new ConvertStaticIntoText(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());
		// End of the convert actions

		action = new AdjustTextFontSize(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Move group and detail actions
		action = new MoveGroupUpAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MoveGroupDownAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MoveDetailUpAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new MoveDetailDownAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new EncloseIntoFrameAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BindElementsAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new UnBindElementsAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new CreateCompositeElementAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new ConnectToDatasetAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Action to open a subreport into the editor
		action = new OpenEditorAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// Action to handle the background

		action = new BackgroundFitAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundKeepRatioAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundEndTransformationAction(this);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_5);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_10);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_15);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_20);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_25);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_30);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_40);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_50);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_75);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		action = new BackgroundTransparencyAction(this, BackgroundTransparencyAction.TRANSPARENCY_100);
		registry.registerAction(action);
		selectionActions.add(action.getId());

		// End of the background actions

		// Contributed actions
		List<Action> contributedActions = JaspersoftStudioPlugin.getExtensionManager().getEditorActions(this);
		for (Action cAction : contributedActions) {
			registry.registerAction(cAction);
			selectionActions.add(cAction.getId());
		}

	}

	protected void createEditorActions(ActionRegistry registry) {

	}

	protected RZoomComboContributionItem zoomItem = null;
	protected IToolBarManager topToolbarManager;
	protected List<ActionContributionItem> act4TextIcon = new ArrayList<>();
	protected IPropertyChangeListener pcListener;

	@Override
	public void dispose() {
		if (pcListener != null)
			JaspersoftStudioPlugin.getInstance().removePreferenceListener(pcListener);
		super.dispose();
	}

	protected void setTextIcon() {
		UIUtils.getDisplay().asyncExec(() -> {
			JasperReportsConfiguration jc = getJrContext();
			Boolean forceText = jc.getPropertyBoolean(DesignerPreferencePage.P_TITLEICON);
			if (pcListener == null) {
				pcListener = event -> {
					String property = event.getProperty();
					if (property.equals(DesignerPreferencePage.P_TITLEICON))
						setTextIcon();
				};
				JaspersoftStudioPlugin.getInstance()
						.addPreferenceListener(pcListener, (IResource) jc.get(FileUtils.KEY_FILE));
			}

			for (ActionContributionItem act : act4TextIcon)
				act.setMode(forceText != null && forceText ? ActionContributionItem.MODE_FORCE_TEXT : 0);
			topToolbarManager.update(true);
		});
	}

	/**
	 * Contributes items to the specified toolbar that is supposed to be put on
	 * the top right of the current visual editor (i.e: ReportEditor,
	 * CrosstabEditor, TableEditor, ListEditor).
	 * <p>
	 * 
	 * Default behavior contributes the following items:
	 * <ul>
	 * <li>Zoom In</li>
	 * <li>Zoom Out</li>
	 * <li>Zoom Combo</li>
	 * <li>Global "View" settings drop down menu</li>
	 * </ul>
	 * 
	 * Sub-classes may want to override this method to modify the toolbar.
	 * 
	 * @param toolbarManager the toolbar manager to be enriched
	 */
	public void contributeItemsToEditorTopToolbar(IToolBarManager toolbarManager) {
		this.topToolbarManager = toolbarManager;
		toolbarManager.add(getActionRegistry().getAction(GEFActionConstants.ZOOM_IN));
		toolbarManager.add(getActionRegistry().getAction(GEFActionConstants.ZOOM_OUT));
		if (zoomItem != null) {
			zoomItem.dispose();
			zoomItem = null;
		}
		GraphicalViewer graphicalViewer = getGraphicalViewer();
		ZoomManager property = (ZoomManager) graphicalViewer.getProperty(ZoomManager.class.toString());
		if (property != null) {
			zoomItem = new RZoomComboContributionItem(property);
			toolbarManager.add(zoomItem);
		}
		toolbarManager.add(new Separator());
		// Global "View" menu items
		toolbarManager.add(new ViewSettingsDropDownAction(getActionRegistry()));
		setTextIcon();
	}

	/**
	 * Return the main element managed by this editor, page and root are
	 * excluded
	 */
	public INode getManagedElement() {
		INode node = model;
		while (node != null && !node.getChildren().isEmpty() && (node instanceof MRoot || node instanceof MPage))
			node = node.getChildren().get(node.getChildren().size() - 1);
		return node;
	}

	/**
	 * Check if the current editor is the visible page of the multi page editor
	 * 
	 * @return true if the editor is visible, false otherwise
	 */
	public boolean isEditorVisible() {
		IEditorPart editor = SelectionHelper.getActiveJRXMLEditor();
		if (editor instanceof AbstractJRXMLEditor) {
			AbstractJRXMLEditor jrxmlEditor = (AbstractJRXMLEditor) editor;
			IEditorPart activeEditor = jrxmlEditor.getActiveInnerEditor();
			if (activeEditor != null)
				return (this == activeEditor);
		}
		return true;
	}
}
