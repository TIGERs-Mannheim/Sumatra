/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.pass.rating.EPassRating;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPassFactory;
import edu.tigers.sumatra.drawable.ColorPickerFactory;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.IColorPicker;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.util.BotDistanceComparator;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;


/**
 * Pass to some free spot on the field, no robot as pass Target
 */
@RequiredArgsConstructor
public class KickInsBlaueActionMove extends AOffensiveActionMove
{
	@Configurable(comment = "Activates this action move", defValue = "true")
	private static boolean active = true;

	static
	{
		ConfigRegistration.registerClass("metis", KickInsBlaueActionMove.class);
	}


	private final PassFactory passFactory = new PassFactory();
	private final RatedPassFactory ratedPassFactory = new RatedPassFactory();
	private final Supplier<List<ICircle>> kickInsBlaueSpots;
	private final IColorPicker colorPicker = ColorPickerFactory.greenRedGradient();


	@Override
	public OffensiveAction calcAction(BotID botId) //Checking whether a KickInsBlaue might be appropriate
	{
		//Checking whether there is a set-play/whether there are no kickInsBlaueSpots/whether it's already activated
		if (kickInsBlaueSpots.get().isEmpty() || !active || getAiFrame().getGameState().isStandardSituationForUs())
		{
			return OffensiveAction.builder()
					.move(EOffensiveActionMove.KICK_INS_BLAUE)
					.viability(new OffensiveActionViability(EActionViability.FALSE, 0.0))
					.build();
		}


		var bestKickInsBlaueSpot = kickInsBlaueSpots.get().stream()
				.map(spot -> generateRatedPass(spot, botId))
				.sorted().findFirst();

		for (ICircle spot : kickInsBlaueSpots.get())
		{
			getShapes(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE)
					.add(new DrawableCircle(Circle.createCircle(spot.center(), 1.2 * Geometry.getBotRadius()),
							Color.GRAY));
		}


		return bestKickInsBlaueSpot.map(this::createSuccessfulOffensiveAction).orElse(createDefaultOffensiveAction());
	}


	private OffensiveAction createSuccessfulOffensiveAction(RatedPassWithKickInsBlaueSpot spot)
	{
		getShapes(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE)
				.add(new DrawableCircle(spot.getSpot(), Color.GRAY));
		getShapes(EAiShapesLayer.OFFENSIVE_KICK_INS_BLAUE).add(new DrawableCircle(
				Circle.createCircle(spot.getSpot().center(), 1.2 * Geometry.getBotRadius()),
				colorPicker.getColor(spot.getScore())));
		return OffensiveAction.builder()
				.move(EOffensiveActionMove.KICK_INS_BLAUE)
				.viability(new OffensiveActionViability(EActionViability.PARTIALLY, spot.getScore()))
				.pass(spot.getPass())
				.build();
	}


	private OffensiveAction createDefaultOffensiveAction()
	{

		return OffensiveAction.builder()
				.move(EOffensiveActionMove.KICK_INS_BLAUE)
				.viability(new OffensiveActionViability(EActionViability.FALSE, 0.0))
				.build();

	}


	private RatedPassWithKickInsBlaueSpot generateRatedPass(ICircle spot, BotID shooter)
	{
		passFactory.update(getWFrame());
		passFactory.setAimingTolerance(0.4);
		passFactory.setMaxReceivingBallSpeed(OffensiveConstants.getBallSpeedAtTargetKickInsBlaue() / 2);
		var pass = passFactory
				.straight(getBall().getPos(), spot.center(), shooter, getClosestTiger(spot.center()));


		ratedPassFactory.update(getWFrame().getOpponentBots().values());
		var score = ratedPassFactory.rateMaxCombined(pass, EPassRating.PASSABILITY, EPassRating.INTERCEPTION);
		score = applyMultiplier(score);

		return new RatedPassWithKickInsBlaueSpot(spot, pass, score);
	}


	private BotID getClosestTiger(IVector2 position)
	{
		return getWFrame().getTigerBotsAvailable().values().stream()
				.min(new BotDistanceComparator(position))
				.map(ITrackedBot::getBotId)
				.orElse(BotID.noBot());
	}


	@Persistent
	@Value
	@RequiredArgsConstructor
	static class RatedPassWithKickInsBlaueSpot implements Comparable<RatedPassWithKickInsBlaueSpot>
	{
		ICircle spot;
		Pass pass;
		double score;


		@SuppressWarnings("unused") // used by berkeley
		private RatedPassWithKickInsBlaueSpot()
		{
			this.spot = Circle.createCircle(Vector2.zero(), 0);
			this.pass = Pass.builder().build();
			this.score = 0;
		}


		@Override
		public int compareTo(RatedPassWithKickInsBlaueSpot other)
		{
			return Double.compare(this.score, other.score);
		}
	}
}
