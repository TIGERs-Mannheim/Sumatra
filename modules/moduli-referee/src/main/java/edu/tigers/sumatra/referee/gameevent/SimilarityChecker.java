/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.referee.gameevent;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;


/**
 * Check if two game events are similar up to a threshold.
 */
public class SimilarityChecker
{
	@SuppressWarnings("rawtypes")
	private final Map<Class<? extends IGameEvent>, List<Checker>> map = new HashMap<>();


	public <T extends IGameEvent, U> void register(Class<T> clazz, Check<U> check, Function<T, U> supplier)
	{
		map.computeIfAbsent(clazz, c -> new ArrayList<>()).add(new Checker<>(check, supplier));
	}


	private boolean similarLocation(IVector2 p1, IVector2 p2)
	{
		return p1.distanceTo(p2) < 500;
	}


	@SuppressWarnings("unchecked")
	public boolean isSimilar(IGameEvent e1, IGameEvent e2)
	{
		if (!e1.getClass().equals(e2.getClass()))
		{
			return false;
		}
		if (e1.getType() != e2.getType())
		{
			return false;
		}
		return map.getOrDefault(e1.getClass(), Collections.emptyList()).stream().allMatch(c -> c.isSimilar(e1, e2));
	}


	public SimilarityChecker initAllGameEvents()
	{
		register(AimlessKick.class, Objects::equals, AimlessKick::getTeam);
		register(AimlessKick.class, Objects::equals, AimlessKick::getBot);
		register(AimlessKick.class, this::similarLocation, AimlessKick::getLocation);
		register(AimlessKick.class, this::similarLocation, AimlessKick::getKickLocation);

		register(AttackerDoubleTouchedBall.class, Objects::equals, AttackerDoubleTouchedBall::getTeam);
		register(AttackerDoubleTouchedBall.class, Objects::equals, AttackerDoubleTouchedBall::getBot);
		register(AttackerDoubleTouchedBall.class, this::similarLocation, AttackerDoubleTouchedBall::getLocation);

		register(AttackerTooCloseToDefenseArea.class, Objects::equals, AttackerTooCloseToDefenseArea::getTeam);
		register(AttackerTooCloseToDefenseArea.class, Objects::equals, AttackerTooCloseToDefenseArea::getBot);
		register(AttackerTooCloseToDefenseArea.class, this::similarLocation, AttackerTooCloseToDefenseArea::getLocation);

		register(AttackerTouchedBallInDefenseArea.class, Objects::equals, AttackerTouchedBallInDefenseArea::getTeam);
		register(AttackerTouchedBallInDefenseArea.class, Objects::equals, AttackerTouchedBallInDefenseArea::getBot);
		register(AttackerTouchedBallInDefenseArea.class, this::similarLocation,
				AttackerTouchedBallInDefenseArea::getLocation);

		register(BallLeftFieldGoalLine.class, Objects::equals, BallLeftFieldGoalLine::getTeam);
		register(BallLeftFieldGoalLine.class, Objects::equals, BallLeftFieldGoalLine::getBot);
		register(BallLeftFieldGoalLine.class, this::similarLocation, BallLeftFieldGoalLine::getLocation);

		register(BallLeftFieldTouchLine.class, Objects::equals, BallLeftFieldTouchLine::getTeam);
		register(BallLeftFieldTouchLine.class, Objects::equals, BallLeftFieldTouchLine::getBot);
		register(BallLeftFieldTouchLine.class, this::similarLocation, BallLeftFieldTouchLine::getLocation);

		register(BotCrashDrawn.class, Objects::equals, BotCrashDrawn::getBotY);
		register(BotCrashDrawn.class, Objects::equals, BotCrashDrawn::getBotB);
		register(BotCrashDrawn.class, this::similarLocation, BotCrashDrawn::getLocation);

		register(BotCrashUnique.class, Objects::equals, BotCrashUnique::getTeam);
		register(BotCrashUnique.class, Objects::equals, BotCrashUnique::getViolator);
		register(BotCrashUnique.class, Objects::equals, BotCrashUnique::getVictim);
		register(BotCrashUnique.class, this::similarLocation, BotCrashUnique::getLocation);

		register(BotDribbledBallTooFar.class, Objects::equals, BotDribbledBallTooFar::getTeam);
		register(BotDribbledBallTooFar.class, Objects::equals, BotDribbledBallTooFar::getBot);
		register(BotDribbledBallTooFar.class, this::similarLocation, BotDribbledBallTooFar::getStart);
		register(BotDribbledBallTooFar.class, this::similarLocation, BotDribbledBallTooFar::getEnd);

		register(BotHeldBallDeliberately.class, Objects::equals, BotHeldBallDeliberately::getTeam);
		register(BotHeldBallDeliberately.class, Objects::equals, BotHeldBallDeliberately::getBot);
		register(BotHeldBallDeliberately.class, this::similarLocation, BotHeldBallDeliberately::getLocation);

		register(BotInterferedPlacement.class, Objects::equals, BotInterferedPlacement::getTeam);
		register(BotInterferedPlacement.class, Objects::equals, BotInterferedPlacement::getBot);
		register(BotInterferedPlacement.class, this::similarLocation, BotInterferedPlacement::getLocation);

		register(BotKickedBallToFast.class, Objects::equals, BotKickedBallToFast::getTeam);
		register(BotKickedBallToFast.class, Objects::equals, BotKickedBallToFast::getBot);
		register(BotKickedBallToFast.class, this::similarLocation, BotKickedBallToFast::getLocation);
		register(BotKickedBallToFast.class, Objects::equals, BotKickedBallToFast::getKickType);

		register(BotPushedBot.class, Objects::equals, BotPushedBot::getTeam);
		register(BotPushedBot.class, Objects::equals, BotPushedBot::getViolator);
		register(BotPushedBot.class, Objects::equals, BotPushedBot::getVictim);
		register(BotPushedBot.class, this::similarLocation, BotPushedBot::getLocation);

		register(BotSubstitution.class, Objects::equals, BotSubstitution::getTeam);

		register(BotTippedOver.class, Objects::equals, BotTippedOver::getTeam);
		register(BotTippedOver.class, Objects::equals, BotTippedOver::getBot);
		register(BotTippedOver.class, this::similarLocation, BotTippedOver::getLocation);

		register(BotTooFastInStop.class, Objects::equals, BotTooFastInStop::getTeam);
		register(BotTooFastInStop.class, Objects::equals, BotTooFastInStop::getBot);

		register(BoundaryCrossing.class, Objects::equals, BoundaryCrossing::getTeam);
		register(BoundaryCrossing.class, this::similarLocation, BoundaryCrossing::getLocation);

		register(DefenderInDefenseArea.class, Objects::equals, DefenderInDefenseArea::getTeam);
		register(DefenderInDefenseArea.class, Objects::equals, DefenderInDefenseArea::getBot);
		register(DefenderInDefenseArea.class, this::similarLocation, DefenderInDefenseArea::getLocation);

		register(DefenderTooCloseToKickPoint.class, Objects::equals, DefenderTooCloseToKickPoint::getTeam);
		register(DefenderTooCloseToKickPoint.class, Objects::equals, DefenderTooCloseToKickPoint::getBot);
		register(DefenderTooCloseToKickPoint.class, this::similarLocation, DefenderTooCloseToKickPoint::getLocation);

		register(Goal.class, Objects::equals, Goal::getTeam);
		register(Goal.class, Objects::equals, Goal::getKickingTeam);
		register(Goal.class, Objects::equals, Goal::getKickingBot);
		register(Goal.class, this::similarLocation, Goal::getLocation);

		register(InvalidGoal.class, Objects::equals, InvalidGoal::getTeam);
		register(InvalidGoal.class, Objects::equals, InvalidGoal::getKickingTeam);
		register(InvalidGoal.class, Objects::equals, InvalidGoal::getKickingBot);
		register(InvalidGoal.class, this::similarLocation, InvalidGoal::getLocation);

		register(KeeperHeldBall.class, Objects::equals, KeeperHeldBall::getTeam);
		register(KeeperHeldBall.class, this::similarLocation, KeeperHeldBall::getLocation);

		register(MultipleCards.class, Objects::equals, MultipleCards::getTeam);

		register(MultipleFouls.class, Objects::equals, MultipleFouls::getTeam);

		register(NoProgressInGame.class, this::similarLocation, NoProgressInGame::getLocation);

		register(PenaltyKickFailed.class, Objects::equals, PenaltyKickFailed::getTeam);
		register(PenaltyKickFailed.class, this::similarLocation, PenaltyKickFailed::getLocation);

		register(PlacementFailed.class, Objects::equals, PlacementFailed::getTeam);

		register(PlacementSucceeded.class, Objects::equals, PlacementSucceeded::getTeam);

		register(PossibleGoal.class, Objects::equals, PossibleGoal::getTeam);
		register(PossibleGoal.class, this::similarLocation, PossibleGoal::getLocation);

		register(TooManyRobots.class, Objects::equals, TooManyRobots::getTeam);

		return this;
	}


	@Value
	private static class Checker<T extends IGameEvent, U>
	{
		Check<U> check;
		Function<T, U> supplier;


		boolean isSimilar(T o1, T o2)
		{
			var u1 = supplier.apply(o1);
			var u2 = supplier.apply(o2);
			return check.apply(u1, u2);
		}
	}

	@FunctionalInterface
	public interface Check<T>
	{
		boolean apply(T o1, T o2);
	}
}
