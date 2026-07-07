package edu.tigers.sumatra.math.vector;


/**
 * This is a point with a specific/arbitrary value.
 * This class is rather dangerous to use, because it extends from Vector2.
 * The behavior of equals is problematic. It can not be implemented such that the value is used and the contract of
 * equals is still given (bidirectional)...
 * Please consider creating a new class, if you need a similar data structure!
 */
public class ValuePoint extends Vector2
{
	private double value = 0;


	@SuppressWarnings("unused")
	protected ValuePoint()
	{
	}


	public ValuePoint(final IVector2 vec, final double value)
	{
		super(vec);
		this.setValue(value);
	}


	/**
	 * @return the value
	 */
	public double getValue()
	{
		return value;
	}


	/**
	 * @param value
	 */
	public void setValue(final double value)
	{
		this.value = value;
	}


	@Override
	public String toString()
	{
		return "(x=" + x() + ",y=" + y() + ",val=" + getValue() + ")";
	}
}