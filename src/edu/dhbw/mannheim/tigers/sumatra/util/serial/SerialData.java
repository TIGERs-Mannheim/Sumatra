/*
 * *********************************************************
 * Copyright (c) 2009 - 2013, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 22.10.2013
 * Author(s): AndreR
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.serial;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation for serial members which will be serialized.
 * 
 * @author AndreR
 * 
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SerialData
{
	/** */
	public enum ESerialDataType
	{
		/** */
		UINT8(1),
		/** */
		UINT16(2),
		/** */
		UINT32(4),
		/** */
		INT8(1),
		/** */
		INT16(2),
		/** */
		INT32(4),
		/** */
		FLOAT16(2),
		/** */
		FLOAT32(4),
		/** */
		TAIL(0),
		/** */
		EMBEDDED(0);
		
		private final int	length;
		
		
		private ESerialDataType(int length)
		{
			this.length = length;
		}
		
		
		/**
		 * 
		 * @return
		 */
		public int getLength()
		{
			return length;
		}
	}
	
	
	/** */
	public ESerialDataType type();
}
