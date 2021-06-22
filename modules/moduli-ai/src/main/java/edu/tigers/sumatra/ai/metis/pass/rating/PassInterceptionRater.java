/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.defense.DefenseMath;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.rectangle.IRectangle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Comparator;


/**
 * Rate a pass by interception time.
 */
@RequiredArgsConstructor
public class PassInterceptionRater implements IPassRater
{
	@Configurable(comment = "Lower distance bound for alternative pass rating", defValue = "100.0")
	private static double passRatingLowerDist = 100.0;

	@Configurable(comment = "Upper distance bound for alternative pass rating", defValue = "2000.0")
	private static double passRatingUpperDist = 2000.0;

	@Configurable(comment = "Avoid passes near the edge of the field", defValue = "true")
	private static boolean avoidFieldEdgePasses = true;

	static
	{
		ConfigRegistration.registerClass("metis", PassInterceptionRater.class);
	}

	private final Collection<ITrackedBot> consideredBots;


	@Override
	public double rate(Pass pass)
	{
		IVector2 origin = pass.getKick().getSource();
		Vector3 velMM = pass.getKick().getKickVel().multiplyNew(1000);
		var passTrajectory = Geometry.getBallFactory().createTrajectoryFromKickedBallWithoutSpin(origin, velMM);
		var tStart = passTrajectory.getTouchdownLocations().stream().findFirst()
				.map(passTrajectory::getTimeByPos)
				.orElse(0.0);
		return rateTrajectory(passTrajectory, consideredBots, pass.getKick().getTarget(), tStart);
	}


	private double rateTrajectory(IBallTrajectory trajectory, Collection<ITrackedBot> bots, IVector2 target,
			double tStart)
	{
		double fieldBorderScore = determineFieldBorderRatings(target, trajectory.getPosByTime(0).getXYVector());
		var score = DefenseMath.calcReceiveRatingsForRestrictedStartAndEnd(trajectory, bots, passRatingUpperDist, target,
				tStart)
				.stream()
				.min(Comparator.comparingDouble(DefenseMath.ReceiveData::getDistToBallCurve))
				.map(s -> calcScoreByMinDistToBallCurveAndDangerDist(target, s))
				.orElse(1.0);
		return Math.max(0, score - fieldBorderScore);
	}


	private double determineFieldBorderRatings(IVector2 passTarget, IVector2 ballPos)
	{
		if (!avoidFieldEdgePasses || !ballNearFieldBorder(ballPos))
		{
			return 0;
		}
		double dist = Geometry.getField().nearestPointOutside(passTarget).distanceTo(passTarget);
		return 1 - SumatraMath.relative(dist, 350, 1200);
	}


	private boolean ballNearFieldBorder(final IVector2 ballPos)
	{
		IRectangle field = Geometry.getField().withMargin(-500);
		return !field.isPointInShape(ballPos);
	}


	private Double calcScoreByMinDistToBallCurveAndDangerDist(final IVector2 passTarget, final DefenseMath.ReceiveData s)
	{
		double targetToGoalDist = passTarget.distanceTo(Geometry.getGoalOur().getCenter());
		double backwardsPenaltyMultiplier = 1.5 - SumatraMath.relative(targetToGoalDist, 2000, 6000) / 2.0;
		return SumatraMath.relative(
				s.getDistToBallCurve(),
				passRatingLowerDist * backwardsPenaltyMultiplier,
				passRatingUpperDist * backwardsPenaltyMultiplier
		);
	}
}
