/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.04.2012
 * Author(s): osteinbrecher
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.Pattern;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.FieldPanel;


/**
 * This painting layer illustrates the detected patterns.
 * 
 * @author Oliver Steinbrecher
 * 
 */
public class PatternLayer extends AFieldLayer
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final int	POINT_RADIUS			= 4;
	private static final float	PATTERN_COLOR_OFFSET	= 0.3f;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public PatternLayer()
	{
		super(EFieldLayer.PATTERN_LAYER, false);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayer(Graphics2D g)
	{
		if (getAiFrame() != null)
		{
			final Graphics2D g2 = g;
			
			final List<Pattern> patternList = getAiFrame().getTacticalInfo().getPlayPattern();
			
			for (final Pattern pattern : patternList)
			{
				final Color patternColor = getColor((float) pattern.getMatchingScore());
				drawPatternPosition(g2, pattern.getReciever(), pattern.getIndex(), patternColor, false,
						pattern.isPersisted());
				drawPatternPosition(g2, pattern.getPasser(), pattern.getIndex(), patternColor, true, pattern.isPersisted());
			}
		}
	}
	
	
	/**
	 * Draws a pattern position point on the field.
	 * 
	 * @param g graphics object
	 * @param position
	 * @param patternID
	 * @param color
	 * @param isPasser [false == Receiver]
	 * @param isPersisted
	 */
	private void drawPatternPosition(Graphics2D g, IVector2 position, int patternID, Color color, boolean isPasser,
			boolean isPersisted)
	{
		final IVector2 drawPoint = FieldPanel.transformToGuiCoordinates(position);
		final int drawingX = (int) (drawPoint.x() - POINT_RADIUS);
		final int drawingY = (int) (drawPoint.y() - POINT_RADIUS);
		
		if (isPersisted)
		{
			drawOval(g, drawPoint, Color.black, 5);
		}
		
		g.setColor(color);
		g.fillOval(drawingX, drawingY, POINT_RADIUS * 2, POINT_RADIUS * 2);
		
		g.setColor(Color.white);
		
		final StringBuffer labelBuffer = new StringBuffer();
		
		if (isPasser)
		{
			labelBuffer.append('P');
		} else
		{
			labelBuffer.append('R');
		}
		labelBuffer.append(Integer.toString(patternID));
		
		g.drawString(labelBuffer.toString(), drawingX, drawingY);
	}
	
	
	private void drawOval(Graphics2D g, IVector2 position, Color color, int radius)
	{
		final int drawingX = (int) (position.x() - radius);
		final int drawingY = (int) (position.y() - radius);
		
		g.setColor(color);
		g.drawOval(drawingX, drawingY, radius * 2, radius * 2);
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Calculates the pattern color depending on the matching score.
	 * *
	 * @param matchingScore
	 * @return Color of the pattern
	 */
	private Color getColor(float matchingScore)
	{
		/*
		 * HSB-Color model
		 * red = 0
		 * green = 120
		 */
		float color = (-PATTERN_COLOR_OFFSET * matchingScore) + PATTERN_COLOR_OFFSET;
		
		if (color > PATTERN_COLOR_OFFSET)
		{
			color = PATTERN_COLOR_OFFSET;
		} else if (color < 0.0f)
		{
			color = 0.0f;
		}
		
		return Color.getHSBColor(color, 1, 1f);
	}
	
	
}
