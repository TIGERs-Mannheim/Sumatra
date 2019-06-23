/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.12.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense;

import java.awt.Color;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Referee.SSL_Referee.Command;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.GeoMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.OffensiveMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy.EOffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.SpecialMoveCommand;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.Circle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.circle.DrawableCircle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.triangle.DrawableTriangle;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.IBotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition.EOffensiveMoveType;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ERole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.ABot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBotV3;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.clock.SumatraClock;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


/**
 * Calculates OffenseStrategy for the OffenseRole.
 * 
 * @author Mark Geiger <Mark.Geiger@dlr.de>
 */
public class OffensiveStrategyCalc extends ACalculator
{
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected static final Logger	log							= Logger
																					.getLogger(OffensiveStrategyCalc.class.getName());
	private long						cheeringTimer				= 0;
	
	private OffensiveStrategy		offensiveStrategy			= new OffensiveStrategy();
	
	private boolean					waitForNormalStart		= false;
	
	private boolean					kickoff						= false;
	
	private long						delayTimer					= 0;
	
	private float						waitTime						= 0;
	
	private boolean					waitForHelperResponse	= false;
	
	private SpecialMoveCommand		prevCommand					= null;
	
	private TrackedTigerBot			primaryBot					= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// -------------------------------------- ------------------------------------
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		offensiveStrategy = new OffensiveStrategy();
		calculateOffensiveStrategy(newTacticalField, baseAiFrame);
	}
	
	
	private void calculateOffensiveStrategy(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
		{
			// default all bots are getter.
			offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
		}
		
		BotID bestGetter = OffensiveMath.getBestGetter(baseAiFrame, newTacticalField);
		BotID bestRedirector = OffensiveMath.getBestRedirector(baseAiFrame.getWorldFrame(),
				baseAiFrame.getWorldFrame().tigerBotsAvailable);
		// only for drawings !
		getBestRedirector(baseAiFrame.getWorldFrame(), baseAiFrame.getWorldFrame().tigerBotsAvailable, newTacticalField);
		
		isEpicCornerKickPossible(newTacticalField, baseAiFrame);
		
		if (baseAiFrame.getPrevFrame() != null)
		{
			if (baseAiFrame.getPrevFrame().getPlayStrategy() != null)
			{
				if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE) != null)
				{
					for (ARole role : baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(ERole.OFFENSIVE))
					{
						if (role.getBotID() == bestRedirector)
						{
							// disable switch to redirector when the best redirector bot already tries to kick the ball
							if ((role.getCurrentState() == EOffensiveStrategy.KICK) &&
									(GeoMath.distancePP(role.getPos(), baseAiFrame.getWorldFrame().getBall().getPos()) < 500f))
							{
								bestRedirector = null;
							}
						}
					}
				}
			}
		}
		
		if (bestRedirector != null)
		{
			// add best Redirector.
			offensiveStrategy.getDesiredBots().clear();
			offensiveStrategy.getDesiredBots().add(bestRedirector);
			offensiveStrategy.getCurrentOffensivePlayConfiguration().put(bestRedirector,
					EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE);
			offensiveStrategy.setMinNumberOfBots(1);
			offensiveStrategy.setMaxNumberOfBots(1);
			primaryBot = baseAiFrame.getWorldFrame().getTigerBotsVisible().get(bestRedirector);
		} else if (bestGetter != null)
		{
			offensiveStrategy.getDesiredBots().clear();
			offensiveStrategy.getDesiredBots().add(bestGetter);
			offensiveStrategy.setMinNumberOfBots(0);
			offensiveStrategy.setMaxNumberOfBots(1);
			primaryBot = baseAiFrame.getWorldFrame().getTigerBotsVisible().get(bestGetter);
		}
		
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles() != null)
		{
			// check if more than 1 offensive bot is needed ( passing, kickoff, ... )
			addSecondaryOffensiveBots(newTacticalField, baseAiFrame);
			
			// change States of current offensiveBots if a change is needed
			calculateActiveStateChanges(newTacticalField, baseAiFrame);
		}
		
		// read currentGameState and adjust/change current States of all offensiveRoles
		reactOnRefSignals(newTacticalField, baseAiFrame);
		keepActiveStatus(baseAiFrame.getWorldFrame(), baseAiFrame, newTacticalField, newTacticalField.getGameState());
		
		if (AIConfig.getGeometry().getPenaltyAreaOur()
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos(), 300) ||
				!AIConfig.getGeometry().getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos()))
		{
			offensiveStrategy.setMaxNumberOfBots(0);
			offensiveStrategy.setMinNumberOfBots(0);
			offensiveStrategy.getDesiredBots().clear();
		}
		
		// special case for mixed Team challenge
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() == EAIControlState.MIXED_TEAM_MODE)
		{
			// log.warn("bla");
			try
			{
				mixedChallengeReceiveOffensiveInformations(newTacticalField, baseAiFrame);
				// mixedChallengeSendOffensiveInformations(newTacticalField, baseAiFrame);
			} catch (Exception e)
			{
				log.warn("Error in OffensiveStrategyCalc: could not parse MultiTeamMessage", e);
			}
		}
		
		newTacticalField.setOffensiveStrategy(offensiveStrategy);
		if (OffensiveConstants.isSupportiveAttackerEnabled() &&
				(baseAiFrame.getWorldFrame().getBall().getPos().x() > -1500f))
		{
			if ((offensiveStrategy.getDesiredBots() != null) && (primaryBot != null))
			{
				if (((bestGetter == primaryBot.getId()) && (((offensiveStrategy.getDesiredBots().size() == 1))
				&& (newTacticalField.getGameState() == EGameState.RUNNING))))
				{
					IBotIDMap<TrackedTigerBot> bots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField,
							baseAiFrame);
					bots.remove(bestGetter);
					BotID secondBestGetter = OffensiveMath.getBestGetter(baseAiFrame, bots, newTacticalField);
					
					if (!offensiveStrategy.getDesiredBots().contains(secondBestGetter))
					{
						offensiveStrategy.getDesiredBots().add(secondBestGetter);
						offensiveStrategy.getCurrentOffensivePlayConfiguration().put(secondBestGetter,
								EOffensiveStrategy.SUPPORTIVE_ATTACKER);
						offensiveStrategy.setMaxNumberOfBots(2);
					}
				}
			}
		}
		
		
		// This is really important!
		cheerWhenWeShootAGoal(newTacticalField, baseAiFrame);
	}
	
	
	private void gameStateChange(final WorldFrame wFrame, final EGameState from, final EGameState to)
	{
		switch (to)
		{
			case DIRECT_KICK_THEY:
			case THROW_IN_THEY:
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.INTERCEPT);
				}
				break;
			case CORNER_KICK_THEY:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
			case GOAL_KICK_THEY:
			case HALTED:
			case PREPARE_PENALTY_THEY:
			case PREPARE_KICKOFF_THEY:
			case PREPARE_PENALTY_WE:
			case STOPPED:
				kickoff = false;
				// planedStrategy = EOffensiveStrategy.STOP;
				// waitingForNormalStart = false;
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.STOP);
				}
				break;
			case PREPARE_KICKOFF_WE:
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
				}
				if (primaryBot != null)
				{
					offensiveStrategy.getDesiredBots().add(primaryBot.getId());
				}
				// inKickoff = true;
				kickoff = true;
				// delayTimer = SumatraClock.currentTimeMillis();
				// planedStrategy = EOffensiveStrategy.DELAY;
				waitForNormalStart = true;
				break;
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
				// forcePass = true;
			case DIRECT_KICK_WE:
				
				delayTimer = SumatraClock.nanoTime();
				waitForNormalStart = false;
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.DELAY);
				}
				waitTime = OffensiveConstants.getDelayWaitTime();
				
				break;
			case RUNNING:
				delayTimer = 0;
				// kickOffHelperBot = null;
				kickoff = false;
				waitForNormalStart = false;
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
				}
				break;
			default:
				break;
		}
	}
	
	
	private void reactOnRefSignals(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		WorldFrame wFrame = baseAiFrame.getWorldFrame();
		EGameState gameState = newTacticalField.getGameState();
		EGameState preGameState = baseAiFrame.getPrevFrame().getTacticalField().getGameState();
		
		if (!preGameState.equals(gameState))
		{
			gameStateChange(wFrame, preGameState, gameState);
		}
		
		if (baseAiFrame.getNewRefereeMsg() != null)
		{
			if (baseAiFrame.getNewRefereeMsg().getCommand().equals(Command.NORMAL_START))
			{
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
				}
			}
		}
	}
	
	
	private void keepActiveStatus(final WorldFrame wFrame, final BaseAiFrame baseAiFrame,
			final TacticalField newTacticalField, final EGameState gameState)
	{
		switch (gameState)
		{
		
			case THROW_IN_THEY:
			case DIRECT_KICK_THEY:
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.INTERCEPT);
				}
				offensiveStrategy.getDesiredBots().clear();
				offensiveStrategy.getDesiredBots().add(null);
				offensiveStrategy.setMaxNumberOfBots(1);
				break;
			case CORNER_KICK_THEY:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
			case GOAL_KICK_THEY:
			case HALTED:
			case PREPARE_PENALTY_THEY:
			case PREPARE_KICKOFF_THEY:
			case PREPARE_PENALTY_WE:
				offensiveStrategy.getDesiredBots().clear();
				offensiveStrategy.setMaxNumberOfBots(0);
				offensiveStrategy.setMinNumberOfBots(0);
				break;
			case STOPPED:
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.STOP);
				}
				// no offensibe in stop.
				// offensiveStrategy.getDesiredBots().clear();
				offensiveStrategy.setMaxNumberOfBots(1);
				offensiveStrategy.setMinNumberOfBots(0);
				break;
			case PREPARE_KICKOFF_WE:
				if (waitForNormalStart)
				{
					// offensiveStrategy.getDesiredBots().clear();
					// offensiveStrategy.getDesiredBots().add(best);
					offensiveStrategy.setMaxNumberOfBots(1);
					offensiveStrategy.getCurrentOffensivePlayConfiguration().clear();
					offensiveStrategy.getUnassignedStrategies().add(EOffensiveStrategy.STOP);
				}
				break;
			case CORNER_KICK_WE:
			case GOAL_KICK_WE:
			case THROW_IN_WE:
			case DIRECT_KICK_WE:
				// if (delayTimer)
				if ((waitTime != 0) && (((SumatraClock.nanoTime() - delayTimer) * 1e-9) < waitTime))
				{
					for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
					{
						offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.DELAY);
					}
					offensiveStrategy.getDesiredBots().clear();
					if (primaryBot != null)
					{
						offensiveStrategy.getDesiredBots().add(primaryBot.getId());
					}
					offensiveStrategy.setMaxNumberOfBots(1);
				}
				break;
			case RUNNING:
				// nothing todo here
				break;
			default:
				break;
		}
	}
	
	
	private void addSecondaryOffensiveBots(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IRecordFrame prevFrame = baseAiFrame.getPrevFrame();
		for (IVector2 pos : prevFrame.getAICom().getDelayMoves())
		{
			SpecialMoveCommand command = new SpecialMoveCommand();
			command.getMovePosition().add(pos);
			command.getMoveTimes().add(0f);
			command.setResponseStep(0);
			offensiveStrategy.getSpecialMoveCommands().add(command);
			offensiveStrategy.getUnassignedStrategies().add(EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE);
			offensiveStrategy.getDesiredBots().add(null);
			offensiveStrategy.setMaxNumberOfBots(offensiveStrategy.getMaxNumberOfBots() + 1);
		}
		
		if ((prevFrame.getAICom().getOffensiveRolePassTargetID() != null) &&
				(newTacticalField.getGameState() != EGameState.PREPARE_KICKOFF_WE))
		{
			BotID passReceiver = prevFrame.getAICom().getOffensiveRolePassTargetID();
			SpecialMoveCommand command = new SpecialMoveCommand();
			
			command.getMovePosition().add(prevFrame.getAICom().getOffensiveRolePassTarget());
			command.getMoveTimes().add(0f);
			command.setResponseStep(0);
			
			if (offensiveStrategy.getCurrentOffensivePlayConfiguration().containsKey(passReceiver))
			{
				if (offensiveStrategy.getCurrentOffensivePlayConfiguration()
						.get(passReceiver) == EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE)
				{
					if (prevCommand != null)
					{
						command = prevCommand;
					}
				}
			}
			
			// // if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() ==
			// EAIControlState.MIXED_TEAM_MODE)
			if (baseAiFrame.getWorldFrame().tigerBotsVisible.containsKey(passReceiver) &&
					!baseAiFrame.getWorldFrame().tigerBotsAvailable.containsKey(passReceiver))
			{
				// pass reciever is not a tigers bot but friendly. ( or not availible )
				
			} else if (GeoMath.distancePP(primaryBot.getPos(), baseAiFrame.getWorldFrame().getBall().getPos()) < (OffensiveConstants
					.getFinalKickStateDistance() * 1.5))
			{
				offensiveStrategy.getDesiredBots().add(passReceiver);
				offensiveStrategy.setMaxNumberOfBots(offensiveStrategy.getMaxNumberOfBots() + 1);
				offensiveStrategy.getSpecialMoveCommands().add(command);
				offensiveStrategy.getCurrentOffensivePlayConfiguration().put(passReceiver,
						EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE);
				prevCommand = command;
			}
		}
		
		if (((kickoff == true) && (waitForNormalStart == false)))
		{
			for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
			{
				offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
			}
		}
	}
	
	
	private void calculateActiveStateChanges(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		// change state of GET to KICK and KICK to GET
		checkBallObtained(newTacticalField, baseAiFrame);
		checkNormalStartCalled(newTacticalField, baseAiFrame);
		checkDelayCounter(newTacticalField, baseAiFrame);
	}
	
	
	private void checkNormalStartCalled(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getNewRefereeMsg() != null)
		{
			if ((baseAiFrame.getNewRefereeMsg().getCommand() == Command.NORMAL_START)
					&& (newTacticalField.getGameState() == EGameState.PREPARE_KICKOFF_WE))
			{
				waitForNormalStart = false;
				delayTimer = SumatraClock.nanoTime();
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
				}
				// its the minimum waitTime.
				waitTime = OffensiveConstants.getDelayWaitTime(); // this time doesnt matter, because it gets triggered by
				// the
			}
		}
	}
	
	
	private void checkDelayCounter(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getPrevFrame().getAICom().hasResponded()
		/* || (((SumatraClock.nanoTime() - kickOffTimer) * 1e-9) > kickOffTimeout) */)
		{
			waitForHelperResponse = false;
			if (newTacticalField.getGameState() == EGameState.PREPARE_KICKOFF_WE)
			{
				// specialKickoffTimer = SumatraClock.nanoTime();
			}
		}
		if ((waitTime != 0) && (waitForHelperResponse == false))
		{
			if (((SumatraClock.nanoTime() - delayTimer) * 1e-9) > waitTime)
			{
				waitTime = 0;
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					EOffensiveStrategy state = offensiveStrategy.getCurrentOffensivePlayConfiguration().get(key);
					if ((state == EOffensiveStrategy.DELAY))
					{
						offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
					}
				}
			}
		}
	}
	
	
	private void checkBallObtained(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		IVector2 botPos = null;
		IVector2 destination = null;
		float distanceToBall = 655;
		float distanceToDestination = 740;
		float checkDistance = OffensiveConstants.getFinalKickStateDistance();
		float normalDist = 200f;
		
		// für alle current bots die in GET oder KICK sind
		for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
		{
			EOffensiveStrategy state = offensiveStrategy.getCurrentOffensivePlayConfiguration().get(key);
			if ((state != EOffensiveStrategy.GET) && (state != EOffensiveStrategy.KICK))
			{
				continue;
			}
			if (!baseAiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(key))
			{
				continue;
			}
			if (AIConfig.getGeometry().getPenaltyAreaTheir().isPointInShape(ballPos))
			{
				continue;
			}
			
			botPos = baseAiFrame.getWorldFrame().getTigerBotsAvailable().get(key).getPos();
			destination = newTacticalField.getOffenseMovePositions().get(key);
			
			IVector2 botToBallNormal = ballPos.subtractNew(botPos).getNormalVector();
			IVector2 kt1 = botPos.addNew(botToBallNormal.multiplyNew(normalDist));
			IVector2 kt2 = botPos.addNew(botToBallNormal.multiplyNew(-normalDist));
			
			IVector2 bt1 = ballPos.addNew(botToBallNormal.multiplyNew(normalDist / 4f));
			IVector2 bt2 = ballPos.addNew(botToBallNormal.multiplyNew(-normalDist / 4f));
			
			bt1 = bt1.addNew(botPos.subtractNew(bt1).normalizeNew().multiplyNew(checkDistance));
			bt2 = bt2.addNew(botPos.subtractNew(bt2).normalizeNew().multiplyNew(checkDistance));
			
			DrawableTriangle tri = new DrawableTriangle(bt1, kt1, botPos, new Color(0, 50, 200, 20));
			DrawableTriangle tri2 = new DrawableTriangle(bt2, botPos, kt2, new Color(0, 50, 200, 20));
			DrawableTriangle tri3 = new DrawableTriangle(bt1, bt2, botPos, new Color(0, 50, 200, 20));
			tri.setFill(true);
			tri2.setFill(true);
			tri3.setFill(true);
			
			if ((state == EOffensiveStrategy.GET)
					&& (newTacticalField.getOffenseMovePositions() != null)
					&& (newTacticalField.getOffenseMovePositions().get(key) != null)
					&& (newTacticalField.getOffenseMovePositions().get(key).getType() != null))
			{
				EOffensiveMoveType type = newTacticalField.getOffenseMovePositions().get(key).getType();
				if (((GeoMath.distancePP(botPos, destination) < (distanceToDestination)) && (GeoMath.distancePP(botPos,
						ballPos) < (distanceToBall))) && (type != EOffensiveMoveType.UNREACHABLE))
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.KICK);
				}
				
				boolean freeOfBots = true;
				
				for (BotID obstacle : baseAiFrame.getWorldFrame().getTigerBotsVisible().keySet())
				{
					if (tri.isPointInShape(baseAiFrame.getWorldFrame().getTigerBotsVisible().get(obstacle).getPos()) ||
							tri2.isPointInShape(baseAiFrame.getWorldFrame().getTigerBotsVisible().get(obstacle).getPos()) ||
							tri3.isPointInShape(baseAiFrame.getWorldFrame().getTigerBotsVisible().get(obstacle).getPos()))
					{
						if (obstacle != key)
						{
							freeOfBots = false;
							break;
						}
					}
				}
				for (BotID obstacle : baseAiFrame.getWorldFrame().getFoeBots().keySet())
				{
					if (tri.isPointInShape(baseAiFrame.getWorldFrame().getFoeBots().get(obstacle).getPos()) ||
							tri2.isPointInShape(baseAiFrame.getWorldFrame().getFoeBots().get(obstacle).getPos()) ||
							tri3.isPointInShape(baseAiFrame.getWorldFrame().getFoeBots().get(obstacle).getPos()))
					{
						if (obstacle != key)
						{
							freeOfBots = false;
							break;
						}
					}
				}
				if (freeOfBots)
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.KICK);
				}
				
				if (offensiveStrategy.getCurrentOffensivePlayConfiguration().containsKey(key))
				{
					if ((offensiveStrategy.getCurrentOffensivePlayConfiguration().get(key) == EOffensiveStrategy.KICK)
							&& (GeoMath.distancePP(botPos, ballPos) > (checkDistance + 50f)))
					{
						newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(tri);
						newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(tri2);
						newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE).add(tri3);
					}
				}
			}
		}
		return;
	}
	
	
	private void cheerWhenWeShootAGoal(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getNewRefereeMsg() != null)
		{
			ETeamColor color = baseAiFrame.getWorldFrame().getTeamColor();
			if (color == ETeamColor.YELLOW)
			{
				if (baseAiFrame.getNewRefereeMsg().getCommand() == Command.GOAL_YELLOW)
				{
					cheeringTimer = SumatraClock.nanoTime();
					for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
					{
						ABot bot = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getBot();
						
						if (bot instanceof TigerBotV3)
						{
							((TigerBotV3) bot).setCheering(true);
						}
					}
				}
			} else
			{
				if (baseAiFrame.getNewRefereeMsg().getCommand() == Command.GOAL_BLUE)
				{
					cheeringTimer = SumatraClock.nanoTime();
					for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
					{
						ABot bot = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getBot();
						
						if (bot instanceof TigerBotV3)
						{
							((TigerBotV3) bot).setCheering(true);
						}
					}
				}
			}
		}
		if ((((SumatraClock.nanoTime() - cheeringTimer) * 1e-9) > OffensiveConstants.getCheeringStopTimer())
				&& (cheeringTimer != 0))
		{
			for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
			{
				ABot bot = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getBot();
				
				if (bot instanceof TigerBotV3)
				{
					((TigerBotV3) bot).setCheering(false);
				}
			}
			cheeringTimer = 0;
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	/**
	 * @return the kickOffTarget
	 */
	public static IVector2 getKickOffTarget()
	{
		return new Vector2(
				(AIConfig.getGeometry().getFieldLength() / 4.5f),
				AIConfig.getGeometry().getFieldWidth() / 2.5f);
	}
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		offensiveStrategy = new OffensiveStrategy();
		offensiveStrategy.setMinNumberOfBots(0);
		offensiveStrategy.setMaxNumberOfBots(1);
		offensiveStrategy.getUnassignedStrategies().add(EOffensiveStrategy.GET);
		offensiveStrategy.getDesiredBots().add(null);
		newTacticalField.setOffensiveStrategy(offensiveStrategy);
	}
	
	
	/**
	 * @param wFrame
	 * @param bots
	 * @param newTacticalField
	 * @return
	 */
	public static BotID getBestRedirector(final WorldFrame wFrame,
			final IBotIDMap<TrackedTigerBot> bots, final TacticalField newTacticalField)
	{
		if (wFrame.getBall().getVel().getLength2() > 0.5f)
		{
			final float REDIRECT_TOLERANCE = 350f;
			IVector2 ballPos = wFrame.getBall().getPos();
			IVector2 ballVel = wFrame.getBall().getVel();
			
			IVector2 left = new Vector2(ballVel.getAngle() - 0.2f).normalizeNew();
			IVector2 right = new Vector2(ballVel.getAngle() + 0.2f).normalizeNew();
			
			IVector2 futureBall = wFrame.getBall().getPosByVel(0f);
			float dist = GeoMath.distancePP(ballPos, futureBall) - REDIRECT_TOLERANCE;
			
			IVector2 lp = ballPos.addNew(left.multiplyNew(dist));
			IVector2 rp = ballPos.addNew(right.multiplyNew(dist));
			
			IVector2 lp0 = ballPos.addNew(left.multiplyNew(dist + REDIRECT_TOLERANCE));
			IVector2 rp0 = ballPos.addNew(right.multiplyNew(dist + REDIRECT_TOLERANCE));
			
			DrawableTriangle dtri = new DrawableTriangle(ballPos, lp, rp, new Color(255, 0, 0, 100));
			dtri.setFill(true);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
					.add(dtri);
			
			DrawableTriangle dtri2 = new DrawableTriangle(ballPos, lp0, rp0, new Color(0, 0, 255, 22));
			dtri2.setFill(true);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
					.add(dtri2);
			DrawableCircle dc = new DrawableCircle(
					new Circle(ballPos.addNew(ballVel.normalizeNew().multiplyNew(150f)), 200f), new Color(255, 0, 0, 100));
			dc.setFill(true);
			newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
					.add(dc);
			
			BotID minID = null;
			float minDist = Float.MAX_VALUE;
			for (BotID key : bots.keySet())
			{
				IVector2 pos = bots.get(key).getPos();
				IVector2 kpos = AiMath.getBotKickerPos(bots.get(key));
				if (dtri.isPointInShape(pos) || dc.isPointInShape(pos)
						|| dtri.isPointInShape(kpos) || dc.isPointInShape(kpos))
				{
					newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
							.add(new DrawableCircle(new Circle(pos, 150f), Color.black));
					if (GeoMath.distancePP(pos, ballPos) < minDist)
					{
						minDist = GeoMath.distancePP(pos, ballPos);
						minID = key;
					}
				}
			}
			if (minID != null)
			{
				IVector2 pos = wFrame.getTigerBotsAvailable().get(minID).getPos();
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
						.add(new DrawableCircle(new Circle(pos, 150f), Color.CYAN));
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
						.add(new DrawableCircle(new Circle(pos, 146f), Color.CYAN));
				newTacticalField.getDrawableShapes().get(EDrawableShapesLayer.OFFENSIVE)
						.add(new DrawableCircle(new Circle(pos, 142f), Color.CYAN));
				return minID;
			}
		}
		return null;
	}
	
	
	// private void mixedChallengeSendOffensiveInformations(final TacticalField newTacticalField,
	// final BaseAiFrame baseAiFrame)
	// {
	// if (primaryBot != null)
	// {
	// IVector2 moveTarget = newTacticalField.getOffenseMovePositions().get(primaryBot.getId());
	// IVector2 shotTarget = null;
	//
	// if (newTacticalField.getOffensiveActions().get(primaryBot.getId()).getType() == EOffensiveAction.PASS)
	// {
	// shotTarget = newTacticalField.getOffensiveActions().get(primaryBot.getId()).getPassTarget();
	// } else
	// {
	// shotTarget = newTacticalField.getOffensiveActions().get(primaryBot.getId())
	// .getDirectShotAndClearingTarget();
	// }
	//
	// MultiTeamMessage mes = new MultiTeamMessage();
	// mes.addRobotPlan(primaryBot.getId(), RobotRole.Offense, moveTarget, 0f/* angle */, shotTarget);
	//
	// // MultiTeamMessageSender sender = new MultiTeamMessageSender();
	// // sender.send(mes);
	// }
	// }
	
	private void isEpicCornerKickPossible(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		// IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		// if (newTacticalField.getGameState() == EGameState.CORNER_KICK_WE)
		// {
		// boolean leftCorner = false;
		// if (Math.signum(ballPos.y()) < 0)
		// {
		// leftCorner = true;
		// }
		// // IVector2 passTarget = newTacticalField.getOffensiveActions().get(primaryBot.getId()).getPassTarget();
		//
		// // Dreieck von ball zu PassTarget, ist was im weg ?
		// // preposition target bestimmen.
		//
		//
		// }
	}
	
	
	private void mixedChallengeReceiveOffensiveInformations(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if ((newTacticalField.getMultiTeamMessage() != null) &&
				(newTacticalField.getMultiTeamMessage().getTeamPlan() != null) &&
				(newTacticalField.getMultiTeamMessage().getTeamPlan().getPlansList() != null))
		{
			for (RobotPlan plan : newTacticalField.getMultiTeamMessage().getTeamPlan().getPlansList())
			{
				BotID bot = BotID.createBotId(plan.getRobotId(), baseAiFrame.getWorldFrame().getTeamColor());
				if ((plan.getRole() == RobotRole.Offense)
						&& !baseAiFrame.getWorldFrame().tigerBotsAvailable.containsKey(bot))
				{
					if ((plan.getNavTarget() != null) && (plan.getNavTarget().getLoc() != null))
					{
						int x = plan.getNavTarget().getLoc().getX();
						int y = plan.getNavTarget().getLoc().getY();
						IVector2 moveTarget = new Vector2(x, y);
						if (GeoMath.distancePP(moveTarget, baseAiFrame.getWorldFrame().getBall().getPos()) < 1000)
						{
							if (GeoMath.distancePP(primaryBot.getPos(), baseAiFrame.getWorldFrame().getBall().getPos()) > GeoMath
									.distancePP(moveTarget, baseAiFrame.getWorldFrame().getBall().getPos()))
							{
								// feundlicher bot will an ball fahren ist offensive und er ist näher am ball als ich !
								// --> drop Offensive Role
								offensiveStrategy.getDesiredBots().clear();
								offensiveStrategy.setMinNumberOfBots(0);
								offensiveStrategy.setMaxNumberOfBots(1);
							}
						}
					}
				}
			}
		}
	}
}
