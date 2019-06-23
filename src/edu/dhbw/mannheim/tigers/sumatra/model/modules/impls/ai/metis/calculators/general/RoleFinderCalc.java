/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameBehavior;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.OffensiveStrategy;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.defense.DefensePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.presenter.aicenter.EAIControlState;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * Find number of roles and preferred bots for all roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RoleFinderCalc extends ACalculator
{
	private static final Logger	log						= Logger.getLogger(RoleFinderCalc.class.getName());
	private boolean					noKeeperWarned			= true;
	
	@Configurable(comment = "distance difference needed between our and the enemies nearest bot to the ball to declare the ball possessed by us")
	private static float				ballPossessionDist	= 4 * AIConfig.getGeometry().getBotRadius();
	
	@Configurable(comment = "The minimum number of defenders demanded at all times in the game")
	private static int				minimumNumDefenders	= 1;
	
	
	/**
	 * 
	 */
	public RoleFinderCalc()
	{
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		switch (newTacticalField.getGameState())
		{
			case PREPARE_PENALTY_THEY:
				keeper(newTacticalField, baseAiFrame);
				penaltyTheyMode(newTacticalField, baseAiFrame);
				break;
			case PREPARE_PENALTY_WE:
				keeper(newTacticalField, baseAiFrame);
				penaltyWeMode(newTacticalField, baseAiFrame);
				break;
			case BREAK:
			case TIMEOUT_THEY:
			case TIMEOUT_WE:
				breakGame(newTacticalField, baseAiFrame);
				break;
			case HALTED:
			case UNKNOWN:
				// no plays
				break;
			case POST_GAME:
				postGame(newTacticalField, baseAiFrame);
				break;
			case PREPARE_KICKOFF_WE:
				kickoff(newTacticalField, baseAiFrame);
				break;
			// case STOPPED:
			// normalMode(newTacticalField, baseAiFrame);
			// for (RoleFinderInfo info : newTacticalField.getRoleFinderInfos().values())
			// {
			// info.getDesiredBots().clear();
			// }
			// keeper(newTacticalField, baseAiFrame);
			// break;
			default:
				keeper(newTacticalField, baseAiFrame);
				normalMode(newTacticalField, baseAiFrame);
				break;
		}
	}
	
	
	private void breakGame(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		int numRoles = baseAiFrame.getWorldFrame().getTigerBotsAvailable().size();
		newTacticalField.getRoleFinderInfos().put(EPlay.MAINTENANCE, new RoleFinderInfo(0, numRoles, numRoles));
	}
	
	
	private void postGame(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		int numRoles = baseAiFrame.getWorldFrame().getTigerBotsAvailable().size();
		newTacticalField.getRoleFinderInfos().put(EPlay.CHEERING, new RoleFinderInfo(0, numRoles, numRoles));
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	private void penaltyWeMode(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		int numRoles = Math.max(1, baseAiFrame.getWorldFrame().getTigerBotsAvailable().size() - 3);
		normalDefense(newTacticalField, baseAiFrame);
		newTacticalField.getRoleFinderInfos().put(EPlay.PENALTY_WE, new RoleFinderInfo(0, numRoles, Integer.MAX_VALUE));
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	private void penaltyTheyMode(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		newTacticalField.getRoleFinderInfos().put(EPlay.PENALTY_THEM, new RoleFinderInfo(0, 10, 5));
	}
	
	
	/**
	 * @param newTacticalField
	 * @param baseAiFrame
	 */
	private void kickoff(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		RoleFinderInfo kickOffInfo = new RoleFinderInfo(1, 1, 3);
		OffensiveStrategy offensiveStrategy = newTacticalField.getOffensiveStrategy();
		
		if (offensiveStrategy.getDesiredBots() != null)
		{
			for (BotID key : offensiveStrategy.getDesiredBots())
			{
				if (key != null)
				{
					kickOffInfo.getDesiredBots().add(key);
				}
			}
		}
		newTacticalField.getRoleFinderInfos().put(EPlay.KICKOFF, kickOffInfo);
		normalDefense(newTacticalField, baseAiFrame);
		RoleFinderInfo supporterInfo = new RoleFinderInfo(0, 6, 0);
		newTacticalField.getRoleFinderInfos().put(EPlay.SUPPORT, supporterInfo);
		keeper(newTacticalField, baseAiFrame);
	}
	
	
	private void keeper(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final RoleFinderInfo keeperInfo;
		// if keeper is present request it, else request no, because it is against the law to enter penArea with any other
		// id
		if (baseAiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(baseAiFrame.getKeeperId()))
		{
			keeperInfo = new RoleFinderInfo(1, 1, 1);
			keeperInfo.getDesiredBots().add(baseAiFrame.getKeeperId());
			keeperInfo.setForceNumDesiredBots(1);
			noKeeperWarned = false;
		} else
		{
			if (!noKeeperWarned)
			{
				log.warn("Our keeper id (" + baseAiFrame.getKeeperId() + ") is not present!");
			}
			noKeeperWarned = true;
			keeperInfo = new RoleFinderInfo(0, 0, 0);
		}
		newTacticalField.getRoleFinderInfos().put(EPlay.KEEPER, keeperInfo);
	}
	
	
	private boolean containsOurRobot(final Set<BotID> botIds, final int id)
	{
		for (BotID curId : botIds)
		{
			if (curId.getNumber() == id)
			{
				return true;
			}
		}
		return false;
	}
	
	
	private int modifyForMixedTeam(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			int curDesired)
	{
		MultiTeamMessage multiTeamMsg = newTacticalField.getMultiTeamMessage();
		
		if (null != multiTeamMsg)
		{
			Map<BotID, RobotPlan> ourRobots = multiTeamMsg.getRobotPlans(baseAiFrame.getWorldFrame().getTeamColor());
			TeamPlan teamPlan = multiTeamMsg.getTeamPlan();
			
			List<RobotPlan> robotPlans = teamPlan.getPlansList().stream()
					.filter(robotPlan -> RobotRole.Defense == robotPlan.getRole()).collect(Collectors.toList());
			
			robotPlans = robotPlans.stream()
					.filter(robotPlan -> !containsOurRobot(ourRobots.keySet(), robotPlan.getRobotId()))
					.collect(Collectors.toList());
			
			curDesired -= robotPlans.size();
			curDesired = Math.max(curDesired, 0);
		}
		
		return curDesired;
	}
	
	
	private void normalDefense(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		int desRoles = getNumDefenders(newTacticalField, baseAiFrame);
		
		if (EAIControlState.MIXED_TEAM_MODE == baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState())
		{
			desRoles = modifyForMixedTeam(newTacticalField, baseAiFrame, desRoles);
		}
		
		RoleFinderInfo defenderInfo = new RoleFinderInfo(0, desRoles, desRoles);
		List<TrackedTigerBot> availableBots = new ArrayList<TrackedTigerBot>(baseAiFrame.getWorldFrame()
				.getTigerBotsAvailable().values());
		List<TrackedTigerBot> desBots = new ArrayList<TrackedTigerBot>();
		
		DefensePlay defPlay = null;
		for (APlay curPlay : baseAiFrame.getPrevFrame().getPlayStrategy().getActivePlays())
		{
			if (curPlay.getType().equals(EPlay.DEFENSIVE))
			{
				defPlay = (DefensePlay) curPlay;
			}
		}
		
		if (null != defPlay)
		{
			Map<DefenderRole, DefensePoint> defDistribution = defPlay.getDefenderDistribution();
			
			for (DefenderRole curDefRole : defDistribution.keySet())
			{
				desBots.add(curDefRole.getBot());
			}
		}
		
		availableBots = availableBots.stream().filter(bot -> !desBots.contains(bot)).collect(Collectors.toList());
		Collections.sort(availableBots, TrackedTigerBot.DISTANCE_TO_GOAL_COMPARATOR);
		while ((desBots.size() < desRoles) && (availableBots.size() > 0))
		{
			desBots.add(availableBots.get(0));
			availableBots.remove(0);
		}
		
		for (TrackedTigerBot tiger : desBots)
		{
			defenderInfo.getDesiredBots().add(tiger.getId());
		}
		
		newTacticalField.getRoleFinderInfos().put(EPlay.DEFENSIVE, defenderInfo);
	}
	
	
	private void normalMode(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		normalDefense(newTacticalField, baseAiFrame);
		
		OffensiveStrategy offensiveStrategy = newTacticalField.getOffensiveStrategy();
		int desiredBots;
		if (offensiveStrategy.getDesiredBots() == null)
		{
			desiredBots = 0;
		} else
		{
			desiredBots = offensiveStrategy.getDesiredBots().size();
		}
		// desiredBots = desiredBots + offensiveStrategy.getHelperDestinations().size();
		RoleFinderInfo offenceInfo = new RoleFinderInfo(
				offensiveStrategy.getMinNumberOfBots(),
				offensiveStrategy.getMaxNumberOfBots(),
				desiredBots);
		if (offensiveStrategy.getDesiredBots() != null)
		{
			for (BotID key : offensiveStrategy.getDesiredBots())
			{
				if (key != null)
				{
					offenceInfo.getDesiredBots().add(key);
				}
			}
		}
		newTacticalField.getRoleFinderInfos().put(EPlay.OFFENSIVE, offenceInfo);
		
		RoleFinderInfo supporterInfo = new RoleFinderInfo(0, 6, 2);
		newTacticalField.getRoleFinderInfos().put(EPlay.SUPPORT, supporterInfo);
	}
	
	
	private int getNumDefenders(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		int numDefenders = getNumFoeBotsOwnHalf(aiFrame);
		EGameBehavior gameBehaviour = tacticalField.getGameBehavior();
		
		switch (gameBehaviour)
		{
			case DEFENSIVE:
				break;
			case OFFENSIVE:
			default:
				numDefenders -= 1;
				break;
		}
		
		// in stop predict next GameState
		EGameState state = tacticalField.getGameState();
		if (EGameState.STOPPED == state)
		{
			// we touched the ball last
			if (!aiFrame.getWorldFrame().getTigerBotsAvailable().containsKey(tacticalField.getBotLastTouchedBall()))
			{
				// numDefenders++;
			} else
			{
				numDefenders -= 2;
			}
		} else
		{
			numDefenders += isDangerousSituation(state, aiFrame) ? 1 : 0;
			numDefenders -= isOffensiveSituation(state, aiFrame) ? 1 : 0;
		}
		
		numDefenders = Math.max(minimumNumDefenders, numDefenders);
		if ((EGameState.PREPARE_KICKOFF_THEY == state)
				&& (DefenseCalc.doubleDefKickoffOn || DefenseCalc.forceDoubleBlockAtPenAreaKickoff))
		{
			numDefenders = 2;
		}
		
		return numDefenders;
	}
	
	
	private int getNumFoeBotsOwnHalf(final BaseAiFrame aiFrame)
	{
		
		Collection<TrackedTigerBot> theirBots = aiFrame.getWorldFrame().getFoeBots().values();
		theirBots = theirBots.stream().filter(bot -> bot.getPos().x() <= 0).collect(Collectors.toList());
		
		return theirBots.size();
	}
	
	
	private boolean isDangerousSituation(final EGameState gameState, final BaseAiFrame aiFrame)
	{
		if ((gameState == EGameState.GOAL_KICK_THEY)
				|| (gameState == EGameState.CORNER_KICK_THEY)
				|| (gameState == EGameState.DIRECT_KICK_THEY)
				|| (gameState == EGameState.THROW_IN_THEY)
				|| (aiFrame.getWorldFrame().getBall().getPos().x() <= 0)
				|| !ballPossessionWe(aiFrame))
		{
			return true;
		}
		// in stop predict the next GameState and set
		return false;
	}
	
	
	private boolean isOffensiveSituation(final EGameState gameState, final BaseAiFrame aiFrame)
	{
		if ((gameState == EGameState.CORNER_KICK_WE)
				|| (gameState == EGameState.PREPARE_KICKOFF_WE)
				|| (gameState == EGameState.DIRECT_KICK_WE)
				|| (gameState == EGameState.THROW_IN_WE))
		{
			return true;
		}
		return false;
	}
	
	
	private boolean ballPossessionWe(final BaseAiFrame aiFrame)
	{
		Collection<TrackedTigerBot> ourBots = aiFrame.getWorldFrame().getBots().values();
		Collection<TrackedTigerBot> theirBots = aiFrame.getWorldFrame().getFoeBots().values();
		
		IVector2 ballPos = aiFrame.getWorldFrame().getBall().getPos();
		
		TrackedTigerBot nearestBot = null;
		TrackedTigerBot nearestFoeBot = null;
		
		try
		{
			nearestBot = ourBots.stream().reduce(
					(bot1, bot2) -> bot1.getPos().subtractNew(ballPos).getLength2() < bot2.getPos().subtractNew(ballPos)
							.getLength2() ? bot1
							: bot2).get();
			
			nearestFoeBot = theirBots.stream().reduce(
					(bot1, bot2) -> bot1.getPos().subtractNew(ballPos).getLength2() < bot2.getPos().subtractNew(ballPos)
							.getLength2() ? bot1
							: bot2).get();
		} catch (NoSuchElementException e)
		{
			return false;
		}
		
		if ((nearestBot.getPos().subtractNew(ballPos).getLength2() + ballPossessionDist)
		<= nearestFoeBot.getPos().subtractNew(ballPos).getLength2())
		{
			return true;
		}
		
		return false;
	}
}
