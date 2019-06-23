/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 12, 2013
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.MultiTeamMessage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.IDrawableShape;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.lachesis.RoleFinderInfo;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.data.FoeBotData;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveAction;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.data.OffensiveMovePosition;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.support.data.AdvancedPassTarget;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.view.visualizer.internals.field.EDrawableShapesLayer;


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
	BallPossession getBallPossession();
	
	
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
	Map<BotID, OffensiveMovePosition> getOffenseMovePositions();
	
	
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
	EGameState getGameState();
	
	
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
	Statistics getStatistics();
	
	
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
	Map<EDrawableShapesLayer, List<IDrawableShape>> getDrawableShapes();
	
	
	/**
	 * Best points to pass to near bots
	 * 
	 * @return
	 */
	List<AdvancedPassTarget> getAdvancedPassTargetsRanked();
	
	
	/**
	 * @return the dangerousFoeBots
	 */
	List<FoeBotData> getDangerousFoeBots();
	
	
	/**
	 * @return
	 */
	Map<ELetter, List<IVector2>> getLetters();
	
	
	/**
	 * @return
	 */
	Map<BotID, SortedMap<Long, IVector2>> getBotPosBuffer();
	
	
	/**
	 * @return
	 */
	List<TrackedBall> getBallBuffer();
	
	
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
	boolean needTwoForBallBlock();
	
	
	/**
	 * @param needTwoForBallBlock
	 */
	void setNeedTwoForBallBlock(boolean needTwoForBallBlock);
	
	
	/**
	 * @return
	 */
	Map<BotID, IVector2> getSupportPositions();
	
	
	/**
	 * @return
	 */
	MultiTeamMessage getMultiTeamMessage();
	
	
	/**
	 * @param message
	 */
	void setMultiTeamMessage(MultiTeamMessage message);
	
	
	/**
	 * @return
	 */
	public boolean isMixedTeamBothTouchedBall();
	
}
