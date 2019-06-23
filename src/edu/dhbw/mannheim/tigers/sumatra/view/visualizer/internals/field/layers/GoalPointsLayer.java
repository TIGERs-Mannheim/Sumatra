/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 30.10.2011
 * Author(s): osteinbrecher
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.layers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;


/**
 * This layer paints the defense goal points stored in
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField} of the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame}.
 * 
 * @author Oliver Steinbrecher
 */
public class GoalPointsLayer extends AValuePointLayer
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public GoalPointsLayer()
	{
		super(EFieldLayer.GOAL_POINTS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void paintLayerAif(final Graphics2D g, final IRecordFrame frame)
	{
		final List<DefensePoint> defPoints = frame.getTacticalField().getDefGoalPoints();
		
		// --- draw defense goal points ---
		for (int i = defPoints.size() - 1; i >= 0; i--)
		{
			DefensePoint point = defPoints.get(i);
			// --- from SSLVision-mm to java2d-coordinates ---
			final IVector2 transPoint = getFieldPanel().transformToGuiCoordinates(point,
					frame.getWorldFrame().isInverted());
			final int drawingX = (int) transPoint.x() - POINT_SIZE;
			final int drawingY = (int) transPoint.y() - POINT_SIZE;
			float value = point.getValue();
			if (point.getValue() >= 1)
			{
				value = 1;
			}
			if (point.getValue() <= 0)
			{
				value = 0;
			}
			
			g.setColor(new Color((int) (255 * value), 255 - (int) (255 * value), 0));
			// }
			
			g.fillOval(drawingX, drawingY, POINT_SIZE, POINT_SIZE);
		}
		
		drawValuePoints(g, frame.getTacticalField().getGoalValuePoints(), frame.getWorldFrame().isInverted());
		
		if (frame.getTacticalField().getBestDirectShootTarget() != null)
		{
			g.setColor(Color.blue);
			final IVector2 transPoint = getFieldPanel().transformToGuiCoordinates(
					frame.getTacticalField().getBestDirectShootTarget());
			final int drawingX = ((int) transPoint.x() - (POINT_SIZE / 2));
			final int drawingY = ((int) transPoint.y() - (POINT_SIZE / 2));
			g.fillOval(drawingX, drawingY, POINT_SIZE, POINT_SIZE);
		}
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
