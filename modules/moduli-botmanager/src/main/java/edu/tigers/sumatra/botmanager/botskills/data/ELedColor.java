package edu.tigers.sumatra.botmanager.botskills.data;

import java.awt.Color;


/**
 * Selectable LED colors for the eyes of the robots.
 */
public enum ELedColor
{
	OFF(0, Color.BLACK),
	RED(1, Color.RED),
	GREEN(2, Color.GREEN),
	BLUE(3, Color.BLUE),
	WHITE(4, Color.WHITE),
	LIGHT_BLUE(5, Color.BLUE.brighter().brighter()),
	SLIGHTLY_ORANGE_YELLOW(6, Color.orange),
	PURPLE(7, Color.magenta),
	
	;
	
	private final int id;
	private final Color color;
	
	
	ELedColor(final int id, final Color color)
	{
		this.id = id;
		this.color = color;
	}
	
	
	public int getId()
	{
		return id;
	}
	
	
	public Color getColor()
	{
		return color;
	}

	/**
	 * Convert an id to an enum.
	 *
	 * @param id
	 * @return enum
	 */
	public static ELedColor getLedColorConstant(final int id)
	{
		for (ELedColor s : values())
		{
			if (s.getId() == id)
			{
				return s;
			}
		}

		return null;
	}
}
