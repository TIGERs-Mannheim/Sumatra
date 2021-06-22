/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import com.sleepycat.persist.model.Persistent;
import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * This {@link DynamicPosition} represents an updateable position.
 * It is immutable, so to get the latest value, {@link #update(SimpleWorldFrame)} must be called.
 * This returns an updated instance.
 */
@Persistent(version = 3)
@Value
@Builder(toBuilder = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DynamicPosition
{
	public static final DynamicPosition ZERO = new DynamicPosition();

	IVector2 pos;
	AObjectID trackedId;
	double lookahead;
	boolean useKickerPos;


	/**
	 * @param objId an object id. The position will be zero. The instance must be updated first!
	 */
	public DynamicPosition(@NonNull final AObjectID objId)
	{
		this(Vector2f.ZERO_VECTOR, objId, 0, true);
	}


	/**
	 * @param obj a tracked object. The position will be initialized with the objects position
	 */
	public DynamicPosition(@NonNull final ITrackedObject obj)
	{
		this(obj.getPos(), obj.getId(), 0, true);
	}


	/**
	 * @param pos a fixed position
	 */
	public DynamicPosition(final IVector2 pos)
	{
		this(pos, null, 0, true);
	}


	private DynamicPosition()
	{
		this(Vector2f.ZERO_VECTOR, null, 0, true);
	}


	/**
	 * Update position by consulting {@link SimpleWorldFrame}
	 *
	 * @param swf
	 * @return new updated instance
	 */
	public final DynamicPosition update(final SimpleWorldFrame swf)
	{
		return toBuilder()
				.pos(getLatestPos(swf))
				.build();
	}


	private IVector2 getLatestPos(final SimpleWorldFrame swf)
	{
		if (trackedId == null)
		{
			return pos;
		}
		if (trackedId.isBall())
		{
			if (lookahead > 1e-5)
			{
				return swf.getBall().getTrajectory().getPosByTime(lookahead).getXYVector();
			} else
			{
				return swf.getBall().getPos();
			}
		}
		if (trackedId.isBot())
		{
			ITrackedBot bot = swf.getBot((BotID) trackedId);
			if (bot == null)
			{
				return pos;
			}
			IVector2 botPos;
			double botAngle;
			if (lookahead > 1e-5f)
			{
				botPos = bot.getPosByTime(lookahead);
				botAngle = bot.getAngleByTime(lookahead);
			} else
			{
				botPos = bot.getPos();
				botAngle = bot.getOrientation();
			}
			if (useKickerPos)
			{
				return BotShape.getKickerCenterPos(botPos, botAngle,
						bot.getCenter2DribblerDist() + Geometry.getBallRadius());
			} else
			{
				return botPos;
			}
		}
		return pos;
	}


	public String getSaveableString()
	{
		if (trackedId != null)
		{
			if (trackedId.isBot())
			{
				BotID botId = (BotID) trackedId;
				return trackedId.getNumber() + " " + botId.getTeamColor().name();
			} else if (trackedId.isBall())
			{
				return "-1";
			}
		}
		return pos.getSaveableString();
	}


	@Override
	public String toString()
	{
		return "[" + pos + "," + trackedId + "]";
	}


	/**
	 * String must look like "0 BLUE" or "0,0"
	 *
	 * @param value
	 * @return
	 */
	public static DynamicPosition valueOf(final String value)
	{
		if (StringUtils.isBlank(value))
		{
			return null;
		}
		if ("-1".equals(value) || "ball".equalsIgnoreCase(value))
		{
			return new DynamicPosition(BallID.instance());
		}
		try
		{
			return new DynamicPosition(AVector2.valueOf(value));
		} catch (NumberFormatException err)
		{
			// This is not a simple position, go on with id detection
		}

		List<String> finalValues = parseValues(value);
		if (finalValues.isEmpty() || (finalValues.size() > 2))
		{
			throw new NumberFormatException("Format does not conform to: id[[, ]color]. Values: " + finalValues);
		}
		int id = Integer.parseInt(finalValues.get(0));
		if (finalValues.size() != 2)
		{
			throw new NumberFormatException("missing bot id color");
		}
		ETeamColor color = getTeamColor(finalValues.get(1));
		return new DynamicPosition(BotID.createBotId(id, color));
	}


	private static ETeamColor getTeamColor(final String str)
	{
		if (str.startsWith("Y"))
		{
			return ETeamColor.YELLOW;
		} else if (str.startsWith("B"))
		{
			return ETeamColor.BLUE;
		}
		throw new NumberFormatException("invalid team color: " + str);
	}


	private static List<String> parseValues(final String value)
	{
		String[] values = value.replaceAll("[,;]", " ").split("[ ]");
		List<String> finalValues = new ArrayList<>(2);
		for (String val : values)
		{
			if (!val.trim().isEmpty() && !val.contains(","))
			{
				finalValues.add(val.trim());
			}
		}
		return finalValues;
	}


	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject jsonMapping = pos.toJSON();
		jsonMapping.put("trackedId", trackedId == null ? "" : trackedId.getNumber());
		jsonMapping.put("lookahead", lookahead);
		return jsonMapping;
	}


	public List<Number> getNumberList()
	{
		List<Number> numbers = pos.getNumberList();
		numbers.add(trackedId == null ? -1 : trackedId.getNumber());
		numbers.add(lookahead);
		return numbers;
	}
}
