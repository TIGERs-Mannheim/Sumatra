/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.targetrater;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;


/**
 * An angle range is defined by a left and right angle. The right angle must be the smaller one.
 * Note: The angle range must be relative to some common direction. Absolute angles will wrap from positive to negative!
 */
@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class AngleRange
{
	double offset;
	double width;


	public static AngleRange fromAngles(double angle1, double angle2)
	{
		double width = angle2 - angle1;
		double offset = angle1 + width / 2;
		return new AngleRange(offset, Math.abs(width));
	}


	public static AngleRange width(double width)
	{
		Validate.isTrue(width >= 0, "Width must be >0: ", width);
		return new AngleRange(0.0, width);
	}


	public double getLeft()
	{
		// no normalization here to allow comparisons!
		return offset + width / 2;
	}


	public double getRight()
	{
		// no normalization here to allow comparisons!
		return offset - width / 2;
	}


	public List<AngleRange> cutOutRange(AngleRange cut)
	{
		List<AngleRange> ranges = new ArrayList<>(2);
		if (cut.getLeft() <= getRight() // range is right of us
				|| cut.getRight() > getLeft()) // range is left of us
		{
			ranges.add(this);
			return ranges;
		}
		if (cut.getRight() > getRight())
		{
			// leftover on the right side
			ranges.add(AngleRange.fromAngles(getRight(), cut.getRight()));
		}
		if (cut.getLeft() <= getLeft())
		{
			// leftover on the left side
			ranges.add(AngleRange.fromAngles(cut.getLeft(), getLeft()));
		}
		return ranges;
	}
}
