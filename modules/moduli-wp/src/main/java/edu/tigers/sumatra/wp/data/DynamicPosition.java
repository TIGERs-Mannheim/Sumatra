/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.wp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.UninitializedID;
import edu.tigers.sumatra.math.botshape.BotShape;
import edu.tigers.sumatra.math.vector.AVector2;
import edu.tigers.sumatra.math.vector.IVector2;
import edu.tigers.sumatra.math.vector.Vector2f;
import edu.tigers.sumatra.model.SumatraModel;


/**
 * This {@link DynamicPosition} represents either a normal position vector
 * or an updateable position connected with an object id
 */
@Persistent(version = 2)
public class DynamicPosition
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DynamicPosition.class.getName());

	private IVector2 pos = Vector2f.ZERO_VECTOR;
	private AObjectID trackedId;
	private double lookahead = 0;
	private boolean useKickerPos = true;
	private double passRange = 0;


	/**
	 * @param objId
	 */
	public DynamicPosition(final AObjectID objId)
	{
		Objects.requireNonNull(objId, "Object ID must not be null");
		trackedId = objId;
	}


	/**
	 * @param obj
	 */
	public DynamicPosition(final ITrackedObject obj)
	{
		Objects.requireNonNull(obj, "Tracked object must not be null");
		trackedId = obj.getId();
		pos = obj.getPos();
	}


	/**
	 * @param obj
	 * @param lookahead
	 */
	public DynamicPosition(final ITrackedObject obj, final double lookahead)
	{
		this(obj);
		setLookahead(lookahead);
	}


	/**
	 * @param pos
	 */
	public DynamicPosition(final IVector2 pos)
	{
		setPos(pos);
		trackedId = new UninitializedID();
	}


	/**
	 * Copy constructor
	 *
	 * @param dynamicPosition
	 */
	public DynamicPosition(final DynamicPosition dynamicPosition)
	{
		this.pos = dynamicPosition.pos;
		this.trackedId = dynamicPosition.trackedId;
		this.lookahead = dynamicPosition.lookahead;
		this.useKickerPos = dynamicPosition.useKickerPos;
		this.passRange = dynamicPosition.passRange;
	}


	/**
	 * @param pos
	 * @param passRange the range [rad] in which the pass can be played
	 */
	public DynamicPosition(final IVector2 pos, final double passRange)
	{
		setPos(pos);
		trackedId = new UninitializedID();
		this.passRange = passRange;
	}


	@SuppressWarnings("unused")
	private DynamicPosition()
	{
		trackedId = null;
	}


	/**
	 * Update position by consulting {@link SimpleWorldFrame}
	 *
	 * @param swf
	 */
	public final void update(final SimpleWorldFrame swf)
	{
		if (trackedId.isBall())
		{
			if (lookahead > 1e-5)
			{
				pos = swf.getBall().getTrajectory().getPosByTime(lookahead).getXYVector();
			} else
			{
				pos = swf.getBall().getPos();
			}
		} else if (trackedId.isBot())
		{
			ITrackedBot bot = swf.getBot((BotID) trackedId);
			if (bot != null)
			{
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
					pos = BotShape.getKickerCenterPos(botPos, botAngle,
							bot.getCenter2DribblerDist() + Geometry.getBallRadius());
				} else
				{
					pos = botPos;
				}
			} else if (!SumatraModel.getInstance().isProductive())
			{
				log.warn("No tracked bot with id " + trackedId + " found.", new Exception());
			}
		}
	}


	/**
	 * @param dyn
	 */
	public final void update(final DynamicPosition dyn)
	{
		pos = dyn.pos;
		trackedId = dyn.trackedId;
		lookahead = dyn.lookahead;
		passRange = dyn.passRange;
	}


	public final void update(final AObjectID trackedId)
	{
		this.trackedId = trackedId;
	}


	/**
	 * @return the trackedId
	 */
	public final AObjectID getTrackedId()
	{
		return trackedId;
	}


	public IVector2 getPos()
	{
		return pos;
	}


	/**
	 * @param pos the pos to set
	 */
	public final void setPos(final IVector2 pos)
	{
		Objects.requireNonNull(pos, "Position must not be null");
		this.pos = pos;
	}


	public String getSaveableString()
	{
		if (trackedId.isBot())
		{
			BotID botId = (BotID) trackedId;
			return trackedId.getNumber() + " " + botId.getTeamColor().name();
		} else if (trackedId.isBall())
		{
			return "-1";
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


	/**
	 * @return the lookahead
	 */
	public double getLookahead()
	{
		return lookahead;
	}


	/**
	 * @param lookahead the lookahead to set
	 */
	public void setLookahead(final double lookahead)
	{
		Validate.isTrue(lookahead >= 0, "The lookahead must be greater than or equal to zero");
		this.lookahead = lookahead;
	}


	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject jsonMapping = pos.toJSON();
		jsonMapping.put("trackedId", trackedId.getNumber());
		jsonMapping.put("lookahead", lookahead);
		return jsonMapping;
	}


	public List<Number> getNumberList()
	{
		List<Number> numbers = pos.getNumberList();
		numbers.add(trackedId.getNumber());
		numbers.add(lookahead);
		return numbers;
	}


	public void setUseKickerPos(final boolean useKickerPos)
	{
		this.useKickerPos = useKickerPos;
	}


	public double getPassRange()
	{
		return passRange;
	}


	public void setPassRange(final double passRange)
	{
		this.passRange = passRange;
	}
}
