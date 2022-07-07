/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.autoreferee.engine.detector;

import com.github.g3force.configurable.Configurable;
import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.EAutoRefShapesLayer;
import edu.tigers.sumatra.drawable.DrawableTube;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.tube.ITube;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.referee.gameevent.BotInterferedPlacement;
import edu.tigers.sumatra.referee.gameevent.IGameEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Detect ball placement interference by the opponent team
 */
public class BallPlacementInterferenceDetector extends AGameEventDetector
{
	@Configurable(defValue = "2.0", comment = "The time [s] that a robot is allowed to stay within the forbidden area")
	private static double violationTime = 2.0;

	private final List<Violator> violators = new ArrayList<>();


	public BallPlacementInterferenceDetector()
	{
		super(EGameEventDetectorType.BALL_PLACEMENT_INTERFERENCE, EGameState.BALL_PLACEMENT);
	}


	@Override
	public void doReset()
	{
		violators.clear();
	}


	@Override
	protected Optional<IGameEvent> doUpdate()
	{
		if (!frame.getWorldFrame().getBall().isOnCam(0.2))
		{
			// Can not see the ball
			violators.clear();
			return Optional.empty();
		}

		Set<BotID> violatingBots = violatingBots();
		violators.removeIf(b -> !violatingBots.contains(b.getBotId()));
		violatingBots.stream().filter(this::isNewViolator)
				.forEach(botID -> violators.add(new Violator(botID, frame.getTimestamp())));

		return violators.stream()
				.filter(this::keepsViolating)
				.filter(Violator::isUnpunished)
				.findAny()
				.map(this::createEvent);
	}


	private boolean isNewViolator(BotID id)
	{
		return violators.stream().noneMatch(e -> Objects.equals(e.getBotId(), id));
	}


	private Set<BotID> violatingBots()
	{
		IVector2 ballPos = frame.getWorldFrame().getBall().getPos();
		IVector2 placePos = Optional.ofNullable(frame.getGameState().getBallPlacementPositionNeutral()).orElse(ballPos);
		ITube placementTube = Tube.create(ballPos, placePos, RuleConstraints.getStopRadius() + Geometry.getBotRadius());
		ETeamColor placingTeam = frame.getGameState().getForTeam();

		frame.getShapes().get(EAutoRefShapesLayer.VIOLATED_DISTANCES)
				.add(new DrawableTube(placementTube.withMargin(-Geometry.getBotRadius()), Color.red));

		return frame.getWorldFrame().getBots().values().stream()
				.filter(AutoRefUtil.ColorFilter.get(placingTeam.opposite()))
				.filter(bot -> placementTube.isPointInShape(bot.getPos()))
				.map(ITrackedBot::getBotId)
				.collect(Collectors.toSet());
	}


	private IGameEvent createEvent(Violator violator)
	{
		violator.punish();
		return new BotInterferedPlacement(violator.getBotId(),
				frame.getWorldFrame().getBot(violator.getBotId()).getPos());
	}


	private boolean keepsViolating(Violator violator)
	{
		return (frame.getTimestamp() - violator.getStartTimestamp()) / 1e9 > violationTime;
	}


	private enum PunishedStatus
	{
		PUNISHED,
		UNPUNISHED
	}

	private static class Violator
	{
		private final BotID botId;
		private final long startTimestamp;
		private PunishedStatus punished;


		private Violator(final BotID botId, final long startTimestamp)
		{
			this.botId = botId;
			this.startTimestamp = startTimestamp;
			this.punished = PunishedStatus.UNPUNISHED;
		}


		public BotID getBotId()
		{
			return botId;
		}


		public long getStartTimestamp()
		{
			return startTimestamp;
		}


		public boolean isUnpunished()
		{
			return punished == PunishedStatus.UNPUNISHED;
		}


		public void punish()
		{
			this.punished = PunishedStatus.PUNISHED;
		}
	}
}
