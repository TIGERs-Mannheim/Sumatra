/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;


/**
 * @author MarkG <Mark.Geiger@dlr.de>
 *         Constants for Offensive ActionMove viabilities. This values can be used to strongly direct the behaviour of
 *         the OffensiveRole. A lower value will result in a lower likelihood for the given OffensiveAction to be
 *         executed.
 */
public class ActionMoveConstants
{
	
	@Configurable(comment = "Viability for GoToOtherHalf", defValue = "0.4")
	private static double defaultGoToOtherHalfViability = 0.4;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierClearingKick = 1.0;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierDirectKick = 1.0;
	
	@Configurable(defValue = "0.6")
	private static double viabilityMultiplierKickInsBlaue = 0.6;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierLowChanceDirectKick = 1.0;
	
	@Configurable(defValue = "0.5")
	private static double viabilityMultiplierStandardPass = 0.5;
	
	@Configurable(defValue = "1.0")
	private static double viabilityMultiplierGoToOtherHalf = 1.0;
	
	static
	{
		ConfigRegistration.registerClass("offensive", ActionMoveConstants.class);
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
}
