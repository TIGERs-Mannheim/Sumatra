package edu.tigers.sumatra.bot;

import edu.tigers.sumatra.bot.params.BotMovementLimits;
import edu.tigers.sumatra.data.collector.IExportable;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Constraints on the movement of robots.
 */
@Value
@Builder(toBuilder = true)
@With
public class MoveConstraints implements IExportable
{
	@NonNull
	@Builder.Default
	BotMovementLimits limits = BotMovementLimits.ZERO;

	@NonNull
	@Builder.Default
	IVector2 primaryDirection = Vector2f.ZERO_VECTOR;


	public static final MoveConstraints ZERO = MoveConstraints.builder().build();


	MoveConstraints(@NonNull BotMovementLimits limits, @NonNull IVector2 primaryDirection)
	{
		Validate.isTrue(primaryDirection.isFinite(), "primaryDirection must be finite: ", primaryDirection);
		this.limits = limits;
		this.primaryDirection = primaryDirection;
	}


	public static MoveConstraints from(final BotMovementLimits limits)
	{
		return MoveConstraints.builder().limits(limits).build();
	}


	public MoveConstraints limitedBy(final BotMovementLimits other)
	{
		return withLimits(limits.limitedBy(other));
	}


	public double getVelMax()
	{
		return limits.getVelMax();
	}


	public double getAccMax()
	{
		return limits.getAccMax();
	}


	public double getBrkMax()
	{
		return limits.getBrkMax();
	}


	public double getVelMaxW()
	{
		return limits.getVelMaxW();
	}


	public double getAccMaxW()
	{
		return limits.getAccMaxW();
	}


	public MoveConstraints withVelMax(final double velMax)
	{
		return withLimits(limits.withVelMax(velMax));
	}


	public MoveConstraints withAccMax(final double accMax)
	{
		return withLimits(limits.withAccMax(accMax));
	}


	public MoveConstraints withBrkMax(final double brkMax)
	{
		return withLimits(limits.withBrkMax(brkMax));
	}


	public MoveConstraints withVelMaxW(final double velMaxW)
	{
		return withLimits(limits.withVelMaxW(velMaxW));
	}


	public MoveConstraints withAccMaxW(final double accMaxW)
	{
		return withLimits(limits.withAccMaxW(accMaxW));
	}


	@Override
	public List<Number> getNumberList()
	{
		List<Number> nbrs = new ArrayList<>();
		nbrs.add(getVelMax());
		nbrs.add(getAccMax());
		nbrs.add(getBrkMax());
		nbrs.add(getVelMaxW());
		nbrs.add(getAccMaxW());
		nbrs.add(primaryDirection.x());
		nbrs.add(primaryDirection.y());
		return nbrs;
	}


	@Override
	public List<String> getHeaders()
	{
		return Arrays.asList(
				"velMax", "accMax", "brkMax", "velMaxW", "accMaxW",
				"primaryDirectionX", "primaryDirectionY"
		);
	}
}
