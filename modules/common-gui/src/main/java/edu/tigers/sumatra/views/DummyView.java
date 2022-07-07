/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.views;

import lombok.Getter;

import javax.swing.JPanel;


/**
 * A dummy view class to handle removed sumatra views
 */
public class DummyView extends ASumatraView implements ISumatraViewPresenter
{
	@Getter
	private final JPanel viewPanel = new JPanel();


	public DummyView(ESumatraViewType type)
	{
		super(type);
	}


	@Override
	protected ISumatraViewPresenter createPresenter()
	{
		return this;
	}
}
