/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import edu.tigers.sumatra.ai.athena.roleassigner.RoleMapping;
import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.botdistance.BotDistance;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBallThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseBotThreat;
import edu.tigers.sumatra.ai.metis.defense.data.DefenseThreatAssignment;
import edu.tigers.sumatra.ai.metis.interchange.BotInterchange;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTree;
import edu.tigers.sumatra.ai.metis.offense.action.OffensiveActionTreePath;
import edu.tigers.sumatra.ai.metis.offense.action.moves.OffensiveAction;
import edu.tigers.sumatra.ai.metis.offense.action.situation.EOffensiveSituation;
import edu.tigers.sumatra.ai.metis.offense.ballinterception.BallInterception;
import edu.tigers.sumatra.ai.metis.offense.kickoff.KickoffStrategy;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveAnalysedFrame;
import edu.tigers.sumatra.ai.metis.offense.statistics.OffensiveStatisticsFrame;
import edu.tigers.sumatra.ai.metis.offense.strategy.OffensiveStrategy;
import edu.tigers.sumatra.ai.metis.offense.strategy.SkirmishInformation;
import edu.tigers.sumatra.ai.metis.shootout.PenaltyPlacementTargetGroup;
import edu.tigers.sumatra.ai.metis.statistics.MatchStats;
import edu.tigers.sumatra.ai.metis.statistics.possiblegoal.EPossibleGoal;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.metis.support.SupportPosition;
import edu.tigers.sumatra.ai.metis.targetrater.IRatedTarget;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.botmanager.commands.MultimediaControl;
import edu.tigers.sumatra.drawable.ShapeMap;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeam;
import edu.tigers.sumatra.math.circle.ICircle;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.vision.data.IKickEvent;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Interface for accessing the tactical field with the possibility to modify any fields
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITacticalField
{
	/**
	 * @return
	 */
	BallPossession getBallPossession();
	
	
	/**
	 * @return An ordered {@link Map} with all Tiger bots sorted by their distance to the ball (closest first)
	 */
	List<BotDistance> getTigersToBallDist();
	
	
	/**
	 * @return The {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are no
	 *         Tiger bots!!!)
	 */
	BotDistance getTigerClosestToBall();
	
	
	/**
	 * @return An ordered {@link Map} with all enemy bots sorted by their distance to the ball (closest first)
	 */
	List<BotDistance> getEnemiesToBallDist();
	
	
	/**
	 * @return The opponent {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there
	 *         are no opponent bots!!!)
	 */
	BotDistance getEnemyClosestToBall();
	
	
	/**
	 * @return
	 */
	EPossibleGoal getPossibleGoal();
	
	
	/**
	 * @return the bot who is last touched the ball, or {@link BotID#noBot()} if no bot touched the ball yet
	 */
	Set<BotID> getBotsLastTouchedBall();
	
	
	/**
	 * @return the bot who is touching ball atm., or {@link BotID#noBot()} if no bot is touching the ball
	 */
	Set<BotID> getBotsTouchingBall();
	
	
	/**
	 * @return the bot that just kicked ball in GameState KickOff, Indirect or Direct FreeKick, for double touch
	 *         avoidance
	 */
	BotID getBotNotAllowedToTouchBall();
	
	
	/**
	 * @param team the attacking team
	 * @return the goal kick, if present
	 */
	Optional<IKickEvent> getDetectedGoalKick(ETeam team);
	
	
	/**
	 * @return true if icing rule will definitely be violated by opponent team
	 */
	boolean isOpponentWillDoIcing();
	
	
	/**
	 * @return the bestDirectShootTarget
	 */
	Optional<IRatedTarget> getBestGoalKickTarget();
	
	
	/**
	 * @return the bestDirectShotTargetsForTigerBots
	 */
	Map<BotID, Optional<IRatedTarget>> getBestGoalKickTargetForBot();
	
	
	/**
	 * @return
	 */
	OffensiveStrategy getOffensiveStrategy();
	
	
	/**
	 * @return
	 */
	GameState getGameState();
	
	
	/**
	 * Flag for a goal scored (tigers or foes), used for forcing all bots on our side before prepare kickoff signal
	 * true when a goal was scored, the game state is stopped until it is started again.
	 *
	 * @return the goalScored
	 */
	boolean isGoalScored();
	
	
	/**
	 * @return
	 */
	IVector2 getBallLeftFieldPos();
	
	
	/**
	 * @return the Statistics
	 */
	MatchStats getMatchStatistics();
	
	
	/**
	 * @return
	 */
	Map<EPlay, RoleMapping> getRoleMapping();
	
	
	/**
	 * @return
	 */
	ShapeMap getDrawableShapes();
	
	
	/**
	 * Best points to pass to near bots
	 *
	 * @return
	 */
	List<IPassTarget> getPassTargetsRanked();
	
	
	/**
	 * Crucial defender are those who are currently most attractive for defense and which will be assigned first.<br>
	 * They may not contain crucial offenders and offense may not select those in the current frame.
	 *
	 * @return
	 */
	Set<BotID> getCrucialDefender();
	
	
	/**
	 * Crucial offenders are those who are currently assigned and which may thus not be selected by any other play.<br>
	 * Important: crucial offenders are not guarantied to be actually assigned to offense in this frame!
	 *
	 * @return
	 */
	Set<BotID> getCrucialOffender();
	
	
	/**
	 * @return
	 */
	Map<ECalculator, Integer> getMetisCalcTimes();
	
	
	/**
	 * @return
	 */
	Map<ECalculator, Boolean> getMetisExecutionStatus();
	
	
	/**
	 * @return
	 */
	Map<BotID, OffensiveAction> getOffensiveActions();
	
	
	/**
	 * @return next Keeperstate
	 */
	EKeeperState getKeeperState();
	
	
	/**
	 * @return the ledData
	 */
	Map<BotID, MultimediaControl> getMultimediaControl();
	
	
	/**
	 * @return the SkirmishInformation
	 */
	SkirmishInformation getSkirmishInformation();
	
	
	/**
	 * Note: the frames are only stored if the offensiveStatistics are activated in the config
	 *
	 * @return
	 */
	OffensiveStatisticsFrame getOffensiveStatistics();
	
	
	/**
	 * @return
	 */
	OffensiveAnalysedFrame getAnalyzedOffensiveStatisticsFrame();
	
	
	/**
	 * @return
	 */
	KickoffStrategy getKickoffStrategy();
	
	
	/**
	 * @return the ball interception information
	 */
	Map<BotID, BallInterception> getBallInterceptions();
	
	
	/**
	 * @return true if foe interfere keeper chipping the ball out of pe
	 */
	boolean isBotInterferingKeeperChip();
	
	
	/**
	 * @return
	 */
	IVector2 getSupportiveAttackerMovePos();
	
	
	/**
	 * Returns the number of defenders calculated by the NumDefenderCalc
	 *
	 * @return nDefender
	 */
	int getNumDefender();
	
	
	/**
	 * @return
	 */
	IVector2 getChipKickTarget();
	
	
	/**
	 * @return
	 */
	ITrackedBot getChipKickTargetBot();
	
	
	/**
	 * @return
	 */
	Optional<ITrackedBot> getOpponentPassReceiver();
	
	
	/**
	 * @return
	 */
	List<DefenseThreatAssignment> getDefenseThreatAssignments();
	
	
	/**
	 * @return list of defense threats
	 */
	List<DefenseBotThreat> getDefenseBotThreats();
	
	
	/**
	 * @return who is responsible for handling the ball
	 */
	EBallResponsibility getBallResponsibility();
	
	
	/**
	 * @return
	 */
	Map<EPlay, Integer> getPlayNumbers();
	
	
	/**
	 * @return the ball threat
	 */
	DefenseBallThreat getDefenseBallThreat();
	
	
	/**
	 * @return the desired roles
	 */
	Map<EPlay, Set<BotID>> getDesiredBotMap();
	
	
	/**
	 * @return selected support positions
	 */
	List<SupportPosition> getSelectedSupportPositions();
	
	
	/**
	 * @return The list of PenaltyPlacementTargetGroups
	 */
	List<PenaltyPlacementTargetGroup> getPenaltyPlacementTargetGroups();
	
	
	/**
	 * @return
	 */
	PenaltyPlacementTargetGroup getFilteredPenaltyPlacementTargetGroup();
	
	
	/**
	 * @return OffensiveActionTrees
	 */
	Map<EOffensiveSituation, OffensiveActionTree> getActionTrees();
	
	
	/**
	 * @return currentActive Path of the offensiveTree
	 */
	OffensiveActionTreePath getCurrentPath();
	
	
	/**
	 * @return get currently active Situation
	 */
	EOffensiveSituation getCurrentSituation();


	/***/
	List<ICircle> getFreeSpots();


	/**
	 * @return mutable AI information for next frame
	 */
	IAiInfoForNextFrame getAiInfoForNextFrame();


	/**
	 * @return immutable AI information from previous frame
	 */
	IAiInfoFromPrevFrame getAiInfoFromPrevFrame();


	/**
	 * @return bot interchange data structure
	 */
	BotInterchange getBotInterchange();


	/**
	 * @return true if ball is inside push radius for ball placement (configurable)
	 */
	boolean isBallInPushRadius();


	/**
	 * @return get distance for changing State of KeeperOneOnOneRole into RamboState
	 */
	double getKeeperRamboDistance();
}
