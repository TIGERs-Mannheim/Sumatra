/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.skirmish;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.referee.data.EGameState;
import edu.tigers.sumatra.time.TimestampTimer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


@RequiredArgsConstructor
public class SkirmishCategorizationCalc extends ACalculator
{
	@Configurable(defValue = "300.0", comment = "[mm] ")
	private static double minSkirmishDistance = 300;
	@Configurable(defValue = "400.0", comment = "[mm] ")
	private static double maxSkirmishDistance = 400;

	private final Supplier<BotDistance> tigerClosestToBall;
	private final Supplier<BotDistance> opponentClosestToBall;

	@Getter
	private ESkirmishCategory skirmishCategory = ESkirmishCategory.NO_SKIRMISH;
	private TimestampTimer switchToSkirmishDelay = new TimestampTimer(0.5);


	@Override
	protected void doCalc()
	{
		skirmishCategory = decideSituation();

		getShapes(EAiShapesLayer.AI_SKIRMISH_STRATEGY).add(new DrawableAnnotation(
				getBall().getPos().addNew(Vector2.fromXY(400, 50)),
				skirmishCategory.toString(),
				getAiFrame().getTeamColor().getColor()
		));
	}


	private ESkirmishCategory decideSituation()
	{
		var tiger = tigerClosestToBall.get().getBotId();
		var opponent = opponentClosestToBall.get().getBotId();
		var tNow = getWFrame().getTimestamp();

		if (getAiFrame().getGameState().isStoppedGame()
				&& getAiFrame().getGameState().getNextState() == EGameState.RUNNING)
		{
			// Force start incoming
			return ESkirmishCategory.PENDING_CONTESTED_CONTROL;
		}


		if (!isGeneralSkirmishSituation())
		{
			switchToSkirmishDelay.reset();
			return ESkirmishCategory.NO_SKIRMISH;
		}
		switchToSkirmishDelay.update(tNow);

		if (hasRobotUncontestedBallControl(tiger, opponent, skirmishCategory == ESkirmishCategory.WE_HAVE_CONTROL))
		{
			return switchToSkirmishDelay.isTimeUp(tNow) ?
					ESkirmishCategory.WE_HAVE_CONTROL :
					ESkirmishCategory.PENDING_WE_HAVE_CONTROL;
		}
		if (hasRobotUncontestedBallControl(opponent, tiger,
				skirmishCategory == ESkirmishCategory.OPPONENT_HAS_CONTROL))
		{
			return switchToSkirmishDelay.isTimeUp(tNow) ?
					ESkirmishCategory.OPPONENT_HAS_CONTROL :
					ESkirmishCategory.PENDING_OPPONENT_HAS_CONTROL;
		}
		return switchToSkirmishDelay.isTimeUp(tNow) ?
				ESkirmishCategory.CONTESTED_CONTROL :
				ESkirmishCategory.PENDING_CONTESTED_CONTROL;

	}


	private boolean isGeneralSkirmishSituation()
	{
		var dist = skirmishCategory == ESkirmishCategory.NO_SKIRMISH ? minSkirmishDistance : maxSkirmishDistance;
		return tigerClosestToBall.get().getDist() <= dist && opponentClosestToBall.get().getDist() <= dist;
	}


	private boolean hasRobotUncontestedBallControl(BotID robotId, BotID contestantId, boolean applyHysteresis)
	{
		var robot = getWFrame().getBot(robotId);
		if (robot == null)
		{
			return false;
		}
		var other = getWFrame().getBot(contestantId);
		if (other == null)
		{
			return true;
		}

		var ballToRobot = Vector2.fromPoints(getBall().getPos(), robot.getBotKickerPos());
		var ballToContestant = Vector2.fromPoints(getBall().getPos(), other.getBotKickerPos());

		if (isRobotBetweenBallAndOther(ballToRobot, ballToContestant, applyHysteresis))
		{
			return true;
		}

		var minDistAdvantage = applyHysteresis ? 0.5 * Geometry.getBotRadius() : 1.5 * Geometry.getBotRadius();

		return robot.getBallContact().hasContactFromVisionOrBarrier()
				&& ballToRobot.getLength() + minDistAdvantage < ballToContestant.getLength();
	}


	private boolean isRobotBetweenBallAndOther(IVector2 ballToRobot, IVector2 ballToOther, boolean applyHysteresis)
	{
		var allowedAngle = applyHysteresis ? 0.35 : 0.3;
		return AngleMath.diffAbs(ballToRobot.getAngle(), ballToOther.getAngle()) < allowedAngle
				&& ballToRobot.getLength() < ballToOther.getLength();
	}

}
