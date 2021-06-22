/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.keeper;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.pass.KickOrigin;
import edu.tigers.sumatra.ai.metis.pass.rating.RatedPass;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.LineMath;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static edu.tigers.sumatra.pathfinder.TrajectoryGenerator.generatePositionTrajectory;


/**
 * Calculate fallback pass targets for the keeper
 */
@RequiredArgsConstructor
public class KeeperPassTargetCalc extends ACalculator
{
	@Configurable(comment = "Distance to pass targets", defValue = "5000.0")
	private static double passDistance = 5000.0;

	@Configurable(comment = "Number of targets to generate per frame", defValue = "10")
	private static int targetsPerFrame = 10;

	@Configurable(comment = "Hysteresis [s] for choosing better target", defValue = "0.2")
	private static double hysteresis = 0.2;

	@Configurable(comment = "Angle [deg] for possible pass target generation", defValue = "90")
	private static double angleRangeDeg = 90;

	@Configurable(comment = "Opponent ball dist to chill", defValue = "1000.0")
	private static double minOpponentBotDistToChill = 1000;

	@Configurable(comment = "Distance [mm] from opponent kicker to ball where it gets safe for the keeper to get the ball", defValue = "450")
	private static double opponentBotBallPossessionDistance = 450;

	@Configurable(comment = "Margin [mm] on penalty Area where keeper can still get the ball", defValue = "200")
	private static double getBallPenaltyAreaMargin = 200;

	@Configurable(comment = "Margin [mm] on penalty Area where keeper can safely get the ball", defValue = "-50")
	private static double safeBallPenaltyAreaMargin = -50;


	private final PassFactory passFactory = new PassFactory();
	private SlackBot lastPassTarget;

	private final Supplier<Map<KickOrigin, RatedPass>> selectedPasses;
	private final Supplier<BotDistance> opponentClosestToBall;

	@Getter
	private Pass keeperPass;


	@Override
	protected boolean isCalculationNecessary()
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos())
				&& getWFrame().getBot(getAiFrame().getKeeperId()) != null
				&& ballCanBePassedOutOfPenaltyArea();
	}


	@Override
	protected void reset()
	{
		keeperPass = null;
	}


	@Override
	public void doCalc()
	{
		passFactory.update(getWFrame());

		var lastBestTarget = Optional.ofNullable(lastPassTarget)
				.map(SlackBot::getTarget)
				.map(this::findFastestTiger);
		var ratedTargets = generateTargets().stream()
				.map(this::findFastestTiger)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		lastPassTarget = ratedTargets.stream()
				.max(Comparator.comparing(s -> s.slackTime))
				.filter(p -> lastBestTarget.map(t -> p.slackTime > t.slackTime + hysteresis).orElse(true))
				.orElse(lastBestTarget.orElse(null));

		if (isBallDangerous())
		{
			passFactory.setAimingTolerance(0.6);
			keeperPass = passFactory.chip(
					getBall().getPos(),
					getChipPosForDangerousSituation(),
					getAiFrame().getKeeperId(),
					BotID.noBot());
		} else
		{
			passFactory.setAimingTolerance(0.0);
			keeperPass = findBestPassTargetForKeeper()
					.orElse(Optional.ofNullable(lastPassTarget)
							.map(s -> passFactory
									.chip(getBall().getPos(), s.target, getAiFrame().getKeeperId(), s.bot.getBotId()))
							.orElse(null));
		}

		drawShapes(ratedTargets);
	}


	/**
	 * Go through the normal pass target and find one that is far away enough to be eligible.
	 *
	 * @return
	 */
	private Optional<Pass> findBestPassTargetForKeeper()
	{
		ICircle passCircle = Circle.createCircle(Geometry.getGoalOur().getCenter(), passDistance);
		return selectedPasses.get().values().stream()
				.map(RatedPass::getPass)
				.filter(pass -> !passCircle.isPointInShape(pass.getKick().getTarget()))
				.findFirst();
	}


	private List<IVector2> generateTargets()
	{
		List<IVector2> targets = new ArrayList<>(targetsPerFrame);
		Random rnd = new Random(getWFrame().getTimestamp());
		for (int i = 0; i < targetsPerFrame; i++)
		{
			var angle = AngleMath.deg2rad(rnd.nextDouble() * angleRangeDeg - angleRangeDeg / 2);
			targets.add(Geometry.getGoalOur().getCenter().addNew(Vector2.fromAngleLength(angle, passDistance)));
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
				.map(bot -> new SlackBot(bot,
						target,
						minOpponentArrivalTime - generatePositionTrajectory(bot, target).getTotalTime()))
				.max(Comparator.comparingDouble(bot -> bot.slackTime))
				.orElse(null);
	}


	private void drawShapes(final List<SlackBot> targetPoints)
	{
		List<IDrawableShape> shapes = getShapes(EAiShapesLayer.AI_KEEPER);

		shapes.add(new DrawableCircle(Circle.createCircle(Geometry.getGoalOur().getCenter(), passDistance)));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(Geometry.getGoalOur().getCenter(),
				Vector2.fromAngleLength(AngleMath.deg2rad(angleRangeDeg / 2), passDistance))));
		shapes.add(new DrawableLine(Lines.segmentFromOffset(Geometry.getGoalOur().getCenter(),
				Vector2.fromAngleLength(AngleMath.deg2rad(-angleRangeDeg / 2), passDistance))));

		if (lastPassTarget != null)
		{
			shapes.add(new DrawableCircle(Circle.createCircle(lastPassTarget.target, 50), Color.green).setFill(true));
			shapes.add(new DrawableAnnotation(lastPassTarget.target, String.format("%.5f", lastPassTarget.slackTime)));
			shapes.add(new DrawableLine(Lines.segmentFromPoints(lastPassTarget.target, lastPassTarget.bot.getPos()),
					Color.green));
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


	private IVector2 getChipPosForDangerousSituation()
	{
		IVector2 ballPos = getWFrame().getBall().getPos();
		var bot = getWFrame().getBot(getAiFrame().getKeeperId());
		if (ballPos.x() <= bot.getPos().x())
		{
			return ballPos.addNew(Vector2.fromX(4000));
		}
		return LineMath.stepAlongLine(ballPos, bot.getPos(), -3000);
	}


	private boolean ballCanBePassedOutOfPenaltyArea()
	{
		return isBallInPenaltyArea(getBallPenaltyAreaMargin)
				&& isOpponentSafe()
				&& isBallStill()
				&& isBotNotBetweenBallAndKeeper();
	}


	private boolean isBallSafe()
	{
		return isBallStill() && isBallInPenaltyArea(safeBallPenaltyAreaMargin);
	}


	private boolean isBallInPenaltyArea(final double getBallPenaltyAreaMargin)
	{
		return Geometry.getPenaltyAreaOur().getRectangle().isPointInShape(getBall().getPos(), getBallPenaltyAreaMargin);
	}


	private boolean isBallStill()
	{
		return getBall().getVel().getLength() < 0.1;
	}


	private boolean isBotNotBetweenBallAndKeeper()
	{
		var keeperBot = getWFrame().getBot(getAiFrame().getKeeperId());
		ILineSegment keeperBallLine = Lines.segmentFromPoints(keeperBot.getPos(), getBall().getPos());
		return getWFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(keeperBot.getBotId()))
				.map(ITrackedObject::getPos)
				.noneMatch(pos -> keeperBallLine.distanceTo(pos) < Geometry.getBotRadius());
	}


	private boolean isOpponentSafe()
	{
		return isBallSafeFromOpponent() || isOpponentBlockedByDefenders();
	}


	private boolean isBallSafeFromOpponent()
	{
		return isBallSafe() || opponentClosestToBall.get().getDist() > opponentBotBallPossessionDistance;
	}


	private boolean isOpponentBlockedByDefenders()
	{
		var opponentBot = opponentClosestToBall.get().getBotId();
		if (!opponentBot.isBot())
		{
			return false;
		}

		IVector2 closestOpponentKicker = getWFrame().getBot(opponentBot).getBotKickerPos();

		for (IVector2 goalLinePos : Geometry.getGoalOur().getLineSegment().getSteps(Geometry.getBallRadius() * 2))
		{
			ILineSegment testLine = Lines.segmentFromPoints(goalLinePos, closestOpponentKicker);
			boolean lineBlocked = getWFrame().getTigerBotsAvailable().values().stream()
					.filter(bot -> bot.getBotId() != getAiFrame().getKeeperId())
					.anyMatch(bot -> Circle.createCircle(bot.getPos(), Geometry.getBotRadius() + 5)
							.isIntersectingWithLine(testLine));

			getAiFrame().getShapeMap().get(EAiShapesLayer.AI_KEEPER)
					.add(new DrawableLine(testLine, lineBlocked ? Color.GREEN : Color.RED).setStrokeWidth(1));
			if (!lineBlocked)
			{
				return false;
			}
		}

		return true;
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
