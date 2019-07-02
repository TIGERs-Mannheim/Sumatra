/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath.ReceiveData;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.ball.trajectory.ABallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class PassInterceptionRater implements IPassRater
{
	@Configurable(comment = "Lower distance bound for alternative pass rating", defValue = "100.0")
	private static double passRatingLowerDist = 100.0;
	
	@Configurable(comment = "Upper distance bound for alternative pass rating", defValue = "3000.0")
	private static double passRatingUpperDist = 3000.0;
	
	private Collection<ITrackedBot> consideredBots;
	
	private final EPassInterceptionRaterMode mode;
	
	static
	{
		ConfigRegistration.registerClass("metis", PassInterceptionRater.class);
	}
	
	
	public PassInterceptionRater(Collection<ITrackedBot> consideredBots)
	{
		this.consideredBots = consideredBots;
		this.mode = EPassInterceptionRaterMode.DEFAULT;
	}
	
	
	public PassInterceptionRater(Collection<ITrackedBot> consideredBots, EPassInterceptionRaterMode mode)
	{
		this.consideredBots = consideredBots;
		this.mode = mode;
	}
	
	
	@Override
	public double rateStraightPass(
			final IVector2 passOrigin,
			final IVector2 passTarget)
	{
		ABallTrajectory passTrajectory = straightBallTrajectory(passOrigin, passTarget);
		
		return rateTrajectory(passTrajectory, consideredBots, passTarget, 0.0);
	}
	
	
	@Override
	public double rateChippedPass(
			final IVector2 passOrigin,
			final IVector2 passTarget,
			final double maxChipSpeed)
	{
		ABallTrajectory passTrajectory = chippedBallTrajectory(passOrigin, passTarget, maxChipSpeed);
		
		Collection<ITrackedBot> filteredBots = filterPossibleInterceptors(
				passOrigin,
				passTrajectory);
		
		List<IVector2> touchdown = passTrajectory.getTouchdownLocations();
		double tStart = 0;
		if (!touchdown.isEmpty())
		{
			tStart = passTrajectory.getTimeByPos(touchdown.get(0));
		}
		return rateTrajectory(passTrajectory, filteredBots, passTarget, tStart);
	}
	
	
	private double rateTrajectory(final ABallTrajectory passTrajectory,
			final Collection<ITrackedBot> consideredBots, final IVector2 passTarget, double tStart)
	{
		return DefenseMath
				.calcReceiveRatingsForRestrictedStartAndEnd(passTrajectory, consideredBots, passRatingUpperDist, passTarget,
						tStart)
				.stream()
				.min(Comparator.comparingDouble(ReceiveData::getDistToBallCurve))
				.map(ReceiveData::getDistToBallCurve)
				.map(s -> SumatraMath.relative(s, passRatingLowerDist, passRatingUpperDist))
				.orElse(1.0);
	}
	
	
	private ABallTrajectory straightBallTrajectory(
			final IVector2 passOrigin,
			final IVector2 passTarget)
	{
		final IVector2 passReceiverTarget = Geometry.getGoalTheir().getCenter();
		
		double passVelocity;
		switch (mode)
		{
			case DEFAULT:
				passVelocity = OffensiveMath.passSpeedStraight(
						passOrigin,
						passTarget,
						passReceiverTarget);
				break;
			case KICK_INS_BLAUE:
				passVelocity = OffensiveMath.passSpeedStraightKickInsBlaue(passOrigin, passTarget, OffensiveConstants.getBallSpeedAtTargetKickInsBlaue());
				break;
			default:
				throw new IllegalArgumentException("Not implemented");
		}
		
		IVector2 passDirection = passTarget.subtractNew(passOrigin);
		IVector3 kickVel = Vector3.from2d(passDirection.scaleToNew(passVelocity * 1000), 0.);
		
		return BallFactory.createTrajectoryFromStraightKick(passOrigin, kickVel);
	}
	
	
	private ABallTrajectory chippedBallTrajectory(
			final IVector2 passOrigin,
			final IVector2 passTarget,
			final double maxChipSpeed)
	{
		double distance = passOrigin.distanceTo(passTarget);
		
		double passVelocity;
		switch (mode)
		{
			case DEFAULT:
				passVelocity = OffensiveMath.passSpeedChip(distance, maxChipSpeed);
				break;
			case KICK_INS_BLAUE:
				passVelocity = OffensiveMath.passSpeedChipKickInsBlaue(passOrigin, passTarget, maxChipSpeed);
				break;
			default:
				throw new IllegalArgumentException("Not implemented");
		}
		
		IVector2 passDirection = passTarget.subtractNew(passOrigin);
		IVector2 xyVect = BallFactory.createChipConsultant().absoluteKickVelToVector(passVelocity);
		IVector3 kickVel = Vector3.from2d(passDirection.scaleToNew(xyVect.x() * 1000), xyVect.y() * 1000);
		
		return BallFactory.createTrajectoryFromChipKick(passOrigin, kickVel);
	}
	
	
	private Collection<ITrackedBot> filterPossibleInterceptors(
			final IVector2 passingBot,
			final ABallTrajectory passTrajectory)
	{
		double distance = passTrajectory.getTravelLinesInterceptable().stream()
				.map(iLine -> iLine.supportVector().distanceTo(passingBot)).findFirst().orElse(0.);
		
		return consideredBots.stream()
				.filter(bot -> bot.getPos().subtractNew(passingBot).getLength2() >= distance)
				.collect(Collectors.toList());
	}
}
