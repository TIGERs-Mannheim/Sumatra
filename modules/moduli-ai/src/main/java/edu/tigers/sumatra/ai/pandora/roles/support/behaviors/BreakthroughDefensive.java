/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import java.awt.Color;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ITrackedObject;


/**
 * This behaviour intends to bring our supporter between the opponents defense and their penalty area.
 * It is ONLY for the breakthrough. This is necessary, because it is hard to model and may be deactivated according to
 * the opponent.
 * When the supporter is behind the defense, this behaviour should not be active.
 * The positions close to the penalty area are good for a bounced of ball or a straight pass through the penalty area.
 * The number of allowed supporter near the penalty area is restricted.
 */
public class BreakthroughDefensive extends ASupportBehavior
{
	
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean isActive = true;
	
	@Configurable(comment = "Defender width", defValue = "500")
	private static int defensiveCircleWidth = 500;
	
	@Configurable(comment = "Min gap size for attak", defValue = "500")
	private static int minGapSize = 800;
	
	@Configurable(comment = "Max number of team members at penalty area", defValue = "1")
	private static int maxNumberAtPenaltyArea = 1;
	
	static
	{
		ConfigRegistration.registerClass("roles", BreakthroughDefensive.class);
	}
	
	private IVector2 destination;
	
	
	public BreakthroughDefensive(final ARole role)
	{
		super(role);
	}
	
	
	@Override
	public void doEntryActions()
	{
		getRole().setNewSkill(AMoveToSkill.createMoveToSkill());
	}
	
	
	@Override
	public double calculateViability()
	{
		if (!isActive || !getRole().getAiFrame().getGamestate().isRunning())
		{
			return 0;
		}
		Optional<IVector2> dest = getBreakthroughDestination(getRole().getBot(),
				getRole().getWFrame().getFoeBots().values());
		if (dest.isPresent() && isBreakthroughReasonable(dest.get()))
		{
			destination = dest.get();
			double ballDiff = Math.abs(getRole().getBall().getPos().y() - getRole().getPos().y());
			return SumatraMath.relative(ballDiff, 0, Geometry.getFieldWidth());
			
		}
		return 0;
	}
	
	
	private boolean isBreakthroughReasonable(IVector2 dest)
	{
		long numberTeamRobotsAtPenaltyArea = getRole().getWFrame().getTigerBotsVisible().values().stream()
				.filter(p -> Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 5)
						.isPointInShape(p.getPos()))
				.count();
		boolean isMemberAtPenaltyAreaMissing = maxNumberAtPenaltyArea > numberTeamRobotsAtPenaltyArea;
		boolean isBallOnFoeSide = getRole().getBall().getPos().x() > 0;
		boolean isBotOnFoeSide = getRole().getPos().x() > 0;
		boolean isDestinationInsideFoe = getRole().getWFrame().getFoeBots().values().stream()
				.anyMatch(b -> dest.distanceTo(b.getPos()) < Geometry.getBotRadius());
		return isMemberAtPenaltyAreaMissing && isBallOnFoeSide && isBotOnFoeSide && !isDestinationInsideFoe;
	}
	
	
	@Override
	public void doUpdate()
	{
		if (destination != null)
		{
			getRole().getCurrentSkill().getMoveCon().updateDestination(destination);
			
			getRole().getAiFrame().getTacticalField().getDrawableShapes().get(EAiShapesLayer.SUPPORT_ACTIVE_ROLES)
					.add(new DrawableCircle(destination, Geometry.getBotRadius(), Color.RED));
		}
	}
	
	
	private Optional<IVector2> getBreakthroughDestination(ITrackedBot affectedBot, Collection<ITrackedBot> opponents)
	{
		List<IVector2> hedgehogDefenders = calcHedgehogDefenders(opponents).stream()
				.sorted(Comparator
						.comparingDouble(b -> Geometry.getGoalTheir().getCenter().subtractNew(b).getAngle()))
				.collect(Collectors.toList());
		
		Optional<IVector2> midOfGap = findClosestGap(hedgehogDefenders, affectedBot);
		
		return midOfGap.map(iVector2 -> findPointOnPenaltyAreaAccoringToGap(iVector2, affectedBot));
	}
	
	
	private Set<IVector2> calcHedgehogDefenders(Collection<ITrackedBot> opponents)
	{
		double hedgehogDistance = opponents.stream()
				.filter(b -> b.getBotId() != getRole().getAiFrame().getKeeperFoeId())
				.mapToDouble(b -> b.getPos().distanceTo(Geometry.getGoalTheir().getCenter()))
				.min().orElse(0);
		
		Set<IVector2> hedgehogDefenders = opponents.stream()
				.filter(b -> b.getBotId() != getRole().getAiFrame().getKeeperFoeId())
				.filter(b -> isHedgehogDefender(b.getPos(), hedgehogDistance))
				.map(ITrackedObject::getPos)
				.collect(Collectors.toSet());
		
		hedgehogDefenders.add(Vector2.fromY(hedgehogDistance).add(Geometry.getGoalTheir().getCenter()));
		hedgehogDefenders.add(Vector2.fromY(-hedgehogDistance).add(Geometry.getGoalTheir().getCenter()));
		
		return hedgehogDefenders;
	}
	
	
	private boolean isHedgehogDefender(IVector2 defender, double hedgehogDistance)
	{
		boolean isInCircle = defender.distanceTo(Geometry.getGoalTheir().getCenter()) < hedgehogDistance
				+ defensiveCircleWidth;
		boolean isNearPenaltyArea = Geometry.getPenaltyAreaTheir().withMargin(defensiveCircleWidth)
				.isPointInShape(defender);
		return isInCircle || isNearPenaltyArea;
	}
	
	
	private Optional<IVector2> findClosestGap(List<IVector2> sortedhedgehogDefenders, ITrackedBot affectedBot)
	{
		
		int index = calcIndexOfLastConsideredDefender(sortedhedgehogDefenders, affectedBot);
		
		IVector2 botA = sortedhedgehogDefenders.get(Math.min(index, sortedhedgehogDefenders.size() - 1));
		IVector2 botB = sortedhedgehogDefenders.get(index - 1);
		IVector2 botC = sortedhedgehogDefenders.get(Math.max(index - 2, 0));
		
		Optional<IVector2> midOfGap = Optional.empty();
		if (botA != botB && botA.distanceTo(botB) > minGapSize)
		{
			midOfGap = Optional.of(botA.subtractNew(botB).multiply(0.5).add(botB));
		}
		if (botB != botC && botB.distanceTo(botC) > minGapSize)
		{
			IVector2 secondMidGap = botC.subtractNew(botB).multiply(0.5).add(botB);
			if (midOfGap.isPresent())
			{
				IVector2 intersection1 = Lines.lineFromPoints(Geometry.getGoalTheir().getCenter(), midOfGap.get())
						.closestPointOnLine(affectedBot.getPos());
				IVector2 intersection2 = Lines.lineFromPoints(Geometry.getGoalTheir().getCenter(), secondMidGap)
						.closestPointOnLine(affectedBot.getPos());
				if (intersection2.distanceTo(affectedBot.getPos()) < intersection1.distanceTo(affectedBot.getPos()))
				{
					midOfGap = Optional.of(secondMidGap);
				}
			} else
			{
				midOfGap = Optional.of(secondMidGap);
			}
		}
		return midOfGap;
	}
	
	
	private int calcIndexOfLastConsideredDefender(List<IVector2> sortedHedgehogDefenders, ITrackedBot affectedBot)
	{
		ILineSegment goalLine = Lines.segmentFromPoints(Geometry.getGoalTheir().getCenter(), affectedBot.getPos());
		double distToGoalLine = Double.MAX_VALUE;
		int index;
		
		for (index = 0; index < sortedHedgehogDefenders.size(); index++)
		{
			double dist = goalLine.distanceTo(sortedHedgehogDefenders.get(index));
			if (dist > distToGoalLine)
			{
				break;
			}
			distToGoalLine = dist;
		}
		return index;
	}
	
	
	private IVector2 findPointOnPenaltyAreaAccoringToGap(IVector2 midOfGap, ITrackedBot affectedBot)
	{
		List<IVector2> intersections = Geometry.getPenaltyAreaTheir().withMargin(Geometry.getBotRadius() * 1.5)
				.lineIntersections(Lines.lineFromPoints(midOfGap, affectedBot.getPos()));
		
		Optional<IVector2> newDestination = intersections.stream()
				.sorted(Comparator.comparingDouble(b -> b.distanceTo(affectedBot.getPos())))
				.findFirst();
		
		return newDestination.orElse(affectedBot.getPos());
	}
	
	
	public static int getMaxNumberAtPenaltyArea()
	{
		return maxNumberAtPenaltyArea;
	}
}
