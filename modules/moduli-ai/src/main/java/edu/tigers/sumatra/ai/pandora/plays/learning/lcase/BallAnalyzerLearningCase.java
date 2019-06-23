/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class BallAnalyzerLearningCase extends ALearningCase
{
	private final List<IVector2>	validPositions	= new ArrayList<IVector2>();
	private static final double	radius			= 250;
	private boolean					finished			= false;
	
	
	/**
	 * 
	 */
	public BallAnalyzerLearningCase()
	{
		activeRoleTypes.add(ERole.SIMPLE_SHOOTER);
		validPositions.add(new Vector2((Geometry.getFieldLength() / 2.0) - radius, (Geometry
				.getFieldWidth() / 2.0) - radius));
		validPositions.add(new Vector2((Geometry.getFieldLength() / -2.0f) + radius, (Geometry
				.getFieldWidth() / 2.0) - radius));
		validPositions.add(new Vector2((Geometry.getFieldLength() / 2.0) - radius, (Geometry
				.getFieldWidth() / -2.0f) + radius));
		validPositions.add(new Vector2((Geometry.getFieldLength() / -2.0f) + radius, (Geometry
				.getFieldWidth() / -2.0f) + radius));
	}
	
	
	@Override
	public void update(final List<ARole> roles, final AthenaAiFrame frame)
	{
		for (ARole role : roles)
		{
			if (role.getType() == ERole.SIMPLE_SHOOTER)
			{
				if (isReady(frame, roles))
				{
					for (IVector2 pos : validPositions)
					{
						if ((GeoMath.distancePP(pos, role.getPos()) < (radius * 3)) && (roles.size() > 0)
								&& (frame.getWorldFrame().getBall().getVel().getLength2() > 0.5))
						{
							finished = true;
						}
					}
				}
			}
		}
	}
	
	
	@Override
	public boolean isFinished(final AthenaAiFrame frame)
	{
		return finished;
	}
	
	
	@Override
	public boolean isReady(final AthenaAiFrame frame, final List<ARole> roles)
	{
		for (IVector2 pos : validPositions)
		{
			DrawableCircle dcircle = new DrawableCircle(new Circle(pos, radius));
			frame.getTacticalField().getDrawableShapes().get(EShapesLayer.LEARNING).add(dcircle);
			if ((GeoMath.distancePP(pos, frame.getWorldFrame().getBall().getPos()) < radius) && (roles.size() > 0))
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public String getReadyCriteria()
	{
		return "needed bots: 1 \nball has to be inside one of the red circles (hint: activate LearningShapes)\n"
				+ "BallMovementAnalyzerCalc learning active";
	}
	
}
