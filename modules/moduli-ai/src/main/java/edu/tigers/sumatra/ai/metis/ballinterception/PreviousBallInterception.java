package edu.tigers.sumatra.ai.metis.ballinterception;

import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2;
import lombok.Getter;


public class PreviousBallInterception extends Vector2
{
	@Getter
	private final boolean overshooting;


	public PreviousBallInterception(IVector2 vec, boolean overshooting)
	{
		super(vec);
		this.overshooting = overshooting;
	}
}
