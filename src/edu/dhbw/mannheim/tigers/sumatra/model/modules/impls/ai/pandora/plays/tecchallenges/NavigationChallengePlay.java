/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges.navigation.DrivingTimeContainer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;


/**
 * Technical challenge for 2013
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class NavigationChallengePlay extends APlay
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private final List<MoveRole>					moveRoles					= new ArrayList<MoveRole>(3);
	private final List<IVector2>					startPoints					= new ArrayList<IVector2>(3);
	private final List<IVector2>					endPoints					= new ArrayList<IVector2>(3);
	
	private final List<DrivingTimeContainer>	neededTimesToCenterSpot	= new ArrayList<DrivingTimeContainer>(3);
	private final List<Long>						startTimes					= new ArrayList<Long>(3);
	
	private final List<EState>						states						= new ArrayList<EState>(3);
	private static final int						NUM_ROLES					= 3;
	
	private long										timeStarted					= 0;
	
	private boolean									prepare						= true;
	
	private enum EState
	{
		PREPARE,
		FIRST_HALF,
		DONE
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @param aiFrame
	 * @param numAssignedRoles
	 */
	public NavigationChallengePlay(AIInfoFrame aiFrame, int numAssignedRoles)
	{
		super(aiFrame, numAssignedRoles);
		
		setTimeout(Long.MAX_VALUE);
		
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		startPoints.add(new Vector2((fieldLength / 2) - 100, (fieldWidth / 2) - 100));
		startPoints.add(AIConfig.getGeometry().getPenaltyMarkTheir());
		startPoints.add(new Vector2((fieldLength / 2) - 100, (-fieldWidth / 2) + 100));
		
		endPoints.add(new Vector2(-(fieldLength / 2) + 100, (-fieldWidth / 2) + 100));
		endPoints.add(AIConfig.getGeometry().getPenaltyMarkOur());
		endPoints.add(new Vector2(-(fieldLength / 2) + 100, (fieldWidth / 2) - 100));
		
		for (int roleNum = 0; roleNum < NUM_ROLES; roleNum++)
		{
			MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
			role.setPenaltyAreaAllowed(true);
			moveRoles.add(role);
			addAggressiveRole(role, startPoints.get(roleNum));
			states.add(EState.PREPARE);
		}
		
		startTimes.add(0L);
		startTimes.add(0L);
		startTimes.add(0L);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void beforeUpdate(AIInfoFrame frame)
	{
		try
		{
			if ((frame.refereeMsg != null) && (timeStarted == 0)
					&& (frame.refereeMsg.getSslRefereeMsg().getCommand() == Command.FORCE_START))
			{
				timeStarted = System.nanoTime();
			}
			if (prepare && moveRoles.get(0).checkMoveCondition() && moveRoles.get(1).checkMoveCondition()
					&& moveRoles.get(2).checkMoveCondition())
			{
				prepare = false;
				for (int roleNum = 0; roleNum < NUM_ROLES; roleNum++)
				{
					MovementCon moveCon = new MovementCon();
					moveCon.updateDestination(new Vector2(0, 0));
					moveCon.setBallObstacle(false);
					moveCon.setPenaltyAreaAllowed(true);
					moveCon.setOptimizationWanted(false);
					
					Sisyphus sis = new Sisyphus();
					ISpline splineToMid = sis.calculateSpline(moveRoles.get(roleNum).getBot(), frame.worldFrame, moveCon);
					
					// time in ms
					long time = (long) (splineToMid.getTotalTime() * 1000);
					neededTimesToCenterSpot.add(new DrivingTimeContainer(roleNum, time));
				}
				Collections.sort(neededTimesToCenterSpot);
				long difference = 4000;
				// long lastTimeout = 0;
				// long accumulatedTimeout = 0;
				long secondLongerThanFirst = neededTimesToCenterSpot.get(1).getDrivingTime()
						- neededTimesToCenterSpot.get(0).getDrivingTime();
				startTimes.set(1, difference - secondLongerThanFirst);
				long thirdLongerThanSecond = neededTimesToCenterSpot.get(2).getDrivingTime()
						- neededTimesToCenterSpot.get(1).getDrivingTime();
				startTimes.set(2, (difference * 2) - (secondLongerThanFirst + thirdLongerThanSecond));
				
				// for (DrivingTimeContainer dtc : neededTimesToCenterSpot)
				// {
				// long diff = dtc.getDrivingTime() - lastTimeout;
				// if (diff < 4000)
				// {
				// accumulatedTimeout = accumulatedTimeout + (4000 - diff);
				// startTimes.set(dtc.getRoleNum(), accumulatedTimeout);
				// }
				// lastTimeout = dtc.getDrivingTime();
				// }
			}
			int rolesDone = 0;
			for (int roleNum = 0; roleNum < NUM_ROLES; roleNum++)
			{
				MoveRole role = moveRoles.get(roleNum);
				switch (states.get(roleNum))
				{
					case PREPARE:
						if ((timeStarted != 0)
								&& ((System.nanoTime() - timeStarted) > TimeUnit.MILLISECONDS.toNanos(startTimes.get(roleNum))))
						{
							states.set(roleNum, EState.FIRST_HALF);
							role.updateDestination(endPoints.get(roleNum));
							List<IVector2> centerSpot = new ArrayList<IVector2>();
							centerSpot.add(AIConfig.getGeometry().getCenter());
							role.getMoveCon().setIntermediateStops(centerSpot);
						}
						break;
					case FIRST_HALF:
						if (role.getBot().getPos().equals(AIConfig.getGeometry().getCenter(), 100))
						{
							role.getMoveCon().setIntermediateStops(new ArrayList<IVector2>());
						}
						if (role.checkMoveCondition())
						{
							states.set(roleNum, EState.DONE);
						}
						break;
					case DONE:
						rolesDone++;
						break;
				}
			}
			
			if (rolesDone == NUM_ROLES)
			{
				changeToFinished();
			}
		} catch (Exception e)
		{
			// ignore
		}
	}
	
	
	@Override
	protected void afterUpdate(AIInfoFrame currentFrame)
	{
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
