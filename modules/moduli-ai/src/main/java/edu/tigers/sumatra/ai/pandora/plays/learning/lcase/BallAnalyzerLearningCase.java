/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.tigers.sumatra.ai.data.EAiShapesLayer;
import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class BallAnalyzerLearningCase extends ALearningCase
{
	private final List<IVector2> validPositions = new ArrayList<>();
	private static final double RADIUS = 250;
	private boolean finished = false;
	
	
	/**
	 * Creates a new BallLearningAnalyzer
	 */
	public BallAnalyzerLearningCase()
	{
		activeRoleTypes.add(ERole.SIMPLE_SHOOTER);
		validPositions.add(Vector2.fromXY((Geometry.getFieldLength() / 2.0) - RADIUS, (Geometry
				.getFieldWidth() / 2.0) - RADIUS));
		validPositions.add(Vector2.fromXY((Geometry.getFieldLength() / -2.0f) + RADIUS, (Geometry
				.getFieldWidth() / 2.0) - RADIUS));
		validPositions.add(Vector2.fromXY((Geometry.getFieldLength() / 2.0) - RADIUS, (Geometry
				.getFieldWidth() / -2.0f) + RADIUS));
		validPositions.add(Vector2.fromXY((Geometry.getFieldLength() / -2.0f) + RADIUS, (Geometry
				.getFieldWidth() / -2.0f) + RADIUS));
	}
	
	
	@Override
	public void update(final List<ARole> roles, final AthenaAiFrame frame)
	{
		for (ARole role : roles)
		{
			if (role.getType() != ERole.SIMPLE_SHOOTER || !isReady(frame, roles))
			{
				continue;
			}
			
			for (IVector2 pos : validPositions)
			{
				if ((VectorMath.distancePP(pos, role.getPos()) < (RADIUS * 3)) && (!roles.isEmpty())
						&& (frame.getWorldFrame().getBall().getVel().getLength2() > 0.5))
				{
					finished = true;
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
			DrawableCircle dcircle = new DrawableCircle(Circle.createCircle(pos, RADIUS));
			frame.getTacticalField().getDrawableShapes().get(EAiShapesLayer.LEARNING).add(dcircle);
			if ((VectorMath.distancePP(pos, frame.getWorldFrame().getBall().getPos()) < RADIUS) && (!roles.isEmpty()))
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
