/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.support.passtarget;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.IPassRater;
import edu.tigers.sumatra.ai.metis.targetrater.ReflectorRater;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * A Factory to create PassTargetRatings from a PassTarget with a PassRater and PassTargetRatingFactoryInput
 */
public class PassTargetRatingFactory
{
	@Configurable(comment = "[s] factor for passRating: the longer the pass the worse the rating gets", defValue = "1.2")
	private static double optimalPassDuration = 1.2;
	
	@Configurable(defValue = "0.3")
	private static double passDurationMinValue = 0.3;

	@Configurable(defValue = "-3000.0")
	private static double minXRatingPos = -3000.0;
	
	static
	{
		ConfigRegistration.registerClass("metis", PassTargetRatingFactory.class);
	}
	
	
	public IPassTargetRating ratingFromPassTargetAndInput(IPassTarget passTarget, final IPassRater passRater,
			final PassTargetRatingFactoryInput input)
	{
		PassTargetRating passTargetRating = new PassTargetRating();
		
		passTargetRating.setPassScoreStraight(passScoreStraight(passTarget.getPos(), passRater, input));
		passTargetRating.setPassScoreChip(passScoreChip(passTarget.getPos(), passRater, input));
		
		passTargetRating.setPassDurationStraight(passDurationStraight(passTarget.getPos(), input));
		passTargetRating.setPassDurationChip(passDurationChip(passTarget.getPos(), input));
		
		passTargetRating.setDurationScoreStraight(getDurationScoreStraight(passTargetRating.getPassDurationStraight()));
		passTargetRating.setDurationScoreChip(getDurationScoreChip(passTargetRating.getPassDurationChip()));
		
		passTargetRating.setGoalKickScore(goalKickScore(passTarget, input));
		passTargetRating.setPassScore(passScore(passTargetRating));
		
		passTargetRating.setPressureScore(pressureScore(passTarget));
		
		return passTargetRating;
	}
	
	
	private double pressureScore(final IPassTarget passTarget)
	{
		double xRating = SumatraMath.relative(passTarget.getPos().x(),
				minXRatingPos, Geometry.getFieldLength());
		
		double yGoalCornerMid = Geometry.getFieldWidth() / 4.0;
		double yDistToGoalCornerMid = Math.abs(Math.abs(passTarget.getPos().y()) - yGoalCornerMid);
		
		double distToOptimalYPosRating = SumatraMath.relative(yDistToGoalCornerMid, 0,
				Geometry.getFieldWidth()/4.0);
		double yRating = 1 - distToOptimalYPosRating;
		
		return xRating * Math.max(0.1, yRating);
	}
	
	
	private double getDurationScoreStraight(final double passDurationStraight)
	{
		double straight = Math.abs(optimalPassDuration - passDurationStraight);
		return 1 - SumatraMath.relative(straight, 0, optimalPassDuration);
	}
	
	
	private double getDurationScoreChip(final double passDurationChip)
	{
		double chip = Math.abs(optimalPassDuration - passDurationChip);
		return 1 - SumatraMath.relative(chip, 0, optimalPassDuration);
	}
	
	
	private double passScore(final IPassTargetRating passTargetRating)
	{
		final double scoreStraight = passTargetRating.getPassScoreStraight()
				* Math.max(passDurationMinValue, passTargetRating.getDurationScoreStraight());
		final double scoreChip = passTargetRating.getPassScoreChip()
				* Math.max(passDurationMinValue, passTargetRating.getDurationScoreChip());
		return Math.max(scoreChip, scoreStraight);
	}
	
	
	private double passScoreStraight(final IVector2 passTargetPos, final IPassRater passRater,
			final PassTargetRatingFactoryInput ratingParameters)
	{
		return passRater.rateStraightPass(ratingParameters.getPassOrigin(), passTargetPos);
	}
	
	
	private double passScoreChip(final IVector2 passTargetPos, final IPassRater passRater,
			final PassTargetRatingFactoryInput ratingParameters)
	{
		return passRater.rateChippedPass(ratingParameters.getPassOrigin(), passTargetPos,
				ratingParameters.getMaxChipVel());
	}
	
	
	private double passDurationStraight(IVector2 passTargetPos,
			final PassTargetRatingFactoryInput ratingParameters)
	{
		double passVelocity = OffensiveMath.passSpeedStraight(
				ratingParameters.getPassOrigin(),
				passTargetPos,
				Geometry.getGoalTheir().getCenter());
		double distance = ratingParameters.getPassOrigin().distanceTo(passTargetPos);
		return ratingParameters.getBall().getStraightConsultant().getTimeForKick(distance, passVelocity);
	}
	
	
	private double passDurationChip(IVector2 passTargetPos, final PassTargetRatingFactoryInput ratingParameters)
	{
		double distance = ratingParameters.getPassOrigin().distanceTo(passTargetPos);
		double passVelocity = OffensiveMath.passSpeedChip(distance, ratingParameters.getMaxChipVel());
		return ratingParameters.getBall().getChipConsultant().getTimeForKick(distance, passVelocity);
	}
	
	
	private double goalKickScore(final IPassTarget passTargetPos,
			final PassTargetRatingFactoryInput ratingParameters)
	{
		ReflectorRater reflectorRater = new ReflectorRater(ratingParameters.getBall().getStraightConsultant(),
				ratingParameters.getFoeBots());
		return reflectorRater.rateTarget(ratingParameters.getPassOrigin(), passTargetPos.getPos());
	}
}
