/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.tigers.sumatra.bot.RobotInfo;
import edu.tigers.sumatra.geometry.Geometry;
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
	
	
	public WorldFrame(final SimpleWorldFrame simpleWorldFrame, final EAiTeam team, final boolean invert)
	{
		super(simpleWorldFrame);
		this.teamColor = team.getTeamColor();
		inverted = invert;
		
		foeBots = BotIDMapConst.unmodifiableBotIDMap(computeFoeBots(simpleWorldFrame, team));
		tigerBotsAvailable = BotIDMapConst
				.unmodifiableBotIDMap(computeAvailableTigers(simpleWorldFrame, team));
		tigerBotsVisible = BotIDMapConst.unmodifiableBotIDMap(computeTigersVisible(simpleWorldFrame, team));
		
		BotIDMap<ITrackedBot> bots = new BotIDMap<>();
		bots.putAll(foeBots);
		bots.putAll(tigerBotsVisible);
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
	
	
	private Stream<ITrackedBot> createStreamOfAiTeam(final SimpleWorldFrame swf, EAiTeam aiTeam)
	{
		return swf.getBots().values().stream()
				.filter(bot -> aiTeam.getTeamColor() == bot.getBotId().getTeamColor());
	}
	
	
	private BotIDMap<ITrackedBot> computeAvailableTigers(final SimpleWorldFrame simpleWorldFrame,
			final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> availableTigers = createStreamOfAiTeam(simpleWorldFrame, aiTeam)
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return new BotIDMap<>(availableTigers);
	}
	
	
	private BotIDMap<ITrackedBot> computeFoeBots(final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		
		Map<BotID, ITrackedBot> foes = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> bot.getBotId().getTeamColor() == aiTeam.getTeamColor().opposite())
				.filter(bot -> Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
				.map(bot -> {
					RobotInfo info = RobotInfo.stub(bot.getBotId(), bot.getTimestamp());
					return TrackedBot.newCopyBuilder(bot)
							.withBotInfo(info)
							.withState(bot.getFilteredState().orElse(bot.getBotState()))
							.build();
				})
				.collect(Collectors.toMap(TrackedBot::getBotId, Function.identity()));
		return new BotIDMap<>(foes);
	}
	
	
	private BotIDMap<ITrackedBot> computeTigersVisible(final SimpleWorldFrame simpleWorldFrame, final EAiTeam aiTeam)
	{
		Map<BotID, ITrackedBot> visible = simpleWorldFrame.getBots().values().stream()
				.filter(bot -> aiTeam.matchesColor(bot.getTeamColor()))
				.collect(Collectors.toMap(ITrackedBot::getBotId, Function.identity()));
		return new BotIDMap<>(visible);
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
