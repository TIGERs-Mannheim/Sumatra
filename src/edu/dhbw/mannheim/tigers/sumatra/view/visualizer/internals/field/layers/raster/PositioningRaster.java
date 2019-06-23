/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 09.10.2011
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.raster;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldRasterGenerator;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;


/**
 * Layer for the positioning raster.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class PositioningRaster extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public PositioningRaster()
	{
		super(EFieldLayer.POSITIONING_RASTER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerSwf(Graphics2D g2, SimpleWorldFrame frame)
	{
		drawGrid(g2, Color.red, FieldRasterGenerator.getNumberOfColumns(), FieldRasterGenerator.getNumberOfRows());
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
		final double rowSize = (getFieldPanel().getFieldTotalWidth() - (2 * FieldPanel.FIELD_MARGIN)) / (double) columns;
		final int rowSizePaint = (int) rowSize;
		double error = rowSize - rowSizePaint;
		
		int x = FieldPanel.FIELD_MARGIN;
		int y = FieldPanel.FIELD_MARGIN;
		
		int length = (y + getFieldPanel().getFieldTotalHeight()) - (2 * FieldPanel.FIELD_MARGIN);
		
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
		
		final double columnSize = (getFieldPanel().getFieldTotalHeight() - (2 * FieldPanel.FIELD_MARGIN)) / (double) rows;
		final int columnSizePaint = (int) columnSize;
		error = columnSize - columnSizePaint;
		length = (x + getFieldPanel().getFieldTotalWidth()) - (2 * FieldPanel.FIELD_MARGIN);
		
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
	
	
}
