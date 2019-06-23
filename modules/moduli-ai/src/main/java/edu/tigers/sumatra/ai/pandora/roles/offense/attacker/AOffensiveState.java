/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.offense.attacker;

import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ARoleState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


public abstract class AOffensiveState extends ARoleState
{
	@Configurable(defValue = "30.0", comment = "When distance to ball (without bot/ball radius) is small than this, protection is stopped")
	private static double minDistToOpponent = 60.0;
	
	static
	{
		ConfigRegistration.registerClass("roles", AOffensiveState.class);
	}
	
	
	public AOffensiveState(final ARole role)
	{
		super(role);
	}
	
	
	public Optional<OffensiveAction> getCurrentOffensiveAction()
	{
		return Optional.empty();
	}
	
	
	protected double getOpponentToBallDist()
	{
		ITrackedBot opponentBot = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
		if (opponentBot == null)
		{
			return 1e10;
		}
		return opponentBot.getPos().distanceTo(getBall().getPos()) - Geometry.getBotRadius() - Geometry.getBallRadius();
	}
	
	
	protected boolean ballPossessionIsThreatened(final double protectionDistanceGain)
	{
		double opponentToBallDist = getOpponentToBallDist();
		if (opponentToBallDist < minDistToOpponent
				|| getPos().distanceTo(getBall().getPos()) < Geometry.getBotRadius() + Geometry.getBallRadius() + 20)
		{
			// protection not reasonable anymore
			return false;
		}
		double protectorToBallDist = getPos().distanceTo(getBall().getPos());
		return protectorToBallDist * protectionDistanceGain > opponentToBallDist;
	}
}
