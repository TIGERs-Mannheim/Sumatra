/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.ids;

import java.util.ArrayList;
import java.util.List;

import com.github.g3force.s2vconverter.IString2ValueConverter;


/**
 * Converter for {@link AObjectID}s
 */
public class ObjectIdConverter implements IString2ValueConverter
{
	@Override
	public boolean supportedClass(final Class<?> impl)
	{
		return impl.equals(BotID.class)
				|| impl.equals(BallID.class)
				|| impl.equals(UninitializedID.class);
	}
	
	
	@Override
	public Object parseString(final Class<?> impl, final String value)
	{
		if ("-1".equals(value) || "ball".equalsIgnoreCase(value))
		{
			return BallID.instance();
		}
		List<String> finalValues = parseValues(value);
		if (finalValues.isEmpty() || (finalValues.size() > 2))
		{
			throw new NumberFormatException("Format does not conform to: id[[, ]color]. Values: " + finalValues);
		}
		int id = Integer.parseInt(finalValues.get(0));
		if (id == UninitializedID.UNINITIALIZED_ID)
		{
			return new UninitializedID();
		}
		if (finalValues.size() != 2)
		{
			throw new NumberFormatException("missing bot id color");
		}
		ETeamColor color = getTeamColor(finalValues.get(1));
		return BotID.createBotId(id, color);
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
	
	
	@Override
	public String toString(final Class<?> impl, final Object value)
	{
		AObjectID vec = (AObjectID) value;
		return vec.getSaveableString();
	}
	
}
