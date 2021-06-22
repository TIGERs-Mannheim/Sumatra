/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.AllArgsConstructor;
import lombok.Value;


/**
 * State of a kicked ball after fitting.
 */
@Persistent
@Value
@AllArgsConstructor
public class BallKickFitState implements IMirrorable<BallKickFitState>
{
	/**
	 * Kick pos [mm]
	 */
	IVector2 kickPos;

	/**
	 * Kick velocity [m/s]
	 */
	IVector3 kickVel;

	/**
	 * Kick timestamp [ns]
	 */
	long kickTimestamp;


	@SuppressWarnings("unused")
	private BallKickFitState()
	{
		kickPos = Vector2.zero();
		kickVel = Vector3.zero();
		kickTimestamp = 0;
	}


	@Override
	public BallKickFitState mirrored()
	{
		return new BallKickFitState(
				kickPos.multiplyNew(-1),
				Vector3.from2d(kickVel.getXYVector().multiplyNew(-1), kickVel.z()),
				kickTimestamp
		);
	}


	/**
	 * @return the absolute kick speed
	 */
	public double getAbsoluteKickSpeed()
	{
		return kickVel.getLength();
	}
}
