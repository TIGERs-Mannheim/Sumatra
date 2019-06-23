/*
 * *********************************************************
 * Copyright (c) 2009 - 2016, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Mar 16, 2016
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoreferee;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import edu.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.ids.IBotIDMap;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.wp.data.ITrackedBot;


/**
 * @author "Lukas Magel"
 */
public final class AutoRefUtil
{
	
	/**
	 * A Predicate implementation that filters ITrackedBot instances by their color
	 * 
	 * @author "Lukas Magel"
	 */
	public static class ColorFilter implements Predicate<ITrackedBot>
	{
		private static Map<ETeamColor, ColorFilter>	filters;
		private final ETeamColor							color;
		
		static
		{
			Map<ETeamColor, ColorFilter> tempFilters = new HashMap<>();
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
	 * @author "Lukas Magel"
	 */
	public static class ToBotIDMapper implements Function<ITrackedBot, BotID>
	{
		private static final ToBotIDMapper	INSTANCE	= new ToBotIDMapper();
		
		
		@Override
		public BotID apply(final ITrackedBot bot)
		{
			return bot.getBotId();
		}
		
		
		/**
		 * @return
		 */
		public static ToBotIDMapper get()
		{
			return INSTANCE;
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
	
	
	/**
	 * @param bots
	 * @return
	 */
	public static Set<BotID> mapToID(final Collection<ITrackedBot> bots)
	{
		return bots.stream()
				.map(ToBotIDMapper.get())
				.collect(Collectors.toSet());
	}
	
	
	/**
	 * @param bots
	 * @return
	 */
	public static Set<BotID> mapToID(final IBotIDMap<ITrackedBot> bots)
	{
		return mapToID(bots.values());
	}
	
	
	/**
	 * @return
	 */
	public static Optional<AutoRefModule> getAutoRefModule()
	{
		try
		{
			AutoRefModule autoref = (AutoRefModule) SumatraModel.getInstance().getModule(AutoRefModule.MODULE_ID);
			return Optional.of(autoref);
		} catch (ModuleNotFoundException e)
		{
		}
		return Optional.empty();
	}
	
	
	/**
	 * Execute the specified consumer on the autoreferee module if it is present
	 * 
	 * @param consumer
	 */
	public static void ifAutoRefModulePresent(final Consumer<? super AutoRefModule> consumer)
	{
		Optional<AutoRefModule> module = getAutoRefModule();
		module.ifPresent(consumer);
	}
}
