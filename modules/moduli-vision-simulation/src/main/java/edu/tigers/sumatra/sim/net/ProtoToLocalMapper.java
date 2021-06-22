/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.net;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.bot.MoveConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.math.vector.Vector3f;
import edu.tigers.sumatra.math.vector.VectorN;
import edu.tigers.sumatra.sim.SimKickEvent;


/**
 * Map protobuf data structures to local data types
 */
public final class ProtoToLocalMapper
{
	private ProtoToLocalMapper()
	{
	}


	public static BotID mapBotId(SimCommon.BotId botId)
	{
		return BotID.createBotId(botId.getId(), mapTeamColor(botId.getColor()));
	}


	public static ETeamColor mapTeamColor(SimCommon.TeamColor teamColor)
	{
		return teamColor == SimCommon.TeamColor.YELLOW ? ETeamColor.YELLOW : ETeamColor.BLUE;
	}


	public static IVector3 mapVector3(SimCommon.Vector3 v)
	{
		return Vector3f.fromXYZ(v.getX(), v.getY(), v.getZ());
	}


	public static IVector2 mapVector2(SimCommon.Vector2 v)
	{
		return Vector2f.fromXY(v.getX(), v.getY());
	}


	public static Pose mapPose(SimCommon.Vector3 v)
	{
		return Pose.from(mapVector3(v));
	}


	public static SimKickEvent mapKickEvent(SimRequestOuterClass.SimKickEvent kickEvent)
	{
		return new SimKickEvent(
				mapVector2(kickEvent.getPos()),
				mapBotId(kickEvent.getKickingBot()),
				kickEvent.getTimestamp(),
				mapVector2(kickEvent.getKickingBotPosition()),
				kickEvent.getBotDirection(),
				mapBallState(kickEvent.getKickBallState()));
	}


	public static BallState mapBallState(SimState.SimBallState ballState)
	{
		return BallState.builder()
				.withPos(mapVector3(ballState.getPose()))
				.withVel(mapVector3(ballState.getVel()))
				.withAcc(mapVector3(ballState.getAcc()))
				.withSpin(mapVector2(ballState.getSpin()))
				.build();
	}


	public static IVectorN mapVectorN(final SimCommon.VectorN v)
	{
		return VectorN.from(v.getXList().stream().mapToDouble(d -> d).toArray());
	}


	public static IMoveConstraints mapDriveLimits(final SimBotActionOuterClass.DriveLimits d)
	{
		final MoveConstraints m = new MoveConstraints();
		m.setVelMax(d.getVelMax());
		m.setAccMax(d.getAccMax());
		m.setJerkMax(d.getJerkMax());
		m.setVelMaxW(d.getVelMaxW());
		m.setAccMaxW(d.getAccMaxW());
		m.setJerkMaxW(d.getJerkMaxW());
		return m;
	}
}
