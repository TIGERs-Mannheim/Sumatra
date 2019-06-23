/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 03.09.2010
 * Author(s):
 * Gunther Berthold <gunther.berthold@gmx.net>
 * Oliver Steinbrecher
 * Daniel Waigand
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.WorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.types.ValuePoint;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ApproximateScoringChance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.BotToBallDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.BallPossessionCalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.DangerousOpponents;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ScoringChance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.TeamClosestToBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePoints;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsCarrier;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsReceiver;


/**
 * 
 * This class does situation/field analysis. Metis coordinates all calculators to analyze the {@link WorldFrame}.
 * She will eventually put all the gathered conclusions in the {@link AIInfoFrame}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Metis implements IAIProcessor
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	protected final Logger						log												= Logger.getLogger(getClass());
	
	// calculators
	private final BotToBallDistance			tigersToBallDistance							= new BotToBallDistance(ETeam.TIGERS);
	private final BotToBallDistance			enemiesToBallDistance						= new BotToBallDistance(
																													ETeam.OPPONENTS);
	private final DefenseHelper				defenseHelper									= new DefenseHelper();
	private final DefensePoints				defPointCalc									= new DefensePoints();
	private final BallPossessionCalculator	ballPossessionCalc							= new BallPossessionCalculator();
	private final TeamClosestToBall			teamClosestToBallCalc						= new TeamClosestToBall();
	private final ScoringChance				tigersScoringChanceCalc						= new ScoringChance(ETeam.TIGERS);
	private final ScoringChance				opponentScoringChanceCalc					= new ScoringChance(ETeam.OPPONENTS);
	private final ApproximateScoringChance	approximateTigersScoringChanceCalc		= new ApproximateScoringChance(
																													ETeam.TIGERS);
	private final ApproximateScoringChance	approximateOpponentScoringChanceCalc	= new ApproximateScoringChance(
																													ETeam.OPPONENTS);
	private final OffensePointsCarrier		offPointCarrierCalc							= new OffensePointsCarrier();
	private final OffensePointsReceiver		offPointLeftReceiverCalc					= new OffensePointsReceiver(ESide.LEFT);
	private final OffensePointsReceiver		offPointRightReceiverCalc					= new OffensePointsReceiver(
																													ESide.RIGHT);
	
	private final DangerousOpponents			dangerousOpponents							= new DangerousOpponents();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public Metis()
	{
		
	}
	
	
	@Override
	public AIInfoFrame process(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		// check whether there is a worldFrame
		if (curFrame.worldFrame != null)
		{
			curFrame.tacticalInfo.setTigersToBallDist(tigersToBallDistance.calculate(curFrame));
			curFrame.tacticalInfo.setEnemiesToBallDist(enemiesToBallDistance.calculate(curFrame));
			curFrame.tacticalInfo.setBallInOurPenArea(defenseHelper.isBallInOurPenaltyArea(curFrame));
			curFrame.tacticalInfo.setDefGoalPoints(defPointCalc.calculate(curFrame));
			curFrame.tacticalInfo.setBallPossesion(ballPossessionCalc.calculate(curFrame, preFrame));
			curFrame.tacticalInfo.setTigersScoringChance(tigersScoringChanceCalc.calculate(curFrame));
			curFrame.tacticalInfo.setOpponentScoringChance(opponentScoringChanceCalc.calculate(curFrame));
			curFrame.tacticalInfo
					.setTigersApproximateScoringChance(approximateTigersScoringChanceCalc.calculate(curFrame));
			curFrame.tacticalInfo.setOpponentApproximateScoringChance(approximateOpponentScoringChanceCalc
					.calculate(curFrame));
			curFrame.tacticalInfo.setTeamClosestToBall(teamClosestToBallCalc.calculate(curFrame));
			curFrame.tacticalInfo.setOffCarrierPoints(offPointCarrierCalc.calculate(curFrame));
			curFrame.tacticalInfo.setOffLeftReceiverPoints(offPointLeftReceiverCalc.calculate(curFrame));
			curFrame.tacticalInfo.setOffRightReceiverPoints(offPointRightReceiverCalc.calculate(curFrame));
			curFrame.tacticalInfo.setDangerousOpponents(dangerousOpponents.calculate(curFrame, preFrame));
			curFrame.tacticalInfo.setOpponentBallGetter(dangerousOpponents.getOpponentBallGetter());
			curFrame.tacticalInfo.setOpponentPassReceiver(dangerousOpponents.getOpponentPassReceiver());
			curFrame.tacticalInfo.setOpponentKeeper(dangerousOpponents.getOpponentKeeper());
		} else
		{
			log.warn("Metis received null frame");
			
			// create default list
			Vector2f fieldCenter = AIConfig.getGeometry().getCenter();
			List<ValuePoint> defaultList = new ArrayList<ValuePoint>();
			defaultList.add(new ValuePoint(fieldCenter.x, fieldCenter.y));
			
			// take default values for tactical field
			curFrame.tacticalInfo.setDefGoalPoints(defaultList);
			curFrame.tacticalInfo.setBallPossesion(null);
			curFrame.tacticalInfo.setTigersScoringChance(false);
			curFrame.tacticalInfo.setBallInOurPenArea(false);
			curFrame.tacticalInfo.setOpponentScoringChance(false);
			curFrame.tacticalInfo.setTigersApproximateScoringChance(false);
			curFrame.tacticalInfo.setOpponentApproximateScoringChance(false);
			curFrame.tacticalInfo.setTeamClosestToBall(ETeam.UNKNOWN);
			curFrame.tacticalInfo.setOffCarrierPoints(defaultList);
			curFrame.tacticalInfo.setOffLeftReceiverPoints(defaultList);
			curFrame.tacticalInfo.setOffRightReceiverPoints(defaultList);
			curFrame.tacticalInfo.setDangerousOpponents(dangerousOpponents.calculate(curFrame, preFrame));
			curFrame.tacticalInfo.setOpponentBallGetter(null);
			curFrame.tacticalInfo.setOpponentPassReceiver(null);
			curFrame.tacticalInfo.setOpponentKeeper(null);
		}
		
		return curFrame;
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
}
