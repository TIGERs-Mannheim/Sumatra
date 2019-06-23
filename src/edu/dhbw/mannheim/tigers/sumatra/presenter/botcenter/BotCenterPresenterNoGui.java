/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.09.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter;

import java.awt.Component;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.IModuliStateObserver;
import edu.dhbw.mannheim.tigers.sumatra.presenter.moduli.ModuliStateAdapter;
import edu.dhbw.mannheim.tigers.sumatra.view.main.ISumatraView;
import edu.moduli.listenerVariables.ModulesState;


/**
 * This class is the basic presenter
 * 
 * @author Gero
 * 
 */
public class BotCenterPresenterNoGui implements IBotCenterPresenter, ISumatraView, IModuliStateObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int	ID				= 2;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotCenterPresenterNoGui()
	{
		// Register at moduli-observer to be prepared to react on moduli-state changes
		ModuliStateAdapter.getInstance().addObserver(this);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public void onModuliStateChanged(ModulesState state)
	{
		switch (state)
		{
			case ACTIVE:
				break;
			

			default:
				break;
		}
	}
	

	@Override
	public void reloadNode(DefaultMutableTreeNode node)
	{
	}
	

	@Override
	public int getID()
	{
		return ID;
	}
	

	@Override
	public String getTitle()
	{
		return null;
	}
	

	@Override
	public Component getViewComponent()
	{
		return null;
	}
	

	@Override
	public List<JMenu> getCustomMenus()
	{
		return null;
	}
	

	@Override
	public void onShown()
	{
	}
	

	@Override
	public void onHidden()
	{
	}
	

	@Override
	public void onFocused()
	{
	}
	

	@Override
	public void onFocusLost()
	{
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
