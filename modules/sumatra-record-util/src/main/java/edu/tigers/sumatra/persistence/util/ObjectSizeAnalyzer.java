package edu.tigers.sumatra.persistence.util;

import org.openjdk.jol.info.GraphLayout;


public class ObjectSizeAnalyzer
{
	public long getTotalBytes(Object object)
	{
		if (object == null)
		{
			return 0;
		}
		return GraphLayout.parseInstance(object).totalSize();
	}
}
