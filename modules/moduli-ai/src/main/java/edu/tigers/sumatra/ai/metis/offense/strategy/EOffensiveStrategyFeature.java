/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.ai.metis.offense.strategy.features.AddSecondaryFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.BallNotInOffensePosition;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.DrawingsFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.FindBestPrimaryFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.InitFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.KeepActiveStatusFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.MixedTeamDropBotFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.ReadyForKickFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.ReceiveMixedTeamInformationFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.SkirmishFreeBallFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.StatisticsFeature;
import edu.tigers.sumatra.ai.metis.offense.strategy.features.SupportiveAttackerFeature;


/**
 * @author MarkG
 */
public enum EOffensiveStrategyFeature implements IInstanceableEnum
{
	/**
	 *
	 */
	INIT(new InstanceableClass(InitFeature.class)),
	/**
	 *
	 */
	FIND_BEST_PRIMARY(new InstanceableClass(FindBestPrimaryFeature.class)),
	/**
	 *
	 */
	MIXED_TEAM_DROP(new InstanceableClass(MixedTeamDropBotFeature.class)),
	/**
	 *
	 */
	KEEP_ACTIVE_STATUS(new InstanceableClass(KeepActiveStatusFeature.class)),
	/**
	 *
	 */
	BALL_POSITION_FORBIDS_OFFENSE(new InstanceableClass(BallNotInOffensePosition.class)),
	/**
	 *
	 */
	ADD_SECONDARY(new InstanceableClass(AddSecondaryFeature.class)),
	/**
	 *
	 */
	RECEIVE_MIXED_TEAM_INFO(new InstanceableClass(ReceiveMixedTeamInformationFeature.class)),
	/**
	 *
	 */
	SUPPORTIVE_ATTACKER(new InstanceableClass(SupportiveAttackerFeature.class)),
	/**
	 *
	 */
	SKIRMISH_FREE_BALL(new InstanceableClass(SkirmishFreeBallFeature.class)),
	/**
	 *
	 */
	DRAWING(new InstanceableClass(DrawingsFeature.class)),
	/**
	 *
	 */
	READY_FOR_KICK(new InstanceableClass(ReadyForKickFeature.class)),
	/**
	 *
	 */
	STATISTICS(new InstanceableClass(StatisticsFeature.class));

	
	private final InstanceableClass clazz;
	
	
	/**
	 */
	EOffensiveStrategyFeature(final InstanceableClass clazz)
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
