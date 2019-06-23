/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.bot;

import java.awt.*;

/**
 * Primary robot mode.
 * 
 * @author UlrikeL
 */
public enum ERobotMode {
    /** */
    IDLE(0, Color.YELLOW),
    /** */
    READY(1, Color.GREEN),
    /** */
    LOW_POWER(2, Color.RED),
    /** */
    TEST(3, Color.CYAN);

    private final int		id;
    private final Color	color;


    ERobotMode(final int id, final Color color)
    {
        this.id = id;
        this.color = color;
    }


    /**
     * Get id of source.
     *
     * @return
     */
    public int getId()
    {
        return id;
    }


    /**
     * @return the color
     */
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
    public static ERobotMode getRobotModeConstant(final int id)
    {
        for (ERobotMode s : values())
        {
            if (s.getId() == id)
            {
                return s;
            }
        }

        return IDLE;
    }
}
