/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.finisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.skillsystem.skills.util.SkillCommand;


/**
 * Abstract base class for finisher moves
 */
public abstract class AFinisherMove implements IFinisherMove
{
	protected final List<SkillCommand> skillCommands = new ArrayList<>();
	private final EFinisherMove type;
	
	
	protected AFinisherMove(final EFinisherMove type)
	{
		this.type = type;
	}
	
	
	@Override
	public List<SkillCommand> getSkillCommands()
	{
		return skillCommands;
	}
	
	
	@Override
	public EFinisherMove getType()
	{
		return type;
	}
	
	
	@Override
	public List<IDrawableShape> generateShapes()
	{
		return Collections.emptyList();
	}
	
	
	protected boolean isValidTrajectory(final CommandListTrajectory trajectory, final BaseAiFrame aiFrame)
	{
		for (double t = 0; t < trajectory.getTotalTime(); t += 0.05)
		{
			Pose p = trajectory.getPose(t);
			final double finalTime = t;
			
			boolean closeFoe = aiFrame.getWorldFrame().getFoeBots().values().stream()
					.filter(b -> b.getPosByTime(finalTime)
							.distanceTo(p.getPos()) < (b.getRobotInfo().getBotParams().getDimensions().getDiameter() * 0.5))
					.findAny().isPresent();
			
			if (closeFoe)
			{
				return false;
			}
		}
		
		if (Geometry.getPenaltyAreaOur().isPointInShape(trajectory.getFinalPose().getPos()))
		{
			return false;
		}
		
		return !Geometry.getPenaltyAreaTheir().isPointInShape(trajectory.getFinalPose().getPos());
	}
}
