/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.KickSpeedFactory;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.vector.IVector;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.time.TimestampTimer;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static edu.tigers.sumatra.pathfinder.TrajectoryGenerator.generatePositionTrajectory;


/**
 * Calculate fallback pass targets for the keeper
 */
@RequiredArgsConstructor
public class KeeperPassTargetCalc extends ACalculator
{
	@Configurable(comment = "Number of targets to generate per frame", defValue = "10")
	private static int targetsPerFrame = 10;

	@Configurable(comment = "Hysteresis [s] for choosing better target", defValue = "0.1")
	private static double hysteresis = 0.1;

	@Configurable(comment = "Angle [deg] for possible pass target generation", defValue = "90")
	private static double angleRangeDeg = 90;

	@Configurable(comment = "Opponent ball dist to chill", defValue = "1000.0")
	private static double minOpponentBotDistToChill = 1000;
	@Configurable(comment = "[mm] Roll distance after last touchdown of keeper chip", defValue = "500.0")
	private static double minRollDistanceAfterChip = 500;


	private final PassFactory passFactory = new PassFactory();
	private final KickSpeedFactory kickSpeedFactory = new KickSpeedFactory();
	private final Supplier<EBallResponsibility> ballResponsibility;
	private final Supplier<BotDistance> opponentClosestToBall;
	private final TimestampTimer heldBallTimer = new TimestampTimer(RuleConstraints.getKeeperHeldBallPeriod());
	private SlackBot lastPassTarget;
	@Getter
	private Pass keeperPass;


	@Override
	protected boolean isCalculationNecessary()
	{
		return getAiFrame().getGameState().isRunning() && getWFrame().getBot(getAiFrame().getKeeperId()) != null
				&& ballResponsibility.get() == EBallResponsibility.KEEPER;
	}


	@Override
	protected void reset()
	{
		keeperPass = null;
		heldBallTimer.reset();
	}


	@Override
	public void doCalc()
	{
		passFactory.update(getWFrame());
		heldBallTimer.update(getWFrame().getTimestamp());
		double kept =
				RuleConstraints.getKeeperHeldBallPeriod() - heldBallTimer.getRemainingTime(getWFrame().getTimestamp());
		var lastBestTarget = Optional.ofNullable(lastPassTarget)
				.map(SlackBot::getTarget)
				.map(this::findFastestTiger);
		var searchCircle = createSearchCircle();
		var searchAngles = calculateSearchAngles();
		var ratedTargets = generateTargets(searchCircle, searchAngles).stream()
				.map(this::findFastestTiger)
				.filter(Objects::nonNull)
				.toList();
		lastPassTarget = ratedTargets.stream()
				.max(Comparator.comparing(s -> s.slackTime))
				.filter(p -> lastBestTarget.map(t -> p.slackTime > t.slackTime + hysteresis * kept).orElse(true))
				.orElse(lastBestTarget.orElse(null));

		if (isBallDangerous())
		{
			passFactory.setAimingTolerance(0.6);
		} else
		{
			passFactory.setAimingTolerance(0.0);
		}
		keeperPass = Optional.ofNullable(lastPassTarget).map(this::createKeeperPass).orElse(null);

		drawShapes(ratedTargets, searchCircle, searchAngles);
	}


	private Pass createKeeperPass(SlackBot passTarget)
	{
		var keeper = getWFrame().getBot(getAiFrame().getKeeperId());
		passFactory.setMaxReceivingBallSpeed(Double.POSITIVE_INFINITY);

		if (kickSpeedFactory.maxChip(keeper) <= 0)
		{
			return passFactory.straight(
					getBall().getPos(),
					passTarget.target,
					getAiFrame().getKeeperId(),
					passTarget.bot.getBotId(),
					EBallReceiveMode.RECEIVE
			).orElseThrow();
		}

		return passFactory.chip(
				getBall().getPos(),
				passTarget.target,
				getAiFrame().getKeeperId(),
				passTarget.bot.getBotId(),
				EBallReceiveMode.RECEIVE
		).orElseThrow(); // with infinite max receiving speed, this should never fail
	}


	private ICircle createSearchCircle()
	{
		var keeper = getWFrame().getBot(getAiFrame().getKeeperId());
		if (kickSpeedFactory.maxChip(keeper) <= 0)
		{
			return Circle.createCircle(getBall().getPos(), Geometry.getFieldLength() * 2. / 3.);
		}
		var chipTrajectory = getBall().getChipConsultant().getChipTrajectory(kickSpeedFactory.maxChip(keeper));
		var distanceChip = chipTrajectory.getTouchdownLocations().stream()
				.mapToDouble(IVector::getLength).max()
				.orElseThrow();
		return Circle.createCircle(
				getBall().getPos(),
				SumatraMath.min(Geometry.getFieldLength() * 2. / 3., distanceChip + minRollDistanceAfterChip)
		);
	}


	private AngleRangeLimited calculateSearchAngles()
	{
		IVector2 ballPos = getBall().getPos();
		double yLimit = Geometry.getGoalOur().getWidth() / 4.0;
		double xLimit = Geometry.getFieldHalfOur().minX() + Geometry.getPenaltyAreaDepth() * 2. / 3.;
		if (ballPos.x() > xLimit || Math.abs(ballPos.y()) < yLimit)
		{
			return new AngleRangeLimited(angleRangeDeg, angleRangeDeg / 2.);
		} else if (ballPos.y() > 0)
		{
			return new AngleRangeLimited(angleRangeDeg / 2., 0.0);
		} else
		{
			return new AngleRangeLimited(-angleRangeDeg / 2., 0.0);
		}
	}


	private List<IVector2> generateTargets(ICircle searchCircle, AngleRangeLimited searchAngles)
	{
		List<IVector2> targets = new ArrayList<>(targetsPerFrame);
		Random rnd = new Random(getWFrame().getTimestamp());

		for (int i = 0; i < targetsPerFrame; i++)
		{
			var angle = AngleMath.deg2rad(rnd.nextDouble() * searchAngles.angleFactor() - searchAngles.angleShift());
			var target = searchCircle.center().addNew(Vector2.fromAngleLength(angle, searchCircle.radius()));
			if (Geometry.getField().withMargin(-2 * Geometry.getBotRadius()).isPointInShape(target))
			{
				targets.add(target);
			}
		}
		return targets;
	}


	private SlackBot findFastestTiger(IVector2 target)
	{
		double minOpponentArrivalTime = getWFrame().getOpponentBots().values().stream()
				.mapToDouble(bot -> generatePositionTrajectory(bot, target).getTotalTime())
				.min()
				.orElse(1000000);
		return getWFrame().getTigerBotsVisible().values().stream()
				.filter(bot -> !bot.getBotId().equals(getAiFrame().getKeeperId()))
				.map(bot -> new SlackBot(
						bot,
						target,
						minOpponentArrivalTime - generatePositionTrajectory(bot, target).getTotalTime()
				))
				.max(Comparator.comparingDouble(bot -> bot.slackTime))
				.orElse(null);
	}


	private void drawShapes(final List<SlackBot> targetPoints, ICircle searchCircle, AngleRangeLimited searchAngles)
	{
		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.KEEPER_BEHAVIOR);

		shapes.add(new DrawableCircle(searchCircle));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				searchCircle.center(),
				Vector2.fromAngleLength(
						AngleMath.deg2rad(searchAngles.angleFactor - searchAngles.angleShift),
						searchCircle.radius()
				)
		)));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(
				searchCircle.center(),
				Vector2.fromAngleLength(AngleMath.deg2rad(-searchAngles.angleShift), searchCircle.radius())
		)));

		if (lastPassTarget != null)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(lastPassTarget.target, 50), Color.green).setFill(true));
			shapes.add(new DrawableAnnotation(lastPassTarget.target, String.format("%.5f", lastPassTarget.slackTime)));
			shapes.add(new DrawableLine(lastPassTarget.target, lastPassTarget.bot.getPos(), Color.green));
		}

		for (var slackBot : targetPoints)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(slackBot.target, 40), Color.red));
			shapes.add(new DrawableAnnotation(slackBot.target, String.format("%.5f", slackBot.slackTime)));
		}
	}


	private boolean isBallDangerous()
	{
		return opponentClosestToBall.get().getDist() < minOpponentBotDistToChill
				&& !Geometry.getPenaltyAreaOur().isPointInShape(getWFrame().getBall().getPos());
	}


	private record AngleRangeLimited(double angleFactor, double angleShift)
	{
	}

	@AllArgsConstructor
	@Data
	private static class SlackBot
	{
		ITrackedBot bot;
		IVector2 target;
		double slackTime;
	}
}
