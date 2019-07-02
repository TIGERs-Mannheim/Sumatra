/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.pandora.roles.keeper;


import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.CatchOverChipState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.CriticalKeeperState;
import edu.tigers.sumatra.ai.pandora.roles.keeper.states.RamboState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.line.v2.ILine;
import edu.tigers.sumatra.math.line.v2.Lines;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.skillsystem.skills.AMoveToSkill;
import edu.tigers.sumatra.statemachine.AState;
import edu.tigers.sumatra.statemachine.IEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * KeeperRole in OneOnOne Penalty ShootoutRound
 *
 * @author ChrisC
 */
public class KeeperOneOnOneRole extends KeeperRole
{
	
	@Configurable(comment = "Keeper go out lookahead", defValue = "0.2")
	private static double lookahead = 0.2;
	
	
	/**
	 * Add a new State to the KeeperRole
	 */
	public KeeperOneOnOneRole()
	{
		super(ERole.ONE_ON_ONE_KEEPER);
		addTransition(KeeperOneOnOneEvents.RAMBO, new RamboState(this));
		addTransition(KeeperOneOnOneEvents.NORMAL, new CriticalKeeperState(this));
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
		double keeperRamboDistance = getAiFrame().getTacticalField().getKeeperRamboDistance();
		
		EKeeperState keeperState = getAiFrame().getTacticalField().getKeeperState();
		if (gameState.isPreparePenaltyForThem())
		{
			triggerEvent(KeeperOneOnOneEvents.PREPARE_SHOOTOUT);
		} else if (isKeeperGoingRambo(keeperRamboDistance))
		{
			// Move to ball
			triggerEvent(KeeperOneOnOneEvents.RAMBO);
		} else if ((getPos().x() > getBall().getPos().x()) && !gameState.isStop())
		{
			triggerEvent(KeeperOneOnOneEvents.CATCH);
		} else if (keeperState == EKeeperState.CRITICAL)
		{
			triggerEvent(KeeperOneOnOneEvents.NORMAL);
		} else
		{
			triggerEvent(keeperState);
		}
	}
	
	
	private boolean isKeeperGoingRambo(double keeperRamboDistance)
	{
		boolean isBallCloseToGoal = Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), keeperRamboDistance);
		boolean isNotShootingAtGoal = !getAiFrame().getTacticalField().getKeeperState()
				.equals(EKeeperState.CRITICAL);
		boolean isKeeperOnLine = false;
		
		if (!isNotShootingAtGoal)
		{
			ILine line = Lines.lineFromDirection(getBall().getPos(), getBall().getVel());
			isKeeperOnLine = line.distanceTo(getPos()) < Geometry.getBotRadius() / 2.;
		}
		
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
		return isRamboValid && !isStopped && (isNotShootingAtGoal || isKeeperOnLine);
	}
	
	enum KeeperOneOnOneEvents implements IEvent
	{
		RAMBO,
		NORMAL,
		CATCH,
		PREPARE_SHOOTOUT
	}
	
	private class PreparePenaltyShootout extends AState
	{
		AMoveToSkill posSkill;

		
		@Override
		public void doEntryActions()
		{
			posSkill = AMoveToSkill.createMoveToSkill();
			posSkill.getMoveCon().setPenaltyAreaAllowedOur(true);
			posSkill.getMoveCon()
					.updateDestination(Geometry.getGoalOur().getCenter().addNew(Vector2.fromX(Geometry.getBotRadius())));
			setNewSkill(posSkill);
		}
	}
}
