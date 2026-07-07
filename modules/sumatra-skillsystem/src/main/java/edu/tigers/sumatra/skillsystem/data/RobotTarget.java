package edu.tigers.sumatra.skillsystem.data;

import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.wp.data.DynamicPosition;
import edu.tigers.sumatra.wp.data.ITrackedObject;
import edu.tigers.sumatra.wp.data.WorldFrame;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import lombok.With;


@Value
@Getter(AccessLevel.NONE)
@AllArgsConstructor
public class RobotTarget
{
	@With
	IVector2 destination;
	@With
	Double targetAngle;
	DynamicPosition lookAtTarget;


	public RobotTarget()
	{
		destination = null;
		targetAngle = null;
		lookAtTarget = null;
	}


	public RobotTarget(IVector2 destination, Double targetAngle)
	{
		this.destination = destination;
		this.targetAngle = targetAngle;
		lookAtTarget = null;
	}


	public RobotTarget withLookAtTarget(DynamicPosition lookAtTarget)
	{
		return new RobotTarget(destination, null, lookAtTarget);
	}


	public RobotTarget withLookAtTarget(ITrackedObject object)
	{
		return new RobotTarget(destination, null, new DynamicPosition(object));
	}


	public IVector2 getDestination(WorldFrame wFrame, BotID botId)
	{
		if (destination == null)
		{
			return wFrame.getBot(botId).getPos();
		}
		return destination;
	}


	public double getTargetAngle(WorldFrame wFrame, BotID botId)
	{
		if (targetAngle != null)
		{
			return targetAngle;
		}
		var bot = wFrame.getBot(botId);
		if (lookAtTarget != null)
		{
			return lookAtTarget.update(wFrame).getPos().subtractNew(bot.getPos()).getAngle();
		}
		return bot.getOrientation();
	}
}
