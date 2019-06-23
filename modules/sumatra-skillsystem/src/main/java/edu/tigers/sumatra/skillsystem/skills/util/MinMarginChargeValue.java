/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.skillsystem.skills.util;

import static edu.tigers.sumatra.skillsystem.skills.util.ChargingValue.aChargingValue;

import edu.tigers.sumatra.math.Hysterese;


/**
 * A charging value with a hysteresis for a distance. Used for kick skills to get a charging minMargin when ready for
 * kick.
 *
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class MinMarginChargeValue
{
	private final Hysterese			dist2DestHysterese;
	private final ChargingValue	chargingValue;
	
	private double						minMargin;
	
	
	private MinMarginChargeValue(final Builder builder)
	{
		dist2DestHysterese = new Hysterese(builder.lowerThreshold, builder.upperThreshold);
		chargingValue = aChargingValue().withDefaultValue(builder.defaultValue)
				.withChargeRate(builder.chargeRate)
				.withLimit(builder.limit)
				.build();
		minMargin = builder.defaultValue;
	}
	
	
	/**
	 * @return a new factory
	 */
	public static Builder aMinMargin()
	{
		return new Builder();
	}
	
	
	/**
	 * @param dist distance between dest and bot
	 * @param timestamp current timestamp
	 */
	public void updateMinMargin(final double dist, final long timestamp)
	{
		dist2DestHysterese.update(dist);
		if (dist2DestHysterese.isLower())
		{
			chargingValue.update(timestamp);
		} else
		{
			chargingValue.reset();
		}
		minMargin = chargingValue.getValue();
	}
	
	
	/**
	 * Reset charging value
	 */
	public void reset()
	{
		chargingValue.reset();
	}
	
	
	public double getMinMargin()
	{
		return minMargin;
	}
	
	/**
	 * {@code MinMarginChargeValue} factory static inner class.
	 */
	public static final class Builder
	{
		private double	lowerThreshold	= 5;
		private double	upperThreshold	= 20;
		private double	chargeRate		= -50;
		private double	defaultValue	= -1;
		private double	limit				= Double.POSITIVE_INFINITY;
		
		
		private Builder()
		{
		}
		
		
		/**
		 * Sets the {@code lowerThreshold} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param lowerThreshold the {@code lowerThreshold} to set
		 * @return a reference to this Builder
		 */
		public Builder withLowerThreshold(final double lowerThreshold)
		{
			this.lowerThreshold = lowerThreshold;
			return this;
		}
		
		
		/**
		 * Sets the {@code upperThreshold} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param upperThreshold the {@code upperThreshold} to set
		 * @return a reference to this Builder
		 */
		public Builder withUpperThreshold(final double upperThreshold)
		{
			this.upperThreshold = upperThreshold;
			return this;
		}
		
		
		/**
		 * Sets the {@code chargeRate} and returns a reference to this Builder so that the methods can be chained
		 * together.
		 *
		 * @param chargeRate the {@code chargeRate} to set
		 * @return a reference to this Builder
		 */
		public Builder withChargeRate(final double chargeRate)
		{
			this.chargeRate = chargeRate;
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
		 * Returns a {@code MinMarginChargeValue} built from the parameters previously set.
		 *
		 * @return a {@code MinMarginChargeValue} built with parameters of this {@code MinMarginChargeValue.Builder}
		 */
		public MinMarginChargeValue build()
		{
			return new MinMarginChargeValue(this);
		}
	}
}
