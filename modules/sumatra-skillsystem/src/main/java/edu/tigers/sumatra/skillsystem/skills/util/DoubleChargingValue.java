package edu.tigers.sumatra.skillsystem.skills.util;


import org.apache.commons.lang3.Validate;


/**
 * A value that can be charged in both direction with independent speeds. It can also be stalled.
 */
public class DoubleChargingValue
{
	private final ChargingValue increasingValue;
	private final ChargingValue decreasingValue;

	private ChargeMode chargeMode = ChargeMode.STALL;

	public enum ChargeMode
	{
		INCREASE,
		DECREASE,
		STALL
	}


	/**
	 * @param defaultValue
	 * @param increaseRate
	 * @param decreaseRate
	 * @param lowerLimit
	 * @param upperLimit
	 */
	public DoubleChargingValue(
			double defaultValue, double increaseRate, double decreaseRate,
			double lowerLimit, double upperLimit
	)
	{
		Validate.isTrue(increaseRate > 0);
		Validate.isTrue(decreaseRate < 0);
		Validate.isTrue(lowerLimit <= upperLimit);
		Validate.isTrue(lowerLimit <= defaultValue && defaultValue <= upperLimit);

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
	 * @param timestamp current timestamp
	 */
	public void update(final long timestamp)
	{
		if (chargeMode == ChargeMode.DECREASE)
		{
			decreasingValue.update(timestamp);
			increasingValue.reset();

			// Assign values vice versa to ensure both limits are always enforced
			// (Necessary in rare cases, where timestamps might run back in time)
			increasingValue.setValue(decreasingValue.getValue());
			decreasingValue.setValue(increasingValue.getValue());
		} else if (chargeMode == ChargeMode.INCREASE)
		{
			increasingValue.update(timestamp);
			decreasingValue.reset();

			// Assign values vice versa to ensure both limits are always enforced
			// (Necessary in rare cases, where timestamps might run back in time)
			decreasingValue.setValue(increasingValue.getValue());
			increasingValue.setValue(decreasingValue.getValue());
		} else
		{
			decreasingValue.updateStall(timestamp);
			increasingValue.updateStall(timestamp);
		}

		Validate.isTrue(increasingValue.getValue() == decreasingValue.getValue());
	}


	/**
	 * @return the current value
	 */
	public double getValue()
	{
		return decreasingValue.getValue();
	}


	public void setChargeMode(final ChargeMode chargeMode)
	{
		this.chargeMode = chargeMode;
	}


	public void reset()
	{
		increasingValue.reset();
		decreasingValue.reset();
		chargeMode = ChargeMode.STALL;
	}
}
