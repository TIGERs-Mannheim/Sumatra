package edu.tigers.sumatra.ai.metis;

import edu.tigers.sumatra.ai.metis.ballpossession.BallPossession;
import edu.tigers.sumatra.ai.metis.ballpossession.EBallPossession;
import edu.tigers.sumatra.geometry.Geometry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;


@RequiredArgsConstructor
public class AdvantageRuleCalc extends ACalculator
{
	private final Supplier<BallPossession> ballPossession;


	@Getter
	private boolean keepPlaying;


	@Override
	protected void doCalc()
	{
		boolean ballInOurField = Geometry.getFieldHalfOur().isPointInShape(getWFrame().getBall().getPos());
		var possession = ballPossession.get().getEBallPossession();
		//Check if ball is in our field -> No: keepPlaying, Yes: STOP
		if (!ballInOurField)
		{
			//Check if we have the ball
			keepPlaying = possession == EBallPossession.WE;
		} else
		{
			keepPlaying = false;
		}
	}
}
