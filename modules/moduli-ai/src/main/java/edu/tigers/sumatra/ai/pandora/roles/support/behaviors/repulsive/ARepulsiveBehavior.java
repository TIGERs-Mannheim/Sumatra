/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors.repulsive;

import static edu.tigers.sumatra.geometry.RuleConstraints.getBotToPenaltyAreaMarginStandard;
import static java.lang.Math.exp;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.ai.pandora.roles.support.SupportRole;
import edu.tigers.sumatra.drawable.DrawableArrow;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.tube.Tube;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.TrackedBot;


/**
 * This is the base for all repulsive behaviours, which uses forces to model good reaction to the opponents defensive.
 * It also provides several standard forces e.g. repel from opponent bot.
 * All repulsive behaviours are called from this class.
 */
public abstract class ARepulsiveBehavior extends ASupportBehavior
{
	@Configurable(comment = "Maximum shoot distance (for move out of goal sight)", defValue = "6000.0")
	private static double maxShootDistance = 6000;

	@Configurable(comment = "Margin around the field which robots should not move into", defValue = "200.0")
	private static double fieldMargin = 200.0;
	@Configurable(comment = "Margin around our penalty area which robots should not move into", defValue = "1000.0")
	private static double penAreaOurMargin = 1000.0;
	@Configurable(comment = "Margin around their penalty area which robots should not move into", defValue = "270.0")
	private static double penAreaTheirMargin = 270.0;

	// Sigmas
	@Configurable(comment = "[mm]", defValue = "1250.0")
	private static double sigmaFoeBot = 1250;
	@Configurable(comment = "[mm]", defValue = "2000.0")
	private static double sigmaTeamBot = 2000;
	@Configurable(comment = "[mm]", defValue = "300.0")
	private static double sigmaFieldBorderRepel = 300;
	@Configurable(comment = "[mm]", defValue = "300.0")
	private static double sigmaBallRepel = 300;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaGoalSight = 400;
	@Configurable(comment = "[mm]", defValue = "400.0")
	private static double sigmaPassLine = 400;

	// Magnitudes
	@Configurable(comment = "[mm]", defValue = "-1000.0")
	private static double magnitudeFoeBot = -1000;
	@Configurable(comment = "[mm]", defValue = "-800.0")
	private static double magnitudeTeamBot = -800;
	@Configurable(comment = "[mm]", defValue = "-3000.0")
	private static double magnitudeFieldBorderRepel = -3000;
	@Configurable(comment = "[mm]", defValue = "-2000.0")
	private static double magnitudeBallRepel = -2000;
	@Configurable(comment = "[mm]", defValue = "-2500.0")
	private static double magnitudeGoalSight = -2500;
	@Configurable(comment = "[mm]", defValue = "-2500.0")
	private static double magnitudePassLine = -2500;

	@Configurable(defValue = "50.0", comment = "Extra margin [mm] to add to forbidden areas")
	private static double extraMargin = 50.0;

	static
	{
		ConfigRegistration.registerClass("roles", ARepulsiveBehavior.class);
	}


	public ARepulsiveBehavior(final ARole role)
	{
		super(role);
	}


	@Override
	public void doEntryActions()
	{
		getRole().setNewSkill(AMoveToSkill.createMoveToSkill());
	}


	@Override
	public void doUpdate()
	{
		List<ITrackedBot> opponents = new ArrayList<>(getRole().getWFrame().getFoeBots().values());
		List<ITrackedBot> supporter = ((SupportRole) getRole()).getCurrentSupportBots().stream()
				.filter(s -> s.getBotId() != getRole().getBotID())
				.collect(Collectors.toList());

		ITrackedBot fakeBot = getRole().getBot();
		IVector2 resultingForce = Vector2.zero();

		final int numberOfLookAHeads = 10;
		for (int i = 0; i < numberOfLookAHeads; i++)
		{
			List<Force> forces = collectForces(fakeBot, supporter, opponents);
			IVector2 force = calcResultingDirection(forces, fakeBot).multiplyNew(1. / (i + 1));


			resultingForce = resultingForce.addNew(force);
			IVector2 destination = setPositionInsideAllowedArea(fakeBot.getPos(), force);
			fakeBot = TrackedBot.stubBuilder(getRole().getBotID(), getRole().getWFrame().getTimestamp())
					.withPos(destination).build();

		}
		IVector2 destination = setPositionInsideAllowedArea(getRole().getPos(), resultingForce);
		getRole().getCurrentSkill().getMoveCon().updateDestination(destination);
		getRole().getCurrentSkill().getMoveCon().updateLookAtTarget(getLookAtTarget());

		if (isDrawing())
		{
			drawGrid();
		}
	}


	private DynamicPosition getLookAtTarget()
	{
		final AObjectID objectID = getRole().getAiFrame().getTacticalField().getOffensiveStrategy().getAttackerBot()
				.map(o -> (AObjectID) getRole().getWFrame().getBot(o).getBotId())
				.orElse(BallID.instance());
		return new DynamicPosition(objectID);
	}


	abstract List<Force> collectForces(ITrackedBot affectedBot, List<ITrackedBot> supporter,
			List<ITrackedBot> opponents);


	protected List<Force> getForceRepelFromOpponentBot(List<ITrackedBot> opponents, ITrackedBot affectedBot)
	{
		return opponents.stream()
				.filter(b -> b.getPos().distanceTo(affectedBot.getPos()) < sigmaFoeBot * 3)
				.map(b -> new Force(b.getPos(), sigmaFoeBot, magnitudeFoeBot))
				.collect(Collectors.toList());
	}


	protected List<Force> getForceRepelFromTeamBot(List<ITrackedBot> supporter, ITrackedBot affectedBot)
	{
		return getForceRepelFromTeamBot(supporter, affectedBot, sigmaTeamBot, magnitudeTeamBot);
	}


	protected List<Force> getForceRepelFromTeamBot(List<ITrackedBot> supporter, ITrackedBot affectedBot, double sigma,
			double magnitude)
	{
		return supporter.stream()
				.filter(b -> b.getBotId() != affectedBot.getBotId())
				.filter(b -> b.getPos().distanceTo(affectedBot.getPos()) < sigmaTeamBot * 3)
				.map(b -> new Force(b.getPos(), sigma, magnitude))
				.collect(Collectors.toList());
	}


	protected List<Force> getForceRepelFromPassLine(ITrackedBot affectedBot)
	{
		List<Force> forces = new ArrayList<>();
		List<ARole> offensiveRoles = getRole().getAiFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE);

		for (int i = 0; i < offensiveRoles.size(); i++)
		{
			IVector2 offensiveA = offensiveRoles.get(i).getPos();
			for (int j = i + 1; j < offensiveRoles.size(); j++)
			{
				IVector2 offensiveB = offensiveRoles.get(j).getPos();
				ILineSegment passSegment = Lines.segmentFromPoints(offensiveA, offensiveB);
				IVector2 referencePoint = passSegment.closestPointOnLine(affectedBot.getPos());
				forces.add(new Force(referencePoint, sigmaPassLine, magnitudePassLine));
			}
		}
		return forces;
	}


	protected Force getForceStayInsideField(ITrackedBot affectedBot)
	{
		IVector2 referencePoint = Geometry.getField().withMargin(Geometry.getBotRadius())
				.nearestPointOutside(affectedBot.getPos());
		return new Force(referencePoint, sigmaFieldBorderRepel, magnitudeFieldBorderRepel);
	}


	protected Force getForceRepelFromBall()
	{
		return new Force(getRole().getBall().getPos(), sigmaBallRepel, magnitudeBallRepel);
	}


	protected List<Force> getForceRepelFromOffensiveGoalSight(ITrackedBot affectedBot)
	{
		if (getRole().getAiFrame().getGamestate().isIndirectFreeForUs()) // only here, because we have to pass
		{
			return Collections.emptyList();
		}
		int minDistanceOffensiveToDestination = 500;
		List<IVector2> offensivePositions = getRole().getAiFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE)
				.stream()
				.filter(r -> r.getCurrentSkill() != null && r.getCurrentSkill().getMoveCon() != null
						&& r.getCurrentSkill().getMoveCon().getDestination() != null)
				.filter(r -> r.getCurrentSkill().getMoveCon().getDestination()
						.distanceTo(r.getPos()) < minDistanceOffensiveToDestination)
				.filter(r -> r.getCurrentSkill().getMoveCon().getDestination()
						.distanceTo(Geometry.getGoalTheir().getCenter()) < maxShootDistance)
				.map(ARole::getPos)
				.collect(Collectors.toList());

		List<Force> forces = new ArrayList<>();
		for (IVector2 offensive : offensivePositions)
		{
			ILineSegment goalLine = Lines.segmentFromPoints(offensive, Geometry.getGoalTheir().getCenter());
			IVector2 referencePoint = goalLine.closestPointOnLine(affectedBot.getPos());


			forces.add(new Force(referencePoint, sigmaGoalSight, magnitudeGoalSight));
		}
		return forces;
	}


	private IVector2 calcResultingDirection(List<Force> forces, ITrackedBot affectedBot)
	{
		IVector2 resultingForce = Vector2.zero();

		for (Force f : forces)
		{
			double dist = f.mean - f.position.distanceTo(affectedBot.getPos());
			double resultingLength;
			switch (f.func)
			{
				case CONSTANT:
					resultingLength = 1;
					break;
				case LINEAR:
					resultingLength = 1 / dist;
					break;
				case EXPONENTIAL:
					resultingLength = calcExponentialFactor(f, dist);
					break;
				default:
					resultingLength = 0;
			}
			if (f.invert)
			{
				resultingLength = 1 - resultingLength;
			}
			resultingLength *= f.magnitude;

			IVector2 force = f.position.subtractNew(affectedBot.getPos()).scaleTo(resultingLength);
			if (dist > 0)
			{
				force = force.multiplyNew(-1);
			}
			resultingForce = resultingForce.addNew(force);
		}
		return resultingForce;
	}


	private double calcExponentialFactor(Force f, double dist)
	{
		return exp(-(dist * dist) / (2 * f.sigma * f.sigma));
	}


	protected IVector2 setPositionInsideAllowedArea(IVector2 currentPos, IVector2 force)
	{
		IVector2 destination = currentPos.addNew(force);

		// not too close to their penalty area
		final IPenaltyArea theirPenAreaWithMargin = Geometry.getPenaltyAreaTheir().withMargin(penAreaTheirMargin);
		if (theirPenAreaWithMargin.isPointInShapeOrBehind(destination))
		{
			destination = theirPenAreaWithMargin.projectPointOnToPenaltyAreaBorder(destination);
		}

		// not too close to our penalty area
		final IPenaltyArea ourPenAreaWithMargin = Geometry.getPenaltyAreaOur().withMargin(penAreaOurMargin);
		if (ourPenAreaWithMargin.isPointInShapeOrBehind(destination))
		{
			destination = ourPenAreaWithMargin.projectPointOnToPenaltyAreaBorder(destination);
		}

		// not too close to field borders
		destination = Geometry.getField().nearestPointInside(destination, -(fieldMargin + Geometry.getBotRadius()));

		if (!getRole().getAiFrame().getGamestate().isRunning())
		{
			// not in forbidden area around their penalty area during stop and standards
			double penAreaStandardSitMargin = getBotToPenaltyAreaMarginStandard() + Geometry.getBotRadius() + extraMargin;
			final IPenaltyArea theirPenAreaForStop = Geometry.getPenaltyAreaTheir().withMargin(penAreaStandardSitMargin);
			if (theirPenAreaForStop.isPointInShapeOrBehind(destination))
			{
				destination = theirPenAreaForStop.projectPointOnToPenaltyAreaBorder(destination);
			}

			// not inside ball placement tube
			IVector2 ballPos = getRole().getBall().getPos();
			IVector2 ballPlacementPositionForUs = getRole().getAiFrame().getGamestate().getBallPlacementPositionForUs();
			IVector2 targetBallPos = Optional.ofNullable(ballPlacementPositionForUs).orElse(ballPos);
			double penAreaStopMargin = RuleConstraints.getStopRadius() + Geometry.getBotRadius() + extraMargin;
			destination = Tube.create(ballPos, targetBallPos, penAreaStopMargin).nearestPointOutside(destination);
		}

		return destination;
	}


	private void drawGrid()
	{
		int numX = 100;
		int numY = 50;
		List<IDrawableShape> shapes = getRole().getAiFrame().getTacticalField().getDrawableShapes()
				.get(EAiShapesLayer.SUPPORT_FORCE_FIELD);
		if (!shapes.isEmpty())
		{
			return;
		}
		List<ITrackedBot> opponents = new ArrayList<>(getRole().getWFrame().getFoeBots().values());
		List<ITrackedBot> supportBots = ((SupportRole) getRole()).getCurrentSupportBots();


		for (int x = -numX / 2; x < numX / 2; x++)
		{
			for (int y = -numY / 2; y < numY / 2; y++)
			{
				IVector2 fakeBotPos = Vector2.fromXY(x * Geometry.getFieldLength() / numX,
						y * Geometry.getFieldWidth() / numY);
				BotID fakeBotID = BotID.createBotId(10, getRole().getBotID().getTeamColor());
				ITrackedBot fakeSupportBot = TrackedBot.stubBuilder(fakeBotID, getRole().getWFrame().getTimestamp())
						.withPos(fakeBotPos).build();

				supportBots.add(fakeSupportBot);
				List<Force> forces = collectForces(fakeSupportBot, supportBots, opponents);
				IVector2 direction = calcResultingDirection(forces, fakeSupportBot);
				supportBots.remove(fakeSupportBot);

				Color c = new Color(0, 0, 0, (int) (20 + 235 * SumatraMath.relative(direction.getLength(), 0, 2000)));
				shapes.add(new DrawableArrow(fakeBotPos, direction.normalizeNew().multiply(100), c, 30));
			}
		}

	}


	abstract boolean isDrawing();

}
