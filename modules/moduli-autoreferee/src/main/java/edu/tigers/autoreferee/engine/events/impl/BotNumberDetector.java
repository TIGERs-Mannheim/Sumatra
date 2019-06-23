/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.events.impl;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.engine.events.BotNumberViolation;
import edu.tigers.autoreferee.engine.events.EGameEventDetectorType;
import edu.tigers.autoreferee.engine.events.IGameEvent;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.referee.data.TeamInfo;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule compares the number of bots that are located inside the field with the maximum allowed number of bots. It
 * also consults the team info sent by the referee box to detect if a team did not decrease their bot number after a
 * yellow card.
 * Currently it only returns a RuleViolation when the game state changes to {@link EGameState#RUNNING} but does
 * not stop the game.
 * 
 * @author "Lukas Magel"
 */
public class BotNumberDetector extends AGameEventDetector
{
	private static final int PRIORITY = 1;
	
	static
	{
		AGameEventDetector.registerClass(BotInDefenseAreaDetector.class);
	}
	
	private final ETeamColor teamColor;
	private int lastDiff = 0;
	
	
	public BotNumberDetector(final ETeamColor teamColor)
	{
		super(EGameEventDetectorType.BOT_NUMBER, EGameState.RUNNING);
		this.teamColor = teamColor;
	}
	
	
	@Override
	public int getPriority()
	{
		return PRIORITY;
	}
	
	
	@Override
	public Optional<IGameEvent> update(final IAutoRefFrame frame)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		RefereeMsg refMsg = frame.getRefereeMsg();
		long ts = frame.getTimestamp();
		
		int allowedCount = getAllowedTeamBotCount(refMsg, teamColor, ts);
		int actualCount = getTeamOnFieldBotCount(bots, teamColor);
		
		int diff = actualCount - allowedCount;
		
		if ((diff > lastDiff) && (diff > 0))
		{
			lastDiff = diff;
			return Optional.of(new BotNumberViolation(frame.getTimestamp(), teamColor, null, allowedCount,
					actualCount));
		}
		return Optional.empty();
	}
	
	
	private int getAllowedTeamBotCount(final RefereeMsg msg, final ETeamColor color, final long curTime_ns)
	{
		TeamInfo teamInfo = color == ETeamColor.BLUE ? msg.getTeamInfoBlue() : msg.getTeamInfoYellow();
		long msgTimeNs = msg.getFrameTimestamp();
		long passedTimeUs = TimeUnit.NANOSECONDS.toMicros(curTime_ns - msgTimeNs);
		
		int yellowCards = (int) teamInfo.getYellowCardsTimes().stream()
				.map(cardTimeUs -> cardTimeUs - passedTimeUs)
				.filter(cardTimeUs -> cardTimeUs > 0)
				.count();
		
		return RuleConstraints.getBotsPerTeam() - yellowCards;
	}
	
	
	private int getTeamOnFieldBotCount(final Collection<ITrackedBot> bots, final ETeamColor color)
	{
		/*
		 * The filter mechanism uses the extended field to also catch bots which might be positioned partially outside the
		 * regular field
		 */
		return (int) bots.stream()
				.filter(ColorFilter.get(color))
				.filter(bot -> Geometry.getFieldWBorders().isPointInShape(bot.getPos()))
				.count();
	}
	
	
	@Override
	public void reset()
	{
		lastDiff = 0;
	}
}
