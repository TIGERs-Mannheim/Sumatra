package edu.tigers.sumatra.bot.params;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.Validate;

import java.beans.ConstructorProperties;


/**
 * Immutable robot movement limitations.
 */
@Value
@Builder(toBuilder = true)
@With
public class BotMovementLimits
{
	double velMax;
	double accMax;
	double brkMax;
	double velMaxW;
	double accMaxW;


	public static final BotMovementLimits ZERO = BotMovementLimits.builder().build();


	@ConstructorProperties({ "velMax", "accMax", "brkMax", "velMaxW", "accMaxW" })
	BotMovementLimits(double velMax, double accMax, double brkMax, double velMaxW, double accMaxW)
	{
		Validate.isTrue(velMax >= 0, "velMax must be >=0: ", velMax);
		Validate.isTrue(accMax >= 0, "accMax must be >=0: ", accMax);
		Validate.isTrue(brkMax >= 0, "brkMax must be >=0: ", brkMax);
		Validate.isTrue(velMaxW >= 0, "velMaxW must be >=0: ", velMaxW);
		Validate.isTrue(accMaxW >= 0, "accMaxW must be >=0: ", accMaxW);
		this.velMax = velMax;
		this.accMax = accMax;
		this.brkMax = brkMax;
		this.velMaxW = velMaxW;
		this.accMaxW = accMaxW;
	}


	/**
	 * @param other the limits to cap each value by
	 * @return new limits where each value is the minimum of this and other
	 */
	public BotMovementLimits limitedBy(final BotMovementLimits other)
	{
		return BotMovementLimits.builder()
				.velMax(Math.min(velMax, other.velMax))
				.accMax(Math.min(accMax, other.accMax))
				.brkMax(Math.min(brkMax, other.brkMax))
				.velMaxW(Math.min(velMaxW, other.velMaxW))
				.accMaxW(Math.min(accMaxW, other.accMaxW))
				.build();
	}
}
