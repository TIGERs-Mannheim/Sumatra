/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee.engine.detector;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import com.github.g3force.configurable.Configurable;

import edu.tigers.autoreferee.AutoRefUtil.ColorFilter;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.referee.gameevent.TooManyRobots;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * This rule compares the number of bots that are located inside the field with the maximum allowed number of bots. It
 * also consults the team info sent by the referee box to detect if a team did not decrease their bot number after a
 * yellow card.
 */
public class BotNumberDetector extends AGameEventDetector
{
	
	@Configurable(comment = "[s] After this time period the detectors triggers the event", defValue = "0.5")
	private static double minTimeDiff = 0.5;
	
	private final Map<ETeamColor, Violation> violationPerTeam = new EnumMap<>(ETeamColor.class);
	
	
	public BotNumberDetector()
	{
		super(EGameEventDetectorType.BOT_NUMBER, EGameState.RUNNING);
	}
	
	
	@Override
	public Optional<IGameEvent> doUpdate()
	{
		final Optional<IGameEvent> yellow = checkTeam(ETeamColor.YELLOW);
		if (yellow.isPresent())
		{
			return yellow;
		}
		return checkTeam(ETeamColor.BLUE);
	}
	
	
	private Optional<IGameEvent> checkTeam(final ETeamColor teamColor)
	{
		Collection<ITrackedBot> bots = frame.getWorldFrame().getBots().values();
		
		int allowedCount = frame.getRefereeMsg().getTeamInfo(teamColor).getMaxAllowedBots();
		int actualCount = getTeamOnFieldBotCount(bots, teamColor);
		
		int diff = actualCount - allowedCount;
		
		if (diff > 0)
		{
			violationPerTeam.putIfAbsent(teamColor, new Violation(frame.getTimestamp()));
			Violation violation = violationPerTeam.get(teamColor);
			if (timeDiffTooBig(violation) && violation.isUnpunished())
			{
				violation.punish();
				return Optional.of(new TooManyRobots(teamColor));
			}
		} else
		{
			violationPerTeam.remove(teamColor);
		}
		return Optional.empty();
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
	
	
	private boolean timeDiffTooBig(final Violation violation)
	{
		return ((frame.getTimestamp() - violation.getStartTimestamp()) / 1e9) >= minTimeDiff;
	}
	
	private enum PunishedStatus
	{
		PUNISHED,
		UNPUNISHED
	}
	
	private class Violation
	{
		private final long startTimestamp;
		private PunishedStatus punishedStatus;
		
		
		private Violation(final long startTimestamp)
		{
			this.startTimestamp = startTimestamp;
			this.punishedStatus = PunishedStatus.UNPUNISHED;
		}
		
		
		public long getStartTimestamp()
		{
			return startTimestamp;
		}
		
		
		public boolean isUnpunished()
		{
			return punishedStatus == PunishedStatus.UNPUNISHED;
		}
		
		
		public void punish()
		{
			this.punishedStatus = PunishedStatus.PUNISHED;
		}
	}
}
