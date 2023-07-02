/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.kicking;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.botmanager.botskills.data.EKickerDevice;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.RuleConstraints;
import edu.tigers.sumatra.math.line.Lines;
import edu.tigers.sumatra.math.quadrilateral.IQuadrilateral;
import edu.tigers.sumatra.math.quadrilateral.Quadrilateral;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;
import java.util.Optional;


/**
 * Checks if its reasonable to chip the ball from a source with a certain velocity.
 * This includes touchdown validity and obstacles.
 */
public class ChipKickFactory
{
	@Configurable(defValue = "50.0", comment = "Front width of chip kick detection quadrilateral")
	private static double offsetWidthFront = 50.0;

	@Configurable(defValue = "65.0", comment = "Back width of chip kick detection quadrilateral")
	private static double offsetWidthBack = 65.0;

	@Configurable(defValue = "0.25", comment = "Prediction time for robot obstacles.")
	private static double botPredictionTime = 0.25;


	static
	{
		ConfigRegistration.registerClass("metis", ChipKickFactory.class);
	}


	public IVector3 speedToVel(double direction, double speed)
	{
		return Geometry.getBallFactory().createChipConsultant().speedToVel(direction, speed);
	}


	public boolean reasonable(Pass chipPass, Collection<ITrackedBot> bots)
	{
		if (chipPass.getKick().getKickParams().getDevice() != EKickerDevice.CHIP)
		{
			throw new IllegalStateException();
		}
		var dir = chipPass.getKick().getTarget().subtractNew(chipPass.getKick().getSource()).getAngle();
		var kickVel = speedToVel(dir, chipPass.getKick().getKickParams().getKickSpeed());
		return reasonable(chipPass.getKick().getSource(), kickVel, bots);
	}


	public boolean reasonable(IVector2 source, IVector3 kickVel, Collection<ITrackedBot> bots)
	{
		return getObstacleExclusionArea(source, kickVel)
				.map(quad -> botsInExclusionArea(bots, quad))
				.orElse(false);
	}


	private Optional<IQuadrilateral> getObstacleExclusionArea(IVector2 source, IVector3 kickVel)
	{
		return getFirstTouchDown(source, kickVel)
				.flatMap(firstTouchDown -> obstacleExclusionArea(source, kickVel, firstTouchDown));
	}


	private boolean botsInExclusionArea(Collection<ITrackedBot> bots, IQuadrilateral quad)
	{
		return bots.stream().anyMatch(bot -> isBotInQuad(quad, bot));
	}


	private Optional<IQuadrilateral> obstacleExclusionArea(IVector2 source, IVector3 kickVel, IVector2 firstTouchDown)
	{
		var robotHeight = RuleConstraints.getMaxRobotHeight();
		var chipDist = source.distanceTo(firstTouchDown);
		var margin = Geometry.getBotRadius();
		var minDist = Geometry.getBallFactory().createChipConsultant()
				.getMinimumDistanceToOverChip(kickVel.getLength(), robotHeight);
		var offset = minDist + margin;
		if (chipDist - 2 * offset < 0)
		{
			// we can't over-chip anything if the "chip above kickingRobot height is smaller 0"
			return Optional.empty();
		}

		IQuadrilateral quad = Quadrilateral.isoscelesTrapezoid(
				source.addNew(kickVel.getXYVector().scaleToNew(offset)),
				offsetWidthFront + 2 * margin,
				firstTouchDown.subtractNew(kickVel.getXYVector().scaleToNew(offset)),
				offsetWidthBack + 2 * margin
		);
		return Optional.of(quad);
	}


	private Optional<IVector2> getFirstTouchDown(IVector2 source, IVector3 kickVel)
	{
		return Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(source, kickVel.multiplyNew(1000))
				.getTouchdownLocations()
				.stream()
				.findFirst();
	}


	private boolean isBotInQuad(IQuadrilateral quad, ITrackedBot bot)
	{
		IVector2 futureBotPos = bot.getPosByTime(botPredictionTime);
		return isSegmentTouchingQuad(quad, bot.getPos(), futureBotPos);
	}


	private boolean isSegmentTouchingQuad(IQuadrilateral quad, IVector2 p1, IVector2 p2)
	{
		return quad.isPointInShape(p1)
				|| quad.isPointInShape(p2)
				|| quad.isIntersectingWithPath(Lines.segmentFromPoints(p1, p2));
	}
}
