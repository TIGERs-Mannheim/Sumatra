package edu.tigers.sumatra.drawable.animated;

import java.awt.Color;


/**
 * Animates colors.
 * 
 * @author AndreR <andre@ryll.cc>
 */
@FunctionalInterface
public interface IColorAnimator
{
	/**
	 * Get the current color.
	 * 
	 * @return
	 */
	Color getColor();
}
