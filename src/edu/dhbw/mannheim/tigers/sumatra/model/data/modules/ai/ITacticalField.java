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

import edu.dhbw.mannheim.tigers.sumatra.model.data.ValuedField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.DefensePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValueBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.valueobjects.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.AIRectangleVector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.EnhancedFieldAnalyser;


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
	List<DefensePoint> getDefGoalPoints();
	
	
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
	 * @return dynamicFieldAnalyser - a analyser witch allowed to find some specific points with the help of
	 *         {@link AIRectangleVector}
	 */
	EnhancedFieldAnalyser getEnhancedFieldAnalyser();
	
	
	/**
	 * @return the goalValuePoints
	 */
	List<ValuePoint> getGoalValuePoints();
	
	
	/**
	 * @return the bestDirectShootTarget
	 */
	ValuePoint getBestDirectShootTarget();
	
	
	/**
	 * @return
	 */
	Map<BotID, ValuePoint> getBestDirectShotTargetBots();
	
	
	/**
	 * @return
	 */
	Map<BotID, List<ValueBot>> getShooterReceiverStraightLines();
	
	
	/**
	 * @return
	 */
	Map<BotID, ValueBot> getBallReceiverStraightLines();
	
	
	/**
	 * @return
	 */
	Map<BotID, IVector2> getSupportPositions();
	
	
	/**
	 * @return
	 */
	Map<BotID, IVector2> getSupportTargets();
	
	
	/**
	 * @return
	 */
	List<IVector2> getSupportIntersections();
	
	
	/**
	 * @return
	 */
	Map<BotID, IVector2> getSupportRedirectPositions();
	
	
	/**
	 * @return
	 */
	Map<BotID, ValuedField> getSupportValues();
	
	
	/**
	 * @return
	 */
	Map<BotID, ValuePoint> getOffenseMovePositions();
	
	
	/**
	 * @return
	 */
	Map<BotID, BotAiInformation> getBotAiInformation();
	
	
	/**
	 * @return
	 */
	EGameState getGameState();
	
	
	/**
	 * @return
	 */
	IVector2 getBallLeftFieldPos();
	
	
	/**
	 * @return
	 */
	ValueBot getBestPassTarget();
	
	
	/**
	 * @return the Statistics
	 */
	Statistics getStatistics();
}
