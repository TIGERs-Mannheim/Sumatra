/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.data.collector;

import java.util.Collections;
import java.util.List;

import edu.tigers.sumatra.export.INumberListable;


/**
 * Interface for exportable data structures
 */
public interface IExportable extends INumberListable
{
	/**
	 * @return the names of the fields returned by {@link INumberListable#getNumberList()}
	 */
	default List<String> getHeaders()
	{
		return Collections.emptyList();
	}
}
