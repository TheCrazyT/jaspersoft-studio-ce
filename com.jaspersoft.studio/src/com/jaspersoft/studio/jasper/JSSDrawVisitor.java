/*******************************************************************************
 * Copyright © 2010-2023. Cloud Software Group, Inc. All rights reserved.
 *******************************************************************************/
package com.jaspersoft.studio.jasper;

import java.awt.Graphics2D;

import net.sf.jasperreports.engine.JRBreak;
import net.sf.jasperreports.engine.JRElement;
import net.sf.jasperreports.engine.JRElementGroup;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JRReport;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.convert.ReportConverter;
import net.sf.jasperreports.engine.export.AwtTextRenderer;
import net.sf.jasperreports.engine.export.draw.Offset;
import net.sf.jasperreports.engine.export.draw.PrintDrawVisitor;
import net.sf.jasperreports.engine.export.draw.TextDrawer;
import net.sf.jasperreports.engine.util.JRStyledText;
import net.sf.jasperreports.engine.util.UniformElementVisitor;
import net.sf.jasperreports.export.Graphics2DReportConfiguration;

public class JSSDrawVisitor extends UniformElementVisitor {

	protected JSSConvertVisitor convertVisitor;
	protected PrintDrawVisitor drawVisitor;
	protected ReportConverter reportConverter;
	/**
	 * The graphics 2d actually used by the visitor
	 */
	private Graphics2D grx;
	
	private boolean ignoreFont;
	
	private boolean minPrintJobSize;

	/**
	 *
	 */
	public JSSDrawVisitor(ReportConverter reportConverter, Graphics2D grx) {
		this.reportConverter = reportConverter;
		this.convertVisitor = new JSSConvertVisitor(reportConverter);
		JasperReportsContext jasperReportsContext = reportConverter.getJasperReportsContext();
		JRPropertiesUtil putil = JRPropertiesUtil.getInstance(jasperReportsContext);
		JRReport report = reportConverter.getReport();
		minPrintJobSize = putil.getBooleanProperty(report, Graphics2DReportConfiguration.MINIMIZE_PRINTER_JOB_SIZE, true);
		ignoreFont = putil.getBooleanProperty(report, JRStyledText.PROPERTY_AWT_IGNORE_MISSING_FONT, false);
		
		//BUild the render cache
		JSSRenderersCache renderCache = new JSSRenderersCache(jasperReportsContext);
		this.drawVisitor = new PrintDrawVisitor(jasperReportsContext, renderCache, minPrintJobSize, ignoreFont);
		this.grx = grx;
		setGraphics2D(grx);
		this.drawVisitor.setClip(true);
	}
	
	/**
	 * Force the drawer to refresh the font cache, this is done by creating
	 * a new drawer. Should be called when something in global fonts changes
	 */
	public void refreshFontsCache(){
		JasperReportsContext jasperReportsContext = reportConverter.getJasperReportsContext();
		AwtTextRenderer textRenderer = new AwtTextRenderer(jasperReportsContext, minPrintJobSize, ignoreFont);
		TextDrawer textDrawer = new TextDrawer(jasperReportsContext, textRenderer);
		drawVisitor.setTextDrawer(textDrawer);
	}

	public void setClip(boolean clip) {
		this.drawVisitor.setClip(clip);
	}

	/**
	 * Set the used Graphics 2D
	 */
	public void setGraphics2D(Graphics2D grx) {
		this.grx = grx;
		drawVisitor.setGraphics2D(grx);
	}

	/**
	 * Return the actually used graphics 2d
	 * 
	 * @return a graphics 2d, can be null
	 */
	public Graphics2D getGraphics2d() {
		return grx;
	}

	public ReportConverter getReportConverter() {
		return reportConverter;
	}

	public JSSConvertVisitor getConvertVisitor() {
		return convertVisitor;
	}

	public PrintDrawVisitor getDrawVisitor() {
		return drawVisitor;
	}

	@Override
	public void visitBreak(JRBreak breakElement) {
		// FIXMEDRAW
	}

	@Override
	protected void visitElement(JRElement element) {
		try {
			JRPrintElement printElement = convertVisitor.getVisitPrintElement(element);
		
			printElement.accept(drawVisitor, elementOffset(element));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static Offset elementOffset(JRElement element) {
		return new Offset(-element.getX(), -element.getY());
	}

	/**
	 *
	 */
	public void visitElementGroup(JRElementGroup elementGroup) {
		// nothing to draw. elements are drawn individually.
	}
	
	

}
