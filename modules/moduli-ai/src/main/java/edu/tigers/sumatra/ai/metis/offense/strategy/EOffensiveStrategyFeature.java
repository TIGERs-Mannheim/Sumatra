/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.strategy;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;


/**
 * @author MarkG
 */
public enum EOffensiveStrategyFeature implements IInstanceableEnum
{
	INIT(new InstanceableClass(InitFeature.class)),
	FIND_BEST_PRIMARY(new InstanceableClass(FindBestPrimaryFeature.class)),
	KEEP_ACTIVE_STATUS(new InstanceableClass(KeepActiveStatusFeature.class)),
	ADD_SECONDARY(new InstanceableClass(AddSecondaryFeature.class)),
	SUPPORTIVE_ATTACKER(new InstanceableClass(SupportiveAttackerFeature.class)),
	SKIRMISH_FREE_BALL(new InstanceableClass(SkirmishFreeBallFeature.class)),
	READY_FOR_KICK(new InstanceableClass(ReadyForKickFeature.class)),
	STATISTICS(new InstanceableClass(StatisticsFeature.class)),
	
	;
	
	
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
