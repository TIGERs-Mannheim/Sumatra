/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.ai.metis.offense;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.g3force.configurable.Configurable;

import edu.tigers.sumatra.ai.data.KickoffStrategy;
import edu.tigers.sumatra.ai.data.TacticalField;
import edu.tigers.sumatra.ai.data.frames.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.ACalculator;
import edu.tigers.sumatra.ai.metis.support.IPassTarget;
import edu.tigers.sumatra.ai.pandora.roles.ARole;
import edu.tigers.sumatra.ai.pandora.roles.ERole;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.VectorMath;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.referee.data.GameState;
import edu.tigers.sumatra.trajectory.BangBangTrajectory2D;
import edu.tigers.sumatra.wp.ball.prediction.IStraightBallConsultant;


/**
 * @author Phillipp Mevenkamp <phillippmevenkamp@gmail.com>
 */
public class KickoffActionsCalc extends ACalculator
{
	private IVector2 bestPosition = Vector2.fromXY(0, 0);
	
	private Map<BotID, IPassTarget> bestMovementPositionsForBots = new HashMap<>();
	
	private double startPassVelocity = 1.0;
	
	@Configurable(comment = "The upper bound for time puffer to shoot the ball to the selected bot", defValue = "1.0")
	private static double timePuffer = 1.0;
	
	private static final double TIME_PUFFER_DIFF = 0.15;
	
	
	@Override
	public boolean isCalculationNecessary(final TacticalField tacticalField, final BaseAiFrame aiFrame)
	{
		return tacticalField.getGameState().isKickoffOrPrepareKickoffForUs();
	}
	
	
	@Override
	public void doCalc(final TacticalField newTacticalField, final BaseAiFrame baseAiFrame)
	{
		final GameState gameState = newTacticalField.getGameState();
		final boolean allowedToCalculate = gameState != null && gameState.isPrepareKickoffForUs();
		
		if (allowedToCalculate)
		{
			calculateBestShotTargetfromPassTargets(baseAiFrame, newTacticalField.getPassTargetsRanked());
			
			calculateBestMovingPositionFromPassTargets(baseAiFrame, newTacticalField);
		}
		
		KickoffStrategy strategy = newTacticalField.getKickoffStrategy();
		strategy.setBestShotTarget(bestPosition);
		strategy.setBestMovementPositions(bestMovementPositionsForBots);
		strategy.setPassVelocity(startPassVelocity);
	}
	
	
	private void calculateBestShotTargetfromPassTargets(final BaseAiFrame baseAiFrame,
			List<IPassTarget> rankedPassTargets)
	{
		List<ARole> activeKickoffShooters = baseAiFrame.getPrevFrame().getPlayStrategy()
				.getActiveRoles(ERole.KICKOFF_SHOOTER);
		
		if (!activeKickoffShooters.isEmpty())
		{
			BotID currentKickoffShooter = activeKickoffShooters.get(0).getBotID();
			
			final List<IPassTarget> rankedPassTargetsWithoutShooter = rankedPassTargets.stream()
					.filter(target -> target.getBotId() != activeKickoffShooters.get(0).getBotID())
					.collect(Collectors.toList());
			
			if (!rankedPassTargetsWithoutShooter.isEmpty())
			{
				bestPosition = rankedPassTargetsWithoutShooter.get(0).getKickerPos();
			}
			
			calculateEndPassVelocity(bestPosition, currentKickoffShooter, baseAiFrame);
		} else
		{
			bestPosition = Geometry.getGoalTheir().getCenter();
			startPassVelocity = 8.0;
		}
	}
	
	
	private void calculateEndPassVelocity(IVector2 passTarget, BotID passTargetBot, BaseAiFrame aiFrame)
	{
		BangBangTrajectory2D trajectoryToTarget = TrajectoryGenerator.generatePositionTrajectory(
				aiFrame.getWorldFrame().getTiger(passTargetBot),
				passTarget);
		
		final double timeForBallToGetToPosition = trajectoryToTarget.getTotalTime();
		
		final double distanceForBallToTargetPosition = VectorMath.distancePP(passTarget,
				Geometry.getCenter());
		
		IStraightBallConsultant ballConsultant = getBall().getStraightConsultant();
		
		// The maximum velocity
		double kickVelocity = 2.7;
		final int maximumSearchSteps = 15;
		for (int i = 0; i < maximumSearchSteps; i++)
		{
			final double timeForArrival = ballConsultant.getTimeForKick(distanceForBallToTargetPosition, kickVelocity);
			
			final double timeDifference = timeForArrival - timeForBallToGetToPosition;
			
			if (timeDifference > timePuffer - TIME_PUFFER_DIFF && timeDifference < timePuffer + TIME_PUFFER_DIFF)
			{
				break;
			}
			
			if (timeForArrival < timeForBallToGetToPosition)
			{
				kickVelocity -= 0.1;
			} else
			{
				kickVelocity += 0.1;
			}
		}
		startPassVelocity = kickVelocity;
	}
	
	
	private void calculateBestMovingPositionFromPassTargets(final BaseAiFrame baseAiFrame,
			final TacticalField newTacticalField)
	{
		bestMovementPositionsForBots.clear();
		
		final List<IPassTarget> passTargetsRanked = newTacticalField.getPassTargetsRanked();
		
		for (BotID bot : baseAiFrame.getWorldFrame().getBots().keySet())
		{
			passTargetsRanked.stream()
					.filter(pT -> pT.getBotId() == bot)
					.findFirst()
					.ifPresent(pT -> bestMovementPositionsForBots.put(bot, pT));
		}
	}
	
	
}
