/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Nov 19, 2015
 * Author(s): "Lukas Magel"
 * *********************************************************
 */
package edu.tigers.autoref;

import java.awt.EventQueue;

import edu.tigers.autoref.gui.AutoRefMainPresenter;


/**
 * @author "Lukas Magel"
 */
public class AutoReferee
{
	
	/**
	 * @param args
	 */
	public static void main(final String[] args)
	{
		EventQueue.invokeLater(() -> new AutoRefMainPresenter());
	}
}
