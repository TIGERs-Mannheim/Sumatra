/*
 * *********************************************************
 * Copyright (c) 2014 DHBW Mannheim - Tigers Mannheim
 * Project: tigers - artificial intelligence
 * Date: 28.05.2014
 * Authors: Daniel Andres <andreslopez.daniel@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general;

import java.util.HashMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.BaseAiFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EGameState;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.EPossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.Percentage;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.Statistics;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.TacticalField;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.EBallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;


/**
 * This calculator generates statistics for a game for:
 * - ball possession
 * - ball lost after zweikampf
 * - ball win after zweikampf
 * 
 * @author Daniel Andres <andreslopez.daniel@gmail.com>
 */
public class StatisticsCalc extends ACalculator
{
	// --------------------------------------------------------------------------
	// --- variable(s) ----------------------------------------------------------
	// --------------------------------------------------------------------------
	private int											ballPosEventCount			= 0;
	/** Zweikampf flag, wird für folgende Events gesetzt und ausgewertet */
	private Map<EBallPossession, Percentage>	ballPossessionGeneral;
	private Map<BotID, Percentage>				ballPossessionTigers;
	private Map<BotID, Percentage>				ballPossessionOpponents;
	
	private Percentage								tackleGeneralWon;
	private Percentage								tackleGeneralLost;
	private Map<BotID, Percentage>				tackleWon;
	private Map<BotID, Percentage>				tackleLost;
	private boolean									tackle						= false;
	/** Tiger BotID which is in a tackle */
	private BotID										tackleTiger					= null;
	private int											tackleCount					= 0;
	
	/** Possible Goal members */
	private Map<BotID, Percentage>				possibleBotGoals;
	private int											possibleTigersGoals		= 0;
	private int											possibleOpponentsGoals	= 0;
	private EPossibleGoal							lastPossibleGoalVal		= EPossibleGoal.NO_ONE;
	
	
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public StatisticsCalc()
	{
		ballPossessionGeneral = new HashMap<EBallPossession, Percentage>();
		ballPossessionTigers = new HashMap<BotID, Percentage>();
		ballPossessionOpponents = new HashMap<BotID, Percentage>();
		tackleWon = new HashMap<BotID, Percentage>();
		tackleLost = new HashMap<BotID, Percentage>();
		tackleGeneralWon = new Percentage();
		tackleGeneralLost = new Percentage();
		possibleBotGoals = new HashMap<BotID, Percentage>();
	}
	
	
	// --------------------------------------------------------------------------
	// --- method(s) ------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		// If the game is not running, take the last values...
		if (!newTacticalField.getGameState().equals(EGameState.RUNNING))
		{
			fallbackCalc(newTacticalField, baseAiFrame);
			return;
		}
		
		BallPossession curBallPos = newTacticalField.getBallPossession();
		EBallPossession curEBallPos = curBallPos.getEBallPossession();
		// update ball Possession Counter
		ballPosEventCount++;
		
		// General Statistics over Ball Possesion
		if (!ballPossessionGeneral.containsKey(curEBallPos))
		{
			ballPossessionGeneral.put(curEBallPos, new Percentage());
		}
		ballPossessionGeneral.get(curEBallPos).inc();
		
		// Check for tackle in previous event and if it was lost or won
		// TO DO Maybe use more intelligent tackle indicator for bots. In a tackle the bots can change, which is a lost
		// tackle and a new tackle. This is not implemented here
		switch (curEBallPos)
		{
			case BOTH:
				// tackle is still active
				tackle = true;
				break;
			case WE:
				// tackle won
				if (tackle && (tackleTiger != null))
				{
					tackleCount++;
					tackleGeneralWon.inc();
					if (!tackleWon.containsKey(tackleTiger))
					{
						tackleWon.put(tackleTiger, new Percentage());
					}
					tackleWon.get(tackleTiger).inc();
					
					tackleTiger = null;
				}
				tackle = false;
				break;
			case THEY:
			case NO_ONE:
				// tackle lost
				if (tackle && (tackleTiger != null))
				{
					tackleCount++;
					tackleGeneralLost.inc();
					if (!tackleLost.containsKey(tackleTiger))
					{
						tackleLost.put(tackleTiger, new Percentage());
					}
					tackleLost.get(tackleTiger).inc();
					tackleTiger = null;
				}
				tackle = false;
				break;
			default:
				tackle = false;
				break;
		}
		
		// Update per bot ballpossession count
		switch (curEBallPos)
		{
			case BOTH:
				tackle = true;
				BotID tiger = curBallPos.getTigersId();
				tackleTiger = tiger;
				if (!ballPossessionTigers.containsKey(tiger))
				{
					ballPossessionTigers.put(tiger, new Percentage());
				}
				ballPossessionTigers.get(tiger).inc();
				BotID opponent = curBallPos.getTigersId();
				if (!ballPossessionOpponents.containsKey(opponent))
				{
					ballPossessionOpponents.put(opponent, new Percentage());
				}
				ballPossessionOpponents.get(opponent).inc();
				break;
			case WE:
				BotID tigerSingle = curBallPos.getTigersId();
				if (!ballPossessionTigers.containsKey(tigerSingle))
				{
					ballPossessionTigers.put(tigerSingle, new Percentage());
				}
				ballPossessionTigers.get(tigerSingle).inc();
				break;
			case THEY:
				BotID opponentSingle = curBallPos.getTigersId();
				if (!ballPossessionOpponents.containsKey(opponentSingle))
				{
					ballPossessionOpponents.put(opponentSingle, new Percentage());
				}
				ballPossessionOpponents.get(opponentSingle).inc();
				break;
			default:
				break;
		}
		
		
		// update percentages
		for (Percentage ballPosCount : ballPossessionGeneral.values())
		{
			ballPosCount.setAll(ballPosEventCount);
		}
		for (Percentage ballPosBot : ballPossessionTigers.values())
		{
			ballPosBot.setAll(ballPosEventCount);
		}
		for (Percentage ballPosOppBot : ballPossessionOpponents.values())
		{
			ballPosOppBot.setAll(ballPosEventCount);
		}
		for (Percentage tackleCounter : tackleWon.values())
		{
			tackleCounter.setAll(tackleCount);
		}
		for (Percentage tackleCounter : tackleLost.values())
		{
			tackleCounter.setAll(tackleCount);
		}
		tackleGeneralWon.setAll(tackleCount);
		tackleGeneralLost.setAll(tackleCount);
		
		// calc possible goals
		calcPossibleGoals(newTacticalField.getPossibleGoal(), newTacticalField.getBotLastTouchedBall());
		
		// Create Data Holder package
		Statistics stats = new Statistics();
		stats.setBallPossessionGeneral(ballPossessionGeneral);
		stats.setBallPossessionOpponents(ballPossessionOpponents);
		stats.setBallPossessionTigers(ballPossessionTigers);
		stats.setTackleGeneral(tackleGeneralWon, tackleGeneralLost);
		stats.setTackleLost(tackleLost);
		stats.setTackleWon(tackleWon);
		stats.setPossibleTigersGoals(possibleTigersGoals);
		stats.setPossibleOpponentsGoals(possibleOpponentsGoals);
		stats.setPossibleBotGoals(possibleBotGoals);
		
		// Update tacticalfield
		newTacticalField.setStatistics(stats);
	}
	
	
	private void calcPossibleGoals(final EPossibleGoal ePossibleGoal, final BotID botLastTouched)
	{
		switch (ePossibleGoal)
		{
			case WE:
				if (ePossibleGoal == lastPossibleGoalVal)
				{
					break;
				}
				possibleTigersGoals++;
				if (!possibleBotGoals.containsKey(botLastTouched))
				{
					possibleBotGoals.put(botLastTouched, new Percentage());
				}
				possibleBotGoals.get(botLastTouched).inc();
				break;
			case THEY:
				if (ePossibleGoal == lastPossibleGoalVal)
				{
					break;
				}
				possibleOpponentsGoals++;
				if (!possibleBotGoals.containsKey(botLastTouched))
				{
					possibleBotGoals.put(botLastTouched, new Percentage());
				}
				possibleBotGoals.get(botLastTouched).inc();
				break;
			case NO_ONE:
				break;
		}
		lastPossibleGoalVal = ePossibleGoal;
	}
	
	
	/**
	 * Sets in tacticalField a statistics object with the last values. So the statistics object contains non-empty
	 * values.
	 */
	@Override
	public void fallbackCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		Statistics stats = new Statistics();
		stats.setBallPossessionGeneral(ballPossessionGeneral);
		stats.setBallPossessionOpponents(ballPossessionOpponents);
		stats.setBallPossessionTigers(ballPossessionTigers);
		stats.setTackleGeneral(tackleGeneralWon, tackleGeneralLost);
		stats.setTackleLost(tackleLost);
		stats.setTackleWon(tackleWon);
		stats.setPossibleTigersGoals(possibleTigersGoals);
		stats.setPossibleOpponentsGoals(possibleOpponentsGoals);
		stats.setPossibleBotGoals(possibleBotGoals);
		
		newTacticalField.setStatistics(stats);
	}
}
