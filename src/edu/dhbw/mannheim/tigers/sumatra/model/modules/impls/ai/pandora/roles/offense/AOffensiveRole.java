/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 26, 2014
 * Author(s): Mark Geiger <Mark.Geiger@dlr.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.offense;

import java.awt.Color;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * The Offensive role is always ball oriented.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public abstract class AOffensiveRole extends ARole
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected static final Logger	log							= Logger.getLogger(AOffensiveRole.class.getName());
	
	@Configurable
	protected static boolean		showDebugInformations	= false;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	public AOffensiveRole()
	{
		super(ERole.OFFENSIVE);
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.MOVE);
	}
	
	
	protected enum EStateId
	{
		/**  */
		GET,
		/**  */
		KICK,
		/**  */
		STOP
	}
	
	protected enum EEvent
	{
		/**  */
		BALL_CONTROL_OBTAINED,
		/**  */
		LOST_BALL,
		/**  */
		GAME_STARTED,
		/**  */
		STOP,
		/**  */
		NORMALSTART
	}
	
	
	// ----------------------------------------------------------------------- //
	// -------------------- functions ---------------------------------------- //
	// ----------------------------------------------------------------------- //
	
	/**
	 * checks if bot is in control of the ball, if so change to kicker,
	 * else change to getter.
	 * 
	 * @param destination target movePosition
	 */
	protected boolean checkBallObtained(final ValuePoint destination)
	{
		IVector2 ballPos = getAiFrame().getWorldFrame().ball.getPos();
		IVector2 botPos = getPos();
		float distanceToBall = 325;
		float distanceToDestination = 140;
		float leavingDistance = 450;
		
		if (getCurrentState() == EStateId.GET)
		{
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(ballPos, distanceToBall), Color.RED));
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(destination, distanceToDestination), Color.RED));
		} else
		{
			getAiFrame().addDebugShape(new DrawableCircle(new Circle(ballPos, leavingDistance), Color.YELLOW));
		}
		
		if (((GeoMath.distancePP(botPos, destination) < (distanceToDestination)) && (GeoMath.distancePP(botPos, ballPos) < (distanceToBall)))
				|| ((getCurrentState() == EStateId.KICK) && (GeoMath.distancePP(ballPos, botPos) < leavingDistance)))
		{
			if (getCurrentState() == EStateId.GET)
			{
				printDebugInformation("Ball Control Obtained");
			}
			return true;
		}
		if (getCurrentState() != EStateId.GET)
		{
			printDebugInformation("Lost Ball");
		}
		return false;
	}
	
	
	protected void printDebugInformation(final String message)
	{
		if (showDebugInformations)
		{
			log.debug(message);
		}
	}
	
}
