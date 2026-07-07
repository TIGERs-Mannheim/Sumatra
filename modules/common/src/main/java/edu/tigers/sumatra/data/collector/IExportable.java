package edu.tigers.sumatra.data.collector;

import edu.tigers.sumatra.export.INumberListable;

import java.util.Collections;
import java.util.List;


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
