/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.ImageIcon;
import java.awt.Image;


/**
 * Util class for scaling images
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ImageScaler
{
	private static ImageIcon scaleImageIcon(final ImageIcon imageIcon, final int width, final int height)
	{
		return scale(imageIcon, width, height, java.awt.Image.SCALE_DEFAULT);
	}


	/**
	 * Scale the image and reduce aliasing artifacts
	 *
	 * @param imageIcon
	 * @param width
	 * @param height
	 * @return
	 */
	public static ImageIcon scaleImageIconSmooth(final ImageIcon imageIcon, final int width, final int height)
	{
		return scale(imageIcon, width, height, java.awt.Image.SCALE_SMOOTH);
	}


	private static ImageIcon scale(final ImageIcon imageIcon, final int width, final int height, final int hints)
	{
		Image image = imageIcon.getImage();
		Image newimg = image.getScaledInstance(width, height, hints);
		return new ImageIcon(newimg);
	}


	/**
	 * Scale an image from resources to default button size
	 *
	 * @param path
	 * @return
	 */
	public static ImageIcon scaleDefaultButtonImageIcon(final String path)
	{
		ImageIcon imageIcon = new ImageIcon(ImageScaler.class.getResource(path));
		return scaleImageIcon(imageIcon, ScalingUtil.getImageButtonSize(), ScalingUtil.getImageButtonSize());
	}


	/**
	 * Scale an image from resources to small button size
	 *
	 * @param path
	 * @return
	 */
	public static ImageIcon scaleSmallButtonImageIcon(final String path)
	{
		ImageIcon imageIcon = new ImageIcon(ImageScaler.class.getResource(path));
		return scaleImageIcon(imageIcon, ScalingUtil.getImageButtonSmallSize(), ScalingUtil.getImageButtonSmallSize());
	}
}
