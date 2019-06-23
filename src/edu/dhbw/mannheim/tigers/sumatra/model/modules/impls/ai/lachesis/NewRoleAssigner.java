/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 07.11.2013
 * Author(s): MalteJ
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AthenaAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.ETeamSpecRefCmd;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.AScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.EScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.FeatureScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.KickerScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.score.PenaltyScore;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.APlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.DefensePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.ARole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.roles.defense.DefenderRole;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.sisyphus.Sisyphus;
import edu.dhbw.mannheim.tigers.sumatra.util.config.Configurable;


/**
 * This class assignes the given bots to the given plays.
 * It also may change role count of a play using the {@link RoleFinder}, but only when in Match Mode oder Mixed Team
 * Mode.
 * (see {@link NewRoleAssigner#assignRoles(BotIDMap, List, AthenaAiFrame)}
 * 
 * @author MalteJ
 */
public class NewRoleAssigner implements INewRoleAssigner
{
	
	
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private final Assigner			assigner;
	
	private static final Logger	log								= Logger.getLogger(NewRoleAssigner.class.getName());
	
	// Lists to store the current Plays to assign
	private List<APlay>				currentPlaysToAssign;																				// variable
																																						// in
																																						// each
																																						// frame
	private List<APlay>				playsToAssign;																						// constant
																																						// in
																																						// each
																																						// frame
																																						
	// How much should the position of the bots influence their assignment (in the back for defense)
	private final float				POSITION_FACTOR				= 0.5f;
	
	// Store last assignment time in this variable to avoid toggling
	private long						lastAssignmentTime;
	
	// Store current GameState for different decisions
	private EGameState				currentGameState				= EGameState.UNKNOWN;
	private boolean					gameStateChangedThisFrame	= true;
	
	@Configurable(comment = "time [ms] - Time to wait after roleAssignment")
	private static long				calcInterval					= 900;
	
	// Scores for determination of suitability of bots
	private Map<EScore, AScore>	scores;
	
	// RoleFinder instance
	private RoleFinder				roleFinder;
	
	private APlay						offensePlay						= null;
	private APlay						defensePlay						= null;
	private APlay						supportPlay						= null;
	private APlay						keeperPlay						= null;
	private APlay						guiTestPlay						= null;
	
	// Keeper needs some special treatment
	private BotID						keeperId							= null;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Create and initialize an instance of {@link NewRoleAssigner}
	 */
	public NewRoleAssigner()
	{
		assigner = new Assigner();
		roleFinder = new RoleFinder();
		lastAssignmentTime = System.currentTimeMillis();
		
		// Adding score calculators for determination of bot's suitability to a certain task.
		scores = new HashMap<EScore, AScore>();
		scores.put(EScore.PENALTY, new PenaltyScore());
		scores.put(EScore.FEATURES, new FeatureScore());
		scores.put(EScore.KICKER, new KickerScore());
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void assignRoles(final BotIDMap<TrackedTigerBot> assignees, final List<APlay> playsToAssign,
			final AthenaAiFrame frame)
	{
		// checking whether GameState has changed.
		updateGameState(frame.getTacticalField().getGameState());
		
		// if there was a referee message, check whether normal start was commanded
		boolean normalStartCommanded = false;
		if (frame.getNewRefereeMsg() != null)
		{
			normalStartCommanded = (frame.getNewRefereeMsg().getTeamSpecRefCmd() == ETeamSpecRefCmd.NormalStart);
		}
		
		// Do nothing if the GameState didn't change and we are in Penalty State
		// (roles will be only assigned once for this state)
		if (!gameStateChangedThisFrame && (currentGameState == EGameState.PREPARE_PENALTY_WE)
				&& !normalStartCommanded)
		{
			return;
		}
		
		this.playsToAssign = playsToAssign;
		currentPlaysToAssign = new LinkedList<APlay>(playsToAssign);
		
		// if no bot or play is present, there's nothing to do.
		if ((assignees.isEmpty()) || (currentPlaysToAssign.isEmpty()))
		{
			return;
		}
		
		// if the GameState didn't change and the last calculation is not calcInterval ago, let the bots get their stuff
		// done and don't calculate again. (prevents toggling)
		if (((System.currentTimeMillis() - lastAssignmentTime) < calcInterval)
				&& !gameStateChangedThisFrame)
		{
			return;
		}
		
		// When in Match or MixedTeam Mode, let the RoleFinder adjust the roles in the plays according to the situation.
		switch (frame.getPlayStrategy().getAIControlState())
		{
			case MATCH_MODE:
			case MIXED_TEAM_MODE:
				roleFinder.computeRoles(currentPlaysToAssign, frame);
				break;
			default:
				break;
		}
		
		keeperPlay = null;
		offensePlay = null;
		supportPlay = null;
		defensePlay = null;
		
		// Find the specialized plays to do tailored assignment later.
		for (final APlay play : currentPlaysToAssign)
		{
			if (play.getType() == EPlay.KEEPER)
			{
				keeperPlay = play;
			}
			if (play.getType() == EPlay.OFFENSIVE)
			{
				offensePlay = play;
				continue;
			}
			
			if (play.getType() == EPlay.DEFENSIVE)
			{
				defensePlay = play;
				continue;
			}
			
			if (play.getType() == EPlay.SUPPORT)
			{
				supportPlay = play;
				continue;
			}
			
			if (play.getType() == EPlay.GUI_TEST)
			{
				guiTestPlay = play;
				continue;
			}
			
		}
		
		// Special treatment for keeper.
		if ((keeperPlay != null) && (keeperPlay.getRoles().size() > 0)) // if that's not true, no keeper is wanted
		{
			// check whether the configured keeper is present
			TrackedTigerBot keeper = frame.getWorldFrame().getTigerBotsAvailable().getWithNull(frame.getKeeperId());
			// if not, try to get anyone else to be the keeper.
			if (keeper == null)
			{
				// the bot who was keeper last time is preferred
				keeper = assignees.getWithNull(keeperId);
				if (keeper == null)
				{
					// if that bot is also gone, try the next assignee
					for (TrackedTigerBot tBot : assignees.values())
					{
						keeper = tBot;
						break;
					}
				}
				if (keeper != null)
				{
					// An alternative keeper is found -> store his ID to prevent toggling
					keeperId = keeper.getId();
				}
			}
			
			List<TrackedTigerBot> keeperBots = new LinkedList<TrackedTigerBot>();
			keeperBots.add(keeper);
			// assign the keeper to the KeeperPlay
			assignBotsToPlay(keeperBots, keeperPlay, assignees.values());
			
			// This bot is no longer available for other roles in this frame
			assignees.values().removeAll(keeperBots);
			
			currentPlaysToAssign.remove(keeperPlay); // done with this play
		}
		
		// Assigning offense bot(s)
		if ((offensePlay != null) && (offensePlay.getRoles().size() > 0))
		{
			List<TrackedTigerBot> offBots = new LinkedList<TrackedTigerBot>();
			// for all desired offense roles, get the best offense bot
			for (int i = 0; i < offensePlay.getRoles().size(); i++)
			{
				TrackedTigerBot bestOffenseBot = getBestOffenseBot(assignees.values(), frame);
				assignees.remove(bestOffenseBot.getId());
				offBots.add(bestOffenseBot);
			}
			// assign the fount bots to the OffensePlay
			assignBotsToPlay(offBots, offensePlay, assignees.values());
			
			// These bots are no longer available for other roles in this frame
			assignees.values().removeAll(offBots);
			currentPlaysToAssign.remove(offensePlay); // done with this play
		}
		
		// Assigning defense bots
		if ((defensePlay != null) && (defensePlay.getRoles().size() > 0))
		{
			int defenderCount = defensePlay.getRoles().size();
			if (defensePlay.getRoles().size() > assignees.size())
			{
				defenderCount = assignees.size();
				log.warn("Not enough Bots for all DEFENSE-Roles. Some will not be assigned!");
			}
			
			// Get the best suited defends points for the number of defenders
			List<DefensePoint> defPoints = DefensePlay.getPointsToDefend(frame.getWorldFrame(), frame.getPrevFrame(),
					frame.getTacticalField(),
					defenderCount, frame
							.getWorldFrame().getBall().getPos());
			
			// Calculate the best bots for those points
			List<TrackedTigerBot> defBots = getBestDefBots(assignees.values(), defPoints, frame);
			
			// Assign these bots to the DefensePlay
			assignBotsToPlay(defBots, defensePlay, assignees.values());
			
			// These bots are no longer available for other roles in this frame
			assignees.values().removeAll(defBots);
			currentPlaysToAssign.remove(defensePlay); // done with this play
		}
		
		// GuiTestPlay does most of the logic itself, just assigning roles with uninitialized BotIDs
		if ((guiTestPlay != null) && (guiTestPlay.getRoles().size() > 0))
		{
			for (ARole role : guiTestPlay.getRoles())
			{
				if (!role.getBotID().isUninitializedID() && assignees.containsKey(role.getBotID()))
				{
					assignees.remove(role.getBotID());
				} else
				{
					assigner.assign((TrackedTigerBot) assignees.values().toArray()[0], assignees.values(), role);
					assignees.values().remove(assignees.values().toArray()[0]);
				}
			}
			currentPlaysToAssign.remove(guiTestPlay); // done with this play.
		}
		
		// Assigning remaining bots to remaining plays
		for (APlay play : currentPlaysToAssign)
		{
			if ((play != null) && (play.getRoles().size() > 0))
			{
				List<TrackedTigerBot> pBots = new LinkedList<TrackedTigerBot>(assignees.values());
				if (pBots.size() < play.getRoles().size())
				{
					log.warn("not enough bots for all roles of " + play.getType() + ". Some will not be assigned!");
				}
				if (pBots.size() > play.getRoles().size())
				{
					pBots = pBots.subList(0, play.getRoles().size());
				}
				assignBotsToPlay(pBots, play, assignees.values());
				assignees.values().removeAll(pBots);
			}
		}
		
	}
	
	
	/**
	 * This method assignes the given bots to the play. It may adjust the roles assigned to the play, if role and bot
	 * count are not equal. But in most cases this was already done by the {@link RoleFinder}
	 * 
	 * @param bots the bots which should be assigned to the play
	 * @param play the play
	 * @param assignees the available bots for this assignment
	 */
	private void assignBotsToPlay(final List<TrackedTigerBot> bots, final APlay play,
			final Collection<TrackedTigerBot> assignees)
	{
		final List<TrackedTigerBot> botsToAssign = new LinkedList<TrackedTigerBot>();
		final List<ARole> rolesToAssign = new LinkedList<ARole>(play.getRoles());
		
		// Gather required role-assignments
		for (final TrackedTigerBot bot : bots)
		{
			boolean alreadyAssigned = false;
			
			// if a role of this play has bot already assigned, don't assign to that role
			for (final ARole role : play.getRoles())
			{
				if (role.getBotID().equals(bot.getId()))
				{
					alreadyAssigned = true;
					rolesToAssign.remove(role);
					break;
				}
			}
			// if this bot is not already assigned to a role in this play, add it to the botsToAssign list
			if (!alreadyAssigned)
			{
				botsToAssign.add(bot);
			}
		}
		
		// delete roles with assigned BotIDs, that are not present.
		List<ARole> rolesToDelete = new ArrayList<ARole>();
		for (final ARole role : play.getRoles())
		{
			boolean botForRoleExists = false;
			for (final TrackedTigerBot bot : bots)
			{
				if (role.getBotID().isUninitializedID() || role.getBotID().equals(bot.getId()))
				{
					botForRoleExists = true;
				}
			}
			if (!botForRoleExists)
			{
				rolesToDelete.add(role);
			}
		}
		for (ARole role : rolesToDelete)
		{
			play.removeRole(role);
		}
		
		// add roles, if there are more bots to assign
		if (botsToAssign.size() > rolesToAssign.size())
		{
			List<ARole> newRoles = play.addRoles(botsToAssign.size() - rolesToAssign.size());
			rolesToAssign.addAll(newRoles);
		}
		
		// remove unneeded roles
		if (rolesToAssign.size() > botsToAssign.size())
		{
			int numberOfRolesToRemove = rolesToAssign.size() - botsToAssign.size();
			for (int i = 0; i < numberOfRolesToRemove; i++)
			{
				ARole overheadRole = rolesToAssign.get(0);
				play.removeRole(overheadRole);
				rolesToAssign.remove(overheadRole);
			}
		}
		
		// if Heisenberg did something mysterious...
		if (rolesToAssign.size() != botsToAssign.size())
		{
			log.error("Size of rolesToAssign (" + rolesToAssign.size() + ") not equal to botsToAssign ("
					+ botsToAssign.size() + "). This should never happen because of previous balancing.");
			return;
		}
		
		// assign botsToAssign to RolesToAssign
		int iBot = 0;
		for (final ARole role : rolesToAssign)
		{
			
			// Checking for old roles of the bot and removing them
			freeBot(botsToAssign.get(iBot));
			
			BotID oldBotID = role.getBotID();
			if (!oldBotID.isUninitializedID())
			{
				// Removing old bot from play
				ARole roleToAssign = resetRoleFromPlay(role, play);
				assigner.assign(botsToAssign.get(iBot), assignees, roleToAssign);
				assignees.remove(botsToAssign.get(iBot));
				lastAssignmentTime = System.currentTimeMillis();
				iBot++;
				continue;
			}
			assigner.assign(botsToAssign.get(iBot), assignees, role);
			assignees.remove(botsToAssign.get(iBot));
			iBot++;
		}
		
	}
	
	
	/**
	 * This method finds a role in a play and reinitializes it.
	 * 
	 * @param role the role to reset
	 * @param play the play where to find the role
	 * @return the reset role
	 */
	private ARole resetRoleFromPlay(final ARole role, final APlay play)
	{
		play.removeRole(role);
		ARole newRole = play.addRoles(1).get(0);
		return newRole;
	}
	
	
	/**
	 * searches for a bot in a List of roles.
	 * 
	 * @param roles the list of roles where to find the bot
	 * @param bot the desired bot
	 * @return the role where the bot was fount
	 */
	private ARole searchRolesForBot(final List<ARole> roles, final TrackedTigerBot bot)
	{
		
		for (final ARole role : roles)
		{
			if (role.getBotID().getNumber() == bot.getId().getNumber())
			{
				return role;
			}
		}
		return null;
	}
	
	
	/**
	 * Unbinds a bot from any roles in playsToAssign
	 * 
	 * @param bot the bot to free
	 */
	private void freeBot(final TrackedTigerBot bot)
	{
		for (APlay p : playsToAssign)
		{
			ARole r = searchRolesForBot(p.getRoles(), bot);
			if (r != null)
			{
				r = resetRoleFromPlay(r, p);
			}
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- calculation routines --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * This method calculates for each bot how suitable it is to be the offense bot.
	 * 
	 * @param assignees bots available for the offense
	 * @param frame current AthenaAiFrame
	 * @return the best offense bot
	 */
	private TrackedTigerBot getBestOffenseBot(final Collection<TrackedTigerBot> assignees, final AthenaAiFrame frame)
	{
		TrackedTigerBot bestOffenseBot = null;
		float bestCost = Float.MAX_VALUE;
		for (final TrackedTigerBot tBot : assignees)
		{
			// Get the best calculated OffenseMovePosition
			IVector2 pos = frame.getTacticalField().getOffenseMovePositions().get(tBot.getId());
			
			float bestPathCost = getTimeToPos(tBot, pos, frame.getWorldFrame())
					+ calcCost(tBot, offensePlay.getRoles().get(0), frame);
			
			if (bestPathCost <= bestCost)
			{
				bestOffenseBot = tBot;
				bestCost = bestPathCost;
			}
		}
		return bestOffenseBot;
	}
	
	
	private List<TrackedTigerBot> getBestDefBots(final Collection<TrackedTigerBot> assignees,
			final List<DefensePoint> defPoints,
			final AthenaAiFrame frame)
	{
		// paarweise
		// Berechnung der Minimalzeit f�r den Weg zu den defPoints
		List<TrackedTigerBot> currentDefenderBots = new ArrayList<TrackedTigerBot>();
		for (ARole existingRole : frame.getPrevFrame().getPlayStrategy().getActiveRoles().values())
		{
			if (existingRole instanceof DefenderRole)
			{
				for (TrackedTigerBot possibleDefender : assignees)
				{
					if (possibleDefender.getId().equals(existingRole.getBotID()))
					{
						currentDefenderBots.add(possibleDefender);
					}
				}
				
			}
		}
		
		List<TrackedTigerBot> resultBots = new LinkedList<TrackedTigerBot>();
		if (currentDefenderBots.size() >= defensePlay.getRoles().size())
		{
			getMinimalPairTime(currentDefenderBots, defPoints, resultBots, frame);
		} else
		{
			List<TrackedTigerBot> bots = new LinkedList<TrackedTigerBot>(assignees);
			getMinimalPairTime(bots, defPoints, resultBots, frame);
		}
		
		return resultBots;
	}
	
	
	private float getMinimalPairTime(final List<TrackedTigerBot> bots, final List<DefensePoint> points,
			final List<TrackedTigerBot> resultBots, final AthenaAiFrame frame)
	{
		if (points.size() == 0)
		{
			return 0;
		}
		
		float minTime = Float.MAX_VALUE;
		for (int i = 0; i < bots.size(); i++)
		{
			
			float time = getTimeToPos(bots.get(i), points.get(0), frame.getWorldFrame());
			time += (bots.get(i).getPos().x() * POSITION_FACTOR);
			time += calcCost(bots.get(i), defensePlay.getRoles().get(0), frame); // bots wich cannot serve all defender
																										// features are not prefered
			if ((supportPlay != null) && !supportPlay.getRoles().isEmpty())
			{
				time -= calcCost(bots.get(i), supportPlay.getRoles().get(0), frame); // bots which may not be good for the
																											// supportPlay are prefered
			}
			
			
			List<TrackedTigerBot> newBotList = new LinkedList<>(bots);
			List<DefensePoint> newPosList = new LinkedList<>(points);
			newBotList.remove(i);
			newPosList.remove(0);
			List<TrackedTigerBot> childResults = new LinkedList<TrackedTigerBot>();
			time += getMinimalPairTime(newBotList, newPosList, childResults, frame);
			if (time < minTime)
			{
				resultBots.clear(); // ein bisschen Speicher sparen in Java
				resultBots.addAll(childResults);
				resultBots.add(bots.get(i));
				minTime = time;
			}
		}
		
		return minTime;
	}
	
	
	private void updateGameState(final EGameState newGameState)
	{
		if (currentGameState != newGameState)
		{
			gameStateChangedThisFrame = true;
			currentGameState = newGameState;
			return;
		}
		gameStateChangedThisFrame = false;
	}
	
	
	private float getTimeToPos(final TrackedTigerBot tBot, final IVector2 pos,
			final WorldFrame wFrame)
	{
		
		// very cheap calculation of time...
		float bestTime = 0;
		
		float wayToPos = pos.subtractNew(tBot.getPos().addNew(tBot.getVel())).getLength2();
		bestTime = (1 * tBot.getaVel()) + (wayToPos / (Sisyphus.maxLinearVelocity * 0.8f));
		
		return bestTime;
		
	}
	
	
	/**
	 * This is the point where you can add some deeper thoughts to the whole calculation very comfortably
	 * <p>
	 * Values have to be <u>between</u> 0 and {@link Integer#MAX_VALUE}.<br/>
	 * <strong>ATTENTION:</strong> Returning 0 is equivalent to "not yet calculated"!
	 * </p>
	 * 
	 * @param tiger
	 * @param role
	 * @param frame
	 * @return The cost this tiger would have to effort to take this role
	 */
	private int calcCost(final TrackedTigerBot tiger, final ARole role, final AthenaAiFrame frame)
	{
		// use this method to deactivate a score. there is not yet a gui interface TODO unassigned create gui
		// scores.get(EScore.BALL_ROLE_DISTANCE).setActive(true);
		int score = 0;
		for (AScore method : scores.values())
		{
			score += method.calcScore(tiger, role, frame);
		}
		return score;
	}
	
}
