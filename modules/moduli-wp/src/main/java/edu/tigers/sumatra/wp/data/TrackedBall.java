/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.ball.trajectory.IBallTrajectory;
import edu.tigers.sumatra.ball.trajectory.IChipBallConsultant;
import edu.tigers.sumatra.ball.trajectory.IFlatBallConsultant;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.vision.data.FilteredVisionBall;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A tracked (filtered, predicted) ball.
 */
public class TrackedBall implements ITrackedBall
{
	private final long timestamp;
	private final BallState state;
	private final long lastVisibleTimestamp;

	// cache some fields on demand to increase performance
	private transient IVector2 pos;
	private transient IVector2 vel;
	private transient IBallTrajectory ballTrajectory;
	private transient IFlatBallConsultant straightBallConsultant;
	private transient IChipBallConsultant chipBallConsultant;


	/**
	 * Create an empty tracked ball.
	 */
	private TrackedBall()
	{
		state = new BallState();
		lastVisibleTimestamp = 0;
		timestamp = 0;
	}


	/**
	 * @param timestamp
	 * @param state
	 * @param lastVisibleTimestamp
	 */
	private TrackedBall(final long timestamp, final BallState state, final long lastVisibleTimestamp)
	{
		this.timestamp = timestamp;
		this.state = state;
		this.lastVisibleTimestamp = lastVisibleTimestamp;
	}


	/**
	 * @return an empty tracked ball
	 */
	public static TrackedBall createStub()
	{
		return new TrackedBall();
	}


	/**
	 * Create a tracked ball from a BallState.<br>
	 * The last visible timestamp is set to <code>timestamp</code> and therefore always visible.
	 *
	 * @param timestamp [ns]
	 * @param state     State in milli units.
	 * @return
	 */
	public static TrackedBall fromBallStateVisible(final long timestamp, final BallState state)
	{
		return new TrackedBall(timestamp, state, timestamp);
	}


	/**
	 * Create a tracked ball from a FilteredVisionBall.
	 *
	 * @param timestamp
	 * @param ball
	 * @return
	 */
	public static TrackedBall fromFilteredVisionBall(final long timestamp, final FilteredVisionBall ball)
	{
		return new TrackedBall(timestamp, ball.getBallState(), ball.getLastVisibleTimestamp());
	}


	@Override
	public long getTimestamp()
	{
		return timestamp;
	}


	@Override
	public IBallTrajectory getTrajectory()
	{
		if (ballTrajectory == null)
		{
			ballTrajectory = Geometry.getBallFactory().createTrajectoryFromState(state);
		}
		return ballTrajectory;
	}


	@Override
	public TrackedBall mirrored()
	{
		return new TrackedBall(timestamp, state.mirrored(), lastVisibleTimestamp);
	}


	@Override
	public AObjectID getId()
	{
		return BallID.instance();
	}


	@Override
	public double getRpm()
	{
		// v = r × RPM × 0.10472
		// RPM = v / (r x 0.10472)
		return getVel().getLength2() / ((Geometry.getBallRadius() / 1000.0) * 0.10472);
	}


	@Override
	public IVector2 getPos()
	{
		if (pos == null)
		{
			pos = getPos3().getXYVector();
		}
		return pos;
	}


	@Override
	public IVector2 getVel()
	{
		if (vel == null)
		{
			vel = getVel3().getXYVector();
		}
		return vel;
	}


	@Override
	public IVector2 getAcc()
	{
		return getAcc3().getXYVector();
	}


	@Override
	public IVector3 getPos3()
	{
		return state.getPos().getXYZVector();
	}


	@Override
	public IVector3 getVel3()
	{
		return state.getVel().multiplyNew(0.001).getXYZVector();
	}


	@Override
	public IVector3 getAcc3()
	{
		return state.getAcc().multiplyNew(0.001).getXYZVector();
	}


	@Override
	public double invisibleFor()
	{
		return (getTimestamp() - lastVisibleTimestamp) * 1e-9;
	}


	@Override
	public boolean isOnCam(final double seconds)
	{
		return invisibleFor() <= seconds;
	}


	@Override
	public double getHeight()
	{
		return getPos3().z();
	}


	@Override
	public IFlatBallConsultant getStraightConsultant()
	{
		if (straightBallConsultant == null)
		{
			straightBallConsultant = Geometry.getBallFactory().createFlatConsultant();
		}
		return straightBallConsultant;
	}


	@Override
	public IChipBallConsultant getChipConsultant()
	{
		if (chipBallConsultant == null)
		{
			chipBallConsultant = Geometry.getBallFactory().createChipConsultant();
		}
		return chipBallConsultant;
	}


	@Override
	public long getLastVisibleTimestamp()
	{
		return lastVisibleTimestamp;
	}


	@Override
	public boolean isChipped()
	{
		return state.isChipped();
	}


	@Override
	public BallState getState()
	{
		return state;
	}


	@Override
	public double getQuality()
	{
		final double invisibleTime = (timestamp - lastVisibleTimestamp) / 1e9;
		return 1 - Math.min(1, invisibleTime / 0.2);
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(timestamp);
		numbers.addAll(getPos3().getNumberList());
		numbers.addAll(getVel3().getNumberList());
		numbers.addAll(getAcc3().getNumberList());
		numbers.add(lastVisibleTimestamp);
		numbers.add(state.isChipped() ? 1 : 0);
		return numbers;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList("timestamp", "pos_x", "pos_y", "pos_z", "vel_x", "vel_y", "vel_z", "acc_x", "acc_y", "acc_z",
				"lastVisibleTimestamp", "chipped");
	}


	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pos", getPos3())
				.append("vel", getVel3())
				.toString();
	}
}
