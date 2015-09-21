/*******************************************************************************
 * Copyright (C) 2005 - 2014 TIBCO Software Inc. All rights reserved.
 * http://www.jaspersoft.com.
 * 
 * Unless you have purchased  a commercial license agreement from Jaspersoft,
 * the following license terms  apply:
 * 
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package com.jaspersoft.studio.components.map.property;

import net.sf.jasperreports.components.map.StandardMapComponent;
import net.sf.jasperreports.eclipse.ui.util.UIUtils;
import net.sf.jasperreports.engine.design.JRDesignExpression;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import com.jaspersoft.studio.components.map.messages.Messages;
import com.jaspersoft.studio.components.map.model.MMap;
import com.jaspersoft.studio.components.map.model.marker.dialog.MarkerPage;
import com.jaspersoft.studio.properties.view.TabbedPropertySheetPage;
import com.jaspersoft.studio.property.section.AbstractSection;
import com.jaspersoft.studio.property.section.widgets.SPEvaluationTime;
import com.jaspersoft.studio.widgets.map.core.LatLng;
import com.jaspersoft.studio.widgets.map.core.MapType;
import com.jaspersoft.studio.widgets.map.ui.BasicInfoMapDialog;

/*
 * The location section on the location tab.
 * 
 * @author Chicu Veaceslav
 */
public class MapSection extends AbstractSection {

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ITabbedPropertySection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		parent.setLayout(new GridLayout(2, false));

		FormText mapPickSuggestion = new FormText(parent, SWT.NONE);
		mapPickSuggestion.setText(Messages.MapSection_0, true, false);
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 2;
		mapPickSuggestion.setLayoutData(gd);
		mapPickSuggestion.setWhitespaceNormalized(true);
		mapPickSuggestion.addHyperlinkListener(new HyperlinkAdapter() {
			private MarkerPage.BasicMapInfo mapInfo;

			@Override
			public void linkActivated(HyperlinkEvent e) {
				MMap mmap = (MMap) getElement();
				BasicInfoMapDialog pickmapDialog = new BasicInfoMapDialog(
						UIUtils.getShell()) {
					@Override
					protected void configureShell(Shell newShell) {
						super.configureShell(newShell);
						UIUtils.resizeAndCenterShell(newShell, 800, 600);
					}
				};
				if (mapInfo == null)
					mapInfo = MarkerPage.getBasicMapInformation(mmap);
				if (mapInfo.getLatitude() != null
						&& mapInfo.getLongitude() != null)
					pickmapDialog.setMapCenter(new LatLng(
							mapInfo.getLatitude(), mapInfo.getLongitude(), true));
				if (mapInfo.getMapType() != null)
					pickmapDialog.setMapType(MapType.fromStringID(mapInfo
							.getMapType().getName()));
				if (mapInfo.getZoom() != 0)
					pickmapDialog.setZoomLevel(mapInfo.getZoom());
				if (pickmapDialog.open() == Dialog.OK) {
					LatLng center = pickmapDialog.getMapCenter();
					int zoom = pickmapDialog.getZoomLevel();
					getElement().setPropertyValue(
							StandardMapComponent.PROPERTY_LATITUDE_EXPRESSION,
							new JRDesignExpression(center.getLat() + "f")); //$NON-NLS-1$
					getElement().setPropertyValue(
							StandardMapComponent.PROPERTY_LONGITUDE_EXPRESSION,
							new JRDesignExpression(center.getLng() + "f")); //$NON-NLS-1$
					getElement().setPropertyValue(
							StandardMapComponent.PROPERTY_ZOOM_EXPRESSION,
							new JRDesignExpression(String.valueOf(zoom)));
					getElement().setPropertyValue(
							StandardMapComponent.PROPERTY_MAP_TYPE,
							pickmapDialog.getMapType().ordinal());
				}
			}
		});
		createWidget4Property(parent, StandardMapComponent.PROPERTY_MAP_TYPE);
		createWidget4Property(parent,
				StandardMapComponent.PROPERTY_LATITUDE_EXPRESSION);
		createWidget4Property(parent,
				StandardMapComponent.PROPERTY_LONGITUDE_EXPRESSION);
		createWidget4Property(parent,
				StandardMapComponent.PROPERTY_ADDRESS_EXPRESSION);
		createWidget4Property(parent,
				StandardMapComponent.PROPERTY_ZOOM_EXPRESSION);

		createWidget4Property(parent,
				StandardMapComponent.PROPERTY_LANGUAGE_EXPRESSION);
		createWidget4Property(parent, StandardMapComponent.PROPERTY_MAP_SCALE);
		IPropertyDescriptor pd = getPropertyDesriptor(StandardMapComponent.PROPERTY_EVALUATION_TIME);
		IPropertyDescriptor gpd = getPropertyDesriptor(StandardMapComponent.PROPERTY_EVALUATION_GROUP);
		getWidgetFactory().createCLabel(parent, pd.getDisplayName());
		widgets.put(pd.getId(), new SPEvaluationTime(parent, this, pd, gpd));
		createWidget4Property(parent, StandardMapComponent.PROPERTY_IMAGE_TYPE);
		createWidget4Property(parent,
				StandardMapComponent.PROPERTY_ON_ERROR_TYPE);
	}

	@Override
	protected void initializeProvidedProperties() {
		super.initializeProvidedProperties();
		addProvidedProperties(StandardMapComponent.PROPERTY_EVALUATION_TIME,
				Messages.MMap_evaluation_time);
		addProvidedProperties(
				StandardMapComponent.PROPERTY_LATITUDE_EXPRESSION,
				Messages.MMap_latitude);
		addProvidedProperties(
				StandardMapComponent.PROPERTY_LONGITUDE_EXPRESSION,
				Messages.MMap_longitude);
		addProvidedProperties(StandardMapComponent.PROPERTY_ZOOM_EXPRESSION,
				Messages.MMap_zoom);
		addProvidedProperties(
				StandardMapComponent.PROPERTY_LANGUAGE_EXPRESSION,
				Messages.MMap_languageExpressionTitle);
		addProvidedProperties(StandardMapComponent.PROPERTY_MAP_TYPE,
				Messages.MMap_mapTypeTitle);
		addProvidedProperties(StandardMapComponent.PROPERTY_MAP_SCALE,
				Messages.MMap_mapScaleTitle);
		addProvidedProperties(StandardMapComponent.PROPERTY_IMAGE_TYPE,
				Messages.MMap_imageTypeTitle);
		addProvidedProperties(StandardMapComponent.PROPERTY_ON_ERROR_TYPE,
				Messages.MMap_OnErrorType);
	}

}
