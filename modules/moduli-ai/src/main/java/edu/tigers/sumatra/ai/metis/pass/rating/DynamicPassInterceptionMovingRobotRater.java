/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass.rating;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.offense.situation.zone.OffensiveZones;
import edu.tigers.sumatra.ai.metis.pass.PassStats;
import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.math.SumatraMath;
import edu.tigers.sumatra.wp.data.ITrackedBot;

import java.util.Collection;
import java.util.List;


/**
 * Rate pass interception by generating moving robot circles based on robot speed and acceleration
 * and intersect them with the ball travel line at different points in time.
 * The distance that the circle covers on the ball travel line is used for the rating.
 * Adjust the final scoring in 2 dynamic phases.
 * Adjustment phase 1: Adjust score by looking at past pass success rates from a given zone to a specific target zone.
 * Adjustment phase 2: Adjust score by looking at the overall past pass success ratio.
 */
public class DynamicPassInterceptionMovingRobotRater extends APassRater
{
	@Configurable(defValue = "0.15", comment = "Adjustment phase 1: max bonus for zone passes")
	private static double maxDynamicAdjustment = 0.15;

	@Configurable(defValue = "0.03", comment = "Adjustment phase 1: calculate bonus via past pass success history of zone passes")
	private static double dynamicAdjustmentStepSize = 0.03;

	@Configurable(defValue = "0.3", comment = "Adjustment phase 2: success ratio bonus multiplier")
	private static double dynamicAdjustmentFactor = 0.3;
	@Configurable(defValue = "0.7", comment = "the pass success ratio that shall be achieved using dynamic adjustment")
	private static double targetedPassRatio = 0.7;

	@Configurable(defValue = "10", comment = "Adjustment phase 2: calculate bonus via pass success ratio")
	private static int numOfPassesToStartDynamicScoringFactorAdjustment = 10;

	static
	{
		ConfigRegistration.registerClass("metis", DynamicPassInterceptionMovingRobotRater.class);
	}

	private final PassInterceptionMovingRobotRater passInterceptionMovingRobotRater;
	private final PassStats passStats;
	private final OffensiveZones offensiveZones;


	public DynamicPassInterceptionMovingRobotRater(
			Collection<ITrackedBot> consideredBots,
			Collection<ITrackedBot> consideredBotsIntercept,
			PassStats passStats,
			OffensiveZones offensiveZones
	)
	{
		passInterceptionMovingRobotRater = new PassInterceptionMovingRobotRater(consideredBots);
		this.passStats = passStats;
		this.offensiveZones = offensiveZones;

		if (passStats.getNPasses() > numOfPassesToStartDynamicScoringFactorAdjustment)
		{
			// start using dynamic scoring offset
			double ratio = passStats.getSuccessfulPasses() / (double) passStats.getNPasses();
			double offset = SumatraMath.cap((ratio - targetedPassRatio), -0.5, 0.5) * 2 * dynamicAdjustmentFactor;
			passInterceptionMovingRobotRater.setScoringFactorOffset(offset);
		}
	}


	@Override
	public double rate(Pass pass)
	{
		double rawScore = passInterceptionMovingRobotRater.rate(pass);
		var eOriginZone = offensiveZones.getZoneByPoint(pass.getKick().getSource());
		var eTargetZone = offensiveZones.getZoneByPoint(pass.getKick().getTarget());
		double dynamicAdjustment = 0;
		if (eOriginZone.isPresent() && eTargetZone.isPresent())
		{
			var targetStats = passStats.getZonePassSuccessMap().get(eOriginZone.get().getZoneName())
					.get(eTargetZone.get().getZoneName());
			int nTotal = targetStats.getTotalPasses();
			int nSuccess = targetStats.getSuccessfulPasses();
			int nFailure = nTotal - nSuccess;
			dynamicAdjustment = Math.min(maxDynamicAdjustment,
					Math.max(-maxDynamicAdjustment, (nSuccess - nFailure) * dynamicAdjustmentStepSize));
		}
		return Math.min(1, Math.max(0, rawScore + dynamicAdjustment));
	}


	@Override
	public void setShapes(List<IDrawableShape> shapes)
	{
		passInterceptionMovingRobotRater.setShapes(shapes);
	}


	@Override
	public void drawShapes(List<IDrawableShape> shapes)
	{
		passInterceptionMovingRobotRater.drawShapes(shapes);
	}
}
