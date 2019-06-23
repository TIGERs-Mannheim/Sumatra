/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 24.10.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.raster;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.ColorPickerFactory;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IColorPicker;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.EnhancedFieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.AFieldLayer;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;


/**
 * Layer for the analyzing raster.
 * 
 * @author Oliver Steinbrecher
 */
public class AnalysingRasterLayer extends AFieldLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final IColorPicker	colorPickerOur		= ColorPickerFactory.scaledTransparent(Color.blue);
	private final IColorPicker	colorPickerThey	= ColorPickerFactory.scaledTransparent(Color.red);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public AnalysingRasterLayer()
	{
		super(EFieldLayer.ANALYSING_RASTER);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void paintLayerAif(final Graphics2D g2, final IRecordFrame frame)
	{
		drawVisualizedRectangles(g2, frame);
	}
	
	
	/**
	 * Draws all {@link AIRectangle}s. Color depends on their {@link AIRectangle#getValue()}.
	 * The smaller the value the more the painted rectangle is transparent.
	 * 
	 * @param g Graphics to draw on
	 */
	private void drawVisualizedRectangles(final Graphics2D g, final IRecordFrame frame)
	{
		final Composite originalComposite = g.getComposite();
		EnhancedFieldAnalyser analyser = frame.getTacticalField().getEnhancedFieldAnalyser();
		if (analyser != null)
		{
			final Iterator<AIRectangle> rectIterator = analyser.getAnalysingRectangleVector().getRectangles().iterator();
			
			final List<AIRectangle> maxFoeRectangles = analyser.getMaxFoeTakenRectangles();
			
			final List<AIRectangle> maxTigersRectangles = analyser.getMaxTigersTakenRectangles();
			
			while (rectIterator.hasNext())
			{
				final AIRectangle rect = rectIterator.next();
				final IVector2 point = rect.bottomLeft();
				
				final int x = (int) getFieldPanel().transformToGuiCoordinates(point, frame.getWorldFrame().isInverted())
						.x();
				final int y = (int) getFieldPanel().transformToGuiCoordinates(point, frame.getWorldFrame().isInverted())
						.y();
				
				
				// TODO PhilippP anpassen des factors
				final float FACTOR = frame.getTacticalField().getEnhancedFieldAnalyser().getTotalMaximum();
				float transparency = rect.getValue() / FACTOR;
				Color color = Color.red;
				
				IColorPicker cp = colorPickerThey;
				if (transparency < 0)
				{
					cp = colorPickerOur;
					transparency = -transparency;
				}
				if (transparency > 1)
				{
					transparency = 1;
				}
				
				color = cp.applyColor(g, transparency);
				if (isInList(rect.getRectangleID(), maxFoeRectangles))
				{
					color = Color.black;
					if (rect.getValue() != 0)
					{
						transparency = 1;
					}
				}
				
				if (isInList(rect.getRectangleID(), maxTigersRectangles))
				{
					color = Color.white;
					if (rect.getValue() != 0)
					{
						transparency = 1;
					}
				}
				
				g.setColor(color);
				g.fillRect(x, y,
						getFieldPanel().scaleYLength(rect.yExtend()),
						getFieldPanel().scaleXLength(rect.xExtend()));
			}
		}
		g.setComposite(originalComposite);
	}
	
	
	@Override
	protected void paintDebugInformation(final Graphics2D g2, final IRecordFrame frame)
	{
		
		final Iterator<AIRectangle> rectIterator = frame.getTacticalField().getEnhancedFieldAnalyser()
				.getAnalysingRectangleVector().getRectangles().iterator();
		
		while (rectIterator.hasNext())
		{
			final AIRectangle rect = rectIterator.next();
			final IVector2 midPoint = rect.getMidPoint();
			final int textX = (int) getFieldPanel()
					.transformToGuiCoordinates(midPoint, frame.getWorldFrame().isInverted()).x();
			final int textY = (int) getFieldPanel()
					.transformToGuiCoordinates(midPoint, frame.getWorldFrame().isInverted()).y();
			
			g2.setColor(Color.white);
			g2.drawString("" + rect.getValue(), textX - (getFieldPanel().scaleXLength(rect.xExtend()) / 4), textY);
		}
	}
	
	
	/**
	 * Check if the rectangle id is in the list
	 * 
	 * @param rectangleID
	 * @param maxRectangles
	 * @return true or false
	 */
	private boolean isInList(final int rectangleID, final List<AIRectangle> maxRectangles)
	{
		for (AIRectangle rec : maxRectangles)
		{
			if (rec.getRectangleID() == rectangleID)
			{
				return true;
			}
		}
		return false;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
