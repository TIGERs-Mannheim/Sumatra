/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.math.line.v2.ILineSegment;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Comparator;


/**
 * A nice rater
 */
public class ReflectorRater
{
	
	@Configurable(defValue = "15000.0")
	private static double maxPassShotDist = 15000.0;
	
	@Configurable(defValue = "5000.0")
	private static double maxfirstPassDist = 5000.0;
	
	private IStraightBallConsultant ballConsultant;
	private IBotIDMap<ITrackedBot> obstacles;
	
	
	static
	{
		ConfigRegistration.registerClass("metis", ReflectorRater.class);
	}
	
	
	private ReflectorRater()
	{
		// hide
	}
	
	
	public ReflectorRater(final IStraightBallConsultant ballConsultant, final IBotIDMap<ITrackedBot> obstacles)
	{
		this.ballConsultant = ballConsultant;
		this.obstacles = obstacles;
	}
	
	
	public double rateTarget(IVector2 origin, IVector2 reflector)
	{
		IVector2 left = Geometry.getGoalTheir().getLeftPost();
		IVector2 right = Geometry.getGoalTheir().getRightPost();
		
		double angleLeft = OffensiveMath.getRedirectAngle(origin, reflector, left);
		double angleRight = OffensiveMath.getRedirectAngle(origin, reflector, right);
		double maxAngle = Math.max(angleLeft, angleRight);
		
		if (maxAngle < OffensiveConstants.getMaximumReasonableRedirectAngle())
		{
			return Math.max(0.75,
					1 - SumatraMath.relative(maxAngle, 45, OffensiveConstants.getMaximumReasonableRedirectAngle()))
					* rateByDistance(origin, reflector, Geometry.getGoalTheir().getCenter());
		}
		
		return 0;
	}
	
	
	private double rateByDistance(final IVector2 origin, final IVector2 reflector, final Vector2f center)
	{
		if (origin.distanceTo(reflector) > maxfirstPassDist)
		{
			return 0;
		}
		double passDist = origin.distanceTo(reflector) + reflector.distanceTo(center);
		return (1 - SumatraMath.relative(passDist, 0, maxPassShotDist))
				* Math.max(0.75, calcAndRateTarget(reflector).getScore())
				* Math.max(0.75, ratePassScore(origin, reflector));
	}
	
	
	private double ratePassScore(final IVector2 origin, final IVector2 reflector)
	{
		ILineSegment passLine = Lines.segmentFromPoints(origin, reflector);
		
		return obstacles.values().stream()
				.map(e -> passLine.distanceTo(e.getPos()))
				.min(Comparator.naturalOrder())
				.map(e -> SumatraMath.relative(e, 0.0, 1000.0)).orElse(1.0);
	}
	
	
	protected IRatedTarget calcAndRateTarget(IVector2 origin)
	{
		final AngleRangeRater rater = AngleRangeRater.forGoal(Geometry.getGoalTheir());
		rater.setObstacles(obstacles.values());
		rater.setStraightBallConsultant(ballConsultant);
		return rater.rate(origin).orElse(RatedTarget.ratedPoint(Geometry.getGoalTheir().getCenter(), 0));
	}
	
	
	public void setBallConsultant(final IStraightBallConsultant ballConsultant)
	{
		this.ballConsultant = ballConsultant;
	}
	
	
	public void setObstacles(final IBotIDMap<ITrackedBot> obstacles)
	{
		this.obstacles = obstacles;
	}
}
