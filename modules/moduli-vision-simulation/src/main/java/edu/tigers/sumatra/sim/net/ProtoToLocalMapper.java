package edu.tigers.sumatra.sim.net;

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
import edu.tigers.sumatra.vision.data.IKickEvent;


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
	
	
	public static IKickEvent mapKickEvent(SimRequestOuterClass.SimKickEvent kickEvent)
	{
		return new SimKickEvent(
				mapVector2(kickEvent.getPos()),
				mapBotId(kickEvent.getKickingbot()),
				kickEvent.getTimestamp());
	}
	
	
	public static IVectorN mapVectorN(final SimCommon.VectorN v)
	{
		return VectorN.from(v.getXList().stream().mapToDouble(d -> d).toArray());
	}
	
	
	public static MoveConstraints mapDriveLimits(final SimBotActionOuterClass.DriveLimits d)
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
