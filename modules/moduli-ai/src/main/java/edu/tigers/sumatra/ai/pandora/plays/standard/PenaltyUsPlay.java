package edu.tigers.sumatra.ai.pandora.plays.standard;


import com.github.g3force.configurable.Configurable;
import com.github.g3force.configurable.EConfigUnit;
import edu.tigers.sumatra.ai.common.PointChecker;
import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.APlay;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.move.MoveRole;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableRectangle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.circle.Circle;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.rectangle.Rectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.extern.log4j.Log4j2;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This play handles all remaining bots in case of a penalty for us.
 * It places them behind the ball in a random spot.
 * If there is already something there, find an available spot next to it.
 */
@Log4j2
public class PenaltyUsPlay extends APlay
{
	@Configurable(comment = "How far bots should stay away from the penaltyMark",
			defValue = "200.0", unit = EConfigUnit.DISTANCE_MM)
	private static double horizontalExtraSafetyMargin = 2000.0;
	@Configurable(comment = "How many times we try to find a valid position until giving up",
			defValue = "100", unit = EConfigUnit.COUNT)
	private static int maxTries = 100;
	@Configurable(comment = "By how much the search radius increases each try",
			defValue = "100.0", unit = EConfigUnit.DISTANCE_MM)
	private static double searchRadiusIncrease = 100.0;
	@Configurable(comment = "Toggle for printing statistics for point calculations",
			defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean calculateStatistics = false;
	@Configurable(comment = "Toggle for logging the initial destination points",
			defValue = "false", unit = EConfigUnit.BOOLEAN)
	private static boolean logInitialPoints = false;

	// values for statistics calculation
	private static int triesTotal = 0;
	private static int recalculations = 0;
	private static int triesMax = 0;

	//Map of all currently planned positions for the bots
	private Map<BotID, IVector2> botDestinations = new HashMap<>();
	// The rectangle the bots are supposed to be placed in
	private IRectangle mainConstraints;
	private PointChecker pointChecker;


	public PenaltyUsPlay()
	{
		// pass through the role name to the superclass
		super(EPlay.PENALTY_US);
		// initialize point checker
		pointChecker = new PointChecker();
		pointChecker.checkNotInPenaltyAreas();
		pointChecker.checkCustom(this::isPointWithinConstraints);
	}


	/**
	 * This is the main hook that gets called from above to decide what to do next
	 */
	@Override
	protected void doUpdateBeforeRoles()
	{
		mainConstraints = generateMainConstraints();
		updateBotPositions();
		drawRectangle(mainConstraints);
	}


	/**
	 * This method initializes the constraint rectangle we can place bots in
	 * It has the following dimensions:
	 * - height: the entire height of the field
	 * - width: from the field edge to the penalty mark
	 * - additionally, we shrink it by a safety factor and configurable margin so bots don't get placed at the edge and overlap
	 */
	private IRectangle generateMainConstraints()
	{
		// we need to build two points, we can span the rectangle over
		// first the height (from min field height to max field height)
		double minY = Geometry.getField().minY();
		double maxY = Geometry.getField().maxY();
		// then the width
		// the origin of our rectangle starts from the outer edge of our field half
		double minX = -Geometry.getFieldLength() / 2.0;
		// the maximum defaults to the penalty mark (minus a configurable safety margin)
		double maxX = Geometry.getPenaltyMarkTheir().x() - RuleConstraints.getDistancePenaltyMarkToPenaltyLine()
				- horizontalExtraSafetyMargin;
		// build an initial Rectangle
		return Rectangle
				// generate the initial rectangle
				.fromPoints(Vector2.fromXY(minX, minY), Vector2.fromXY(maxX, maxY))
				// shrink it by some length so bots don't go over field boundaries
				.withMargin(-Geometry.getBotRadius() * 2);
	}


	/**
	 * This function checks if all bots have a valid position to go to
	 * and updates them if needed
	 */
	private void updateBotPositions()
	{
		//iterate over the list of all the bots that are governed by the play
		for (MoveRole bot : findRoles(MoveRole.class))
		{
			// if the bot does not have a destination, create one
			botDestinations.putIfAbsent(bot.getBotID(), pickInitialPoint(bot));
			IVector2 destination = botDestinations.get(bot.getBotID());
			// if it is not valid, try making it valid
			if (!isPointValid(destination, bot))
			{
				destination = findValidPoint(destination, bot);
			}
			botDestinations.put(bot.getBotID(), destination);

			// set the new destination and make the robots slow (we have all the time we need and do not want to crash)
			bot.updateDestination(destination);
			bot.setVelMax(1.0);
		}
	}


	/**
	 * Finds a point near to the given position. This is chosen randomly while continuously increasing the search radius
	 */
	private IVector2 findValidPoint(IVector2 currentPosition, MoveRole bot)
	{
		for (int tries = 1; tries <= maxTries; tries++)
		{
			// create a search radius from where to pick a random point (it increases each try)
			double searchRadius = Geometry.getBotRadius() + tries * searchRadiusIncrease + Math.pow(1.1, tries);
			// pick a new point by adding random values to the current position
			IVector2 newDestination = Vector2.fromXY(
							randomDouble(-searchRadius, searchRadius), randomDouble(-searchRadius, searchRadius))
					.add(currentPosition);
			// draw debug graphics
			drawCircle(newDestination, 20);
			drawCircle(bot.getDestination(), searchRadius);
			if (isPointValid(newDestination, bot))
			{
				if (calculateStatistics)
				{
					destinationRecalculationStats(tries, bot);
				}
				return newDestination;
			}
		}
		// if no valid point has been found after a set amount of tries, throw a warning and give back the old one
		// (and the movement logic prevents a crash)
		log.warn("Unable to find valid destination for Bot {} after {} tries", bot.getBotID(), maxTries);
		return currentPosition;
	}


	/**
	 * Picks the initial point for a bot within the set constraints
	 */
	private IVector2 pickInitialPoint(MoveRole bot)
	{
		if (logInitialPoints)
		{
			log.info(
					"Picking initial point for {} at {}", bot.getBotID(), mainConstraints.nearestPointInside(bot.getPos()));
		}
		return mainConstraints.nearestPointInside(bot.getPos());
	}


	/**
	 * This function checks if a bot can be placed at the given point
	 */
	private boolean isPointValid(IVector2 point, MoveRole bot)
	{
		// check basic requirements by the point checker (inside constraints, outside penalty area)
		if (!pointChecker.allMatch(getAiFrame().getBaseAiFrame(), point, bot.getBotID()))
		{
			return false;
		}
		// check if we are already at our destination (no changing needed)
		if (bot.getPos().equals(point))
		{
			return true;
		}
		// lastly, check if the destination is far enough away from other bots
		return isPointFreeOfBotsWithMargin(point, bot);
	}


	/**
	 * Returns a random value between a and b
	 */
	private double randomDouble(final double limitA, final double limitB
	)
	{
		// check if both values are equal, if so, return that value
		if (limitA == limitB)
		{
			return limitB;
		}
		return ThreadLocalRandom.current().nextDouble(Math.min(limitA, limitB), Math.max(limitA, limitB));
	}


	private void drawRectangle(IRectangle rect)
	{
		getShapes(EAiShapesLayer.PENALTY_US_DESTINATIONS).add(new DrawableRectangle(rect));
	}


	private void drawCircle(IVector2 destination, double radius)
	{
		getShapes(EAiShapesLayer.PENALTY_US_DESTINATIONS).add(
				new DrawableCircle(Circle.createCircle(destination, radius), Color.RED));
	}


	/**
	 * Necessary as class method, to get updated mainConstraint in point checker, no touchy!
	 */
	private boolean isPointWithinConstraints(IVector2 point)
	{
		return mainConstraints.isPointInShape(point);
	}


	/**
	 * This Method calculates and prints stats on how many tries it took to find a valid destination
	 */
	private static void destinationRecalculationStats(int tries, MoveRole bot)
	{
		recalculations++;
		triesMax = Math.max(tries, triesMax);
		triesTotal += tries;
		log.info(
				"Replacement for {} finished with {} tries. (Max: {} | Avg {})",
				bot.getBotID(), tries, triesMax, triesTotal / recalculations
		);
	}


	private boolean isPointFreeOfBotsWithMargin(final IVector2 point, MoveRole activeBot)
	{
		double distance = Geometry.getBotRadius() * 5;
		return getAiFrame().getWorldFrame().getBots().values().stream()
				.filter(bot -> !bot.getBotId().equals(activeBot.getBotID()))
				.noneMatch(bot -> bot.getPosByTime(1).distanceTo(point) < distance);
	}
}