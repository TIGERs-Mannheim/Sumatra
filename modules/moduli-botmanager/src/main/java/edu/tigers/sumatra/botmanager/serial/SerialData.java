/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.10.2013
 * Author(s): AndreR
 * *********************************************************
 */
package edu.tigers.sumatra.botmanager.serial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for serial members which will be serialized.
 * 
 * @author AndreR
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerialData
{
	/**
	 * Data type specification.
	 */
	public enum ESerialDataType
	{
		/** */
		UINT8(1, 0, 255),
		/** */
		UINT16(2, 0, 65535),
		/** */
		UINT32(4, 0, 4294967295L),
		/** */
		INT8(1, -128, 127),
		/** */
		INT16(2, -32768, 32767),
		/** */
		INT32(4, -2147483648L, 2147483647L),
		/** */
		FLOAT16(2, 0, 0),
		/** */
		FLOAT32(4, 0, 0),
		/** */
		TAIL(0, 0, 0),
		/** */
		EMBEDDED(0, 0, 0);
		
		private final int		length;
		private final long	min;
		private final long	max;
		
		
		private ESerialDataType(final int length, final long min, final long max)
		{
			this.length = length;
			this.min = min;
			this.max = max;
		}
		
		
		/**
		 * @return
		 */
		public int getLength()
		{
			return length;
		}
		
		
		/**
		 * @return
		 */
		public long getMin()
		{
			return min;
		}
		
		
		/**
		 * @return
		 */
		public long getMax()
		{
			return max;
		}
	}
	
	
	/**
	 * Get the type of this annotation.
	 */
	public ESerialDataType type();
}
