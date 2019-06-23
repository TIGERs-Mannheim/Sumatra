/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.support.behaviors;

import static java.lang.Math.min;

import java.awt.Color;
import java.util.List;
import java.util.Optional;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.EAiShapesLayer;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.support.ASupportBehavior;
import edu.tigers.sumatra.drawable.DrawableLine;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.drawable.animated.AnimatedCrosshair;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.stream.StreamUtil;


/**
 * The aim of this behavior is iff the supporter is within the pass line of our own team, to move out of this pass line
 * but pretend to receive the pass
 */
public class FakePassReceiver extends ASupportBehavior
{
	@Configurable(comment = "Defines whether this behavior is active of not", defValue = "true")
	private static boolean isActive = true;
	
	@Configurable(comment = "Min distance to both passing bots[mm]", defValue = "1300")
	private static int minDistanceToPassingBots = 1300;
	
	@Configurable(comment = "Distance between passline and FakePassReceiver[mm]", defValue = "50")
	private static int distanceToPassLine = 50;
	
	@Configurable(comment = "Max time to fakePosition[sec]", defValue = "1.0")
	private static double maxTimeToDestination = 1;
	
	static
	{
		ConfigRegistration.registerClass("roles", FakePassReceiver.class);
	}
	
	private ILineSegment passLine;
	
	
	public FakePassReceiver(final ARole role)
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
		if (!isActive)
		{
			return 0;
		}
		List<ARole> offensiveRoles = getRole().getAiFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE);
		
		Optional<ILineSegment> passSegment = StreamUtil.nonRepeatingPermutation2Fold(offensiveRoles)
				.map(a -> Lines.segmentFromPoints(a.get(0).getPos(), a.get(1).getPos()))
				.filter(this::isFakePassReceiverReasonalbeOn)
				.findFirst();
		
		if (passSegment.isPresent())
		{
			passLine = passSegment.get();
			return 1;
		}
		return 0;
	}
	
	
	private boolean isFakePassReceiverReasonalbeOn(ILineSegment segment)
	{
		IVector2 referencePoint = segment.closestPointOnLine(getRole().getPos());
		boolean isBehindPassLine = referencePoint.subtractNew(getRole().getPos()).x() > 0;
		boolean isInsidePenaltyArea = Geometry.getPenaltyAreaTheir().isPointInShapeOrBehind(referencePoint);
		boolean isReachableInTime = TrajectoryGenerator
				.generatePositionTrajectory(getRole().getBot(), referencePoint)
				.getTotalTime() <= maxTimeToDestination;
		double distToA = referencePoint.distanceTo(segment.getStart());
		double distToB = referencePoint.distanceTo(segment.getEnd());
		return min(distToA, distToB) > minDistanceToPassingBots
				&& isBehindPassLine
				&& !isInsidePenaltyArea
				&& isReachableInTime;
	}
	
	
	@Override
	public void doUpdate()
	{
		
		IVector2 intersectionPoint = passLine.closestPointOnLine(getRole().getPos());
		IVector2 direction = passLine.toLine().getOrthogonalLine().directionVector().normalizeNew();
		if (direction.x() > 0)
		{
			direction = direction.multiplyNew(-1);
		}
		IVector2 destination = intersectionPoint
				.addNew(direction.scaleToNew(Geometry.getBotRadius() + distanceToPassLine));
		
		getRole().getCurrentSkill().getMoveCon().updateDestination(destination);
		getRole().getCurrentSkill().getMoveCon().updateLookAtTarget(Geometry.getGoalTheir().getCenter());
		draw();
	}
	
	
	private void draw()
	{
		List<IDrawableShape> shapes = getRole().getAiFrame().getTacticalField().getDrawableShapes()
				.get(EAiShapesLayer.SUPPORT_ACTIVE_ROLES);
		DrawableLine line = new DrawableLine(passLine, Color.RED);
		IVector2 destination = getRole().getCurrentSkill().getMoveCon().getDestination();
		AnimatedCrosshair dest = AnimatedCrosshair.aCrazyCrosshair(destination,
				(float) Geometry.getBotRadius(),
				(float) Geometry.getBotRadius() * 2, 500,
				Color.RED,
				Color.RED,
				new Color(255, 0, 0, 0));
		shapes.add(dest);
		shapes.add(line);
	}
}
