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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.EnhancedFieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.rectangle.AIRectangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers.EFieldLayer;


/**
 * Layer for the analyzing raster.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class AnalysingRasterLayer extends ARasterLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
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
	protected void paintLayer(Graphics2D g2)
	{
		drawVisualizedRectangles(g2);
	}
	
	
	/**
	 * Draws all {@link AIRectangle}s. Color depends on their {@link AIRectangle#getValue()}.
	 * The smaller the value the more the painted rectangle is transparent.
	 * 
	 * @param g Graphics to draw on
	 */
	private void drawVisualizedRectangles(Graphics2D g)
	{
		final Composite originalComposite = g.getComposite();
		EnhancedFieldAnalyser analyser = getAiFrame().getTacticalInfo().getEnhancedFieldAnalyser();
		if (analyser != null)
		{
			final Iterator<AIRectangle> rectIterator = analyser.getAnalysingRectangleVector().getRectangles().iterator();
			
			final List<AIRectangle> maxFoeRectangles = getAiFrame().getTacticalInfo().getEnhancedFieldAnalyser()
					.getMaxFoeTakenRectangles();
			final List<AIRectangle> maxTigersRectangles = getAiFrame().getTacticalInfo().getEnhancedFieldAnalyser()
					.getMaxTigersTakenRectangles();
			
			while (rectIterator.hasNext())
			{
				final AIRectangle rect = rectIterator.next();
				final IVector2 point = rect.bottomLeft();
				
				final int x = (int) FieldPanel.transformToGuiCoordinates(point).x();
				final int y = (int) FieldPanel.transformToGuiCoordinates(point).y();
				
				
				// TODO PhilippP anpassen des factors
				final float FACTOR = getAiFrame().getTacticalInfo().getEnhancedFieldAnalyser().getTotalMaximum();
				float transparency = rect.getValue() / FACTOR;
				Color color = Color.red;
				
				if (transparency < 0)
				{
					color = Color.blue;
					transparency = -transparency;
				}
				if (transparency > 1)
				{
					transparency = 1;
				}
				if (transparency <= 1)
				{
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
						if (rect.getValue() != 00)
						{
							transparency = 1;
						}
					}
					
					
					// final IVector2 midPointrect = rect.getMidPoint();
					// IVector2 guiPos = FieldPanel.transformToGuiCoordinates(midPointrect);
					// g.drawString(rect.getRectangleID() + "", guiPos.x(), guiPos.y());
					g.setComposite(makeTransparentComposite(transparency));
					g.setColor(color);
					g.fillRect(x, y, FieldPanel.scaleYLength(rect.yExtend()), FieldPanel.scaleXLength(rect.xExtend()));
					g.setComposite(originalComposite);
				}
			}
		}
	}
	
	
	@Override
	protected void paintDebugInformation(Graphics2D g2)
	{
		
		final Iterator<AIRectangle> rectIterator = getAiFrame().getTacticalInfo().getEnhancedFieldAnalyser()
				.getAnalysingRectangleVector().getRectangles().iterator();
		
		while (rectIterator.hasNext())
		{
			final AIRectangle rect = rectIterator.next();
			final IVector2 midPoint = rect.getMidPoint();
			final int textX = (int) FieldPanel.transformToGuiCoordinates(midPoint).x();
			final int textY = (int) FieldPanel.transformToGuiCoordinates(midPoint).y();
			
			g2.setColor(Color.white);
			g2.drawString("" + rect.getValue(), textX - (FieldPanel.scaleXLength(rect.xExtend()) / 4), textY);
		}
	}
	
	
	/**
	 * Creates a transparent component.
	 * @param transparency factor [0-1]
	 * @return AlphaComposite
	 */
	private AlphaComposite makeTransparentComposite(float transparency)
	{
		final int type = AlphaComposite.SRC_OVER;
		return (AlphaComposite.getInstance(type, transparency));
	}
	
	
	/**
	 * Check if the rectangle id is in the list
	 * 
	 * @param rectangleID
	 * @param maxRectangles
	 * @return true or false
	 */
	private boolean isInList(int rectangleID, List<AIRectangle> maxRectangles)
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
