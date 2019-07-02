/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoreferee;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * Utility class for the autoRef
 */
public final class AutoRefUtil
{
	private AutoRefUtil()
	{
		// Hide constructor
	}
	
	/**
	 * A Predicate implementation that filters ITrackedBot instances by their color
	 */
	public static class ColorFilter implements Predicate<ITrackedBot>
	{
		private static Map<ETeamColor, ColorFilter> filters;
		private final ETeamColor color;
		
		static
		{
			Map<ETeamColor, ColorFilter> tempFilters = new EnumMap<>(ETeamColor.class);
			Arrays.stream(ETeamColor.values()).forEach(color -> tempFilters.put(color, new ColorFilter(color)));
			filters = Collections.unmodifiableMap(tempFilters);
		}
		
		
		/**
		 * @param color The color to filter for
		 */
		public ColorFilter(final ETeamColor color)
		{
			this.color = color;
		}
		
		
		@Override
		public boolean test(final ITrackedBot bot)
		{
			return bot.getBotId().getTeamColor() == color;
		}
		
		
		/**
		 * Returns a color filter instance matching the color
		 * 
		 * @param color
		 * @return
		 */
		public static ColorFilter get(final ETeamColor color)
		{
			return filters.get(color);
		}
	}
	
	
	/**
	 * Filter the supplied collection of bots by their team color
	 * 
	 * @param bots
	 * @param color
	 * @return a list of all bots that match the color
	 */
	public static List<ITrackedBot> filterByColor(final Collection<ITrackedBot> bots, final ETeamColor color)
	{
		return bots.stream()
				.filter(ColorFilter.get(color))
				.collect(Collectors.toList());
	}
	
	
	/**
	 * Filter the supplied collection of bots by their team color
	 * 
	 * @param bots
	 * @param color
	 * @return a list of all bots that match the color
	 */
	public static List<ITrackedBot> filterByColor(final IBotIDMap<ITrackedBot> bots, final ETeamColor color)
	{
		return filterByColor(bots.values(), color);
	}
}
