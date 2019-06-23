/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 29.10.2014
 * Author(s): JulianT
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.support;

import java.util.List;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OffensiveMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EFeature;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.AMoveSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.IMoveToSkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.ISkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.statemachine.IRoleState;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Less chaotic and more rational support role.
 * Part of the implementation of intelligent supportive behavior for Small Size League Soccer Robots.
 * 
 * @author JulianT
 */
public class SupportRole extends ARole
{
	
	@Configurable(comment = "Time to wait before first repositioning [ms]")
	private static int		initialWait							= 50;
	
	@Configurable(comment = "Time to wait before repositioning if supporter doesn't have any advanced pass targets [ms]")
	private static int		noAdvancedPassTargetsWaitTime	= 300;
	
	@Configurable(comment = "Bot will move to better positions if his current advanced pass targets are ranked lower than this threshold")
	private static int		topRatingThreshold				= 10;
	
	protected IVector2		destination							= null;
	protected IMoveToSkill	moveToSkill							= null;
	
	// private static final Logger log = Logger.getLogger(SupportCalc.class.getName());
	
	
	private enum ESupportState
	{
		STOPPED,
		WAITING,
		MOVING
	}
	
	private enum ESupportEvent
	{
		STOP,
		WAIT,
		MOVE
	}
	
	
	/**
	 * Constructor. What else?
	 */
	public SupportRole()
	{
		super(ERole.SUPPORT);
		
		IRoleState stoppedState = new StoppedState();
		IRoleState waitingState = new WaitingState();
		IRoleState movingState = new MovingState();
		
		setInitialState(stoppedState);
		addTransition(ESupportEvent.STOP, stoppedState);
		addTransition(ESupportEvent.WAIT, waitingState);
		addTransition(ESupportEvent.MOVE, movingState);
	}
	
	
	@Override
	public void fillNeededFeatures(final List<EFeature> features)
	{
		features.add(EFeature.MOVE);
		features.add(EFeature.STRAIGHT_KICKER);
		features.add(EFeature.BARRIER);
	}
	
	
	/**
	 * @return Destination support position
	 */
	public IVector2 getDestination()
	{
		return destination;
	}
	
	
	/**
	 * Game is running
	 * 
	 * @author JulianT
	 */
	private class WaitingState implements IRoleState
	{
		private long	lastAdvancedPassTargetsTime	= 0;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			// at start we assume to have had advanced pass targets, so we don't move
			lastAdvancedPassTargetsTime = SumatraClock.nanoTime();
			
			moveToSkill = AMoveSkill.createMoveToSkill();
			setNewSkill(moveToSkill);
			destination = getPos();
			moveToSkill.getMoveCon().updateDestination(destination);
			
			// don't keep standing at current pos (otherwise supporters will chill out at defense positions)
			boolean isLegal = LegalPointChecker.checkPoint(getPos(), getAiFrame(), getAiFrame().getTacticalField());
			
			// Prevent cuddling supporters
			boolean isBalanced = AiMath.isBalancedSupportPosition(getPos(), getAiFrame());
			// TODO JulianT: Remove quick fix
			isBalanced = true;
			
			if (!isLegal || !isBalanced)
			{
				triggerEvent(ESupportEvent.MOVE);
			}
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			if (hasTopAdvancedPassTarget(topRatingThreshold))
			{
				lastAdvancedPassTargetsTime = SumatraClock.nanoTime();
			}
			
			boolean didntHaveAdvancedPassTargets = (SumatraClock.nanoTime() - lastAdvancedPassTargetsTime) > (noAdvancedPassTargetsWaitTime * 1000000L);
			boolean isBotPosLegal = LegalPointChecker.checkPoint(getPos(), getAiFrame(), getAiFrame().getTacticalField());
			boolean isDestLegal = LegalPointChecker.checkPoint(destination, getAiFrame(), getAiFrame().getTacticalField());
			boolean botCloseToDest = GeoMath.distancePP(getPos(), destination) < AIConfig.getGeometry().getBotRadius();
			boolean insideField = AIConfig.getGeometry().getField().isPointInShape(destination);
			boolean isRunning = getAiFrame().getTacticalField().getGameState() == EGameState.RUNNING;
			boolean gameStateChanged = getAiFrame().getPrevFrame().getTacticalField().getGameState() != getAiFrame()
					.getTacticalField().getGameState();
			
			if ((didntHaveAdvancedPassTargets && botCloseToDest && (isRunning || gameStateChanged))
					|| (!isBotPosLegal && !isDestLegal) || !insideField)
			{
				triggerEvent(ESupportEvent.MOVE);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return ESupportState.WAITING;
		}
	}
	
	
	private class MovingState implements IRoleState
	{
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			destination = getAiFrame().getTacticalField().getSupportPositions().get(getBotID());
			moveToSkill.getMoveCon().updateDestination(destination);
			moveToSkill.getMoveCon().updateLookAtTarget(destination);
		}
		
		
		@Override
		public void doExitActions()
		{
			// set orientation the same way as offensive will do if it passes to this bot, to minimize needed repositioning
			IVector2 target = getAiFrame().getTacticalField().getBestDirectShootTarget();
			
			if (target == null)
			{
				target = AIConfig.getGeometry().getGoalTheir().getGoalCenter();
			}
			
			if (OffensiveMath.isBallRedirectPossible(getWFrame(), getPos(), target))
			{
				IVector3 poss = AiMath.calcRedirectPositions(getBot(), destination, target.subtractNew(destination)
						.getAngle(), getWFrame()
						.getBall(),
						target,
						4.0f);
				float orientation = poss.z();
				moveToSkill.getMoveCon().updateTargetAngle(orientation);
			} else
			{
				moveToSkill.getMoveCon().updateLookAtTarget(getWFrame().getBall().getPos());
			}
		}
		
		
		@Override
		public void doUpdate()
		{
			if (GeoMath.distancePP(getBot(), destination) < AIConfig.getGeometry().getBotRadius())
			{
				triggerEvent(ESupportEvent.WAIT);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return ESupportEvent.MOVE;
		}
	}
	
	
	/**
	 * Game is on halt
	 * 
	 * @author JulianT
	 */
	private class StoppedState implements IRoleState
	{
		private long	startTime;
		
		
		@Override
		public void onSkillStarted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void onSkillCompleted(final ISkill skill, final BotID botID)
		{
		}
		
		
		@Override
		public void doEntryActions()
		{
			startTime = SumatraClock.nanoTime();
		}
		
		
		@Override
		public void doExitActions()
		{
		}
		
		
		@Override
		public void doUpdate()
		{
			long runningTime = (SumatraClock.nanoTime() - startTime);
			if (runningTime >= (initialWait * 1000000L))
			{
				triggerEvent(ESupportEvent.WAIT);
			}
		}
		
		
		@Override
		public Enum<? extends Enum<?>> getIdentifier()
		{
			return ESupportState.STOPPED;
		}
	}
	
	
	/**
	 * Checks if the bot has an advanced pass target among the top x advanced pass targets.
	 */
	private boolean hasTopAdvancedPassTarget(final int cutoff)
	{
		int i = 0;
		
		for (AdvancedPassTarget target : getAiFrame().getTacticalField().getAdvancedPassTargetsRanked())
		{
			if (i > cutoff)
			{
				return false;
			}
			
			if (getBotID().equals(target.getBotId()))
			{
				return true;
			}
			
			i++;
		}
		
		return false;
	}
}
