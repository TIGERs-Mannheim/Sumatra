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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.AIInfoFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ESide;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.fieldraster.FieldRasterGenerator.EGeneratorTyp;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.IAIProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.MetisCalculators;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ACalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.ECalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefenseHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.defense.DefensePointsCalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.fieldanalysis.FieldAnalyser;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.ApproximateScoringChance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BallKickLearningCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BallPossessionCalculator;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotLastTouchedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotNotAllowedToTouchBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.BotToBallDistance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.DangerousOpponents;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.PossibleGoal;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.ScoringChance;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.general.TeamClosestToBall;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.mixedteam.MixedTeamTouchCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsCarrier;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.OffensePointsReceiver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.offense.ShooterMemoryCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.playpattern.PlayPatternDetect;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.metis.calculators.standard.ForceStartAfterKickoffCalc;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.IMetisControlHandler;


/**
 * 
 * This class does situation/field analysis. Metis coordinates all calculators to analyze the
 * {@link edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrame}.
 * She will eventually put all the gathered conclusions in the {@link AIInfoFrame}.
 * 
 * @author Oliver Steinbrecher <OST1988@aol.com>
 */
public class Metis implements IAIProcessor, IMetisControlHandler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log							= Logger.getLogger(Metis.class.getName());
	
	private final Set<BotID>							manualControlledTigers	= new HashSet<BotID>();
	
	private final Map<ECalculator, ACalculator>	calculators					= new LinkedHashMap<ECalculator, ACalculator>();
	
	private final List<ECalculator>					activeCalculators			= new ArrayList<ECalculator>(
																										ECalculator.values().length);
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 */
	public Metis()
	{
		calculators.put(ECalculator.BOT_TO_BALL_DISTANCE_TIGERS, new BotToBallDistance(ETeam.TIGERS));
		calculators.put(ECalculator.BOT_TO_BALL_DISTANCE_OPPONENTS, new BotToBallDistance(ETeam.OPPONENTS));
		calculators.put(ECalculator.DEFENSE_HELPER, new DefenseHelper());
		calculators.put(ECalculator.DEFENSE_POINTS, new DefensePointsCalculator());
		calculators.put(ECalculator.BALL_POSSESSION, new BallPossessionCalculator());
		calculators.put(ECalculator.POSSIBLE_GOAL, new PossibleGoal());
		calculators.put(ECalculator.TEAM_CLOSEST_TO_BALL, new TeamClosestToBall());
		calculators.put(ECalculator.SCORING_CHANCE_TIGERS, new ScoringChance(ETeam.TIGERS));
		calculators.put(ECalculator.SCORING_CHANCE_OPPONENTS, new ScoringChance(ETeam.OPPONENTS));
		calculators.put(ECalculator.APPROXIMATE_SCORING_CHANCE_TIGERS, new ApproximateScoringChance(ETeam.TIGERS));
		calculators.put(ECalculator.APPROXIMATE_SCORING_CHANCE_OPOONENTS, new ApproximateScoringChance(ETeam.OPPONENTS));
		calculators.put(ECalculator.OFFENSE_POINTS_CARRIER, new OffensePointsCarrier());
		calculators.put(ECalculator.OFFENSE_POINTS_RECEIVER_LEFT, new OffensePointsReceiver(ESide.LEFT));
		calculators.put(ECalculator.OFFENSE_POINTS_RECEIVER_RIGHT, new OffensePointsReceiver(ESide.RIGHT));
		calculators.put(ECalculator.BOT_LAST_TOUCHED_BALL, new BotLastTouchedBall());
		calculators.put(ECalculator.BOT_NOT_ALLOWED_TO_TOUCH_BALL, new BotNotAllowedToTouchBall());
		calculators.put(ECalculator.DANGEROUS_OPPONENTS, new DangerousOpponents());
		calculators.put(ECalculator.FIELD_ANALYSER, new FieldAnalyser(EGeneratorTyp.MAIN));
		calculators.put(ECalculator.PLAY_PATTERN_DETECT, new PlayPatternDetect());
		calculators.put(ECalculator.OTHER_MIXED_TEAM_TOUCH, new MixedTeamTouchCalc());
		calculators.put(ECalculator.SHOOTER_MEMORY, new ShooterMemoryCalc());
		calculators.put(ECalculator.BALL_KICK_LEARNING, new BallKickLearningCalc());
		calculators.put(ECalculator.FORCE_AFTER_KICKOFF, new ForceStartAfterKickoffCalc());
		
		for (Map.Entry<ECalculator, ACalculator> entry : calculators.entrySet())
		{
			if (!entry.getKey().isInitiallyActive())
			{
				entry.getValue().setActive(false);
			}
		}
	}
	
	
	@Override
	public void process(AIInfoFrame curFrame, AIInfoFrame preFrame)
	{
		// check whether there is a worldFrame
		if (curFrame.worldFrame != null)
		{
			setBotControl(curFrame);
			curFrame.tacticalInfo.setActiveCalculators(activeCalculators);
			for (ACalculator calc : calculators.values())
			{
				calc.calculate(curFrame, preFrame);
			}
			
		} else
		{
			log.warn("Metis received null worldframe");
			curFrame.tacticalInfo.setActiveCalculators(new ArrayList<ECalculator>());
			for (ACalculator calc : calculators.values())
			{
				calc.fallbackCalc(curFrame, preFrame);
			}
		}
	}
	
	
	/**
	 * set bots to manual controlled according to our knowledge in manualControlledTigers
	 * @param curFrame
	 */
	private void setBotControl(AIInfoFrame curFrame)
	{
		List<BotID> toBeRemoved = new LinkedList<BotID>();
		for (final Map.Entry<BotID, TrackedTigerBot> tiger : curFrame.worldFrame.tigerBotsAvailable)
		{
			if (manualControlledTigers.contains(tiger.getKey()))
			{
				tiger.getValue().setManualControl(true);
				toBeRemoved.add(tiger.getKey());
			} else
			{
				tiger.getValue().setManualControl(false);
			}
		}
		for (BotID botID : toBeRemoved)
		{
			curFrame.worldFrame.tigerBotsAvailable.remove(botID);
		}
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	
	@Override
	public void setConfiguration(MetisCalculators config)
	{
		ACalculator calc = calculators.get(ECalculator.PLAY_PATTERN_DETECT);
		if (calc instanceof PlayPatternDetect)
		{
			((PlayPatternDetect) calc).setConfiguration(config);
		}
	}
	
	
	@Override
	public void loadAnalyzingResults()
	{
		ACalculator calc = calculators.get(ECalculator.PLAY_PATTERN_DETECT);
		if (calc instanceof PlayPatternDetect)
		{
			((PlayPatternDetect) calc).loadAnalyzingResults();
		}
	}
	
	
	@Override
	public void persistAnalyzingResults()
	{
		ACalculator calc = calculators.get(ECalculator.PLAY_PATTERN_DETECT);
		if (calc instanceof PlayPatternDetect)
		{
			((PlayPatternDetect) calc).persistAnalyzingResults();
		}
	}
	
	
	/**
	 * @param bot
	 */
	public void onManualBotAdded(BotID bot)
	{
		manualControlledTigers.add(bot);
	}
	
	
	/**
	 * @param bot
	 */
	public void onManualBotRemoved(BotID bot)
	{
		manualControlledTigers.remove(bot);
	}
	
	
	/**
	 * sets the active state of a calculator
	 * @param calc
	 * @param active
	 */
	public void setCalculatorActive(ECalculator calc, boolean active)
	{
		ACalculator calculator = calculators.get(calc);
		if (calculator != null)
		{
			calculator.setActive(active);
			if (!active && activeCalculators.contains(calc))
			{
				activeCalculators.remove(calc);
			} else if (active && !activeCalculators.contains(calc))
			{
				activeCalculators.add(calc);
			}
		}
	}
}
