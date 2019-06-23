/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Dec 3, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.GenericManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BotCenterWrapperPresenter implements ISumatraViewPresenter
{
	private static final Logger	log					= Logger.getLogger(BotCenterWrapperPresenter.class.getName());
	private BotCenterPresenter		presenterV1			= null;
	private BotCenterPresenterV2	presenterV2			= null;
	private ISumatraViewPresenter	selectedPresenter	= null;
	private final JPanel				panelWrapper		= new JPanel();
	private final WrapperView		wrapperView			= new WrapperView();
	
	
	/**
	 * 
	 */
	public BotCenterWrapperPresenter()
	{
		panelWrapper.setLayout(new BorderLayout());
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				ABotManager botManager;
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					if (botManager instanceof GenericManager)
					{
						if (presenterV1 == null)
						{
							presenterV1 = new BotCenterPresenter();
						}
						selectedPresenter = presenterV1;
					} else
					{
						if (presenterV2 == null)
						{
							presenterV2 = new BotCenterPresenterV2();
						}
						selectedPresenter = presenterV2;
					}
					panelWrapper.add(selectedPresenter.getComponent());
					panelWrapper.repaint();
				} catch (ModuleNotFoundException err)
				{
					log.error("Botmanager not found", err);
				}
				break;
			case NOT_LOADED:
				break;
			case RESOLVED:
				panelWrapper.removeAll();
				panelWrapper.repaint();
				break;
			default:
				break;
		}
		if (selectedPresenter != null)
		{
			selectedPresenter.onModuliStateChanged(state);
		}
	}
	
	
	@Override
	public Component getComponent()
	{
		return panelWrapper;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return wrapperView;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
		if (selectedPresenter != null)
		{
			selectedPresenter.onEmergencyStop();
		}
	}
	
	private class WrapperView implements ISumatraView
	{
		
		@Override
		public List<JMenu> getCustomMenus()
		{
			if (selectedPresenter != null)
			{
				return selectedPresenter.getSumatraView().getCustomMenus();
			}
			return Collections.emptyList();
		}
		
		
		@Override
		public void onShown()
		{
			if (selectedPresenter != null)
			{
				selectedPresenter.getSumatraView().onShown();
			}
		}
		
		
		@Override
		public void onHidden()
		{
			if (selectedPresenter != null)
			{
				selectedPresenter.getSumatraView().onHidden();
			}
		}
		
		
		@Override
		public void onFocused()
		{
			if (selectedPresenter != null)
			{
				selectedPresenter.getSumatraView().onFocused();
			}
		}
		
		
		@Override
		public void onFocusLost()
		{
			if (selectedPresenter != null)
			{
				selectedPresenter.getSumatraView().onFocusLost();
			}
		}
	}
}
