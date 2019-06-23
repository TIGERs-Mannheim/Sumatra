/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.guinotifications;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.SubnodeConfiguration;

import edu.tigers.moduli.AModule;
import edu.tigers.moduli.exceptions.InitModuleException;
import edu.tigers.moduli.exceptions.StartModuleException;
import edu.tigers.sumatra.guinotifications.visualizer.IVisualizerObserver;
import edu.tigers.sumatra.ids.BotID;
import edu.tigers.sumatra.math.vector.IVector2;


/**
 * @author Sebastian Stein <sebastian-stein@gmx.de>
 */
public class GuiNotificationsController extends AModule implements IVisualizerObserver
{
	
	public static final String				MODULE_TYPE	= "GuiNotificationsController";
	public static final String				MODULE_ID	= "gui_notifications_controller";
	
	private List<IVisualizerObserver>	visualizerObservers;
	
	
	/**
	 * Creates a new Controller
	 * 
	 * @param subconfig
	 */
	public GuiNotificationsController(SubnodeConfiguration subconfig)
	{
		// Nothing to do.
	}
	
	
	@Override
	public void initModule() throws InitModuleException
	{
		
		visualizerObservers = new ArrayList<>();
	}
	
	
	@Override
	public void deinitModule()
	{
		
		for (int i = visualizerObservers.size() - 1; i >= 0; i--)
		{
			
			visualizerObservers.remove(i);
		}
		
	}
	
	
	@Override
	public void startModule() throws StartModuleException
	{
		
		// Nothing to do here
	}
	
	
	@Override
	public void stopModule()
	{
		
		// Nothing to do here
	}
	
	
	/**
	 * Adds an visualizer observer
	 * 
	 * @param o
	 */
	public void addVisualizerObserver(IVisualizerObserver o)
	{
		
		visualizerObservers.add(o);
	}
	
	
	/**
	 * Removes an visualizer observer
	 * 
	 * @param o
	 */
	public void removeVisualizerObserver(IVisualizerObserver o)
	{
		
		visualizerObservers.remove(o);
	}
	
	
	@Override
	public void onMoveClick(final BotID botID, final IVector2 pos)
	{
		
		for (IVisualizerObserver o : visualizerObservers)
		{
			
			o.onMoveClick(botID, pos);
		}
	}
	
	
	@Override
	public void onRobotClick(final BotID botID)
	{
		
		for (IVisualizerObserver o : visualizerObservers)
		{
			
			o.onRobotClick(botID);
		}
	}
	
	
	@Override
	public void onHideFromRcm(final BotID botID, final boolean hide)
	{
		
		for (IVisualizerObserver o : visualizerObservers)
		{
			
			o.onHideFromRcm(botID, hide);
		}
	}
}
