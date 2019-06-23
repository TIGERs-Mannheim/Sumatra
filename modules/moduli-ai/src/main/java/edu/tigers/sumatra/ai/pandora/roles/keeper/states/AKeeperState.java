/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.data.frames.AthenaAiFrame;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.skillsystem.skills.ISkill;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.WorldFrame;


/**
 * @author ChrisC
 */
@SuppressWarnings("WeakerAccess")
public abstract class AKeeperState implements IState
{

	protected final KeeperRole parent;
	
	
	protected AKeeperState(KeeperRole parent)
	{
		this.parent = parent;
	}
	
	
	protected double calcDefendingOrientation()
	{
		return parent.getAiFrame().getWorldFrame().getBall().getPos().subtractNew(parent.getPos()).getAngle()
				+ KeeperRole.getTurnAngleOfKeeper();
	}
	
	
	/**
	 * Checks if the given destination is inside a goalpost.
	 * If so, the returned position is now outside of the goalpost (in y direction)
	 *
	 * @param destination to check
	 * @return new destination slightly with a slightly offset, if the destination was inside a goalpost
	 * @author ChrisC
	 */
	protected IVector2 setDestinationOutsideGoalPosts(final IVector2 destination)
	{
		IVector2 newDestination = destination;
		// Is Bot able to hit Goalpost?
		IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPost = Geometry.getGoalOur().getRightPost();
		
		if (isDestinationIn(leftPost, destination))
		{
			newDestination = moveDestinationOutsideGoalpost(leftPost, destination);
		} else if (isDestinationIn(rightPost, destination))
		{
			newDestination = moveDestinationOutsideGoalpost(rightPost, destination);
		}
		if (isDestinationBehindGoalLine(newDestination)
				|| !isDestinationBetweenGoalposts(newDestination))
		{
			newDestination = moveDestinationInsideField(newDestination);
		}
		return newDestination;
		
	}
	
	
	private boolean isDestinationIn(IVector2 goalpost, IVector2 destination)
	{
		return VectorMath.distancePP(destination,
				goalpost) <= (Geometry.getBotRadius() + Geometry.getBallRadius());
	}
	
	
	private boolean isDestinationBehindGoalLine(IVector2 destination)
	{
		return destination.x() < Geometry.getGoalOur().getCenter().x();
	}
	
	
	private boolean isDestinationBetweenGoalposts(IVector2 destination)
	{
		return Math.abs(destination.y()) < Math.abs(Geometry.getGoalOur().getLeftPost().y() + Geometry.getBotRadius());
	}
	
	
	private IVector2 moveDestinationOutsideGoalpost(IVector2 goalpost, IVector2 destination)
	{
		return LineMath.stepAlongLine(goalpost, destination,
				Geometry.getBotRadius() + Geometry.getBallRadius());
	}
	
	
	private IVector2 moveDestinationInsideField(IVector2 destination)
	{
		return Vector2.fromXY(
				Geometry.getGoalOur().getCenter().x() + Geometry.getBotRadius() + Geometry.getBallRadius() / 2,
				destination.y());
	}
	
	
	protected void setNewSkill(ISkill skill)
	{
		parent.setNewSkill(skill);
	}
	
	
	protected AthenaAiFrame getAiFrame()
	{
		return parent.getAiFrame();
	}
	
	
	protected WorldFrame getWFrame()
	{
		return parent.getWFrame();
	}
	
	
	protected IVector2 getPos()
	{
		return parent.getPos();
	}
}
