/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 14, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.wp.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.github.g3force.s2vconverter.String2ValueConverter;
import com.sleepycat.persist.model.Persistent;

import edu.tigers.sumatra.ids.AObjectID;
import edu.tigers.sumatra.ids.BallID;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.UninitializedID;
import edu.tigers.sumatra.math.AVector2;
import edu.tigers.sumatra.math.GeoMath;
import edu.tigers.sumatra.math.IVector2;
import edu.tigers.sumatra.math.Vector2;
import edu.tigers.sumatra.wp.fieldPrediction.FieldPredictionInformation;


/**
 * This {@link DynamicPosition} represents either a normal position vector
 * or an updateable position connected with an object id
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
@Persistent(version = 1)
public class DynamicPosition extends AVector2
{
	static
	{
		String2ValueConverter.getDefault().addConverter(new DynamicPositionConverter());
	}
	
	@SuppressWarnings("unused")
	private static final Logger						log			= Logger.getLogger(DynamicPosition.class.getName());
																				
	private IVector2										pos			= Vector2.ZERO_VECTOR;
	private AObjectID										trackedId;
	private double											lookahead	= 0;
	private transient FieldPredictionInformation	predInfo		= null;
																				
																				
	/**
	 * @param objId
	 */
	public DynamicPosition(final AObjectID objId)
	{
		trackedId = objId;
	}
	
	
	/**
	 * @param obj
	 */
	public DynamicPosition(final ITrackedObject obj)
	{
		trackedId = obj.getBotId();
		pos = obj.getPos();
	}
	
	
	/**
	 * @param obj
	 * @param lookahead
	 */
	public DynamicPosition(final ITrackedObject obj, final double lookahead)
	{
		trackedId = obj.getBotId();
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
			ITrackedBot bot = swf.getBot((BotID) trackedId);
			assert bot != null : "Tracked bot does not exist";
			if (bot != null)
			{
				IVector2 botPos;
				double botAngle;
				if ((lookahead > 1e-5f))
				{
					botPos = bot.getPosByTime(lookahead);
					botAngle = bot.getAngleByTime(lookahead);
				} else
				{
					botPos = bot.getPos();
					botAngle = bot.getAngle();
				}
				pos = GeoMath.getBotKickerPos(botPos, botAngle, bot.getCenter2DribblerDist());
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
		predInfo = dyn.predInfo;
	}
	
	
	/**
	 * Get future position, if this a updated tracked object
	 * 
	 * @param t
	 * @return
	 */
	public IVector2 getPosAt(final double t)
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
	public double x()
	{
		return pos.x();
	}
	
	
	@Override
	public double y()
	{
		return pos.y();
	}
	
	
	@Override
	public synchronized String getSaveableString()
	{
		if (trackedId.isBot())
		{
			BotID botId = (BotID) trackedId;
			return trackedId.getNumber() + " " + botId.getTeamColor().name();
		} else if (trackedId.isBall())
		{
			return "-1";
		}
		return super.getSaveableString();
	}
	
	
	@Override
	public synchronized String toString()
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
		try
		{
			return new DynamicPosition(AVector2.valueOf(value));
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
			return new DynamicPosition(new BallID());
		}
		if (finalValues.size() != 2)
		{
			throw new NumberFormatException("missing bot id color");
		}
		ETeamColor color = ETeamColor.valueOf(finalValues.get(1));
		return new DynamicPosition(BotID.createBotId(id, color));
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
		this.lookahead = lookahead;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized JSONObject toJSON()
	{
		JSONObject jsonMapping = super.toJSON();
		jsonMapping.put("trackedId", trackedId.getNumber());
		jsonMapping.put("lookahead", lookahead);
		return jsonMapping;
	}
	
	
	@Override
	public synchronized List<Number> getNumberList()
	{
		List<Number> numbers = super.getNumberList();
		numbers.add(trackedId.getNumber());
		numbers.add(lookahead);
		return numbers;
	}
	
	
	@Override
	public Vector2 getXYVector()
	{
		return new Vector2(pos);
	}
}
