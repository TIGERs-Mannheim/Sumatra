/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 19, 2012
 * Author(s): NicolaiO
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.test;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze.ETuneableParameter;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.analyze.TuneableParameter;


/**
 * 
 * 
 * @author DirkK
 * 
 */
public class PathplanningTestPlay extends APlay
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger		log				= Logger.getLogger(PathplanningTestPlay.class.getName());
	
	private final MoveRole				role;
	
	private List<MoveRole>				idiots;
	
	private List<Vector2>				positions;
	
	private final List<Vector2>		points			= new LinkedList<Vector2>();
	
	private int								counter			= 0;
	
	private TuneableParameter			testedParams	= new TuneableParameter(0.6f, 0.3f, 200, 90, 30, 2500, 200, 500);
	
	private boolean						isSetup			= false;
	// private ETuneableParameter checkType = ETuneableParameter.pGoal;
	// private float start = 0f;
	// private float end = 1f;
	// private float stepSize = 0.05f;
	
	
	// private ETuneableParameter checkType = ETuneableParameter.maxIterations;
	// private float start = 4000f;
	// private float end = 100f;
	// private float stepSize = -100f;
	
	private final ETuneableParameter	checkType		= ETuneableParameter.probabilities;
	private static final float			start				= 0f;
	private static final float			end				= 1f;
	private static final float			stepSize			= 0.05f;
	
	
	// pGoal new TuneableParameter(0.0f, 0.7f, 200, 100, 90, 30, 2500, 200, 500);
	// stepSize new TuneableParameter(0.2f, 0.7f, 10, 100, 90, 30, 2500, 200, 500);
	// maxIterations new TuneableParameter(0.2f, 0.7f, 200, 100, 90, 30, 100, 200, 500);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public PathplanningTestPlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		setTimeout(Long.MAX_VALUE);
		
		testedParams.set(checkType, start);
		
		points.add(new Vector2(800, 800));
		points.add(new Vector2(-800, 800));
		points.add(new Vector2(-800, -800));
		points.add(new Vector2(800, -800));
		
		
		if (isSetup)
		{
			for (final Vector2 vec : points)
			{
				vec.x = vec.x - 2000;
			}
			
			positions = new LinkedList<Vector2>();
			positions.add(new Vector2(0, 750));
			positions.add(new Vector2(0, -700));
			positions.add(new Vector2(0, -900));
			positions.add(new Vector2(600, 0));
			positions.add(new Vector2(900, 0));
			positions.add(new Vector2(800, 800));
			for (final Vector2 vec : positions)
			{
				vec.x = vec.x - 2000;
			}
			
			idiots = new LinkedList<MoveRole>();
			for (int i = 0; i < (getNumAssignedRoles() - 1); i++)
			{
				idiots.add(new MoveRole(EMoveBehavior.LOOK_AT_BALL));
				addDefensiveRole(idiots.get(i), positions.get(i));
			}
		}
		
		role = new MoveRole(EMoveBehavior.LOOK_AT_BALL);
		
		// target = aiFrame.tacticalInfo.getPlayPattern().get(0).getPasser();
		addDefensiveRole(role, nextTarget());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	protected void beforeUpdate(AIInfoFrame aiFrame)
	{
		if (isSetup)
		{
			boolean setupComplete = true;
			for (final MoveRole role : idiots)
			{
				if (!role.checkAllConditions(aiFrame.worldFrame))
				{
					setupComplete = false;
				}
			}
			if (setupComplete)
			{
				isSetup = false;
			}
		} else
		{
			if (role.checkAllConditions(aiFrame.worldFrame))
			{
				role.updateDestination(nextTarget());
				if (counter == (points.size() - 1))
				{
					testedParams = new TuneableParameter(testedParams);
					check();
					// ERRTPlanner_WPC.getInstanceUsedBySumatra().goalReached(testedParams, aiFrame.worldFrame);
				} else
				{
					// ERRTPlanner_WPC.getInstanceUsedBySumatra().goalReached();
				}
			}
		}
	}
	
	
	private void check()
	{
		if (checkType.equals(ETuneableParameter.probabilities))
		{
			if ((testedParams.getpGoal() + testedParams.getpWaypoint()) >= 1)
			{
				if ((testedParams.getpGoal() + stepSize) >= 1)
				{
					// ERRTPlanner_WPC.getInstanceUsedBySumatra().printDebuggerLogs();
				}
				testedParams.setpGoal(testedParams.getpGoal() + stepSize);
				testedParams.setpWaypoint(0);
			} else
			{
				testedParams.setpWaypoint(testedParams.getpWaypoint() + stepSize);
			}
			log.warn(checkType.name() + " Goal: " + testedParams.getpGoal() + " / WP: " + testedParams.getpWaypoint());
		} else
		{
			testedParams.set(checkType, testedParams.get(checkType) + stepSize);
			log.warn(checkType.name() + " " + testedParams.get(checkType));
			
			if (testedParams.get(checkType) >= end)
			{
				// ERRTPlanner_WPC.getInstanceUsedBySumatra().printDebuggerLogs();
			}
		}
		
	}
	
	
	// private void checkStepSize(float end, float stepSize)
	// {
	// testedParams.setStepSize(testedParams.getStepSize() + 10);
	// log.warn("Step size: " + testedParams.getStepSize());
	// if (testedParams.getStepSize() == 400)
	// ERRTPlanner_WPC.getInstance().printDebuggerLogs();
	// }
	//
	//
	// private void checkGoalProbability()
	// {
	// testedParams.setpGoal(testedParams.getpGoal() + 0.05f);
	// log.warn("pGoal: " + testedParams.getpGoal());
	// if (testedParams.getpGoal() >= 1)
	// ERRTPlanner_WPC.getInstance().printDebuggerLogs();
	//
	// }
	
	
	/**
	 * i
	 * Calculate the next target
	 * 
	 * @return
	 */
	private IVector2 nextTarget()
	{
		final IVector2 target = points.get(counter);
		counter++;
		if (counter == points.size())
		{
			counter = 0;
		}
		
		return target;
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
		// nothing todo
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
