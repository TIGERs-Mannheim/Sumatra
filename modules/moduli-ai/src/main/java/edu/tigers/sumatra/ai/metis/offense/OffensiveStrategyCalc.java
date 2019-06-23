/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 12.12.2014
 * Author(s): MarkG
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.awt.Color;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.tigers.sumatra.Referee.SSL_Referee.Command;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.EShapesLayer;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.OffensiveStrategy.EOffensiveStrategy;
import edu.tigers.sumatra.ai.data.SpecialMoveCommand;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.AIInfoFrame;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.data.math.ProbabilityMath;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction.EOffensiveAction;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.bot.IBot;
import edu.tigers.sumatra.botmanager.bots.TigerBotV3;
import edu.tigers.sumatra.drawable.DrawableCircle;
import edu.tigers.sumatra.drawable.DrawableText;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.BotIDMap;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.shapes.circle.Circle;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;


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
	
	private boolean					waitForHelperResponse	= false;
	
	private SpecialMoveCommand		prevCommand					= null;
	
	private ITrackedBot				primaryBot					= null;
	
	private int							animator						= 0;
	
	private BotID						oldPrimary					= null;
	
	private long						newPrimarySetTimer		= 0;
	
	private boolean					initialDelayStep			= true;
	
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
			offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.KICK);
		}
		BotID bestGetter = OffensiveMath.getBestGetter(baseAiFrame, newTacticalField);
		
		// only switch bot if it has been active at least for 500 ms.
		if ((oldPrimary != null)
				&& OffensiveMath.getPotentialOffensiveBotMap(newTacticalField, baseAiFrame).containsKey(oldPrimary))
		{
			if (oldPrimary != bestGetter)
			{
				if (newPrimarySetTimer == 0)
				{
					newPrimarySetTimer = baseAiFrame.getWorldFrame().getTimestamp();
				}
				if ((baseAiFrame.getWorldFrame().getTimestamp() - newPrimarySetTimer) < 200_000_000L) // 500 ms
				{
					bestGetter = oldPrimary;
				} else
				{
					// do nothing here yet.
				}
			} else
			{
				newPrimarySetTimer = 0;
			}
		}
		
		oldPrimary = bestGetter;
		BotID bestRedirector = OffensiveMath.getBestRedirector(baseAiFrame.getWorldFrame(),
				OffensiveMath.getPotentialOffensiveBotMap(newTacticalField, baseAiFrame),
				baseAiFrame.getWorldFrame().getBall().getVel(), newTacticalField);
		
		// BotIDMap<ITrackedBot> allBots = new BotIDMap<>(baseAiFrame.getWorldFrame().getTigerBotsVisible());
		// allBots.putAll(baseAiFrame.getWorldFrame().getFoeBots());
		// BotID bestRedirectorAll = OffensiveMath.getBestRedirector(baseAiFrame.getWorldFrame(), allBots);
		//
		// if ((bestRedirector != null) && (bestRedirectorAll != bestRedirector))
		// {
		// // This case should also happen when enemy robots move towards my pass area.
		// bestGetter = bestRedirector;
		// bestRedirector = null;
		// }
		
		if (OffensiveMath.isKeeperInsane(baseAiFrame, newTacticalField))
		{
			bestGetter = baseAiFrame.getKeeperId();
			bestRedirector = null;
		}
		
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
									(GeoMath.distancePP(role.getPos(), baseAiFrame.getWorldFrame().getBall().getPos()) < 500))
							{
								bestRedirector = null;
							}
						}
					}
				}
			}
		}
		
		if ((multiTeamChallengsCheks(newTacticalField, baseAiFrame) &&
				(baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() == EAIControlState.MIXED_TEAM_MODE)))
		{
			bestGetter = null;
			offensiveStrategy.getDesiredBots().clear();
			offensiveStrategy.setMinNumberOfBots(0);
			offensiveStrategy.setMaxNumberOfBots(0);
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
		// reactOnRefSignals(newTacticalField, baseAiFrame);
		keepActiveStatus(baseAiFrame.getWorldFrame(), baseAiFrame, newTacticalField, newTacticalField.getGameState());
		
		boolean movingTowardsPrimary = (primaryBot == null ? false : (bestRedirector == primaryBot.getBotId()));
		if ((Geometry.getPenaltyAreaOur()
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos(), 250) && !movingTowardsPrimary) ||
				!Geometry.getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos()))
		{
			offensiveStrategy.setMaxNumberOfBots(0);
			offensiveStrategy.setMinNumberOfBots(0);
			offensiveStrategy.getDesiredBots().clear();
		} else if ((Geometry.getPenaltyAreaTheir()
				.isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos(), 30) && !movingTowardsPrimary) ||
				!Geometry.getField().isPointInShape(baseAiFrame.getWorldFrame().getBall().getPos()))
		{
			offensiveStrategy.setMaxNumberOfBots(0);
			offensiveStrategy.setMinNumberOfBots(0);
			offensiveStrategy.getDesiredBots().clear();
		}
		// special case for mixed Team challenge
		if (baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState() == EAIControlState.MIXED_TEAM_MODE)
		{
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
		if (OffensiveConstants.isSupportiveAttackerEnabled() && (baseAiFrame.getWorldFrame().getBall().getPos().x() > 0)
				&&
				(baseAiFrame.getWorldFrame().getBall().getPos().x() > -2500f)) // TODO: make me configurable
		{
			if ((offensiveStrategy.getDesiredBots() != null) && (primaryBot != null))
			{
				if (((bestGetter == primaryBot.getBotId()) && (((offensiveStrategy.getDesiredBots().size() == 1))
						&& (newTacticalField.getGameState() == EGameStateTeam.RUNNING))))
				{
					IBotIDMap<ITrackedBot> bots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField,
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
		drawCrucialDefenders(newTacticalField, baseAiFrame);
		
		if (isPrimaryRoleReady2Kick(newTacticalField, baseAiFrame) && (primaryBot != null))
		{
			DrawableText dt = new DrawableText(primaryBot.getPos().addNew(new Vector2(170, 150)), "Ready!", Color.RED);
			newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt);
			if (newTacticalField.getOffensiveActions().get(primaryBot.getBotId()) != null)
			{
				newTacticalField.getOffensiveActions().get(primaryBot.getBotId()).setRoleReadyToKick(true);
			}
		} else if (primaryBot != null)
		{
			DrawableText dt = new DrawableText(primaryBot.getPos().addNew(new Vector2(170, 150)), "Not Ready!", Color.RED);
			newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt);
		}
		
		// draw directHitScoreChances
		for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
		{
			IVector2 pos = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getPos();
			double directHitChance = ProbabilityMath.getDirectShootScoreChance(baseAiFrame.getWorldFrame(), pos, false);
			double directScoreChance = 1 - newTacticalField.getBestDirectShotTargetsForTigerBots().get(key).value;
			newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE_ADDITIONAL).add(
					new DrawableText(pos.addNew(new Vector2(-200, 0)),
							"hit chances: " + directHitChance + "/" + directScoreChance, Color.orange));
		}
		
		// This is really important!
		cheerWhenWeShootAGoal(newTacticalField, baseAiFrame);
	}
	
	
	// private void gameStateChange(final WorldFrame wFrame, final EGameStateTeam from, final EGameStateTeam to)
	// {
	// switch (to)
	// {
	// case DIRECT_KICK_THEY:
	// case THROW_IN_THEY:
	// for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
	// {
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.INTERCEPT);
	// }
	// break;
	// case CORNER_KICK_THEY:
	// case TIMEOUT_THEY:
	// case TIMEOUT_WE:
	// case GOAL_KICK_THEY:
	// case HALTED:
	// case PREPARE_PENALTY_THEY:
	// case PREPARE_KICKOFF_THEY:
	// case PREPARE_PENALTY_WE:
	// case STOPPED:
	// kickoff = false;
	// // planedStrategy = EOffensiveStrategy.STOP;
	// // waitingForNormalStart = false;
	// for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
	// {
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.STOP);
	// }
	// break;
	// case PREPARE_KICKOFF_WE:
	// for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
	// {
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
	// }
	// if (primaryBot != null)
	// {
	// offensiveStrategy.getDesiredBots().add(primaryBot.getBotId());
	// }
	// // inKickoff = true;
	// kickoff = true;
	// // delayTimer = SumatraClock.currentTimeMillis();
	// // planedStrategy = EOffensiveStrategy.DELAY;
	// waitForNormalStart = true;
	// break;
	// case CORNER_KICK_WE:
	// case GOAL_KICK_WE:
	// case THROW_IN_WE:
	// // forcePass = true;
	// case DIRECT_KICK_WE:
	//
	// delayTimer = wFrame.getTimestamp();
	// waitForNormalStart = false;
	// for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
	// {
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.DELAY);
	// }
	// waitTime = OffensiveConstants.getDelayWaitTime();
	//
	// break;
	// case BALL_PLACEMENT_WE:
	// case RUNNING:
	// delayTimer = 0;
	// // kickOffHelperBot = null;
	// kickoff = false;
	// waitForNormalStart = false;
	// for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
	// {
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
	// }
	// break;
	// case BALL_PLACEMENT_THEY:
	// offensiveStrategy.setMaxNumberOfBots(0);
	// offensiveStrategy.setMinNumberOfBots(0);
	// offensiveStrategy.getDesiredBots().clear();
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().clear();
	// break;
	// default:
	// break;
	// }
	// }
	//
	//
	// private void reactOnRefSignals(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	// {
	// WorldFrame wFrame = baseAiFrame.getWorldFrame();
	// EGameStateTeam gameState = newTacticalField.getGameState();
	// EGameStateTeam preGameState = baseAiFrame.getPrevFrame().getTacticalField().getGameState();
	//
	// if (!preGameState.equals(gameState))
	// {
	// // gameStateChange(wFrame, preGameState, gameState);
	// }
	//
	// if (baseAiFrame.isNewRefereeMsg())
	// {
	// if (baseAiFrame.getRefereeMsg().getCommand().equals(Command.NORMAL_START))
	// {
	// for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
	// {
	// offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.GET);
	// }
	// }
	// }
	// }
	
	
	private void keepActiveStatus(final WorldFrame wFrame, final BaseAiFrame baseAiFrame,
			final TacticalField newTacticalField, final EGameStateTeam gameState)
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
				
				if (OffensiveConstants.isInterceptorEnabled())
				{
					offensiveStrategy.getDesiredBots().add(null);
					offensiveStrategy.setMaxNumberOfBots(1);
				} else
				{
					offensiveStrategy.setMaxNumberOfBots(0);
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
				initialDelayStep = true;
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
				if (initialDelayStep)
				{
					waitForHelperResponse = true;
					initialDelayStep = false;
				}
				if (waitForHelperResponse)
				{
					for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
					{
						offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.DELAY);
					}
					offensiveStrategy.getDesiredBots().clear();
					if (primaryBot != null)
					{
						offensiveStrategy.getDesiredBots().add(primaryBot.getBotId());
					}
					offensiveStrategy.setMaxNumberOfBots(1);
				}
				break;
			case BALL_PLACEMENT_WE:
			case RUNNING:
				initialDelayStep = true;
				break;
			case BALL_PLACEMENT_THEY:
				offensiveStrategy.setMaxNumberOfBots(0);
				offensiveStrategy.setMaxNumberOfBots(0);
				offensiveStrategy.setMinNumberOfBots(0);
				offensiveStrategy.getDesiredBots().clear();
				offensiveStrategy.getCurrentOffensivePlayConfiguration().clear();
				break;
			default:
				break;
		}
	}
	
	
	private void addSecondaryOffensiveBots(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		AIInfoFrame prevFrame = baseAiFrame.getPrevFrame();
		for (IVector2 pos : prevFrame.getAICom().getDelayMoves())
		{
			SpecialMoveCommand command = new SpecialMoveCommand();
			command.getMovePosition().add(pos);
			command.getMoveTimes().add(0.0);
			command.setResponseStep(0);
			offensiveStrategy.getSpecialMoveCommands().add(command);
			offensiveStrategy.getUnassignedStrategies().add(EOffensiveStrategy.REDIRECT_CATCH_SPECIAL_MOVE);
			offensiveStrategy.getDesiredBots().add(null);
			offensiveStrategy.setMaxNumberOfBots(offensiveStrategy.getMaxNumberOfBots() + 1);
		}
		
		if ((prevFrame.getAICom().getOffensiveRolePassTargetID() != null) &&
				(newTacticalField.getGameState() != EGameStateTeam.PREPARE_KICKOFF_WE))
		{
			BotID passReceiver = prevFrame.getAICom().getOffensiveRolePassTargetID();
			SpecialMoveCommand command = new SpecialMoveCommand();
			
			command.getMovePosition().add(prevFrame.getAICom().getOffensiveRolePassTarget());
			command.getMoveTimes().add(0.0);
			command.setResponseStep(0);
			
			double time = -1;
			if (primaryBot.getBot().getCurrentTrajectory().isPresent())
			{
				double ballStartVelocityOffset = OffensiveConstants.getBallStartSpeedOffsetForPassTimeCalculation();
				double shootTime = primaryBot.getBot().getCurrentTrajectory().get()
						.getRemainingTrajectoryTime(baseAiFrame.getWorldFrame().getTimestamp());
				// add time from primaryBot to ball;
				double initialBallSpeed = Math.max(0.5, (Geometry.getBallModel()
						.getVelForDist(GeoMath.distancePP(baseAiFrame.getWorldFrame().getBall().getPos(),
								prevFrame.getAICom().getOffensiveRolePassTarget()), OffensiveConstants.getDefaultPassEndVel())
						+ OffensiveConstants.getKickSpeedOffset()) + ballStartVelocityOffset);
				
				double dist = GeoMath.distancePP(baseAiFrame.getWorldFrame().getBall().getPos(),
						prevFrame.getAICom().getOffensiveRolePassTarget());
				double ballTravelTime = Geometry.getBallModel().getTimeByDist(initialBallSpeed,
						dist);
				if (ballTravelTime > 5.0)
				{
					// log.warn(Arrays.toString(Geometry.getBallModel().getP()));
					// log.warn("Offensive ball model calculated invalid ballTravelTime: " + ballTravelTime
					// + " dist: " + dist + " vel:" + initialBallSpeed);
				}
				time = shootTime + ballTravelTime;
				command.setTimeUntilPassArrives(time);
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(2);
				DrawableCircle dc = new DrawableCircle(new Circle(prevFrame.getAICom().getOffensiveRolePassTarget(), 50),
						Color.black);
				DrawableText dt = new DrawableText(
						prevFrame.getAICom().getOffensiveRolePassTarget().addNew(new Vector2(200, 0)),
						"Full time (t/b): " + df.format(time) + " -> (" + df.format(shootTime) + "|"
								+ df.format(ballTravelTime)
								+ ")" + " initVel: " + df.format(initialBallSpeed) + " dist: " + df.format(dist),
						Color.black);
				newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dc);
				newTacticalField.getDrawableShapes().get(EShapesLayer.OFFENSIVE).add(dt);
			}
			
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
				// pass reciever is not a tigers bot but friendly. (or not availible)
			} else if (GeoMath.distancePP(primaryBot.getPos(),
					baseAiFrame.getWorldFrame().getBall().getPos()) < (OffensiveConstants
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
				offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.KICK);
			}
		}
	}
	
	
	private void calculateActiveStateChanges(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		// change state of GET to KICK and KICK to GET
		// checkBallObtained(newTacticalField, baseAiFrame);
		checkNormalStartCalled(newTacticalField, baseAiFrame);
		checkDelayCounter(newTacticalField, baseAiFrame);
	}
	
	
	private void checkNormalStartCalled(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.isNewRefereeMsg())
		{
			if ((baseAiFrame.getRefereeMsg().getCommand() == Command.NORMAL_START)
					&& (newTacticalField.getGameState() == EGameStateTeam.PREPARE_KICKOFF_WE))
			{
				waitForNormalStart = false;
				for (BotID key : offensiveStrategy.getCurrentOffensivePlayConfiguration().keySet())
				{
					offensiveStrategy.getCurrentOffensivePlayConfiguration().put(key, EOffensiveStrategy.KICK);
				}
			}
		}
	}
	
	
	private void checkDelayCounter(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.getPrevFrame().getAICom().hasResponded()
		/* || (((wFrame.getTimestamp() - kickOffTimer) * 1e-9) > kickOffTimeout) */)
		{
			waitForHelperResponse = false;
			if (newTacticalField.getGameState() == EGameStateTeam.PREPARE_KICKOFF_WE)
			{
				// specialKickoffTimer = wFrame.getTimestamp();
			}
		}
	}
	
	
	private void cheerWhenWeShootAGoal(
			final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (baseAiFrame.isNewRefereeMsg())
		{
			ETeamColor color = baseAiFrame.getWorldFrame().getTeamColor();
			if (color == ETeamColor.YELLOW)
			{
				if (baseAiFrame.getRefereeMsg().getCommand() == Command.GOAL_YELLOW)
				{
					cheeringTimer = baseAiFrame.getSimpleWorldFrame().getTimestamp();
					for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
					{
						IBot bot = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getBot();
						
						if (bot instanceof TigerBotV3)
						{
							((TigerBotV3) bot).setCheering(true);
						}
					}
				}
			} else
			{
				if (baseAiFrame.getRefereeMsg().getCommand() == Command.GOAL_BLUE)
				{
					cheeringTimer = baseAiFrame.getSimpleWorldFrame().getTimestamp();
					for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
					{
						IBot bot = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getBot();
						
						if (bot instanceof TigerBotV3)
						{
							((TigerBotV3) bot).setCheering(true);
						}
					}
				}
			}
		}
		if ((((baseAiFrame.getSimpleWorldFrame().getTimestamp() - cheeringTimer) * 1e-9) > OffensiveConstants
				.getCheeringStopTimer())
				&& (cheeringTimer != 0))
		{
			for (BotID key : baseAiFrame.getWorldFrame().tigerBotsAvailable.keySet())
			{
				IBot bot = baseAiFrame.getWorldFrame().tigerBotsAvailable.get(key).getBot();
				
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
	
	
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		offensiveStrategy = new OffensiveStrategy();
		offensiveStrategy.setMinNumberOfBots(0);
		offensiveStrategy.setMaxNumberOfBots(1);
		offensiveStrategy.getUnassignedStrategies().add(EOffensiveStrategy.KICK);
		offensiveStrategy.getDesiredBots().add(null);
		newTacticalField.setOffensiveStrategy(offensiveStrategy);
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
	// mes.addRobotPlan(primaryBot.getId(), RobotRole.Offense, moveTarget, 0/* angle */, shotTarget);
	//
	// // MultiTeamMessageSender sender = new MultiTeamMessageSender();
	// // sender.send(mes);
	// }
	// }
	
	
	// private BotID getBestGetter(final TacticalField newTacticalField,
	// final BaseAiFrame baseAiFrame)
	// {
	// BotIDMap<ITrackedBot> potentialOffensiveBots = OffensiveMath.getPotentialOffensiveBotMap(newTacticalField,
	// baseAiFrame);
	// for (BotID key : potentialOffensiveBots.keySet())
	// {
	// if (newTacticalField.getOffensiveActions().containsKey(key))
	// {
	// newTacticalField.getOffensiveActions().get(key).getMovePosition().getScoring();
	// }
	// }
	// return null;
	// }
	//
	
	private void mixedChallengeReceiveOffensiveInformations(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if ((baseAiFrame.getMultiTeamMessage() != null) &&
				(baseAiFrame.getMultiTeamMessage().getTeamPlan() != null) &&
				(baseAiFrame.getMultiTeamMessage().getTeamPlan().getPlansList() != null))
		{
			for (RobotPlan plan : baseAiFrame.getMultiTeamMessage().getTeamPlan().getPlansList())
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
						if ((GeoMath.distancePP(moveTarget, baseAiFrame.getWorldFrame().getBall().getPos()) < 1000)
								&& baseAiFrame.getWorldFrame().tigerBotsVisible.containsKey(bot))
						{
							IVector2 friendlyPos = baseAiFrame.getWorldFrame().tigerBotsVisible.get(bot).getPos();
							if (GeoMath.distancePP(primaryBot.getPos(),
									baseAiFrame.getWorldFrame().getBall().getPos()) > GeoMath
											.distancePP(friendlyPos, baseAiFrame.getWorldFrame().getBall().getPos()))
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
	
	
	private boolean multiTeamChallengsCheks(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (primaryBot == null)
		{
			return false;
		}
		
		BotIDMap<ITrackedBot> friendlyBots = new BotIDMap<ITrackedBot>(baseAiFrame.getWorldFrame().tigerBotsVisible);
		for (BotID key : baseAiFrame.getWorldFrame().getTigerBotsAvailable().keySet())
		{
			friendlyBots.remove(key);
		}
		IVector2 myPos = primaryBot.getPos();
		IVector2 ballPos = baseAiFrame.getWorldFrame().getBall().getPos();
		for (BotID key : friendlyBots.keySet())
		{
			IVector2 friendlyPos = friendlyBots.get(key).getPos();
			double distMeToBall = GeoMath.distancePP(myPos, ballPos);
			double distFriendToBall = GeoMath.distancePP(friendlyPos, ballPos);
			if (distFriendToBall < 300)
			{
				if ((distMeToBall - 50) > distFriendToBall)
				{
					// drop primary role here.
					return true;
				}
			}
		}
		return false;
	}
	
	
	private void drawCrucialDefenders(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		for (BotID id : baseAiFrame.getPrevFrame().getTacticalField().getCrucialDefenders())
		{
			IVector2 botPos = baseAiFrame.getWorldFrame().getTigerBotsVisible().get(id).getPos();
			double radius = 150 - (animator % 50);
			DrawableCircle dc = new DrawableCircle(new Circle(botPos, radius), new Color(125, 255, 50));
			DrawableCircle dcb = new DrawableCircle(new Circle(botPos, radius), new Color(125, 255, 50, 100));
			dcb.setFill(true);
			newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE).add(dc);
			newTacticalField.getDrawableShapes().get(EShapesLayer.DEFENSE).add(dcb);
		}
		animator++;
	}
	
	
	private boolean isPrimaryRoleReady2Kick(final TacticalField newTacticalField,
			final BaseAiFrame baseAiFrame)
	{
		if (primaryBot == null)
		{
			return true;
		}
		if (!newTacticalField.getOffensiveActions().containsKey(primaryBot.getBotId())
				|| (newTacticalField.getOffensiveActions().get(primaryBot.getBotId()) == null))
		{
			return true;
		}
		
		OffensiveAction action = newTacticalField.getOffensiveActions().get(primaryBot.getBotId());
		if ((action.getType() == EOffensiveAction.PASS) && (action.getPassTarget() != null))
		{
			BotID passReceiver = baseAiFrame.getPrevFrame().getAICom().getOffensiveRolePassTargetID();
			IVector2 passPos = baseAiFrame.getPrevFrame().getAICom().getOffensiveRolePassTarget();
			
			if ((passReceiver == null) || (passPos == null))
			{
				return true;
			}
			
			IBotIDMap<ITrackedBot> bots = new BotIDMap<ITrackedBot>();
			bots.put(passReceiver, baseAiFrame.getWorldFrame().getTiger(passReceiver));
			IVector2 ballVelEstimation = passPos.subtractNew(baseAiFrame.getWorldFrame().getBall().getPos())
					.scaleTo(10.0);
			BotID estimatedReceiver = OffensiveMath.getBestRedirector(baseAiFrame.getWorldFrame(),
					bots, ballVelEstimation, newTacticalField);
			if ((passReceiver != null) && (passReceiver == estimatedReceiver))
			{
				return true;
			}
			return false;
		}
		return true;
	}
}
