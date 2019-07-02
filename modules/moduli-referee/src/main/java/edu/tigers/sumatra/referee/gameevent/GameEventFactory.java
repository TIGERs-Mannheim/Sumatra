package edu.tigers.sumatra.referee.gameevent;

import java.util.Optional;

import org.apache.log4j.Logger;

import com.github.g3force.instanceables.InstanceableClass;

import edu.tigers.sumatra.SslGameEvent;


public final class GameEventFactory
{
	private static final Logger log = Logger.getLogger(GameEventFactory.class.getName());
	
	
	private GameEventFactory()
	{
	}
	
	
	public static Optional<IGameEvent> fromProtobuf(SslGameEvent.GameEvent event)
	{
		try
		{
			final EGameEvent gameEvent = EGameEvent.fromProto(event.getType());
			return Optional.of((IGameEvent) gameEvent.getInstanceableClass().newInstance(event));
		} catch (InstanceableClass.NotCreateableException e)
		{
			log.warn("Could not convert game event: " + event, e);
		}
		return Optional.empty();
	}
}
