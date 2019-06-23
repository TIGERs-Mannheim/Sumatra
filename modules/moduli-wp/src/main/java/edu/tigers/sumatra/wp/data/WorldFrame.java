/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 21.07.2010
 * Author(s): Gero
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import java.util.Map;

import edu.tigers.sumatra.bot.DummyBot;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;


/**
 * This frame contains world info for a specific AI
 * <p>
 * <i>(Being aware of EJ-SE Items 13, 14 and 55: members are public to reduce noise)</i>
 * </p>
 * 
 * @author Gero
 */
public class WorldFrame extends SimpleWorldFrame
{
	/** our enemies visible */
	public final BotIDMapConst<ITrackedBot>	foeBots;
	
	/** tiger bots that were detected by the WorldPredictor */
	public final BotIDMapConst<ITrackedBot>	tigerBotsVisible;
	/** tiger bots that were detected by the WorldPredictor AND are connected */
	public final IBotIDMap<ITrackedBot>			tigerBotsAvailable;
	
	
	private final ETeamColor						teamColor;
	
	private final boolean							inverted;
	
	
	/**
	 * @param simpleWorldFrame
	 * @param teamColor
	 * @param invert
	 */
	public WorldFrame(final SimpleWorldFrame simpleWorldFrame, final ETeamColor teamColor, final boolean invert)
	{
		super(simpleWorldFrame);
		this.teamColor = teamColor;
		inverted = invert;
		
		BotIDMap<ITrackedBot> foes = new BotIDMap<>();
		BotIDMap<ITrackedBot> tigersVisible = new BotIDMap<>();
		BotIDMap<ITrackedBot> tigersAvailable = new BotIDMap<>();
		for (Map.Entry<BotID, ITrackedBot> entry : simpleWorldFrame.getBots().entrySet())
		{
			final BotID botID = entry.getKey();
			ITrackedBot bot = entry.getValue();
			
			if (bot.getBotId().getTeamColor() == getTeamColor())
			{
				tigersVisible.put(botID, bot);
				if (bot.isAvailableToAi()
						&& Geometry.getFieldWReferee()
								.isPointInShape(bot.getPos(), -Geometry.getBotRadius()))
				{
					tigersAvailable.put(botID, bot);
				}
			} else
			{
				TrackedBot nBot = new TrackedBot(bot);
				nBot.setBot(new DummyBot(botID));
				foes.put(botID, nBot);
			}
		}
		foeBots = BotIDMapConst.unmodifiableBotIDMap(foes);
		tigerBotsAvailable = BotIDMapConst.unmodifiableBotIDMap(tigersAvailable);
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(tigersVisible);
	}
	
	
	/**
	 * Providing a <strong>shallow</strong> copy of original (Thus new collections are created, but filled with the same
	 * values
	 * 
	 * @param original
	 */
	public WorldFrame(final WorldFrame original)
	{
		super(original);
		teamColor = original.getTeamColor();
		inverted = original.isInverted();
		foeBots = BotIDMapConst.unmodifiableBotIDMap(original.getFoeBots());
		tigerBotsAvailable = original.getTigerBotsAvailable();
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(original.getTigerBotsVisible());
	}
	
	
	@Override
	public String toString()
	{
		final StringBuilder b = new StringBuilder();
		b.append("[WorldFrame, id = ").append(getId()).append("|\n");
		b.append("Ball: ").append(getBall().getPos()).append("|\n");
		b.append("Tigers: ");
		for (final ITrackedBot tiger : tigerBotsVisible.values())
		{
			b.append(tiger.getPos()).append(",");
		}
		b.append("|\n");
		b.append("Enemies: ");
		for (final ITrackedBot bot : foeBots.values())
		{
			b.append(bot.getPos()).append(",");
		}
		b.append("]\n");
		return b.toString();
	}
	
	
	/**
	 * Get {@link TrackedBot} from current {@link WorldFrame}.
	 * 
	 * @param botId
	 * @return tiger {@link TrackedBot}
	 */
	public ITrackedBot getTiger(final BotID botId)
	{
		return getBot(botId);
	}
	
	
	/**
	 * Get foe {@link TrackedBot} from current {@link WorldFrame}.
	 * 
	 * @param botId
	 * @return foe {@link TrackedBot}
	 */
	public ITrackedBot getFoeBot(final BotID botId)
	{
		return getBot(botId);
	}
	
	
	/**
	 * @return
	 */
	public BotIDMapConst<ITrackedBot> getFoeBots()
	{
		return foeBots;
	}
	
	
	/**
	 * @return
	 */
	public BotIDMapConst<ITrackedBot> getTigerBotsVisible()
	{
		return tigerBotsVisible;
	}
	
	
	/**
	 * @return
	 */
	public IBotIDMap<ITrackedBot> getTigerBotsAvailable()
	{
		return tigerBotsAvailable;
	}
	
	
	/**
	 * @return
	 */
	public final ETeamColor getTeamColor()
	{
		return teamColor;
	}
	
	
	/**
	 * @return the inverted
	 */
	public final boolean isInverted()
	{
		return inverted;
	}
}
