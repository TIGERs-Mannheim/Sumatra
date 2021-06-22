/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;


import edu.tigers.sumatra.math.SumatraMath;

import static java.lang.Double.POSITIVE_INFINITY;
import static java.lang.Double.isFinite;
import static java.lang.Math.signum;


/**
 * A charging value can be charged from a default value up to a limit with a constant charge rate.
 */
public class ChargingValue
{
	private final double defaultValue;
	private final double chargeRate;
	private double limit;

	private double value;
	private long tLastIncrease = 0;


	private ChargingValue(final Builder builder)
	{
		defaultValue = builder.defaultValue;
		chargeRate = builder.chargeRate;
		limit = builder.limit;
		value = builder.initValue;
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


	public void updateStall(long timestamp)
	{
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
	 * @return true, if the limit is reached.
	 */
	public boolean isFullyCharged()
	{
		return SumatraMath.isEqual(value, limit);
	}


	/**
	 * {@code ChargingValue} builder static inner class.
	 */
	public static final class Builder
	{
		private double defaultValue = 0;
		private double chargeRate = 1;
		private double limit = POSITIVE_INFINITY;
		private Double initValue;


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
		 * Sets the {@code initValue} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param initValue the {@code initValue} to set
		 * @return a reference to this Builder
		 */
		public Builder withInitValue(final double initValue)
		{
			this.initValue = initValue;
			return this;
		}


		/**
		 * Returns a {@code ChargingValue} built from the parameters previously set.
		 *
		 * @return a {@code ChargingValue} built with parameters of this {@code ChargingValue.Builder}
		 */
		public ChargingValue build()
		{
			if (initValue == null)
			{
				initValue = defaultValue;
			}
			return new ChargingValue(this);
		}
	}
}
