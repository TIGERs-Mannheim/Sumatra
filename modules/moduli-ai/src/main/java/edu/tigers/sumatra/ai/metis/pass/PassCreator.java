/*
 * Copyright (c) 2009 - 2023, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ai.metis.pass;

import com.github.g3force.configurable.ConfigRegistration;
import com.github.g3force.configurable.Configurable;
import edu.tigers.sumatra.ai.metis.kicking.EBallReceiveMode;
import edu.tigers.sumatra.ai.metis.kicking.Pass;
import edu.tigers.sumatra.ai.metis.kicking.PassFactory;
import edu.tigers.sumatra.ai.metis.offense.OffensiveConstants;
import edu.tigers.sumatra.ai.metis.offense.OffensiveMath;
import edu.tigers.sumatra.ai.metis.targetrater.RotationTimeHelper;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.pathfinder.TrajectoryGenerator;
import edu.tigers.sumatra.wp.data.ITrackedBot;
import edu.tigers.sumatra.wp.data.WorldFrame;

import java.util.List;


public class PassCreator
{
	@Configurable(defValue = "0.3", comment = "The time [s] that the pass receiver should get after it moved to the target pos")
	private static double minPassReceiverPrepareTime = 0.3;

	static
	{
		ConfigRegistration.registerClass("metis", PassCreator.class);
	}

	private final PassFactory passFactory = new PassFactory();
	private WorldFrame wFrame;


	public void update(WorldFrame wFrame)
	{
		this.wFrame = wFrame;
		passFactory.update(wFrame);
	}


	public List<Pass> createPasses(KickOrigin passOrigin, IVector2 pos, BotID receiver)
	{
		var minPassDuration = timeUntilBotReached(pos, receiver)
				- passOrigin.impactTimeOrZero()
				+ minPassReceiverPrepareTime;
		var origin = passOrigin.getPos();
		var shooter = passOrigin.getShooter();
		var prepTime = preparationTime(wFrame.getBot(shooter), pos.subtractNew(origin));

		var ballPos = this.wFrame.getBall().getPos();
		boolean isRedirectable = OffensiveMath.getRedirectAngle(ballPos, passOrigin.getPos(), pos)
				< OffensiveConstants.getMaximumReasonableRedirectAngle();
		EBallReceiveMode receiveMode = isRedirectable ? EBallReceiveMode.REDIRECT : EBallReceiveMode.RECEIVE;

		return passFactory.passes(origin, pos, shooter, receiver, minPassDuration, prepTime, receiveMode);
	}


	private double preparationTime(ITrackedBot shooter, IVector2 dir)
	{
		return RotationTimeHelper.calcRotationTime(
				shooter.getAngularVel(),
				shooter.getAngleByTime(0),
				dir.getAngle(),
				shooter.getMoveConstraints().getVelMaxW(),
				shooter.getMoveConstraints().getAccMaxW()
		);
	}


	private double timeUntilBotReached(final IVector2 pos, final BotID botID)
	{
		ITrackedBot tBot = wFrame.getBot(botID);
		if (tBot == null)
		{
			return 0;
		}
		return TrajectoryGenerator.generatePositionTrajectory(tBot, pos).getTotalTime();
	}
}
