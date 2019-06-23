/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.metis.offense.action.moves.ClearingKickActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.DirectKickActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.ForcedPassActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.GoToOtherHalfMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.KickInsBlaueActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.LowChanceDirectKickActionMove;
import edu.tigers.sumatra.ai.metis.offense.action.moves.StandardPassActionMove;


/**
 * @author MarkG
 */
public enum EOffensiveActionMove implements IInstanceableEnum
{
	/**
	 *
	 */
	FORCED_PASS(new InstanceableClass(ForcedPassActionMove.class)),
	/**
	 *
	 */
	DIRECT_KICK(new InstanceableClass(DirectKickActionMove.class)),
	/**
	 *
	 */
	CLEARING_KICK(new InstanceableClass(ClearingKickActionMove.class)),
	/**
	 *
	 */
	STANDARD_PASS(new InstanceableClass(StandardPassActionMove.class)),
	/**
	 *
	 */
	LOW_CHANCE_KICK(new InstanceableClass(LowChanceDirectKickActionMove.class)),
	/**
	 *
	 */
	GO_TO_OTHER_HALF(new InstanceableClass(GoToOtherHalfMove.class)),
	/**
	 *
	 */
	KICK_INS_BLAUE(new InstanceableClass(KickInsBlaueActionMove.class));


	private final InstanceableClass clazz;


	/**
	 */
	EOffensiveActionMove(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	/**
	 * @return the paramImpls
	 */
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
