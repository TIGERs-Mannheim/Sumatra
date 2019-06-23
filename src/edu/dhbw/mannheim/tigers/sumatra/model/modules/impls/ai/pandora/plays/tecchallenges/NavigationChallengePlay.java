/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 21, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.spline.ISpline;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.conditions.move.MovementCon;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.tecchallenges.navigation.DrivingTimeContainer;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.move.MoveRole.EMoveBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;


/**
 * Technical challenge for 2013
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
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
	 */
	public NavigationChallengePlay()
	{
		super(EPlay.NAVIGATION_CHALLENCE);
		
		float fieldLength = AIConfig.getGeometry().getFieldLength();
		float fieldWidth = AIConfig.getGeometry().getFieldWidth();
		startPoints.add(new Vector2((fieldLength / 2) - 100, (fieldWidth / 2) - 100));
		startPoints.add(AIConfig.getGeometry().getPenaltyMarkTheir());
		startPoints.add(new Vector2((fieldLength / 2) - 100, (-fieldWidth / 2) + 100));
		
		endPoints.add(new Vector2(-(fieldLength / 2) + 100, (-fieldWidth / 2) + 100));
		endPoints.add(AIConfig.getGeometry().getPenaltyMarkOur());
		endPoints.add(new Vector2(-(fieldLength / 2) + 100, (fieldWidth / 2) - 100));
		
		startTimes.add(0L);
		startTimes.add(0L);
		startTimes.add(0L);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	protected void doUpdate(final AthenaAiFrame frame)
	{
		if ((frame.getNewRefereeMsg() != null) && (timeStarted == 0)
				&& (frame.getNewRefereeMsg().getCommand() == Command.FORCE_START))
		{
			timeStarted = System.nanoTime();
		}
		boolean rolesPrepared = true;
		for (MoveRole role : moveRoles)
		{
			if (!role.checkMoveCondition())
			{
				rolesPrepared = false;
				break;
			}
		}
		if (prepare && rolesPrepared)
		{
			prepare = false;
			for (int roleNum = 0; roleNum < getRoles().size(); roleNum++)
			{
				MovementCon moveCon = new MovementCon();
				moveCon.updateDestination(new Vector2(0, 0));
				moveCon.setBallObstacle(false);
				moveCon.setPenaltyAreaAllowed(true);
				moveCon.setOptimizationWanted(false);
				
				Sisyphus sis = new Sisyphus();
				ISpline splineToMid = sis.calculateSpline(moveRoles.get(roleNum).getBotID(), frame.getWorldFrame(),
						moveCon);
				
				// time in ms
				long time = (long) (splineToMid.getTotalTime() * 1000);
				neededTimesToCenterSpot.add(new DrivingTimeContainer(roleNum, time));
			}
			Collections.sort(neededTimesToCenterSpot);
			long difference = 4000;
			// long lastTimeout = 0;
			// long accumulatedTimeout = 0;
			if (getRoles().size() > 1)
			{
				long secondLongerThanFirst = neededTimesToCenterSpot.get(1).getDrivingTime()
						- neededTimesToCenterSpot.get(0).getDrivingTime();
				startTimes.set(1, difference - secondLongerThanFirst);
				if (getRoles().size() > 2)
				{
					long thirdLongerThanSecond = neededTimesToCenterSpot.get(2).getDrivingTime()
							- neededTimesToCenterSpot.get(1).getDrivingTime();
					startTimes.set(2, (difference * 2) - (secondLongerThanFirst + thirdLongerThanSecond));
				}
			}
			
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
		for (int roleNum = 0; roleNum < getRoles().size(); roleNum++)
		{
			MoveRole role = moveRoles.get(roleNum);
			switch (states.get(roleNum))
			{
				case PREPARE:
					if ((timeStarted != 0)
							&& ((System.nanoTime() - timeStarted) > TimeUnit.MILLISECONDS.toNanos(startTimes.get(roleNum))))
					{
						states.set(roleNum, EState.FIRST_HALF);
						role.getMoveCon().updateDestination(endPoints.get(roleNum));
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
		
		if (rolesDone == getRoles().size())
		{
			changeToFinished();
		}
	}
	
	
	@Override
	protected ARole onRemoveRole()
	{
		ARole role = getLastRole();
		moveRoles.remove(role);
		return role;
	}
	
	
	@Override
	protected ARole onAddRole()
	{
		MoveRole role = new MoveRole(EMoveBehavior.NORMAL);
		moveRoles.add(role);
		states.add(EState.PREPARE);
		role.getMoveCon().updateDestination(startPoints.get(moveRoles.size() - 1));
		role.getMoveCon().setPenaltyAreaAllowed(true);
		return (role);
	}
	
	
	@Override
	protected void onGameStateChanged(final EGameState gameState)
	{
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
