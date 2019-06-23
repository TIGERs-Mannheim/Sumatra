/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.tigers.sumatra.ai.data.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.data.event.GameEvents;
import edu.tigers.sumatra.ai.lachesis.RoleFinderInfo;
import edu.tigers.sumatra.ai.metis.ECalculator;
import edu.tigers.sumatra.ai.metis.defense.KeeperStateCalc;
import edu.tigers.sumatra.ai.metis.defense.data.AngleDefenseData;
import edu.tigers.sumatra.ai.metis.defense.data.DefensePoint;
import edu.tigers.sumatra.ai.metis.defense.data.FoeBotData;
import edu.tigers.sumatra.ai.metis.offense.data.OffensiveAction;
import edu.tigers.sumatra.ai.metis.support.data.AdvancedPassTarget;
import edu.tigers.sumatra.ai.pandora.plays.EPlay;
import edu.tigers.sumatra.drawable.ValuedField;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.ValuePoint;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.ShapeMap;


/**
 * Interface for accessing the tactical field with the possibility to modify any fields
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public interface ITacticalField
{
	/**
	 * Remove data that is too large to keep
	 */
	void cleanup();
	
	
	/**
	 * @return
	 */
	public AngleDefenseData getAngleDefenseData();
	
	
	/**
	 * @param angleDefenseData the angleDefensePreData to set
	 */
	public void setAngleDefenseData(final AngleDefenseData angleDefenseData);
	
	
	/**
	 * @return
	 */
	BallPossession getBallPossession();
	
	
	/**
	 * @param directShotSingleDefenderDefPoint
	 */
	public void setDirectShotSingleDefenderDefPoint(final DefensePoint directShotSingleDefenderDefPoint);
	
	
	/**
	 * @return
	 */
	public DefensePoint getDirectShotSingleDefenderDefPoint();
	
	
	/**
	 * @return bestCPUPasstargets
	 */
	public List<ValuePoint> getScoreChancePoints();
	
	
	/**
	 * @param directShotDoubleDefenderDefPointA
	 */
	public void setDirectShotDoubleDefenderDefPointA(final DefensePoint directShotDoubleDefenderDefPointA);
	
	
	/**
	 * @return
	 */
	public DefensePoint getDirectShotDoubleDefenderDefPointA();
	
	
	/**
	 * @param directShotDoubleDefenderDefPointB
	 */
	public void setDirectShotDoubleDefenderDefPointB(final DefensePoint directShotDoubleDefenderDefPointB);
	
	
	/**
	 * @return
	 */
	public DefensePoint getDirectShotDoubleDefenderDefPointB();
	
	
	/**
	 * @param directShotDefenderDistr
	 */
	void setDirectShotDefenderDistr(final Map<ITrackedBot, DefensePoint> directShotDefenderDistr);
	
	
	/**
	 * @return
	 */
	public Map<ITrackedBot, DefensePoint> getDirectShotDefenderDistr();
	
	
	/**
	 * @return A {@link LinkedHashMap} with all Tiger bots sorted by their distance to the ball (
	 *         {@link BotDistance#ASCENDING}).
	 */
	Map<BotID, BotDistance> getTigersToBallDist();
	
	
	/**
	 * @return The {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are no
	 *         Tiger bots!!!)
	 */
	BotDistance getTigerClosestToBall();
	
	
	/**
	 * @return A {@link LinkedHashMap} with all enemy bots sorted by their distance to the ball (
	 *         {@link BotDistance#ASCENDING}).
	 */
	Map<BotID, BotDistance> getEnemiesToBallDist();
	
	
	/**
	 * @return The enemy {@link BotDistance} closest to the ball (or {@link BotDistance#NULL_BOT_DISTANCE} if there are
	 *         no enemy bots!!!)
	 */
	BotDistance getEnemyClosestToBall();
	
	
	/**
	 * @return
	 */
	EPossibleGoal getPossibleGoal();
	
	
	/**
	 * @return the botLastTouchedBall
	 */
	BotID getBotLastTouchedBall();
	
	
	/**
	 * @return
	 */
	BotID getBotTouchedBall();
	
	
	/**
	 * @return the bestDirectShootTarget
	 */
	ValuePoint getBestDirectShootTarget();
	
	
	/**
	 * @return the bestDirectShotTargetsForTigerBots
	 */
	Map<BotID, ValuePoint> getBestDirectShotTargetsForTigerBots();
	
	
	/**
	 * @return
	 */
	List<ValuePoint> getGoalValuePoints();
	
	
	/**
	 * @return
	 */
	ValuedField getSupporterValuedField();
	
	
	/**
	 * @return
	 */
	OffensiveStrategy getOffensiveStrategy();
	
	
	/**
	 * @return
	 */
	Map<BotID, BotAiInformation> getBotAiInformation();
	
	
	/**
	 * @return
	 */
	EGameStateTeam getGameState();
	
	
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
	MatchStatistics getStatistics();
	
	
	/**
	 * @return
	 */
	Map<EPlay, RoleFinderInfo> getRoleFinderInfos();
	
	
	/**
	 * @return
	 */
	EGameBehavior getGameBehavior();
	
	
	/**
	 * @return
	 */
	ShapeMap getDrawableShapes();
	
	
	/**
	 * Best points to pass to near bots
	 * 
	 * @return
	 */
	List<AdvancedPassTarget> getAdvancedPassTargetsRanked();
	
	
	/**
	 * @param crucialDefenderID
	 */
	public void addCrucialDefender(final BotID crucialDefenderID);
	
	
	/**
	 * @return
	 */
	public List<BotID> getCrucialDefenders();
	
	
	/**
	 * @return
	 */
	Map<ECalculator, Integer> getMetisCalcTimes();
	
	
	/**
	 * @return
	 */
	List<IVector2> getTopGpuGridPositions();
	
	
	/**
	 * @return
	 */
	Map<BotID, OffensiveAction> getOffensiveActions();
	
	
	/**
	 * @return
	 */
	public boolean isMixedTeamBothTouchedBall();
	
	
	/**
	 * @return
	 */
	AutomatedThrowInInfo getThrowInInfo();
	
	
	/**
	 * @return next Keeperstate
	 */
	public KeeperStateCalc.EStateId getKeeperState();
	
	
	/**
	 * @return
	 */
	GameEvents getGameEvents();
	
	
	/**
	 * @return
	 */
	List<FoeBotData> getDangerousFoeBots();
	
	
	/**
	 * @return the ledData
	 */
	public Map<BotID, LedControl> getLedData();
	
	
	/**
	 * @return
	 */
	List<ValuePoint> getBallDistancePoints();
}
