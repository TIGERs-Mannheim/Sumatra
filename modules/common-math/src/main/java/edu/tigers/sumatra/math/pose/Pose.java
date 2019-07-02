/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.pose;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.math.AngleMath;
import edu.tigers.sumatra.math.IMirrorable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.Vector2;
import edu.tigers.sumatra.math.vector.Vector3f;


/**
 * A pose consists of a position and an orientation.
 */
@Persistent
public class Pose implements IMirrorable<Pose>
{
	/** [x,y] in [mm,mm] */
	private final IVector2 pos;
	/** [rad] **/
	private final double orientation;
	
	
	@SuppressWarnings("unused") // berkeley
	private Pose()
	{
		pos = Vector2.zero();
		orientation = 0;
	}
	
	
	/**
	 * @param pos [mm,mm]
	 * @param orientation [rad]
	 */
	private Pose(final IVector2 pos, final double orientation)
	{
		this.pos = pos;
		this.orientation = orientation;
	}
	
	
	/**
	 * @param pos position of object [mm,mm]
	 * @param orientation of object [rad]
	 * @return new pose
	 */
	public static Pose from(final IVector2 pos, final double orientation)
	{
		return new Pose(pos, orientation);
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
	
	
	/**
	 * @return [mm,mm]
	 */
	public IVector2 getPos()
	{
		return pos;
	}
	
	
	/**
	 * @return [rad]
	 */
	public double getOrientation()
	{
		return orientation;
	}
	
	
	@Override
	public boolean equals(final Object o)
	{
		if (this == o)
			return true;
		
		if (o == null || getClass() != o.getClass())
			return false;
		
		final Pose pose = (Pose) o;
		
		return new EqualsBuilder()
				.append(orientation, pose.orientation)
				.append(pos, pose.pos)
				.isEquals();
	}
	
	
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(17, 37)
				.append(pos)
				.append(orientation)
				.toHashCode();
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("pos", pos)
				.append("orientation", orientation)
				.toString();
	}
}
