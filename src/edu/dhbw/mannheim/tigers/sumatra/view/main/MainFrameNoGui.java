/* 
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 06.08.2010
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.view.main;

import java.util.ArrayList;

import javax.swing.ImageIcon;

/**
 * This is a dummy IMainFrame used with the NOGUI flag.
 * 
 * @author AndreR
 * 
 */
public class MainFrameNoGui implements IMainFrame
{
	@Override
	public void addObserver(IMainFrameObserver o)
	{
	}

	@Override
	public void removeObserver(IMainFrameObserver o)
	{
	}

	@Override
	public void setStartStopButtonState(boolean enable)
	{
	}

	@Override
	public void setStartStopButtonState(boolean enable, ImageIcon icon)
	{
	}

	@Override
	public void loadLayout(String filename)
	{
	}

	@Override
	public void saveLayout(String filename)
	{
	}

	@Override
	public void setMenuLayoutItems(ArrayList<String> names)
	{
	}

	@Override
	public void setMenuModuliItems(ArrayList<String> names)
	{
	}

	@Override
	public void selectModuliItem(String name)
	{
	}

	@Override
	public void selectLayoutItem(String name)
	{
	}

	@Override
	public void setLookAndFeel(String name)
	{
	}

	@Override
	public void addView(ISumatraView view)
	{
	}
}
