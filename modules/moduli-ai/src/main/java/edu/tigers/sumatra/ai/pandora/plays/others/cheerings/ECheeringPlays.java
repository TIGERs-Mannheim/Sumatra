/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.others.cheerings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.statemachine.IEvent;


public enum ECheeringPlays implements IInstanceableEnum, IEvent
{
	PONG(new InstanceableClass(PongCheeringPlay.class)),
	BOWL(new InstanceableClass(BowlingCheeringPlay.class)),
	LAOLA(new InstanceableClass(LaolaCheeringPlay.class)),
	MACARENA(new InstanceableClass(MacarenaCheeringPlay.class)),
	TIGER(new InstanceableClass(TigerCheeringPlay.class)),
	CIRCLE(new InstanceableClass(CircleCheeringPlay.class)),
	;
	
	private static Logger logger = Logger.getLogger(ECheeringPlays.class);
	private final InstanceableClass clazz;
	
	
	ECheeringPlays(final InstanceableClass clazz)
	{
		this.clazz = clazz;
	}
	
	
	public static List<ICheeringPlay> getShuffledList()
	{
		List<ICheeringPlay> plays = new ArrayList<>();
		
		for (ECheeringPlays play : ECheeringPlays.values())
		{
			try
			{
				plays.add((ICheeringPlay) play.getInstanceableClass().newInstance());
			} catch (InstanceableClass.NotCreateableException e)
			{
				logger.error("Could not create CheeringPlay :(", e);
			}
		}
		Collections.shuffle(plays);
		
		return plays;
	}
	
	
	@Override
	public final InstanceableClass getInstanceableClass()
	{
		return clazz;
	}
}
