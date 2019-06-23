/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.raster;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.FieldRasterConfig;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;


/**
 * Base class for all raster layers.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public abstract class ARasterLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --- grid data ---
	protected FieldRasterConfig	rasterConfig	= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Standard ctor. which creates a {@link ARasterLayer} which is not visible.
	 * 
	 * @param name
	 */
	public ARasterLayer(EFieldLayer name)
	{
		super(name, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void paint(Graphics2D g)
	{
		if ((rasterConfig != null) && (getAiFrame() != null))
		{
			super.paint(g);
		}
	}
	
	
	/**
	 * Draws a grid for AI-development.
	 * 
	 * @param g
	 * @param rows
	 * @param columns
	 */
	protected void drawGrid(Graphics2D g, Color c, int rows, int columns)
	{
		final double rowSize = (FieldPanel.getFieldTotalWidth() - (2 * FieldPanel.FIELD_MARGIN)) / (double) columns;
		final int rowSizePaint = (int) rowSize;
		double error = rowSize - rowSizePaint;
		
		int x = FieldPanel.FIELD_MARGIN;
		int y = FieldPanel.FIELD_MARGIN;
		
		int length = (y + FieldPanel.getFieldTotalHeight()) - (2 * FieldPanel.FIELD_MARGIN);
		
		for (int i = 0; i <= columns; i++)
		{
			// --- paint halfway line ---
			g.setColor(c);
			g.setStroke(new BasicStroke(2));
			g.drawLine(x, y, x, length);
			x += rowSizePaint;
			
			// if the error is larger then 1 pixel correct this error.
			if (error > 1)
			{
				final int correctError = (int) error;
				x += correctError;
				error = error - correctError;
				continue;
			}
			error += error;
		}
		
		x = FieldPanel.FIELD_MARGIN;
		y = FieldPanel.FIELD_MARGIN;
		
		final double columnSize = (FieldPanel.getFieldTotalHeight() - (2 * FieldPanel.FIELD_MARGIN)) / (double) rows;
		final int columnSizePaint = (int) columnSize;
		error = columnSize - columnSizePaint;
		length = (x + FieldPanel.getFieldTotalWidth()) - (2 * FieldPanel.FIELD_MARGIN);
		
		for (int i = 0; i <= rows; i++)
		{
			g.setColor(c);
			g.setStroke(new BasicStroke(2));
			g.drawLine(x, y, length, y);
			y += columnSizePaint;
			
			// if the error is larger then 1 pixel correct this error.
			if (error > 1)
			{
				final int correctError = (int) error;
				y += correctError;
				error = error - correctError;
				continue;
			}
			error += error;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Set field raster configuration
	 * 
	 * @param fieldRasterconfig
	 */
	public void setNewFieldRaster(FieldRasterConfig fieldRasterconfig)
	{
		rasterConfig = fieldRasterconfig;
	}
	
	
}
