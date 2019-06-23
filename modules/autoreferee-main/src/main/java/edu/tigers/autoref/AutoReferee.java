/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.autoref;

import java.awt.*;

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
