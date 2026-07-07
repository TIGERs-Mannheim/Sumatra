package edu.tigers.sumatra.trajectory;

import edu.tigers.sumatra.math.vector.IVector2;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.List;


@ToString
@Getter
@RequiredArgsConstructor
public class TimedTrajectory2D implements ITrajectory<IVector2>
{
	private final ITrajectory<IVector2> trajectory;
	private final IVector2 targetDest;
	private final double targetTime;
	private final double timeAtTargetX;
	private final double timeAtTargetY;


	@Override
	public IVector2 getNextDestination(double t)
	{
		return trajectory.getNextDestination(t);
	}


	@Override
	public IVector2 getFinalDestination()
	{
		return trajectory.getFinalDestination();
	}


	@Override
	public PosVelAcc<IVector2> getValuesAtTime(double tt)
	{
		return trajectory.getValuesAtTime(tt);
	}


	@Override
	public List<Double> getTimeSections()
	{
		return trajectory.getTimeSections();
	}


	@Override
	public double getTotalTimeToPrimaryDirection()
	{
		return trajectory.getTotalTimeToPrimaryDirection();
	}


	@Override
	public double getMaxSpeed()
	{
		return trajectory.getMaxSpeed();
	}


	@Override
	public IVector2 getPositionMM(double t)
	{
		return trajectory.getPositionMM(t);
	}


	@Override
	public IVector2 getPosition(double t)
	{
		return trajectory.getPosition(t);
	}


	@Override
	public IVector2 getVelocity(double t)
	{
		return trajectory.getVelocity(t);
	}


	@Override
	public IVector2 getAcceleration(double t)
	{
		return trajectory.getAcceleration(t);
	}


	@Override
	public double getTotalTime()
	{
		return trajectory.getTotalTime();
	}


	public IVector2 getTargetDestMM()
	{
		return targetDest.multiplyNew(1e3);
	}


	@Override
	public ITrajectory<IVector2> mirrored()
	{
		return new TimedTrajectory2D(
				trajectory.mirrored(),
				targetDest.multiplyNew(-1),
				targetTime,
				timeAtTargetX,
				timeAtTargetY
		);
	}
}
