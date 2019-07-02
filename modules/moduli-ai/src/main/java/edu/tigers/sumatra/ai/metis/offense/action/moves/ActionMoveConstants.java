/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.model.SumatraModel;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 *         Constants for Offensive ActionMove viabilities. This values can be used to strongly direct the behaviour of
 *         the OffensiveRole. A lower value will result in a lower likelihood for the given edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction to be
 *         executed.
 */
public class ActionMoveConstants
{
	
	@Configurable(comment = "Viability for GoToOtherHalf", defValue = "0.4")
	private static double defaultGoToOtherHalfViability = 0.4;
	
	@Configurable(defValue = "0.25")
	private static double viabilityMultiplierClearingKick = 0.25;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierDirectKick = 1.0;
	
	@Configurable(defValue = "0.1")
	private static double viabilityMultiplierKickInsBlaue = 0.1;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierLowChanceDirectKick = 1.0;
	
	@Configurable(defValue = "0.5")
	private static double viabilityMultiplierStandardPass = 0.5;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierGoToOtherHalf = 1.0;
	
	@Configurable(defValue = "false", comment = "Disables this action move for testing")
	private static boolean forbidGoalKicks = false;
	
	static
	{
		ConfigRegistration.registerClass("metis", ActionMoveConstants.class);
	}
	
	
	private ActionMoveConstants()
	{
		// hide implicit constructor
	}
	
	
	public static double getDefaultGoToOtherHalfViability()
	{
		return defaultGoToOtherHalfViability;
	}
	
	
	public static double getViabilityMultiplierClearingKick()
	{
		return viabilityMultiplierClearingKick;
	}
	
	
	public static double getViabilityMultiplierDirectKick()
	{
		return viabilityMultiplierDirectKick;
	}
	
	
	public static double getViabilityMultiplierKickInsBlaue()
	{
		return viabilityMultiplierKickInsBlaue;
	}
	
	
	public static double getViabilityMultiplierLowChanceDirectKick()
	{
		return viabilityMultiplierLowChanceDirectKick;
	}
	
	
	public static double getViabilityMultiplierStandardPass()
	{
		return viabilityMultiplierStandardPass;
	}
	
	
	public static double getViabilityMultiplierGoToOtherHalf()
	{
		return viabilityMultiplierGoToOtherHalf;
	}
	
	
	public static boolean allowGoalKick()
	{
		return SumatraModel.getInstance().isProductive() || !forbidGoalKicks;
	}
}
