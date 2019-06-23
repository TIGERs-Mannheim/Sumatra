/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref.presenter;

import org.apache.log4j.Logger;

import edu.tigers.autoreferee.AutoRefUtil;
import edu.tigers.autoreferee.IAutoRefFrame;
import edu.tigers.autoreferee.IAutoRefStateObserver;
import edu.tigers.autoreferee.module.AutoRefState;
import edu.tigers.moduli.listenerVariables.ModulesState;
import edu.tigers.sumatra.drawable.ShapeMap.IShapeLayer;
import edu.tigers.sumatra.visualizer.VisualizerPresenter;
import edu.tigers.sumatra.visualizer.view.field.EShapeLayerSource;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisualizerRefPresenter extends VisualizerPresenter implements IAutoRefStateObserver
{
	@SuppressWarnings("unused")
	private static final Logger	log				= Logger.getLogger(VisualizerRefPresenter.class.getName());
	
	private IAutoRefFrame			latestRefFrame	= null;
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		super.onModuliStateChanged(state);
		switch (state)
		{
			case ACTIVE:
				AutoRefUtil.ifAutoRefModulePresent(ref -> ref.addObserver(this));
				break;
			case RESOLVED:
				AutoRefUtil.ifAutoRefModulePresent(ref -> ref.removeObserver(this));
				break;
			case NOT_LOADED:
			default:
				break;
		}
	}
	
	
	@Override
	public void onAutoRefStateChanged(final AutoRefState state)
	{
		if (state != AutoRefState.RUNNING)
		{
			// To clear out all old drawings
			latestRefFrame = null;
		}
	}
	
	
	@Override
	public void onNewAutoRefFrame(final IAutoRefFrame frame)
	{
		latestRefFrame = frame;
	}
	
	
	@Override
	protected void updateVisFrameShapes()
	{
		super.updateVisFrameShapes();
		
		if (latestRefFrame == null)
		{
			getPanel().getFieldPanel().clearField(EShapeLayerSource.AUTOREFEREE);
		} else
		{
			for (IShapeLayer sl : latestRefFrame.getShapes().getAllShapeLayers())
			{
				getPanel().getOptionsMenu().addMenuEntry(sl);
			}
			getPanel().getFieldPanel().setShapeMap(EShapeLayerSource.AUTOREFEREE, latestRefFrame.getShapes(),
					false);
		}
	}
}
