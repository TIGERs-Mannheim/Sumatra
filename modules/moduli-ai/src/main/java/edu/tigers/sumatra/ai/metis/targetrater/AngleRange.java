/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import edu.tigers.sumatra.math.AngleMath;


/**
 * A angle range is defined by a left and right angle. The right angle must be the smaller one.
 */
public class AngleRange
{
	private final double rightAngle;
	private final double leftAngle;
	
	
	/**
	 * @param rightAngle smaller angle
	 * @param leftAngle larger angle
	 */
	public AngleRange(final double rightAngle, final double leftAngle)
	{
		assert rightAngle <= leftAngle;
		this.leftAngle = leftAngle;
		this.rightAngle = rightAngle;
	}
	
	
	public double getAngleWidth()
	{
		return Math.abs(AngleMath.difference(leftAngle, rightAngle));
	}
	
	
	public double getCenterAngle()
	{
		return rightAngle + getAngleWidth() / 2;
	}
	
	
	public List<AngleRange> cutOutRange(AngleRange cut)
	{
		List<AngleRange> ranges = new ArrayList<>(2);
		if (cut.leftAngle <= rightAngle // range is right of us
				|| cut.rightAngle > leftAngle) // range is left of us
		{
			ranges.add(this);
			return ranges;
		}
		if (cut.rightAngle > rightAngle)
		{
			// remainings on the right side
			ranges.add(new AngleRange(rightAngle, cut.rightAngle));
		}
		if (cut.leftAngle <= leftAngle)
		{
			// remainings on the left side
			ranges.add(new AngleRange(cut.leftAngle, leftAngle));
		}
		return ranges;
	}
	
	
	public double getLeftAngle()
	{
		return leftAngle;
	}
	
	
	public double getRightAngle()
	{
		return rightAngle;
	}
	
	
	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
				.append("rightAngle", rightAngle)
				.append("leftAngle", leftAngle)
				.toString();
	}
}
