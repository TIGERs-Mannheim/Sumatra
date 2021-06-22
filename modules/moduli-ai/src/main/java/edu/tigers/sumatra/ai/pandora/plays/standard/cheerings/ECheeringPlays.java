/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.plays.standard.cheerings;

import com.github.g3force.instanceables.IInstanceableEnum;
import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.statemachine.IEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Getter
@AllArgsConstructor
@Log4j2
public enum ECheeringPlays implements IInstanceableEnum, IEvent
{
	PONG(new InstanceableClass<>(PongCheeringPlay.class)),
	BOWL(new InstanceableClass<>(BowlingCheeringPlay.class)),
	LAOLA(new InstanceableClass<>(LaolaCheeringPlay.class)),
	MACARENA(new InstanceableClass<>(MacarenaCheeringPlay.class)),
	TIGER(new InstanceableClass<>(TigerCheeringPlay.class)),
	CIRCLE(new InstanceableClass<>(CircleCheeringPlay.class)),
	;

	private final InstanceableClass<?> instanceableClass;


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
				log.error("Could not create CheeringPlay :(", e);
			}
		}
		Collections.shuffle(plays);

		return plays;
	}


	@Override
	public IInstanceableEnum parse(String value)
	{
		return valueOf(value);
	}
}
