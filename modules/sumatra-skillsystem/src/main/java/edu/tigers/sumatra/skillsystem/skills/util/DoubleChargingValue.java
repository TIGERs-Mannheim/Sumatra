/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import edu.tigers.sumatra.math.Hysterese;


/**
 * @author n.ommer
 */
public class DoubleChargingValue
{
	private final Hysterese hysteresis;
	private final ChargingValue increasingValue;
	private final ChargingValue decreasingValue;
	
	
	/**
	 * @param hysteresis
	 * @param defaultValue
	 * @param increaseRate
	 * @param decreaseRate
	 * @param lowerLimit
	 * @param upperLimit
	 */
	public DoubleChargingValue(Hysterese hysteresis, double defaultValue, double increaseRate, double decreaseRate,
			double lowerLimit, double upperLimit)
	{
		this.hysteresis = hysteresis;
		increasingValue = ChargingValue.aChargingValue()
				.withDefaultValue(defaultValue)
				.withChargeRate(increaseRate)
				.withLimit(upperLimit)
				.build();
		decreasingValue = ChargingValue.aChargingValue()
				.withDefaultValue(defaultValue)
				.withChargeRate(decreaseRate)
				.withLimit(lowerLimit)
				.build();
	}
	
	
	/**
	 * @param value distance between dest and bot
	 * @param timestamp current timestamp
	 */
	public void update(final double value, final long timestamp)
	{
		hysteresis.update(value);
		if (hysteresis.isLower())
		{
			decreasingValue.update(timestamp);
			increasingValue.reset();
			increasingValue.setValue(decreasingValue.getValue());
		} else
		{
			increasingValue.update(timestamp);
			decreasingValue.reset();
			decreasingValue.setValue(increasingValue.getValue());
		}
	}
	
	
	/**
	 * @return the current value
	 */
	public double getValue()
	{
		return decreasingValue.getValue();
	}
}
