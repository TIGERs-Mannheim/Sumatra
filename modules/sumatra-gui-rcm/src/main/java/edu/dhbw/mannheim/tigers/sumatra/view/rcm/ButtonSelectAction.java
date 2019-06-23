/*
 * *********************************************************
 * Copyright (c) 2009 DHBW Mannheim - Tigers Mannheim
 * Project: tigers-robotControlUtility
 * Date: 19.11.2010
 * Authors: Clemens Teichmann <clteich@gmx.de>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.rcm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import edu.tigers.sumatra.rcm.DynamicAxis;
import edu.tigers.sumatra.rcm.ExtIdentifier;
import edu.tigers.sumatra.rcm.ExtIdentifierParams;
import edu.tigers.sumatra.rcm.POVToButton;
import edu.tigers.sumatra.thread.NamedThreadFactory;
import net.java.games.input.Component;
import net.java.games.input.Controller;


/**
 * @author Clemens
 */
public class ButtonSelectAction
{
	// --------------------------------------------------------------------------
	// --- instance variables ---------------------------------------------------
	// --------------------------------------------------------------------------
	// Logger
	private static final Logger						log			= Logger.getLogger(ButtonSelectAction.class.getName());
																				
	private final Controller							controller;
	private final List<Component>						comps;
	private final List<Component>						unpressedComps;
	private final Map<Component, Float>				compsValueMap;
	private final IIdentifierSelectionObserver	observer;
																
	/** 50ms delay */
	private static final int							DELAY			= 50;
	/** Threshold */
	private static final double						THRESHOLD	= 0.3;
																				
																				
	// --- time for timeout ---
	private final long									startSystemTime;
	private final ScheduledExecutorService			service;
																
																
	// --------------------------------------------------------------------------
	// --- constructor(s) -------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @param observer
	 * @param controller
	 */
	public ButtonSelectAction(final IIdentifierSelectionObserver observer, final Controller controller)
	{
		this.observer = observer;
		this.controller = controller;
		startSystemTime = System.nanoTime();
		
		// --- get all components of current controller ---
		comps = Arrays.asList(controller.getComponents());
		unpressedComps = new ArrayList<Component>(comps);
		compsValueMap = new HashMap<>();
		
		// --- update the controllers components ---
		controller.poll();
		
		for (Component comp : comps)
		{
			compsValueMap.put(comp, comp.getPollData());
		}
		
		service = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ButtonSelection"));
		service.scheduleAtFixedRate(new Polling(), 0, DELAY, TimeUnit.MILLISECONDS);
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private boolean withinTolerance(final double baseValue, final double value)
	{
		return (value < (baseValue + THRESHOLD)) && (value > (baseValue - THRESHOLD));
	}
	
	
	private static class ExtComponent
	{
		private Component			component;
		private ExtIdentifier	extId;
	}
	
	
	private class Polling implements Runnable
	{
		private final List<ExtComponent>	queuedComponents	= new ArrayList<ExtComponent>();
		private final List<ExtComponent>	pressedComponents	= new ArrayList<ExtComponent>();
																			
																			
		@Override
		public void run()
		{
			// --- update the controllers components ---
			controller.poll();
			
			// scan for initial press/move
			List<Component> removeFromUnpressed = new ArrayList<Component>();
			for (Component comp : unpressedComps)
			{
				// POV axis needs special care
				if ((comp.getIdentifier() == Component.Identifier.Axis.POV) && (comp.getPollData() > 0.0001))
				{
					final double povDir = comp.getPollData();
					ExtComponent eComp = new ExtComponent();
					eComp.component = new POVToButton(comp, povDir);
					eComp.extId = new ExtIdentifier(eComp.component.getIdentifier().getName(), ExtIdentifierParams
							.createDefault());
					pressedComponents.add(eComp);
					removeFromUnpressed.add(comp);
					log.info("pov pressed " + povDir);
				}
				// deal with analog axis
				else if (comp.isAnalog())
				{
					if (!withinTolerance(comp.getPollData(), compsValueMap.get(comp)))
					{
						ExtComponent eComp = new ExtComponent();
						eComp.component = comp;
						ExtIdentifierParams params = new ExtIdentifierParams(compsValueMap.get(comp),
								compsValueMap.get(comp), 0);
						eComp.extId = new ExtIdentifier(comp.getIdentifier().getName(), params);
						queuedComponents.add(eComp);
						removeFromUnpressed.add(comp);
						log.info("axis pressed");
					}
				}
				// we most likely have a simple button here
				else if (comp.getPollData() > THRESHOLD)
				{
					ExtComponent eComp = new ExtComponent();
					eComp.component = comp;
					eComp.extId = new ExtIdentifier(comp.getIdentifier().getName(), ExtIdentifierParams
							.createDefault());
					pressedComponents.add(eComp);
					removeFromUnpressed.add(comp);
					log.info("button pressed");
				}
			}
			
			unpressedComps.removeAll(removeFromUnpressed);
			
			for (ExtComponent eComp : queuedComponents)
			{
				Component pressedComponent = eComp.component;
				ExtIdentifier extId = eComp.extId;
				final double minValue = extId.getParams().getMinValue();
				final double maxValue = extId.getParams().getMaxValue();
				double pollData = pressedComponent.getPollData();
				if (Math.abs(minValue - pollData) > Math.abs(minValue - maxValue))
				{
					extId.getParams().setMaxValue(pollData);
				}
				if (withinTolerance(pollData, compsValueMap.get(pressedComponent)))
				{
					log.info("Axis range: [" + minValue + ";" + maxValue + "]");
					eComp.component = new DynamicAxis(pressedComponent, minValue, maxValue);
					pressedComponents.add(eComp);
				}
			}
			
			queuedComponents.removeAll(pressedComponents);
			
			boolean finished = false;
			if (!pressedComponents.isEmpty())
			{
				finished = true;
				for (Component comp : comps)
				{
					double zeroValue = compsValueMap.get(comp);
					if (!withinTolerance(comp.getPollData(), zeroValue))
					{
						finished = false;
						break;
					}
				}
			}
			
			if (finished)
			{
				List<ExtIdentifier> extIds = new ArrayList<ExtIdentifier>(pressedComponents.size());
				for (ExtComponent eComp : pressedComponents)
				{
					extIds.add(eComp.extId);
				}
				observer.onIdentifiersSelected(extIds);
			} else if (((System.nanoTime() - startSystemTime) > 5e9))
			{
				observer.onIdentifiersSelectionCanceled();
				log.info("Button Selection timed out");
			} else
			{
				return;
			}
			service.shutdown();
		}
	}
}