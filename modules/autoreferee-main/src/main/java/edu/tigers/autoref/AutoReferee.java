/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref;

import java.awt.EventQueue;

import edu.tigers.autoref.gui.AutoRefMainPresenter;


/**
 * @author "Lukas Magel"
 */
public final class AutoReferee
{
	
	private AutoReferee()
	{
	}
	
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		EventQueue.invokeLater(AutoRefMainPresenter::new);
	}
}
