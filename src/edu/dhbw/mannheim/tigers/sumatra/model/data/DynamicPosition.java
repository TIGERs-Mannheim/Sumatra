/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.sleepycat.persist.model.Persistent;

import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.SimpleWorldFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.AiMath;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeamColor;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ATrackedObject;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.AObjectID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.UninitializedID;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.fieldPrediction.FieldPredictionInformation;


/**
 * This {@link DynamicPosition} represents either a normal position vector
 * or an updateable position connected with an object id
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent
public class DynamicPosition implements IVector2
{
	@SuppressWarnings("unused")
	private static final Logger						log			= Logger.getLogger(DynamicPosition.class.getName());
	
	private IVector2										pos			= Vector2.ZERO_VECTOR;
	private final AObjectID								trackedId;
	private float											lookahead	= 0;
	private transient FieldPredictionInformation	predInfo		= null;
	
	
	/**
	 * @param obj
	 */
	public DynamicPosition(final ATrackedObject obj)
	{
		trackedId = obj.getId();
		pos = obj.getPos();
	}
	
	
	/**
	 * @param obj
	 * @param lookahead
	 */
	public DynamicPosition(final ATrackedObject obj, final float lookahead)
	{
		trackedId = obj.getId();
		pos = obj.getPos();
		this.lookahead = lookahead;
	}
	
	
	/**
	 * @param pos
	 */
	public DynamicPosition(final IVector2 pos)
	{
		this.pos = pos;
		trackedId = new UninitializedID();
	}
	
	
	@SuppressWarnings("unused")
	private DynamicPosition()
	{
		trackedId = null;
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Update position by consulting {@link SimpleWorldFrame}
	 * 
	 * @param swf
	 */
	public final void update(final SimpleWorldFrame swf)
	{
		if (trackedId.isBall())
		{
			pos = swf.getBall().getPos();
		} else if (trackedId.isBot())
		{
			TrackedTigerBot bot = swf.getBot((BotID) trackedId);
			assert bot != null : "Tracked bot does not exist";
			if (bot != null)
			{
				IVector2 botPos;
				float botAngle;
				if ((lookahead > 1e-5f))
				{
					botPos = bot.getPosByTime(lookahead);
					botAngle = bot.getAngleByTime(lookahead);
				} else
				{
					botPos = bot.getPos();
					botAngle = bot.getAngle();
				}
				pos = AiMath.getBotKickerPos(botPos, botAngle, bot.getBot()
						.getCenter2DribblerDist());
			}
		}
	}
	
	
	/**
	 * Get future position, if this a updated tracked object
	 * 
	 * @param t
	 * @return
	 */
	public IVector2 getPosAt(final float t)
	{
		if (predInfo != null)
		{
			return predInfo.getPosAt(t);
		}
		return this;
	}
	
	
	/**
	 * @return the trackedId
	 */
	public final AObjectID getTrackedId()
	{
		return trackedId;
	}
	
	
	/**
	 * @param pos the pos to set
	 */
	public final void setPos(final IVector2 pos)
	{
		this.pos = pos;
	}
	
	
	@Override
	public float x()
	{
		return pos.x();
	}
	
	
	@Override
	public float y()
	{
		return pos.y();
	}
	
	
	@Override
	public Vector2 normalizeNew()
	{
		return pos.normalizeNew();
	}
	
	
	@Override
	public Vector2 addNew(final IVector2 vector)
	{
		return pos.addNew(vector);
	}
	
	
	@Override
	public Vector2 subtractNew(final IVector2 vector)
	{
		return pos.subtractNew(vector);
	}
	
	
	@Override
	public Vector2 multiplyNew(final float factor)
	{
		return pos.multiplyNew(factor);
	}
	
	
	@Override
	public Vector2 scaleToNew(final float newLength)
	{
		return pos.scaleToNew(newLength);
	}
	
	
	@Override
	public Vector2 turnNew(final float angle)
	{
		return pos.turnNew(angle);
	}
	
	
	@Override
	public Vector2 turnToNew(final float angle)
	{
		return pos.turnToNew(angle);
	}
	
	
	@Override
	public IVector2 roundNew(final int digits)
	{
		return pos.roundNew(digits);
	}
	
	
	@Override
	public float getLength2()
	{
		return pos.getLength2();
	}
	
	
	@Override
	public boolean isZeroVector()
	{
		return pos.isZeroVector();
	}
	
	
	@Override
	public boolean isVertical()
	{
		return pos.isVertical();
	}
	
	
	@Override
	public boolean isHorizontal()
	{
		return pos.isHorizontal();
	}
	
	
	@Override
	public float getAngle()
	{
		return pos.getAngle();
	}
	
	
	@Override
	public float scalarProduct(final IVector2 v)
	{
		return pos.scalarProduct(v);
	}
	
	
	@Override
	public IVector2 projectToNew(final IVector2 v)
	{
		return pos.projectToNew(v);
	}
	
	
	@Override
	public boolean equals(final IVector2 vec, final float tolerance)
	{
		return pos.equals(vec, tolerance);
	}
	
	
	@Override
	public Vector2 absNew()
	{
		return pos.absNew();
	}
	
	
	@Override
	public IVector2 getNormalVector()
	{
		return pos.getNormalVector();
	}
	
	
	@Override
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
		return pos.x() + "," + pos.y();
	}
	
	
	@Override
	public boolean similar(final IVector2 vec, final float treshold)
	{
		return pos.similar(vec, treshold);
	}
	
	
	@Override
	public String toString()
	{
		return "DynamicPosition [pos=" + pos + ", trackedId=" + trackedId + "]";
	}
	
	
	/**
	 * String must look like "0 BLUE" or "0,0"
	 * 
	 * @param value
	 * @return
	 */
	public static Object valueOf(final String value)
	{
		try
		{
			return new DynamicPosition(Vector2.valueOf(value));
		} catch (NumberFormatException err)
		{
			// This is not a simple position, go on with id detection
		}
		
		String[] values = value.replaceAll("[,;]", " ").split("[ ]");
		List<String> finalValues = new ArrayList<String>(2);
		for (String val : values)
		{
			if (!val.trim().isEmpty() && !val.contains(","))
			{
				finalValues.add(val.trim());
			}
		}
		if ((finalValues.size() > 2) || (finalValues.size() < 1))
		{
			throw new NumberFormatException("Format does not conform to: id[[, ]color]. Values: " + finalValues);
		}
		int id = Integer.valueOf(finalValues.get(0));
		if (id == -1)
		{
			return new DynamicPosition(new TrackedBall(AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR, AVector3.ZERO_VECTOR,
					1,
					false));
		}
		if (finalValues.size() != 2)
		{
			throw new NumberFormatException("missing bot id color");
		}
		ETeamColor color = ETeamColor.valueOf(finalValues.get(1));
		return new DynamicPosition(new TrackedTigerBot(BotID.createBotId(id, color), AVector2.ZERO_VECTOR,
				AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 1, null, color));
	}
	
	
	/**
	 * @return the lookahead
	 */
	public float getLookahead()
	{
		return lookahead;
	}
	
	
	/**
	 * @param lookahead the lookahead to set
	 */
	public void setLookahead(final float lookahead)
	{
		this.lookahead = lookahead;
	}
	
	
	@Override
	public Vector2 turnAroundNew(final IVector2 axis, final float angle)
	{
		return pos.turnAroundNew(axis, angle);
	}
	
	
	@Override
	public JSONObject toJSON()
	{
		Map<String, Object> jsonMapping = new LinkedHashMap<String, Object>();
		jsonMapping.put("x", pos.x());
		jsonMapping.put("y", pos.y());
		jsonMapping.put("trackedId", trackedId.getNumber());
		jsonMapping.put("lookahead", lookahead);
		return new JSONObject(jsonMapping);
	}
	
	
	@Override
	public List<Number> getNumberList()
	{
		List<Number> numbers = new ArrayList<>();
		numbers.add(x());
		numbers.add(y());
		numbers.add(trackedId.getNumber());
		numbers.add(lookahead);
		return numbers;
	}
	
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}
	
	
	@Override
	public boolean equals(final Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		DynamicPosition other = (DynamicPosition) obj;
		if (pos == null)
		{
			if (other.pos != null)
			{
				return false;
			}
		} else if (!pos.equals(other.pos))
		{
			return false;
		}
		return true;
	}
}
