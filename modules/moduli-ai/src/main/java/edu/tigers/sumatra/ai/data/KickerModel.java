/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jun 7, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import edu.tigers.sumatra.bot.EBotType;
import edu.tigers.sumatra.functions.Function1dPoly;
import edu.tigers.sumatra.functions.IFunction1D;
import edu.tigers.sumatra.functions.NormalizerFunctionWrapper;
import edu.tigers.sumatra.ml.model.ALearnedModel;


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
	public double getDuration(final double kickSpeed)
	{
		double val = kickSpeed2DurationFn.eval(kickSpeed);
		return Math.min(10000, Math.max(0, val));
	}
	
	
	/**
	 * @param duration
	 * @return
	 */
	public double getKickSpeed(final double duration)
	{
		double val = duration2KickSpeedFn.eval(duration);
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
	
	
	@Override
	protected double[] getDefaultParams()
	{
		return new double[] { 5.78233, 2.246873, -0.6708279, 0.06301028, 3447.6558, 1792.7, 2754.0725, 1436.391,
				301.10553, 13.41283, 4.7423058, 2.1568887 };
	}
}
