/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 7, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.metis.general;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.github.g3force.configurable.Configurable;

import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.RobotPlan.RobotRole;
import edu.dhbw.mannheim.tigers.sumatra.model.data.multi_team.MultiTeamCommunication.TeamPlan;
import edu.tigers.sumatra.ai.data.AutomatedThrowInInfo.EPrepareThrowInAction;
import edu.tigers.sumatra.ai.data.EAIControlState;
import edu.tigers.sumatra.ai.data.EGameStateTeam;
import edu.tigers.sumatra.ai.data.MultiTeamMessage;
import edu.tigers.sumatra.ai.data.OffensiveStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.data.math.OffensiveMath;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.defense.data.AngleDefenseData;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotGroup;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.wp.data.Geometry;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Find number of roles and preferred bots for all roles
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class RoleFinderCalc extends ACalculator
{
	private static final Logger	log									= Logger.getLogger(RoleFinderCalc.class.getName());
	
	private boolean					noKeeperWarned						= true;
	
	@Configurable(comment = "x coordinate of the line beond which foe bots are ignored for our defender count")
	private static double			ignoreEnemyBotsLine				= 1000;
	
	@Configurable(comment = "distance difference needed between our and the enemies nearest bot to the ball to declare the ball possessed by us")
	private static double			ballPossessionDist				= 4 * Geometry.getBotRadius();
	
	@Configurable(comment = "The minimum number of defenders demanded at all times in the game")
	private static int				minimumNumDefenders				= 1;
	
	@Configurable(comment = "The minimum number of defenders demanded in offensive situations")
	private static int				minimumNumDefendersOffensive	= 0;
	
	@Configurable(comment = "Minimum defenders during mixed team games")
	private static int				minNumDefMixedTeam				= 0;
	
	@Configurable(comment = "Maximum defenders during mixed team games")
	private static int				maxNumDefMixedTeam				= 4;
	
	
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
			case THROW_IN_WE:
			case CORNER_KICK_WE:
			case DIRECT_KICK_WE:
				if (OffensiveMath.isKeeperInsane(baseAiFrame, newTacticalField))
				{
					normalMode(newTacticalField, baseAiFrame);
				} else
				{
					keeper(newTacticalField, baseAiFrame);
					normalMode(newTacticalField, baseAiFrame);
				}
				break;
			case BALL_PLACEMENT_WE:
				keeper(newTacticalField, baseAiFrame);
				ballPlacementMode(newTacticalField, baseAiFrame);
				break;
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
	
	
	private int modifyDefenseForMixedTeam(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame,
			int curDesired)
	{
		MultiTeamMessage multiTeamMsg = baseAiFrame.getMultiTeamMessage();
		
		if (null != multiTeamMsg)
		{
			Map<BotID, RobotPlan> ourRobots = multiTeamMsg.getRobotPlans(baseAiFrame.getWorldFrame().getTeamColor());
			TeamPlan teamPlan = multiTeamMsg.getTeamPlan();
			
			List<RobotPlan> robotPlans = teamPlan.getPlansList().stream()
					.filter(robotPlan -> RobotRole.Defense == robotPlan.getRole())
					.filter(robotPlan -> !containsOurRobot(ourRobots.keySet(), robotPlan.getRobotId()))
					.collect(Collectors.toList());
			
			curDesired -= robotPlans.size();
			curDesired = Math.max(curDesired, 0);
		}
		
		curDesired = Math.max(curDesired, minNumDefMixedTeam);
		curDesired = Math.min(curDesired, maxNumDefMixedTeam);
		
		return curDesired;
	}
	
	
	private void normalDefense(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		int numDesRolesTemp = getNumDefenders(newTacticalField, baseAiFrame);
		
		if (EAIControlState.MIXED_TEAM_MODE == baseAiFrame.getPrevFrame().getPlayStrategy().getAIControlState())
		{
			numDesRolesTemp = modifyDefenseForMixedTeam(newTacticalField, baseAiFrame, numDesRolesTemp);
		}
		
		// we need a new final variable for the lambda expression further down
		final int numDesRoles = numDesRolesTemp;
		RoleFinderInfo defenderInfo = new RoleFinderInfo(0, numDesRoles, numDesRoles);
		
		List<BotID> desBots = defenderInfo.getDesiredBots();
		
		// add crucial defenders first
		// desBots.addAll(newTacticalField.getCrucialDefenders());
		final List<BotID> botsToAvoid = new ArrayList<>();
		
		List<BotID> offensiveBots = newTacticalField.getOffensiveStrategy().getDesiredBots();
		
		if ((offensiveBots.size() == 0) || containsOnlyNulls(offensiveBots))
		{
			List<ARole> offBots = baseAiFrame.getPrevFrame().getPlayStrategy().getActiveRoles(EPlay.OFFENSIVE);
			
			offBots.forEach(bot -> botsToAvoid.add(bot.getBotID()));
		} else
		{
			botsToAvoid.addAll(offensiveBots);
		}
		
		List<ITrackedBot> possibleDefenders = baseAiFrame
				.getWorldFrame()
				.getTigerBotsAvailable()
				.values()
				.stream()
				.filter(bot -> !bot.getBotId().equals(baseAiFrame.getKeeperId()))
				.filter(bot -> !botsToAvoid.contains(bot.getBotId()))
				.collect(Collectors.toList());
		possibleDefenders.sort((a, b) -> (int) Math.signum(a.getPos().x() - b.getPos().x()));
		
		possibleDefenders.subList(0, Math.min(possibleDefenders.size(), numDesRoles))
				.forEach(def -> desBots.add(def.getBotId()));
		
		newTacticalField.getRoleFinderInfos().put(EPlay.DEFENSIVE, defenderInfo);
	}
	
	
	private boolean containsOnlyNulls(final List<? extends Object> listOfThings)
	{
		for (Object thing : listOfThings)
		{
			if (thing != null)
			{
				return false;
			}
		}
		
		return true;
	}
	
	
	private void ballPlacementMode(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		normalDefense(newTacticalField, baseAiFrame);
		// OffensiveStrategy offensiveStrategy = newTacticalField.getOffensiveStrategy();
		int desired = 0;
		int min = 0;
		int max = 0;
		
		if ((newTacticalField.getThrowInInfo() != null) && (newTacticalField.getThrowInInfo().getAction() != null))
		{
			if (newTacticalField.getThrowInInfo().getAction() == EPrepareThrowInAction.PASS_TO_RECEIVER_DIRECTLY)
			{
				desired = 2;
				min = 2;
				max = 2;
			} else
			{
				desired = 1;
				min = 1;
				max = 1;
			}
		} else
		{
			desired = 0;
			min = 0;
			max = 0;
		}
		if (newTacticalField.getThrowInInfo().isFinished())
		{
			desired = 0;
			min = 0;
			max = 0;
		}
		RoleFinderInfo throwInInfo = new RoleFinderInfo(
				min,
				max,
				desired);
		for (BotID key : newTacticalField.getThrowInInfo().getDesiredBots())
		{
			throwInInfo.getDesiredBots().add(key);
		}
		newTacticalField.getRoleFinderInfos().put(EPlay.AUTOMATED_THROW_IN, throwInInfo);
		
		RoleFinderInfo supporterInfo = new RoleFinderInfo(0, 6, 2);
		newTacticalField.getRoleFinderInfos().put(EPlay.SUPPORT, supporterInfo);
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
		RoleFinderInfo throwInInfo = new RoleFinderInfo(
				offensiveStrategy.getMinNumberOfBots(),
				offensiveStrategy.getMaxNumberOfBots(),
				desiredBots);
		if (offensiveStrategy.getDesiredBots() != null)
		{
			for (BotID key : offensiveStrategy.getDesiredBots())
			{
				if (key != null)
				{
					throwInInfo.getDesiredBots().add(key);
				}
			}
		}
		newTacticalField.getRoleFinderInfos().put(EPlay.OFFENSIVE, throwInInfo);
		
		RoleFinderInfo supporterInfo = new RoleFinderInfo(0, 6, 2);
		newTacticalField.getRoleFinderInfos().put(EPlay.SUPPORT, supporterInfo);
	}
	
	
	private int getNumDefenders(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		AngleDefenseData angleDefenseData = tacticalField.getAngleDefenseData();
		List<FoeBotGroup> foeBotGroups = angleDefenseData.getFoeBotGroups();
		
		int numDefenders = 0;
		for (FoeBotGroup group : foeBotGroups)
		{
			numDefenders += group.nDesDefenders(tacticalField.getGameState(), aiFrame.getWorldFrame());
		}
		
		EGameStateTeam gameState = tacticalField.getGameState();
		int numCrucialDefenders = tacticalField.getCrucialDefenders().size();
		
		if (isOffensiveStaticSituation(gameState, aiFrame))
		{
			numDefenders = minimumNumDefendersOffensive;
		} else
		{
			numDefenders = Math.max(Math.max(minimumNumDefenders, numDefenders), numCrucialDefenders);
		}
		
		return numDefenders;
	}
	
	
	private boolean isOffensiveStaticSituation(final EGameStateTeam gameState, final BaseAiFrame aiFrame)
	{
		if ((gameState == EGameStateTeam.CORNER_KICK_WE)
				|| (gameState == EGameStateTeam.DIRECT_KICK_WE)
				|| (gameState == EGameStateTeam.THROW_IN_WE)
				|| (gameState == EGameStateTeam.GOAL_KICK_WE))
		{
			return true;
		}
		return false;
	}
}
