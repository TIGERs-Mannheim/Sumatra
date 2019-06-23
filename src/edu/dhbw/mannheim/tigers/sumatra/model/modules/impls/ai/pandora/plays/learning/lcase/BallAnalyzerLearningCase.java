/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 3, 2015
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.learning.lcase;

import java.util.ArrayList;
import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.KickTestRole;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class BallAnalyzerLearningCase extends ALearningCase
{
	private List<IVector2>		validPositions	= new ArrayList<IVector2>();
	private static final float	radius			= 250f;
	private boolean				finished			= false;
	
	
	/**
	 * 
	 */
	public BallAnalyzerLearningCase()
	{
		activeRoleTypes.add(ERole.SIMPLE_KICK);
		validPositions.add(new Vector2((AIConfig.getGeometry().getFieldLength() / 2f) - radius, (AIConfig.getGeometry()
				.getFieldWidth() / 2f) - radius));
		validPositions.add(new Vector2((AIConfig.getGeometry().getFieldLength() / -2f) + radius, (AIConfig.getGeometry()
				.getFieldWidth() / 2f) - radius));
		validPositions.add(new Vector2((AIConfig.getGeometry().getFieldLength() / 2f) - radius, (AIConfig.getGeometry()
				.getFieldWidth() / -2f) + radius));
		validPositions.add(new Vector2((AIConfig.getGeometry().getFieldLength() / -2f) + radius, (AIConfig.getGeometry()
				.getFieldWidth() / -2f) + radius));
	}
	
	
	@Override
	public void update(final List<ARole> roles, final AthenaAiFrame frame)
	{
		for (ARole role : roles)
		{
			if (role instanceof KickTestRole)
			{
				if (isReady(frame, roles))
				{
					for (IVector2 pos : validPositions)
					{
						if ((GeoMath.distancePP(pos, role.getPos()) < (radius * 3f)) && (roles.size() > 0)
								&& (frame.getWorldFrame().getBall().getVel().getLength2() > 0.5f))
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
			frame.getTacticalField().getDrawableShapes().get(EDrawableShapesLayer.LEARNING).add(dcircle);
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
