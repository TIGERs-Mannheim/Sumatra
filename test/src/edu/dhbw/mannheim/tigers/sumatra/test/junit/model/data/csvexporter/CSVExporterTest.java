/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.02.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.data.csvexporter;

import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.model.data.csvexporter.CSVExporter;


/**
 * simple JUnit test class for the {@link CSVExporter}
 * 
 * @author DanielW
 * 
 */
public class CSVExporterTest
{
	/**
	 */
	@Test
	public void testCSVExporter()
	{
		CSVExporter.createInstance("test", "testexport", true);
		final CSVExporter exporter = CSVExporter.getInstance("test");
		
		
		exporter.setHeader("first", "second", "third");
		
		
		exporter.addValues("1", "3", "hallo");
		exporter.addValues("3", "44", "goodbye");
		
		exporter.close();
	}
	
}
