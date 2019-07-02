package edu.tigers.sumatra.referee.data;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.referee.gameevent.IGameEvent;


/**
 * A proposed game event.
 */
@Persistent
public class ProposedGameEvent
{
	private final IGameEvent gameEvent;
	private final long validUntil;
	private final String proposerId;
	
	
	@SuppressWarnings("unused") // used by berkeley
	private ProposedGameEvent()
	{
		gameEvent = null;
		validUntil = 0;
		proposerId = null;
	}
	
	
	public ProposedGameEvent(final IGameEvent gameEvent, final long validUntil, final String proposerId)
	{
		this.gameEvent = gameEvent;
		this.validUntil = validUntil;
		this.proposerId = proposerId;
	}
	
	
	public IGameEvent getGameEvent()
	{
		return gameEvent;
	}
	
	
	public long getValidUntil()
	{
		return validUntil;
	}
	
	
	public String getProposerId()
	{
		return proposerId;
	}
}
