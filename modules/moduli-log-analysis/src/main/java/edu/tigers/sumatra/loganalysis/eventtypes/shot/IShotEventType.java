/*
 * Copyright (c) 2009 - 2019, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.loganalysis.eventtypes.shot;

import edu.tigers.sumatra.drawable.IDrawableShape;
import edu.tigers.sumatra.loganalysis.eventtypes.IEventType;

import java.util.List;

public interface IShotEventType extends IEventType
{

    /**
     * Creates a list of {@link IDrawableShape } from this shot event type to draw it later
     * @return list of shapes for shot
     */
    List<IDrawableShape> getDrawableShotShape();

}
