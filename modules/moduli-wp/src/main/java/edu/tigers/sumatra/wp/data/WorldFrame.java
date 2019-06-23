/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import java.util.Map;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.BotIDMapConst;
import edu.tigers.sumatra.ids.EAiTeam;
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
	public final BotIDMapConst<ITrackedBot> foeBots;
	
	/** tiger bots that were detected by the WorldPredictor */
	public final BotIDMapConst<ITrackedBot> tigerBotsVisible;
	/** tiger bots that were detected by the WorldPredictor AND are connected */
	public final IBotIDMap<ITrackedBot> tigerBotsAvailable;
	
	private final IBotIDMap<ITrackedBot> allBots;
	
	private final ETeamColor teamColor;
	
	private final boolean inverted;
	
	
	/**
	 * @param simpleWorldFrame
	 * @param team
	 * @param invert
	 */
	public WorldFrame(final SimpleWorldFrame simpleWorldFrame, final EAiTeam team, final boolean invert)
	{
		super(simpleWorldFrame);
		this.teamColor = team.getTeamColor();
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
				if (bot.getRobotInfo().getAiType() == team.getAiType())
				{
					tigersAvailable.put(botID, bot);
				}
			} else
			{
				RobotInfo robotInfo = RobotInfo.stub(bot.getBotId(), bot.getTimestamp());
				bot = TrackedBot.newCopyBuilder(bot)
						.withBotInfo(robotInfo)
						.build();
				foes.put(botID, bot);
			}
		}
		foeBots = BotIDMapConst.unmodifiableBotIDMap(foes);
		tigerBotsAvailable = BotIDMapConst.unmodifiableBotIDMap(tigersAvailable);
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(tigersVisible);
		
		BotIDMap<ITrackedBot> bots = new BotIDMap<>();
		bots.putAll(foeBots);
		bots.putAll(tigersVisible);
		allBots = BotIDMapConst.unmodifiableBotIDMap(bots);
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
		allBots = original.allBots;
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
	
	
	@Override
	public IBotIDMap<ITrackedBot> getBots()
	{
		return allBots;
	}
	
	
	@Override
	public ITrackedBot getBot(final BotID botId)
	{
		return allBots.getWithNull(botId);
	}
	
	
	/**
	 * Get {@link ITrackedBot}
	 * 
	 * @param botId of the bot
	 * @return tiger {@link TrackedBot}
	 */
	public ITrackedBot getTiger(final BotID botId)
	{
		return getBot(botId);
	}
	
	
	/**
	 * Get foe {@link ITrackedBot}
	 * 
	 * @param botId of the bot
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
