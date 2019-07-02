/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.PullBallSkill;


/**
 * If ball is inside penalty area, close to its border, not moving and a foe is nearby,
 * carefully pull back the ball to be able to place a proper shoot
 */

public class PullBackState extends AKeeperState
{
	
	/**
	 * @param parent
	 */
	public PullBackState(final KeeperRole parent)
	{
		super(parent, EKeeperState.PULL_BACK);
	}
	
	
	@Override
	public void doEntryActions()
	{
		PullBallSkill placeSkill = new PullBallSkill(getDestination());
		
		placeSkill.setChillVel(
				isBallCloseToGoal(getWFrame().getBall().getPos()) ? 0.08 : KeeperRole.getKeeperDribblingVelocity());
		placeSkill.getMoveCon().setDestinationOutsideFieldAllowed(false);
		placeSkill.getMoveCon().setGoalPostObstacle(true);
		setNewSkill(placeSkill);
	}
	
	
	private IVector2 getDestination()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		IVector2 leftPost = Geometry.getGoalOur().getLeftPost();
		IVector2 rightPost = Geometry.getGoalOur().getRightPost();
		
		if (Geometry.getGoalOur().isPointInShape(ballPos, Geometry.getBotRadius() * 3))
		{
			return ballPos.addNew(Vector2.fromX(3 * Geometry.getBotRadius() + Geometry.getBallRadius()));
		}
		
		IVector2 destination;
		
		if ((ballPos.y() < leftPost.y()) && (ballPos.y() > rightPost.y()))
		{
			IVector2 goalCenter = Geometry.getGoalOur().getCenter();
			
			// set's the y coordinate of goalCenter to ballPos.y()
			goalCenter = goalCenter.subtractNew(Vector2.fromY(goalCenter.y() - ballPos.y()));
			
			destination = goalCenter.addNew(Vector2.fromX(500));
		} else if (ballPos.y() > leftPost.y())
		{
			destination = leftPost.addNew(Vector2.fromX(500));
		} else
		{
			destination = rightPost.addNew(Vector2.fromX(500));
		}
		
		double distanceBallPenArea = Geometry.getPenaltyAreaOur().nearestPointInside(ballPos).distanceTo(ballPos) + 10;
		return LineMath.stepAlongLine(ballPos, destination, Geometry.getBotRadius() * 4 + distanceBallPenArea);
	}
	
	
	private boolean isBallCloseToGoal(IVector2 ballPos)
	{
		return Geometry.getGoalOur().isPointInShape(ballPos, Geometry.getBotRadius() * 3);
	}
}
