/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.defense;

import static edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession.THEY;
import static edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession.WE;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.TacticalField;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.ai.metis.ballresponsibility.EBallResponsibility;
import edu.tigers.sumatra.ai.metis.keeper.EKeeperState;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.math.Hysteresis;
import edu.tigers.sumatra.referee.data.GameState;


/**
 * This class makes some tactical analysis to create a table with the number of bots
 * assigned to defend our goal.
 * It uses the algorithm described by the
 * <a href="http://wiki.robocup.org/images/d/dc/Small_Size_League_-_RoboCup_2016_-_TDP_CMDragons.pdf">
 * CMDragons' 2016 TDP</a>.
 *
 * @author FelixB <bayer.fel@googlemail.com>
 */
public class NumDefenderCalc extends ACalculator
{
	@Configurable(comment = "Lower x value for a hysteresis to determine if the ball is in our half [mm]", defValue = "-250")
	private static double minXValueForBallLower = -250;
	@Configurable(comment = "Upper x value for a hysteresis to determine if the ball is in our half [mm]", defValue = "250")
	private static double minXValueForBallUpper = 250;
	@Configurable(comment = "Lower x value for a hysteresis to determine if the ball is in our half [mm]", defValue = "-250")
	private static double minXValueForFoeLower = -250;
	@Configurable(comment = "Upper x value for a hysteresis to determine if the ball is in our half [mm]", defValue = "250")
	private static double minXValueForFoeUpper = 250;
	@Configurable(comment = "Lookahead for ball position", defValue = "0.3")
	private static double ballLookahead = 0.3;
	@Configurable(comment = "Lookahead for opponent bot positions", defValue = "0.3")
	private static double opponentLookahead = 0.3;
	@Configurable(comment = "Amount of defenders in own standard situations", defValue = "0")
	private static int defendersInOwnStandard = 0;
	private Hysteresis ballXValueHysteresis;
	private Hysteresis foeXValueHysteresis;
	@Configurable(comment = "Maximal velocity of the ball in the penalty area if the keeper will chip, to free defenders", defValue = "0.1")
	private double maxBallVelocityOfBallInPenaltyAreaToFreeDefenders = 0.1;
	
	@Configurable(comment = "If the ball is safe in our penalty free defenders", defValue = "true")
	private boolean defenderForChippingKeeperActivated = true;
	
	
	/**
	 * Initializing the hysteresis.
	 */
	public NumDefenderCalc()
	{
		super();
		ballXValueHysteresis = new Hysteresis(minXValueForBallLower, minXValueForBallUpper);
		foeXValueHysteresis = new Hysteresis(minXValueForFoeLower, minXValueForFoeUpper);
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		// number of available TIGER bots minus the keeper and interchange bots
		int nKeeper = getWFrame().getTigerBotsAvailable().containsKey(baseAiFrame.getKeeperId()) ? 1 : 0;
		final int nAvailableBots = baseAiFrame.getWorldFrame().getTigerBotsVisible().size()
				- nKeeper
				- newTacticalField.getBotInterchange().getNumInterchangeBots();
		
		final EBallPossession ballPossession = newTacticalField.getBallPossession().getEBallPossession();
		final boolean opponentAggressive = isOpponentAggressive(baseAiFrame);
		final boolean ballInOurHalf = isBallInOurHalf();
		final boolean ballAtOurKeeper = isBallSafeAtOurKeeper(newTacticalField);
		
		final EBallResponsibility ballResponsibility = newTacticalField.getBallResponsibility();
		
		final int nDefender;
		GameState gameState = baseAiFrame.getGamestate();
		if (gameState.isStoppedGame() || gameState.isStandardSituationForThem())
		{
			nDefender = nDefenderStandardThem(nAvailableBots, ballResponsibility);
		} else if (gameState.isStandardSituationForUs())
		{
			nDefender = nDefenderStandardWe();
		} else
		{
			nDefender = nDefenderRunning(nAvailableBots, ballInOurHalf, opponentAggressive, ballPossession,
					ballResponsibility, ballAtOurKeeper);
		}
		
		newTacticalField.setNumDefender(nDefender);
	}
	
	
	private boolean isBallSafeAtOurKeeper(final TacticalField newTacticalField)
	{
		return Geometry.getPenaltyAreaOur().isPointInShape(getBall().getPos(), -Geometry.getBotRadius())
				&& getBall().getVel().getLength() < maxBallVelocityOfBallInPenaltyAreaToFreeDefenders
				&& newTacticalField.getKeeperState() == EKeeperState.CHIP_FAST;
	}
	
	
	/**
	 * Use all but one (Offense) bot
	 * 
	 * @param nAvailableBots
	 * @param ballResponsibility
	 * @return
	 */
	private int nDefenderStandardThem(final int nAvailableBots, final EBallResponsibility ballResponsibility)
	{
		return nDefenderMaxDefense(nAvailableBots, ballResponsibility);
		
	}
	
	
	/**
	 * We need supporters in own standard
	 *
	 * @return
	 */
	private int nDefenderStandardWe()
	{
		return max(0, defendersInOwnStandard);
	}
	
	
	private int nDefenderRunning(final int nAvailableBots, final boolean ballInOurHalf, final boolean opponentAggressive,
			final EBallPossession ballPossession, final EBallResponsibility ballResponsibility,
			final boolean ballAtOurKeeper)
	{
		if (defenderForChippingKeeperActivated && ballAtOurKeeper)
		{
			return max(nAvailableBots - 2, 2);
		} else if (ballInOurHalf && (ballPossession != WE))
		{
			return nDefenderMaxDefense(nAvailableBots, ballResponsibility);
		} else if ((!ballInOurHalf || !opponentAggressive) && (ballPossession != THEY))
		{
			return max(0, min(1, nAvailableBots - 1));
		} else
		{
			return max(0, min(2, nAvailableBots - 1));
		}
	}
	
	
	private int nDefenderMaxDefense(final int nAvailableBots, final EBallResponsibility ballResponsibility)
	{
		if (ballResponsibility == EBallResponsibility.DEFENSE)
		{
			return max(0, nAvailableBots);
		}
		
		return max(0, nAvailableBots - 1);
	}
	
	
	private boolean isBallInOurHalf()
	{
		final double ballXPos = getBall().getTrajectory().getPosByTime(ballLookahead).x();
		
		ballXValueHysteresis.setUpperThreshold(minXValueForBallUpper);
		ballXValueHysteresis.setLowerThreshold(minXValueForBallLower);
		ballXValueHysteresis.update(ballXPos);
		
		return ballXValueHysteresis.isLower();
	}
	
	
	private boolean isOpponentAggressive(final BaseAiFrame baseAiFrame)
	{
		final double minFoeXPos = baseAiFrame.getWorldFrame().getFoeBots().values().stream()
				.map(bot -> bot.getPosByTime(opponentLookahead).x())
				.min(Double::compare).orElse(minXValueForFoeUpper + 1);
		
		foeXValueHysteresis.setUpperThreshold(minXValueForFoeUpper);
		foeXValueHysteresis.setLowerThreshold(minXValueForFoeLower);
		foeXValueHysteresis.update(minFoeXPos);
		
		return foeXValueHysteresis.isLower();
	}
}
