/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 22, 2015
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Author(s): MarkG <Mark.Geiger@dlr.de>
 */
public class OffensiveConstants
{
	/**  */
	@Configurable(comment = "time in seconds")
	private static float		cheeringStopTimer					= 3.0f;
	
	/**  */
	@Configurable(comment = "time in seconds")
	private static float		delayWaitTime						= 3.0f;
	
	/**  */
	@Configurable(comment = "show additional informations in debug log, lots of spamming")
	private static boolean	showDebugInformations			= false;
	
	/**  */
	@Configurable(comment = "no directShots when indirectFreeKick is called")
	private static boolean	forcePassWhenIndirectIsCalled	= true;
	
	/**  */
	@Configurable(comment = "minimal distance to ball for move Positions")
	private static float		minDistToBall						= 40f;
	
	/**  */
	@Configurable(comment = "distance to our Penalty Area")
	private static float		distanceToPenaltyArea			= 300f;
	
	
	/**  */
	@Configurable(comment = "finalKickStateDistance")
	private static float		finalKickStateDistance			= 650f;
	
	/**  */
	@Configurable(comment = "finalKickStateUpdate")
	private static float		finalKickStateUpdate				= 300f;
	
	/**  */
	@Configurable(comment = "chance to do hard coded plays for indirects in enemy half")
	private static float		chanceToDoSpecialMove			= 0.7f;
	
	/**  */
	@Configurable(comment = "enable supportive Attacker")
	private static boolean	enableSupportiveAttacker		= true;
	
	
	/**
	 * @return the cheeringStopTimer
	 */
	public static float getCheeringStopTimer()
	{
		return cheeringStopTimer;
	}
	
	
	/**
	 * @return the delayWaitTime
	 */
	public static float getDelayWaitTime()
	{
		return delayWaitTime;
	}
	
	
	/**
	 * @return the showDebugInformations
	 */
	public static boolean isShowDebugInformations()
	{
		return showDebugInformations;
	}
	
	
	/**
	 * @return the forcePassWhenIndirectIsCalled
	 */
	public static boolean isForcePassWhenIndirectIsCalled()
	{
		return forcePassWhenIndirectIsCalled;
	}
	
	
	/**
	 * @return the minDistToBall
	 */
	public static float getMinDistToBall()
	{
		return minDistToBall;
	}
	
	
	/**
	 * @return the distanceToPenaltyArea
	 */
	public static float getDistanceToPenaltyArea()
	{
		return distanceToPenaltyArea;
	}
	
	
	/**
	 * @return the finalKickStateDistance
	 */
	public static float getFinalKickStateDistance()
	{
		return finalKickStateDistance;
	}
	
	
	/**
	 * @return the finalKickStateUpdate
	 */
	public static float getFinalKickStateUpdate()
	{
		return finalKickStateUpdate;
	}
	
	
	/**
	 * @return the chanceToDoSpecialMove
	 */
	public static float getChanceToDoSpecialMove()
	{
		return chanceToDoSpecialMove;
	}
	
	
	/**
	 * @return the enableSupportiveAttacker
	 */
	public static boolean isSupportiveAttackerEnabled()
	{
		return enableSupportiveAttacker;
	}
	
}
