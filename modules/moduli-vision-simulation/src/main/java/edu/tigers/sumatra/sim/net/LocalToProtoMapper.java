/*
 * Copyright (c) 2009 - 2021, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.sim.net;

import edu.tigers.sumatra.ball.BallState;
import edu.tigers.sumatra.bot.IMoveConstraints;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.pose.Pose;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.IVector3;
import edu.tigers.sumatra.math.vector.IVectorN;
import edu.tigers.sumatra.sim.SimKickEvent;

import java.util.stream.Collectors;


/**
 * Map local data structures to protobuf classes
 */
public final class LocalToProtoMapper
{
	private LocalToProtoMapper()
	{
	}


	public static SimCommon.BotId mapBotId(BotID botID)
	{
		return SimCommon.BotId.newBuilder()
				.setId(botID.getNumber())
				.setColor(botID.getTeamColor() == ETeamColor.YELLOW
						? SimCommon.TeamColor.YELLOW
						: SimCommon.TeamColor.BLUE)
				.build();
	}


	public static SimCommon.VectorN mapVectorN(IVectorN v)
	{
		return SimCommon.VectorN.newBuilder()
				.addAllX(v.getNumberList().stream()
						.map(Number::doubleValue)
						.collect(Collectors.toList()))
				.build();
	}


	public static SimCommon.Vector3 mapVector3(IVector3 v)
	{
		return SimCommon.Vector3.newBuilder()
				.setX(v.x())
				.setY(v.y())
				.setZ(v.z())
				.build();
	}


	public static SimCommon.Vector2 mapVector2(IVector2 v)
	{
		return SimCommon.Vector2.newBuilder()
				.setX(v.x())
				.setY(v.y())
				.build();
	}


	public static SimCommon.Vector3 mapPose(Pose p)
	{
		return SimCommon.Vector3.newBuilder()
				.setX(p.getPos().x())
				.setY(p.getPos().y())
				.setZ(p.getOrientation())
				.build();
	}


	public static SimRequestOuterClass.SimKickEvent mapKickEvent(SimKickEvent kickEvent)
	{
		return SimRequestOuterClass.SimKickEvent.newBuilder()
				.setPos(mapVector2(kickEvent.getPosition()))
				.setKickingBot(mapBotId(kickEvent.getKickingBot()))
				.setTimestamp(kickEvent.getTimestamp())
				.setKickingBotPosition(mapVector2(kickEvent.getKickingBotPosition()))
				.setBotDirection(kickEvent.getBotDirection())
				.setKickBallState(mapBallState(kickEvent.getKickBallState()))
				.build();
	}


	public static SimState.SimBallState mapBallState(BallState ballState)
	{
		return SimState.SimBallState.newBuilder()
				.setPose(LocalToProtoMapper.mapVector3(ballState.getPos()))
				.setVel(LocalToProtoMapper.mapVector3(ballState.getVel()))
				.setAcc(LocalToProtoMapper.mapVector3(ballState.getAcc()))
				.setSpin(LocalToProtoMapper.mapVector2(ballState.getSpin()))
				.build();
	}


	public static SimBotActionOuterClass.DriveLimits mapDriveLimits(IMoveConstraints m)
	{
		return SimBotActionOuterClass.DriveLimits.newBuilder()
				.setVelMax(m.getVelMax())
				.setAccMax(m.getAccMax())
				.setJerkMax(m.getJerkMax())
				.setVelMaxW(m.getVelMaxW())
				.setAccMaxW(m.getAccMaxW())
				.setJerkMaxW(m.getJerkMaxW())
				.build();
	}


	public static SimCommon.TeamColor mapTeamColor(ETeamColor teamColor)
	{
		switch (teamColor)
		{
			case YELLOW:
				return SimCommon.TeamColor.YELLOW;
			case BLUE:
				return SimCommon.TeamColor.BLUE;
			default:
				return SimCommon.TeamColor.UNRECOGNIZED;
		}
	}
}
