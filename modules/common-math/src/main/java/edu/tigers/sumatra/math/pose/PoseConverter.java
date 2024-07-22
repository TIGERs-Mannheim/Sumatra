/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.math.pose;

import com.github.g3force.s2vconverter.IString2ValueConverter;
import edu.tigers.sumatra.math.vector.Vector3;
import lombok.extern.log4j.Log4j2;


@Log4j2
public class PoseConverter implements IString2ValueConverter
{
	@Override
	public boolean supportedClass(final Class<?> impl)
	{
		return impl.equals(Pose.class);
	}


	@Override
	public Object parseString(final Class<?> impl, final String value)
	{
		if (impl.equals(Pose.class))
		{
			return Pose.valueOf(value);
		}
		return null;
	}


	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		Pose pose = (Pose) value;
		return Vector3.from2d(pose.getPos(), pose.getOrientation()).getSaveableString();
	}

}
