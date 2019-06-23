/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;


import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.isFinite;
import static java.lang.Math.signum;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class ChargingValue
{
	private double defaultValue;
	private final double chargeRate;
	private double limit;
	
	private double value;
	private long tLastIncrease = 0;
	
	
	private ChargingValue(final Builder builder)
	{
		defaultValue = builder.defaultValue;
		chargeRate = builder.chargeRate;
		limit = builder.limit;
		reset();
	}
	
	
	/**
	 * @return a new builder
	 */
	public static Builder aChargingValue()
	{
		return new Builder();
	}
	
	
	/**
	 * @return the current value
	 */
	public double getValue()
	{
		return value;
	}
	
	
	/**
	 * @param value the current value
	 */
	public void setValue(final double value)
	{
		this.value = value;
	}
	
	
	public void setDefaultValue(final double defaultValue)
	{
		this.defaultValue = defaultValue;
	}
	
	
	public void setLimit(final double limit)
	{
		this.limit = limit;
		if (!isInLimit(value))
		{
			value = limit;
		}
	}
	
	
	/**
	 * Update state.<br>
	 * Note: this is numerically not quite nice, but we do not need high precision anyway.
	 * 
	 * @param timestamp the current timestamp (e.g. worldFrame timestamp)
	 */
	public void update(long timestamp)
	{
		if (tLastIncrease != 0)
		{
			long dt = timestamp - tLastIncrease;
			double inc = chargeRate * dt / 1e9;
			double newValue = value + inc;
			if (!isFinite(limit) || isInLimit(newValue))
			{
				value = newValue;
			}
			if (!isInLimit(newValue))
			{
				value = limit;
			}
		}
		tLastIncrease = timestamp;
	}
	
	
	private boolean isInLimit(double newValue)
	{
		return (signum(chargeRate) < 0 && newValue >= limit)
				|| (signum(chargeRate) > 0 && newValue <= limit);
	}
	
	
	/**
	 * Reset to default value
	 */
	public void reset()
	{
		value = defaultValue;
		tLastIncrease = 0;
	}
	
	/**
	 * {@code ChargingValue} builder static inner class.
	 */
	public static final class Builder
	{
		private double defaultValue = 0;
		private double chargeRate = 1;
		private double limit = POSITIVE_INFINITY;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code chargeRate} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param chargeRate the {@code chargeRate} to set in [1/s]
		 * @return a reference to this Builder
		 */
		public Builder withChargeRate(final double chargeRate)
		{
			this.chargeRate = chargeRate;
			return this;
		}
		
		
		/**
		 * Sets the {@code limit} and returns a reference to this Builder so that the methods can be chained together.
		 *
		 * @param limit the {@code limit} to set
		 * @return a reference to this Builder
		 */
		public Builder withLimit(final double limit)
		{
			this.limit = limit;
			return this;
		}
		
		
		/**
		 * Sets the {@code defaultValue} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param defaultValue the {@code defaultValue} to set
		 * @return a reference to this Builder
		 */
		public Builder withDefaultValue(final double defaultValue)
		{
			this.defaultValue = defaultValue;
			return this;
		}
		
		
		/**
		 * Returns a {@code ChargingValue} built from the parameters previously set.
		 *
		 * @return a {@code ChargingValue} built with parameters of this {@code ChargingValue.Builder}
		 */
		public ChargingValue build()
		{
			return new ChargingValue(this);
		}
	}
}
