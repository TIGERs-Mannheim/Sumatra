/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.metis.offense.action.EActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionViability;
import edu.tigers.sumatra.ai.metis.offense.action.RatedOffensiveAction;
import edu.tigers.sumatra.drawable.DrawableAnnotation;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableQuadrilateral;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.quadrilateral.IQuadrilateral;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.RequiredArgsConstructor;

import java.awt.Color;
import java.util.Optional;


/**
 * Handles Chop Tricks aka Hackentrick
 */
@RequiredArgsConstructor
public class ChopTrickActionMove extends AOffensiveActionMove
{

	@Configurable(defValue = "0.2")
	private static double hadBallContactTime = 0.2;

	@Configurable(defValue = "15.0", comment = "angle in degree")
	private static double maxOrientationError = 15;

	@Configurable(defValue = "-2600", comment = "extended range in x-direction to check for \"close to op. goal\"")
	private static double additionalXExtend = -2600;

	@Configurable(defValue = "600.0", comment = "opponents considered close to me")
	private static double radiusToCheckForCloseOpponents = 600;

	@Configurable(defValue = "2", comment = "number of opponents bot needed to force true viability")
	private static int numberOfOpponentsToCheckFor = 2;

	@Configurable(defValue = "true")
	private static boolean activateChopTrick = true;


	private OffensiveActionViability calcViability(BotID botId)
	{
		if (!getAiFrame().getGameState().isRunning() || getBall().getVel().getLength() > 0.5 || !activateChopTrick)
		{
			return new OffensiveActionViability(EActionViability.FALSE, 0.0);
		}

		var botPos = getWFrame().getBot(botId).getPos();

		// 1. bot looking towards our goal (clearly)
		var botOrient = Vector2.fromAngle(getWFrame().getBot(botId).getOrientation());
		var angle = Vector2.fromX(-1).angleToAbs(botOrient).orElse(0.0);
		boolean isBotLookingToOurGoal = angle < AngleMath.deg2rad(maxOrientationError);

		// 2. ball contact
		boolean botHasBallContact = getWFrame().getBot(botId).hadBallContact(hadBallContactTime);

		// 3. close to enemy goal (x - extended penArea Shape)
		IQuadrilateral quad = getCloseToOpponentGoalQuad();
		getShapes(EAiShapesLayer.CHOP_TRICK).add(
				new DrawableQuadrilateral(quad, new Color(255, 52, 52, 100)).setFill(true));
		boolean botIsCloseToOpponentGoal =
				quad.isPointInShape(botPos) && quad.isPointInShape(getWFrame().getBall().getPos());

		// 4. Path blocked by Robots quad
		IQuadrilateral quad1 = getQuad(1, botPos);
		IQuadrilateral quad2 = getQuad(-1, botPos);
		boolean quad1Blocked = getWFrame().getOpponentBots()
				.values().stream()
				.anyMatch(e -> quad1.isPointInShape(e.getPos()));
		boolean quad2Blocked = getWFrame().getOpponentBots()
				.values().stream()
				.anyMatch(e -> quad2.isPointInShape(e.getPos()));

		getShapes(EAiShapesLayer.CHOP_TRICK).add(
				new DrawableQuadrilateral(quad1, new Color(200, 200, 200, 100)).setFill(true));
		getShapes(EAiShapesLayer.CHOP_TRICK).add(
				new DrawableQuadrilateral(quad2, new Color(200, 200, 200, 100)).setFill(true));

		ICircle cirlce = Circle.createCircle(botPos, radiusToCheckForCloseOpponents);
		getShapes(EAiShapesLayer.CHOP_TRICK).add(new DrawableCircle(cirlce, Color.ORANGE));

		boolean opponentsNearMe = getWFrame().getOpponentBots().values().stream()
				.filter(e -> cirlce.isPointInShape(e.getPos()))
				.count() >= numberOfOpponentsToCheckFor;

		String sb = "closeToOppGoal: " + botIsCloseToOpponentGoal
				+ "\nballContact: " + botHasBallContact
				+ "\nisBotLookingToOurGoal: " + isBotLookingToOurGoal
				+ "\nquad1 Free: " + !quad1Blocked
				+ "\nquad2 Free: " + !quad2Blocked
				+ "\nopponents near: " + opponentsNearMe;
		getShapes(EAiShapesLayer.CHOP_TRICK)
				.add(new DrawableAnnotation(botPos, sb));

		double score = 0.0;
		if (botIsCloseToOpponentGoal && botHasBallContact && isBotLookingToOurGoal)
		{
			score += 0.3;
			if (!quad1Blocked)
			{
				score += 0.3;
			}
			if (!quad2Blocked)
			{
				score += 0.3;
			}
			score = Math.min(1.0, score);
			if (opponentsNearMe)
			{
				return new OffensiveActionViability(EActionViability.TRUE, score);
			}
		}
		return new OffensiveActionViability(EActionViability.PARTIALLY, score);
	}


	private IQuadrilateral getCloseToOpponentGoalQuad()
	{
		var p1 = Geometry.getGoalTheir().getLeftPost().subtractNew(Vector2.fromY(100));
		var p2 = Geometry.getGoalTheir().getRightPost().subtractNew(Vector2.fromY(-100));
		var p3 = p2.addNew(Vector2.fromX(-Geometry.getGoalTheir().getRectangle().xExtent()))
				.addNew(Vector2.fromX(additionalXExtend));
		var p4 = p1.addNew(Vector2.fromX(-Geometry.getGoalTheir().getRectangle().xExtent()))
				.addNew(Vector2.fromX(additionalXExtend));
		return Quadrilateral.fromCorners(p1, p2, p3, p4);
	}


	private IQuadrilateral getQuad(double multiplier, IVector2 botPos)
	{
		var p1 = botPos.addNew(Vector2.fromY(multiplier * Geometry.getBotRadius()));
		var p2 = botPos.addNew(Vector2.fromY(multiplier * Geometry.getBotRadius() * 2));
		var p3 = Geometry.getGoalTheir().getGoalLine().closestPointOnPath(p2)
				.addNew(Vector2.fromY(multiplier * 200));
		var p4 = Geometry.getGoalTheir().getGoalLine().closestPointOnPath(p1)
				.addNew(Vector2.fromY(-multiplier * Geometry.getBotRadius()));
		return Quadrilateral.fromCorners(p1, p2, p3, p4);
	}


	@Override
	public Optional<RatedOffensiveAction> calcAction(BotID botId)
	{
		return Optional.of(RatedOffensiveAction.buildChopTrick(
				EOffensiveActionMove.CHOP_TRICK,
				calcViability(botId)));
	}
}
