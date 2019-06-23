/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 28, 2016
 * Author(s): Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.event;

import java.util.ArrayList;
import java.util.List;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
@Persistent
public class GameEvent
{
	private long			startFrame;
	private long			endFrame;
	private BotID			affectedBot;
	private List<BotID>	additionalBots;
	
	
	/**
	 * @param startFrame
	 */
	public GameEvent(final long startFrame)
	{
		this(startFrame, startFrame, null);
	}
	
	
	/**
	 * Default constructor for persistence test
	 */
	public GameEvent()
	{
		this(0);
	}
	
	
	/**
	 * This is the default constructor.
	 * It will create a game event that is signaled to start at the given frameID.
	 * 
	 * @param frameID The frameID where this event is created.
	 * @param responsibleBot The bot responsible for the event.
	 */
	public GameEvent(final long frameID, final BotID responsibleBot)
	{
		this(frameID, frameID, responsibleBot);
	}
	
	
	/**
	 * @param frameID
	 * @param affectedBot
	 * @param additionalBots
	 */
	public GameEvent(final long frameID, final BotID affectedBot, final List<BotID> additionalBots)
	{
		this(frameID, affectedBot);
		this.additionalBots = additionalBots;
	}
	
	
	/**
	 * @param startFrame
	 * @param affectedTeam
	 */
	public GameEvent(final long startFrame, final ETeamColor affectedTeam)
	{
		this(startFrame, startFrame, null);
	}
	
	
	/**
	 * This constructor is mainly for test purposes it serves as an easy way to create new game event
	 * 
	 * @param startFrame The frame, where the event starts
	 * @param endFrame The frame where the event ends
	 * @param responsibleBot The affected bot
	 */
	public GameEvent(final long startFrame, final long endFrame, final BotID responsibleBot)
	{
		this.startFrame = startFrame;
		this.endFrame = endFrame;
		affectedBot = responsibleBot;
	}
	
	
	/**
	 * This function is used to signal an active event at a given frame
	 * 
	 * @param frameID This is the ID where the frame happened
	 */
	public void signalEventActiveAtFrame(final long frameID)
	{
		if (frameID > endFrame)
		{
			endFrame = frameID;
		}
	}
	
	
	/**
	 * Gets the frame in which an event started
	 * 
	 * @return The start frame
	 */
	public long getStartFrame()
	{
		return startFrame;
	}
	
	
	/**
	 * Gets the frame in which the event ended
	 * 
	 * @return The end frame
	 */
	public long getEndFrame()
	{
		return endFrame;
	}
	
	
	/**
	 * Gets the responsible team for a specified event.
	 * 
	 * @return The team color affected by an event
	 */
	public ETeamColor getAffectedTeam()
	{
		return affectedBot.getTeamColor();
	}
	
	
	/**
	 * Gets the responsible bot for a specified event.
	 * 
	 * @return The BotID of the affected bot
	 */
	public BotID getAffectedBot()
	{
		return affectedBot;
	}
	
	
	/**
	 * Will return the additional involved bots.
	 * This list will be null if there are no additional involved bots.
	 * 
	 * @return the additionalBots The additional involved bots.
	 */
	public List<BotID> getAdditionalBots()
	{
		return additionalBots;
	}
	
	
	/**
	 * This will add a bot to the involved bots
	 * 
	 * @param bot The bot to be added to the involved bots
	 */
	public void addInvolvedBot(final BotID bot)
	{
		if (additionalBots == null)
		{
			additionalBots = new ArrayList<BotID>();
		}
		additionalBots.add(bot);
	}
	
	
	/**
	 * This will return the duration of an event
	 * 
	 * @return The event duration
	 */
	public long getDuration()
	{
		return (endFrame - startFrame) + 1;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((additionalBots == null) ? 0 : additionalBots.hashCode());
		result = (prime * result) + ((affectedBot == null) ? 0 : affectedBot.hashCode());
		result = (prime * result) + (int) (endFrame ^ (endFrame >>> 32));
		result = (prime * result) + (int) (startFrame ^ (startFrame >>> 32));
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		GameEvent other = (GameEvent) obj;
		if (additionalBots == null)
		{
			if (other.additionalBots != null)
			{
				return false;
			}
		} else if (!additionalBots.equals(other.additionalBots))
		{
			return false;
		}
		if (affectedBot == null)
		{
			if (other.affectedBot != null)
			{
				return false;
			}
		} else if (!affectedBot.equals(other.affectedBot))
		{
			return false;
		}
		if (endFrame != other.endFrame)
		{
			return false;
		}
		if (startFrame != other.startFrame)
		{
			return false;
		}
		return true;
	}
	
	
}
