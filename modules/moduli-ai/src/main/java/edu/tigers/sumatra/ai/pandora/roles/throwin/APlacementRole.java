/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.throwin;

import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.MovementCon;


/**
 * @author MarkG
 */
public abstract class APlacementRole extends ARole
{
	/**
	 * @param role
	 */
	public APlacementRole(final ERole role)
	{
		super(role);
	}
	
	
	protected boolean isBallTooCloseToFieldBorder()
	{
		return !Geometry.getField().isPointInShape(getBall().getPos(), Geometry.getBotRadius())
				|| Geometry.getGoalOur().isPointInShape(getBall().getPos(), 200)
				|| Geometry.getGoalTheir().isPointInShape(getBall().getPos(), 200);
	}
	
	
	protected boolean isBallAtTarget()
	{
		return getBall().getPos().distanceTo(getPlacementPos()) < OffensiveConstants.getAutomatedThrowInFinalTolerance();
	}
	
	
	protected void prepareMoveCon(MovementCon moveCon)
	{
		moveCon.setPenaltyAreaAllowedTheir(true);
		moveCon.setPenaltyAreaAllowedOur(true);
		moveCon.setGoalPostObstacle(true);
		moveCon.setIgnoreGameStateObstacles(true);
		moveCon.setDestinationOutsideFieldAllowed(true);
	}
	
	
	protected IVector2 getPlacementPos()
	{
		return getAiFrame().getTacticalField().getThrowInInfo().getPos();
	}
	
	
	protected boolean isInsidePushRadius()
	{
		return getPlacementPos().distanceTo(getBall().getPos()) < OffensiveConstants.getAutomatedThrowInPushDistance();
	}
}
