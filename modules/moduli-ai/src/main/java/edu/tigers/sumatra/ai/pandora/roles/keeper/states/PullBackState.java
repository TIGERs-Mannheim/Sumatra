/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper.states;

import edu.tigers.sumatra.ai.metis.keeper.KeeperStateCalc;
import edu.tigers.sumatra.ai.pandora.roles.keeper.KeeperRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.LineMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.BallPlacementSkill;


/**
 * If ball is inside penalty area, close to its border, not moving and a foe is nearby,
 * carefully pull back the ball to be able to place a proper shoot
 * <p>
 * Created by u.l.li on 09.03.2017.
 */

public class PullBackState extends AKeeperState
{
	private double pbDecisionVel = KeeperStateCalc.getPullBackDecisionVelocity();
	
	
	/**
	 * @param parent
	 */
	public PullBackState(final KeeperRole parent)
	{
		super(parent);
	}
	
	
	@Override
	public void doEntryActions()
	{
		KeeperStateCalc.setPullBackDecisionVelocity(1);
		BallPlacementSkill placeSkill = new BallPlacementSkill(getDestination());
		BallPlacementSkill.setChillVel(KeeperRole.getKeeperDribblingVelocity());
		placeSkill.getMoveCon().setDestinationOutsideFieldAllowed(false);
		placeSkill.getMoveCon().setGoalPostObstacle(true);
		setNewSkill(placeSkill);
	}
	
	
	@Override
	public void doExitActions()
	{
		KeeperStateCalc.setPullBackDecisionVelocity(pbDecisionVel);
	}
	
	
	private IVector2 getDestination()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		if (Geometry.getGoalOur().isPointInShape(ballPos, Geometry.getBotRadius() * 3))
		{
			return ballPos.addNew(Vector2.fromX(3 * Geometry.getBotRadius() + Geometry.getBallRadius()));
		}
		IVector2 destination = Geometry.getGoalOur().getCenter()
				.addNew(Vector2.fromX(Geometry.getPenaltyAreaOur().getRadius() * 0.5));
		double distanceBallPenArea = Geometry.getPenaltyAreaOur().nearestPointInside(ballPos).distanceTo(ballPos) + 10;
		return LineMath.stepAlongLine(ballPos, destination,
				Geometry.getBotRadius() * 4 + distanceBallPenArea);
	}
}
