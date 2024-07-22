/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.general.ESkirmishStrategy;
import edu.tigers.sumatra.ai.metis.general.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.redirector.RedirectorDetectionInformation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;


/**
 * Find a position for the supportive attacker.
 */
@RequiredArgsConstructor
public class SupportiveAttackerPosCalc extends ACalculator
{
	private final Supplier<SkirmishInformation> skirmishInformation;
	private final Supplier<RedirectorDetectionInformation> redirectorDetectionInformation;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final Supplier<IVector2> supportiveBlockerPos;

	@Configurable(defValue = "500.0")
	private static double defaultDistToBall = 500.0;

	@Configurable(defValue = "400.0")
	private static double considerOpponentDistToBall = 400.0;

	@Getter
	private IVector2 supportiveAttackerMovePos;

	@Getter
	private List<IVector2> supportiveAttackerFinisherDefensePos;


	@Override
	public void doCalc()
	{
		if (supportiveBlockerPos.get() != null)
		{
			supportiveAttackerMovePos = supportiveBlockerPos.get();
		} else
		{
			supportiveAttackerMovePos = calcMovePos();
		}
	}


	private IVector2 calcMovePos()
	{
		supportiveAttackerFinisherDefensePos = calcFinisherDefensePositions();
		if (bothTeamsAreReceiving())
		{
			return calcMovePosEarlyReceiver();
		} else if (skirmishInformation.get().getStrategy() == ESkirmishStrategy.FREE_BALL)
		{
			// Add a supportive attacker that helps the primary attacker in a skirmish
			return skirmishInformation.get().getSupportiveCircleCatchPos();
		}
		return calcBestSupportiveMovePos();
	}


	private List<IVector2> calcFinisherDefensePositions()
	{
		if (opponentClosestToBall.get().getBotId().isUninitializedID() ||
				opponentClosestToBall.get().getDist() > considerOpponentDistToBall)
		{
			return Collections.emptyList();
		}

		var closestAttacker = getWFrame().getBot(opponentClosestToBall.get().getBotId());
		double orientation = getBall().getPos().subtractNew(closestAttacker.getPos()).getAngle();
		var a1 = orientation + AngleMath.deg2rad(120);
		var a2 = orientation - AngleMath.deg2rad(120);

		double defDistance = Geometry.getBotRadius() * 2 + 30;
		IVector2 p1 = closestAttacker.getPos().addNew(Vector2.fromAngle(a1).scaleToNew(defDistance));
		IVector2 p2 = closestAttacker.getPos().addNew(Vector2.fromAngle(a2).scaleToNew(defDistance));

		var points = List.of(p1, p2);
		points.forEach(
				e -> getShapes(EAiShapesLayer.OFFENSE_SUPPORTIVE_ATTACKER).add(
						new DrawableCircle(e, Geometry.getBotRadius()).setColor(
								Color.DARK_GRAY))
		);

		return points;
	}


	private boolean bothTeamsAreReceiving()
	{
		var rInfo = redirectorDetectionInformation.get();
		return rInfo.isFriendlyBotReceiving() && rInfo.isOpponentReceiving();
	}


	private IVector2 calcMovePosEarlyReceiver()
	{
		IVector2 opponentRec = redirectorDetectionInformation.get().getOpponentReceiverPos();
		IVector2 opponent = getWFrame().getOpponentBots()
				.values()
				.stream()
				.min(Comparator.comparingDouble(e -> opponentRec.distanceTo(e.getPos())))
				.map(ITrackedObject::getPos)
				.orElse(opponentRec);

		IVector2 dir = getBall().getPos().subtractNew(opponent);
		return opponent.addNew(dir.scaleToNew(Geometry.getBotRadius() * 3));
	}


	private IVector2 calcBestSupportiveMovePos()
	{
		var ballPos = getBall().getPos();

		// between our goal and opponent
		IVector2 goal = Geometry.getGoalOur().bisection(ballPos);
		IVector2 dir = goal.subtractNew(ballPos).normalizeNew();

		return ballPos.addNew(dir.multiplyNew(defaultDistToBall));
	}
}
