/*
 * *********************************************************
 * Copyright (c) 2009 - 2014, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Jan 12, 2014
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util;

import java.awt.Image;

import javax.swing.ImageIcon;


/**
 * Util class for scaling images
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public final class ImageScaler
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/**  */
	public static final int	BUTTON_DEFAULT_SIZE	= 30;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	
	private ImageScaler()
	{
		
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * 
	 * @param imageIcon
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scaleImageIcon(ImageIcon imageIcon, int width, int height)
	{
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_DEFAULT);
		return new ImageIcon(newimg);
	}
	
	
	/**
	 * Scale an image from resources to default button size
	 * 
	 * @param path
	 * @return
	 */
	public static ImageIcon scaleDefaultButtonImageIcon(String path)
	{
		ImageIcon imageIcon = new ImageIcon(ClassLoader.getSystemResource(path));
		return scaleImageIcon(imageIcon, BUTTON_DEFAULT_SIZE, BUTTON_DEFAULT_SIZE);
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
