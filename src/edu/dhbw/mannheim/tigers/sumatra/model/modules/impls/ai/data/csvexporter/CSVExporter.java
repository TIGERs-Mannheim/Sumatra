/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.02.2011
 * Author(s): DanielW
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.data.csvexporter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


/**
 * With {@link CSVExporter} you can export user-defined values to csv-files on disc
 * Usage:
 * create an instance
 * optionally set a header
 * add values as often as applicable
 * close the instance
 * 
 * <pre>
 * CSVExporter.createInstance(&quot;test&quot;, &quot;testexport&quot;, true);
 * CSVExporter exporter = CSVExporter.getInstance(&quot;test&quot;);
 * 
 * exporter.setHeader(&quot;first&quot;, &quot;second&quot;, &quot;third&quot;);
 * 
 * exporter.addValues(&quot;1&quot;, &quot;3&quot;, &quot;hallo&quot;);
 * exporter.addValues(&quot;3&quot;, &quot;44&quot;, &quot;goodbye&quot;);
 * 
 * exporter.close();
 * </pre>
 * 
 * calls to createInstance and addValues can be distributed to different classes
 * for example an instance is created in a role, but values are added from a skill
 * 
 * @author DanielW
 * 
 */
public class CSVExporter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private boolean									autoIncrement	= false;
	private String										export			= "export";
	private Queue<String>							values			= new LinkedList<String>();
	private Queue<String>							header			= new LinkedList<String>();
	private File										f;
	BufferedWriter										fileWriter;
	private boolean									writeHeader		= false;
	private String										delimiter		= ",";
	private int											headerSize		= 0;
	private String										id;
	
	private boolean									isClosed			= false;
	
	private static Map<String, CSVExporter>	instances		= new HashMap<String, CSVExporter>();
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * get an instance of {@link CSVExporter} with the specified id
	 * @param id id of the instance
	 * @return the instance
	 */
	public static CSVExporter getInstance(String id) throws CSVExporterException
	{
		if (instances.containsKey(id))
			return instances.get(id);
		else
			return null;
		// throw new CSVExporterException(id + " is not a known instance", null);
	}
	

	/**
	 * get an instance of {@link CSVExporter} with the specified id
	 * or creates one with the default filename "export.csv"
	 * 
	 * @param id
	 * @return the instance
	 */
	public static CSVExporter createOrGetInstance(String id)
	{
		if (!instances.containsKey(id))
			instances.put(id, new CSVExporter(id, "export", true));
		
		return instances.get(id);
	}
	

	/**
	 * 
	 * creates a new {@link CSVExporter} instance
	 * 
	 * @param id
	 * @param fileName base name of the csv-file (without ".csv")
	 * @param autoIncrement if true a counter is added to the filename which increments each time a new instance is
	 *           created (a new measurement is started) else the file is overwritten
	 * @throws CSVExporterException
	 */
	public static void createInstance(String id, String fileName, boolean autoIncrement) throws CSVExporterException
	{
		if (instances.containsKey(id))
			// throw new CSVExporterException(id + " is already there", null);
			return;
		instances.put(id, new CSVExporter(id, fileName, autoIncrement));
	}
	

	/**
	 * 
	 * creates a new {@link CSVExporter} instance
	 * 
	 * @param id
	 * @param fileName base name of the csv-file (without ".csv")
	 * @throws CSVExporterException
	 */
	public static void createInstance(String id, String fileName) throws CSVExporterException
	{
		createInstance(id, fileName, false);
	}
	

	private CSVExporter(String id, String export, boolean autoIncrement)
	{
		this.id = id;
		this.export = export;
		this.autoIncrement = autoIncrement;
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	

	/**
	 * adds a new data set to a file
	 * @param values list of values. note: count of values has to match the header
	 */
	public void addValues(Object... values)
	{
		this.values.clear();
		for (Object object : values)
		{
			this.values.add(object.toString());
		}
		persistRecord();
	}
	

	/**
	 * set the csv header (first row of the file)
	 * as soon a header is set, it will be written to the file
	 * 
	 * @param header note: value count has to match header count
	 */
	public void setHeader(String... header)
	{
		this.header.clear();
		for (String string : header)
		{
			this.header.add(string);
		}
		writeHeader = true;
		headerSize = this.header.size();
	}
	

	/**
	 * do the actual persiting stuff
	 * 
	 */
	private void persistRecord()
	{
		try
		{
			if (f == null)
			{
				int counter = 0;
				if (autoIncrement)
				{
					while ((f = new File("logs/" + export + counter++ + ".csv")).exists())
					{
					}
				} else
				{
					f = new File("logs/" + export + ".csv");
				}
				fileWriter = new BufferedWriter(new FileWriter(f));
				
				if (writeHeader)
				{
					fileWriter.write("#Sumatra CSVExporter\n");
					fileWriter.write("#" + new Date().toString() + "\n");
					
					fileWriter.write(header.poll());
					for (String s : header)
					{
						fileWriter.write(delimiter + s);
					}
					fileWriter.write("\n");
					fileWriter.flush();
				}
				
			}
			if (writeHeader && headerSize != values.size())
				throw new CSVExporterException("object count on values must match header", null);
			
			fileWriter.write(values.poll());
			for (String s : values)
			{
				fileWriter.write(delimiter + s);
			}
			fileWriter.write("\r\n");
			fileWriter.flush();
			
		} catch (FileNotFoundException err)
		{
			throw new CSVExporterException("file not found", err);
		} catch (IOException err)
		{
			throw new CSVExporterException("io error", err);
		}
		
	}
	

	/**
	 * closes the file stream.
	 * do not forget to call this method when you are done
	 * 
	 */
	public void close()
	{
		if (fileWriter != null)
		{
			try
			{
				fileWriter.close();
			} catch (IOException err)
			{
				throw new CSVExporterException("io error while closing the file", err);
			}
		}
		instances.remove(id);
		isClosed = true;
	}
	

	public boolean isClosed()
	{
		return isClosed;
	}
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
