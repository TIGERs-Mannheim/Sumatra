/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.geometry;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


/**
 * Configuration object for rule parameters.
 *
 * @author Stefan Schneyer
 */
public class RuleConstraints
{
	@Configurable(comment = "Max allowed ball speed", defValue = "6.5")
	private static double maxBallSpeed = 6.5;
	@Configurable(comment = "Stop radius around ball", defValue = "500.0")
	private static double stopRadius = 500.0;
	@Configurable(comment = "Bots must be behind this line on penalty shot", defValue = "400.0")
	private static double distancePenaltyMarkToPenaltyLine = 400;
	@Configurable(comment = "Bot speed in stop phases", defValue = "1.5")
	private static double stopSpeed = 1.5;
	@Configurable(comment = "Distance between bots and penalty area in standard situations", defValue = "200.0")
	private static double botToPenaltyAreaDistanceStandard = 200;
	@Configurable(comment = "Ball placement accuracy tolerance of referee", defValue = "150.0")
	private static double ballPlacementTolerance = 150;
	@Configurable(comment = "The max allowed robot height", defValue = "150.0")
	private static double maxRobotHeight = 150;
	
	static
	{
		ConfigRegistration.registerClass("ruleConst", RuleConstraints.class);
	}
	
	
	private RuleConstraints()
	{
	}
	
	
	/**
	 * @return the stopSpeed
	 */
	public static double getStopSpeed()
	{
		return stopSpeed;
	}
	
	
	/**
	 * @return distance from penalty mark to penalty line
	 */
	public static double getDistancePenaltyMarkToPenaltyLine()
	{
		return distancePenaltyMarkToPenaltyLine;
	}
	
	
	/**
	 * distance between ball and bot required during stop signal (without ball and bot radius!)
	 *
	 * @return distance
	 */
	public static double getStopRadius()
	{
		return stopRadius;
	}
	
	
	/**
	 * Additional margin to opponents penalty area in our standard situations
	 *
	 * @return margin
	 */
	public static double getBotToPenaltyAreaMarginStandard()
	{
		return botToPenaltyAreaDistanceStandard;
	}
	
	
	/**
	 * Maximal speed allowed for kicking the ball
	 *
	 * @return The maximum allowed ball velocity in m/s
	 */
	public static double getMaxBallSpeed()
	{
		return maxBallSpeed;
	}
	
	
	public static double getBallPlacementTolerance()
	{
		return ballPlacementTolerance;
	}
	
	
	public static double getMaxRobotHeight()
	{
		return maxRobotHeight;
	}
}
