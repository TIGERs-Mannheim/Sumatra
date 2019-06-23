/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.views;

import java.awt.Component;

import javax.swing.JPanel;


/**
 * A dummy view class to handle removed sumatra views
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class DummyView extends ASumatraView implements ISumatraViewPresenter
{
	private final ISumatraView view = new DummySumatraView();
	
	
	/**
	 * @param type
	 */
	public DummyView(final ESumatraViewType type)
	{
		super(type);
	}
	
	
	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return this;
	}
	
	
	private class DummySumatraView implements ISumatraView
	{
	}
	
	private class DummySumatraViewPresenter implements ISumatraViewPresenter
	{
		private final JPanel panel = new JPanel();
		
		
		@Override
		public Component getComponent()
		{
			return panel;
		}
		
		
		@Override
		public ISumatraView getSumatraView()
		{
			return view;
		}
	}
}
