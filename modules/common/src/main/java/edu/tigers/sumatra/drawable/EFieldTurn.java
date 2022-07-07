/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.drawable;

import edu.tigers.sumatra.math.AngleMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 *
 */
@Getter
@RequiredArgsConstructor
public enum EFieldTurn
{
	NORMAL(0),
	T90(AngleMath.PI_HALF),
	T180(AngleMath.PI),
	T270(AngleMath.PI + AngleMath.PI_HALF);

	private final double angle;


	/**
	 * Returns the angular opposite of the current value
	 *
	 * @return
	 */
	public EFieldTurn getOpposite()
	{
		return switch (this)
				{
					case NORMAL -> T180;
					case T180 -> NORMAL;
					case T270 -> T90;
					case T90 -> T270;
				};
	}


	public EFieldTurn turnCounterClockwise()
	{
		return switch (this)
				{
					case NORMAL -> T90;
					case T90 -> T180;
					case T180 -> T270;
					case T270 -> NORMAL;
				};
	}


	public EFieldTurn turnClockwise()
	{
		return switch (this)
				{
					case NORMAL -> T270;
					case T270 -> T180;
					case T180 -> T90;
					case T90 -> NORMAL;
				};
	}


	public static EFieldTurn bestFor(int width, int height)
	{
		if (width > height)
		{
			return NORMAL;
		}
		return T90;
	}
}
