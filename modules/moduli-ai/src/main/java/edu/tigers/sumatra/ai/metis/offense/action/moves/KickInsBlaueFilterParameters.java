/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.offense.action.moves;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.IConfigClient;
import com.github.g3force.configurable.IConfigObserver;
import edu.tigers.sumatra.ai.BaseAiFrame;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.geometry.IPenaltyArea;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.ball.prediction.IBallTrajectory;
import edu.tigers.sumatra.wp.ball.trajectory.BallFactory;


public class KickInsBlaueFilterParameters implements IConfigObserver
{
	private IPenaltyArea penaltyAreaOursWithMargin;
	private IPenaltyArea penaltyAreaTheirsWithMargin;
	
	private double minDistanceToOutSqr;
	private double minDistanceToOutSqrNEAR;
	
	private IVector2 ballPosition;
	private double botAngleToBall;
	
	private double maxAllowedTime;
	private double maxAllowedTurnAngle;
	private double maxAllowedTurnAngleBACKSPIN;
	
	
	public KickInsBlaueFilterParameters()
	{
		createPenaltyAreas(KickInsBlaueActionMove.marginAroundPenArea);
		calculateMinDistanceToOut(OffensiveConstants.getBallSpeedAtTargetKickInsBlaue());
		
		ballPosition = Vector2.zero();
		botAngleToBall = 0;
		
		maxAllowedTime = 0;
		maxAllowedTurnAngle = 0;
		maxAllowedTurnAngleBACKSPIN = 0;
		
		
		ConfigRegistration.registerConfigurableCallback("metis", this);
	}
	
	
	@Override
	public void afterApply(final IConfigClient configClient)
	{
		// Min distance from possible grid point to outline, dependent of ballSpeedAtTarget Configurable
		calculateMinDistanceToOut(OffensiveConstants.getBallSpeedAtTargetKickInsBlaue());
	}
	
	
	public void defaultCalc(final BotID id, final BaseAiFrame baseAiFrame)
	{
		// Ball position
		ballPosition = baseAiFrame.getWorldFrame().getBall().getPos();
		
		// Angle of the vector from bot to ball
		botAngleToBall = Vector2.fromPoints(baseAiFrame.getWorldFrame().getBot(id).getPos(), ballPosition).getAngle();
		
		// Max allowed Time for the Move before enemy arrives
		maxAllowedTime = baseAiFrame.getWorldFrame().getFoeBots().values().stream()
				.mapToDouble(e -> TrajectoryGenerator.generatePositionTrajectory(e, ballPosition).getTotalTime()).min()
				.orElse(0.);
		
		// Max allowed angle difference between angle of bot to ball vector and angle of ball to PossibleGridPoint
		calculateMaxTurningAngle(maxAllowedTime);
		
		// PenaltyAreas where Grid Positions are not allowed in, dependent of marginAroundPenArea Configurable
		createPenaltyAreas(KickInsBlaueActionMove.marginAroundPenArea);
	}
	
	
	private void createPenaltyAreas(final double marginAroundPenArea)
	{
		penaltyAreaOursWithMargin = Geometry.getPenaltyAreaOur().withMargin(marginAroundPenArea);
		penaltyAreaTheirsWithMargin = Geometry.getPenaltyAreaTheir().withMargin(marginAroundPenArea);
	}
	
	
	private void calculateMinDistanceToOut(final double ballSpeedAtTarget)
	{
		IBallTrajectory ballTrajectory = BallFactory.createTrajectoryFromStraightKick(Vector2.zero(),
				Vector2.fromX(ballSpeedAtTarget * 1000));
		minDistanceToOutSqr = ballTrajectory.getDistByTime(ballTrajectory.getTimeByVel(0));
		minDistanceToOutSqr *= minDistanceToOutSqr;
		
		IBallTrajectory ballTrajectoryNEAR = BallFactory.createTrajectoryFromStraightKick(Vector2.zero(),
				Vector2.fromX(ballSpeedAtTarget * 500));
		minDistanceToOutSqrNEAR = ballTrajectoryNEAR.getDistByTime(ballTrajectoryNEAR.getTimeByVel(0));
		minDistanceToOutSqrNEAR *= minDistanceToOutSqrNEAR;
	}
	
	
	private void calculateMaxTurningAngle(final double maxAllowedTime)
	{
		final double iAmSomeMinTimeAndThisNeedsSomeExtraWork = 3;
		if (maxAllowedTime <= iAmSomeMinTimeAndThisNeedsSomeExtraWork)
		{
			maxAllowedTurnAngle = Math.max(AngleMath.PI / 16, (maxAllowedTime / iAmSomeMinTimeAndThisNeedsSomeExtraWork)
					* AngleMath.PI);
		} else
		{
			maxAllowedTurnAngle = AngleMath.PI;
		}
		if (maxAllowedTime <= iAmSomeMinTimeAndThisNeedsSomeExtraWork + 1)
		{
			maxAllowedTurnAngleBACKSPIN = maxAllowedTurnAngle - AngleMath.PI_QUART;
		} else
		{
			maxAllowedTurnAngleBACKSPIN = maxAllowedTurnAngle;
		}
	}
	
	
	public IPenaltyArea getPenaltyAreaOursWithMargin()
	{
		return penaltyAreaOursWithMargin;
	}
	
	
	public IPenaltyArea getPenaltyAreaTheirsWithMargin()
	{
		return penaltyAreaTheirsWithMargin;
	}
	
	
	public double getMinDistanceToOutSqr()
	{
		return minDistanceToOutSqr;
	}
	
	
	public double getMinDistanceToOutSqrNEAR()
	{
		return minDistanceToOutSqrNEAR;
	}
	
	
	public IVector2 getBallPosition()
	{
		return ballPosition;
	}
	
	
	public double getBotAngleToBall()
	{
		return botAngleToBall;
	}
	
	
	public double getMaxAllowedTime()
	{
		return maxAllowedTime;
	}
	
	
	public double getMaxAllowedTurnAngle()
	{
		return maxAllowedTurnAngle;
	}
	
	
	public double getMaxAllowedTurnAngleBACKSPIN()
	{
		return maxAllowedTurnAngleBACKSPIN;
	}
}
