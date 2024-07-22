/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.pose;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3;
import edu.tigers.sumatra.math.vector.Vector3f;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;


/**
 * A pose consists of a position and an orientation.
 */
@Persistent
@Data
@RequiredArgsConstructor(staticName = "from")
public class Pose implements IMirrorable<Pose>
{
	/**
	 * [x,y] in [mm,mm]
	 */
	@NonNull
	private final IVector2 pos;

	/**
	 * [rad]
	 **/
	private final double orientation;


	@SuppressWarnings("unused") // berkeley
	private Pose()
	{
		pos = Vector2.zero();
		orientation = 0;
	}


	/**
	 * @param pose pose in 3d vector [mm,mm,rad]
	 * @return new pose
	 */
	public static Pose from(final IVector3 pose)
	{
		return new Pose(pose.getXYVector(), pose.z());
	}


	public static Pose zero()
	{
		return Pose.from(Vector3f.zero());
	}


	public static Pose nan()
	{
		return Pose.from(Vector3f.nan());
	}


	public static Pose valueOf(String value)
	{
		IVector3 pose = Vector3.valueOf(value);
		if (pose == null)
		{
			return null;
		}
		return Pose.from(pose);
	}


	@Override
	public Pose mirrored()
	{
		return Pose.from(pos.multiplyNew(-1), AngleMath.mirror(orientation));
	}


	public Pose interpolate(Pose pose, double percentage)
	{
		double angleDiff = AngleMath.difference(pose.orientation, orientation) * percentage;
		double intpOrientation = AngleMath.normalizeAngle(orientation + angleDiff);
		IVector2 posDiff = pose.pos.subtractNew(pos).multiply(percentage);
		IVector2 intpPos = pos.addNew(posDiff);
		return Pose.from(intpPos, intpOrientation);
	}
}
