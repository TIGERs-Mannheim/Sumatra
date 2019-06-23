/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.data;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.Function1dPoly;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.IFunction1D;
import edu.dhbw.mannheim.tigers.sumatra.model.data.math.functions.NormalizerFunctionWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.models.ALearnedModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.EBotType;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class KickerModel extends ALearnedModel
{
	private IFunction1D										duration2KickSpeedFn	= null;
	private IFunction1D										kickSpeed2DurationFn	= null;
	
	private static final Map<EBotType, KickerModel>	knownModels				= new EnumMap<>(EBotType.class);
	
	
	/**
	 * @param botType
	 * @return
	 */
	public static KickerModel forBot(final EBotType botType)
	{
		if (!knownModels.containsKey(botType))
		{
			knownModels.put(botType, new KickerModel(botType.name().toLowerCase()));
		}
		return knownModels.get(botType);
	}
	
	
	/**
	 * @param identifier
	 */
	private KickerModel(final String identifier)
	{
		super("kicker", identifier);
		onNewParameters();
	}
	
	
	/**
	 * @param kickSpeed
	 * @return
	 */
	public float getDuration(final float kickSpeed)
	{
		float val = kickSpeed2DurationFn.eval(kickSpeed);
		return Math.min(10000, Math.max(0, val));
	}
	
	
	/**
	 * @param duration
	 * @return
	 */
	public float getKickSpeed(final float duration)
	{
		float val = duration2KickSpeedFn.eval(duration);
		return Math.min(8, Math.max(0, val));
	}
	
	
	@Override
	protected void onNewParameters()
	{
		duration2KickSpeedFn = new NormalizerFunctionWrapper(
				new Function1dPoly(Arrays.copyOfRange(p, 0, 4)),
				Arrays.copyOfRange(p, 4, 5), Arrays.copyOfRange(p, 5, 6));
		kickSpeed2DurationFn = new NormalizerFunctionWrapper(
				new Function1dPoly(Arrays.copyOfRange(p, 6, 10)),
				Arrays.copyOfRange(p, 10, 11), Arrays.copyOfRange(p, 11, 12));
	}
}
