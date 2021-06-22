/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.pandora.plays.standard;

import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.v2.LineMath;


/**
 * Handle bots that would normally be Supporters and Offense
 */
public class PenaltyThemPlay extends APenaltyPlay
{

	/**
	  */
	public PenaltyThemPlay()
	{
		super(EPlay.PENALTY_THEM);
	}


	@Override
	protected ARole onRemoveRole()
	{
		return getLastRole();
	}


	@Override
	protected ARole onAddRole()
	{
		return new MoveRole();
	}


	@Override
	protected void doUpdateAfterRoles()
	{
		super.doUpdateAfterRoles();
		double sign = Math.signum(Geometry.getPenaltyMarkOur().subtractNew(Geometry.getGoalOur().getCenter()).x());
		double xLine = LineMath.stepAlongLine(
				Geometry.getPenaltyMarkOur(),
				Geometry.getGoalOur().getCenter(),
				-(RuleConstraints.getDistancePenaltyMarkToPenaltyLine() + Geometry.getBotRadius() + 10)).x();

		updateMoveRoles(sign, xLine);
	}
}
