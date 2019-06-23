/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.math.SumatraMath;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public final class DefenseConstants
{
	
	@Configurable(comment = "Minimum lookahead used for bots [s]", defValue = "0.1")
	private static double minLookaheadBotThreats = 0.1;
	
	@Configurable(comment = "Maximum lookahead used for bots [s]", defValue = "0.2")
	private static double maxLookaheadBotThreats = 0.2;
	
	@Configurable(comment = "Velocity which uses the maximum lookahead for the opposing teams bots[m/s]", defValue = "4.0")
	private static double maxLookaheadBotThreatsVelocity = 4.0;
	
	@Configurable(comment = "Max distance from goal center to go out", defValue = "2000.0")
	private static double maxGoOutDistance = 2000;
	
	@Configurable(comment = "Min distance from penArea to go out", defValue = "300.0")
	private static double minGoOutDistance = 300;
	
	@Configurable(comment = "Maximum bonus for currently assigned bot vs other bot", defValue = "0.2")
	private static double maxSwitchSlackThreshold = 0.2;
	
	@Configurable(comment = "Minimum bonus for currently assigned bot vs other bot", defValue = "0.1")
	private static double minSwitchSlackThreshold = 0.1;
	
	@Configurable(comment = "Ball velocity issuing the maximum switch slack threshold bonus", defValue = "4")
	private static double maxSwitchSlackThresholdVelocity = 4;
	
	@Configurable(comment = "Ball velocity issuing the minimum switch slack threshold bonus", defValue = "0")
	private static double minSwitchSlackThresholdVelocity = 0;
	
	
	static
	{
		ConfigRegistration.registerClass("defense", DefenseConstants.class);
	}
	
	
	private DefenseConstants()
	{
	}
	
	
	/**
	 * @param velocity velocity of the tracked object
	 * @return lookahead in dependency on the lookahead
	 */
	public static double getLookaheadBotThreats(final double velocity)
	{
		double factor = SumatraMath.relative(velocity, 0, maxLookaheadBotThreatsVelocity);
		double linearPart = maxLookaheadBotThreats - minLookaheadBotThreats;
		double constantPart = minLookaheadBotThreats;
		
		return factor * linearPart + constantPart;
	}
	
	
	public static double getMaxGoOutDistance()
	{
		return maxGoOutDistance;
	}
	
	
	public static double getMinGoOutDistance()
	{
		return minGoOutDistance;
	}
	
	
	public static double getMaxSwitchSlackThreshold()
	{
		return maxSwitchSlackThreshold;
	}
	
	
	public static double getMinSwitchSlackThreshold()
	{
		return minSwitchSlackThreshold;
	}
	
	
	public static double getMaxSwitchSlackThresholdVelocity()
	{
		return maxSwitchSlackThresholdVelocity;
	}
	
	
	public static double getMinSwitchSlackThresholdVelocity()
	{
		return minSwitchSlackThresholdVelocity;
	}
	
}
