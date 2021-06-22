/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import com.github.g3force.instanceables.InstanceableClass;
import edu.tigers.sumatra.referee.proto.SslGcGameEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;


public final class GameEventFactory
{
	private static final Logger log = LogManager.getLogger(GameEventFactory.class.getName());


	private GameEventFactory()
	{
	}


	public static Optional<IGameEvent> fromProtobuf(SslGcGameEvent.GameEvent event)
	{
		if (event.getType() == SslGcGameEvent.GameEvent.Type.UNKNOWN_GAME_EVENT_TYPE)
		{
			return Optional.empty();
		}
		try
		{
			final EGameEvent gameEvent = EGameEvent.fromProto(event.getType());
			if (gameEvent == null)
			{
				return Optional.empty();
			}
			return Optional.of((IGameEvent) gameEvent.getInstanceableClass().newInstance(event));
		} catch (InstanceableClass.NotCreateableException e)
		{
			log.warn("Could not convert game event: " + event, e);
		}
		return Optional.empty();
	}
}
