/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;


import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.keeper.KeeperStateCalc;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.CatchOverChipState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.InterceptAndGoOutState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.RamboState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.skills.MoveToTrajSkill;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.statemachine.IState;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * KeeperRole in OneOnOne Penalty Shootout
 *
 * @author ChrisC
 */
public class KeeperOneOnOneRole extends KeeperRole
{
	
	@Configurable(comment = "Keeper rambo distance", defValue = "2000.0")
	private static double keeperRamboDistance = 2000;
	
	@Configurable(comment = "Keeper go out lookahead", defValue = "0.2")
	private static double	lookahead				= 0.2;
	
	
	/**
	 * Add a new State to the KeeperRole
	 */
	public KeeperOneOnOneRole()
	{
		super(ERole.ONE_ON_ONE_KEEPER);
		addTransition(KeeperOneOnOneEvents.RAMBO, new RamboState(this));
		addTransition(KeeperOneOnOneEvents.INTERCEPT_AND_GO_OUT, new InterceptAndGoOutState(this));
		addTransition(KeeperOneOnOneEvents.CATCH, new CatchOverChipState(this));
		addTransition(KeeperOneOnOneEvents.PREPARE_SHOOTOUT, new PreparePenaltyShootout());
	}
	
	
	public static double getLookahead()
	{
		return lookahead;
	}
	
	
	@Override
	protected void beforeUpdate()
	{
		GameState gameState = getAiFrame().getTacticalField().getGameState();
		
		KeeperStateCalc.EKeeperState keeperState = getAiFrame().getTacticalField().getKeeperState();
		if (gameState.isPreparePenaltyForThem())
		{
			triggerEvent(KeeperOneOnOneEvents.PREPARE_SHOOTOUT);
		} else if (isKeeperGoingRambo())
		{
			triggerEvent(KeeperOneOnOneEvents.RAMBO);
		} else if ((getPos().x() > getBall().getPos().x()) && !gameState.isStop())
		{
			triggerEvent(KeeperOneOnOneEvents.CATCH);
		} else if (keeperState == KeeperStateCalc.EKeeperState.INTERCEPT_BALL)
		{
			triggerEvent(KeeperOneOnOneEvents.INTERCEPT_AND_GO_OUT);
		} else
		{
			triggerEvent(keeperState);
		}
	}
	
	
	private boolean isKeeperGoingRambo()
	{
		boolean isBallCloseToGoal = Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), keeperRamboDistance);
		boolean isNotShootingAtGoal = !getAiFrame().getTacticalField().getKeeperState()
				.equals(KeeperStateCalc.EKeeperState.INTERCEPT_BALL);
		
		ITrackedBot foe = getAiFrame().getTacticalField().getEnemyClosestToBall().getBot();
		if (foe == null)
		{
			return false;
		}
		double timeFoe = TrajectoryGenerator.generatePositionTrajectory(foe, getBall().getPos()).getTotalTime();
		double timeKeeper = TrajectoryGenerator.generatePositionTrajectory(getBot(), getBall().getPos()).getTotalTime();
		boolean isKeeperFaster = timeFoe > timeKeeper;
		boolean isStopped = getAiFrame().getTacticalField().getGameState().isStop();
		boolean isBallInFrontOfKeeper = getPos().x() < getWFrame().getBall().getPos().x();
		
		boolean isRamboValid = (isBallCloseToGoal || isKeeperFaster) && isBallInFrontOfKeeper;
		return isRamboValid && !isStopped && isNotShootingAtGoal;
	}
	
	enum KeeperOneOnOneEvents implements IEvent
	{
		RAMBO,
		INTERCEPT_AND_GO_OUT,
		CATCH,
		PREPARE_SHOOTOUT
	}
	
	private class PreparePenaltyShootout implements IState
	{
		MoveToTrajSkill posSkill;
		
		
		@Override
		public void doEntryActions()
		{
			posSkill = new MoveToTrajSkill();
			posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			posSkill.getMoveCon()
					.updateDestination(Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(Geometry.getBotRadius())));
			setNewSkill(posSkill);
		}
	}
}
