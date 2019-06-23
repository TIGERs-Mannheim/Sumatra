/*
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 19.02.2011
 * Author(s): DanielW
 * *********************************************************
 */
package edu.tigers.sumatra.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Stream;

import org.apache.log4j.Logger;


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
 */
public final class CSVExporter
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static final Logger	log				= Logger.getLogger(CSVExporter.class.getName());
	private boolean					autoIncrement	= false;
	private final String				fileName;
	private final Queue<String>	values			= new LinkedList<String>();
	private final Queue<String>	header			= new LinkedList<String>();
	private String						additionalInfo	= "";
	private File						file;
	private BufferedWriter			fileWriter;
	private boolean					writeHeader		= false;
	private static final String	delimiter		= ",";
	private int							headerSize		= 0;
	
	private boolean					isClosed			= false;
	private boolean					append			= false;
	
	
	/**
	 * @param fileName
	 * @param autoIncrement
	 * @param append
	 */
	public CSVExporter(final String fileName, final boolean autoIncrement, final boolean append)
	{
		this.fileName = fileName;
		this.autoIncrement = autoIncrement;
		this.append = append;
	}
	
	
	/**
	 * @param fileName sub-dir and name of exported file without .csv ending
	 * @param autoIncrement
	 */
	public CSVExporter(final String fileName, final boolean autoIncrement)
	{
		this(fileName, autoIncrement, false);
	}
	
	
	/**
	 * @param folder
	 * @param key
	 * @param stream
	 */
	public static void exportList(final String folder, final String key, final Stream<INumberListable> stream)
	{
		CSVExporter exporter = new CSVExporter(folder + "/" + key, false);
		stream.forEach(nl -> exporter.addValues(nl.getNumberList()));
		exporter.close();
	}
	
	
	/**
	 * adds a new data set to a file.
	 * 
	 * @param values list of values. note: count of values has to match the header
	 */
	public void addValues(final Number... values)
	{
		this.values.clear();
		for (final Number f : values)
		{
			this.values.add(String.valueOf(f));
		}
		persistRecord();
	}
	
	
	/**
	 * adds a new data set to a file.
	 * 
	 * @param values list of values. note: count of values has to match the header
	 */
	public void addValues(final List<Number> values)
	{
		this.values.clear();
		for (final Object f : values)
		{
			this.values.add(String.valueOf(f));
		}
		persistRecord();
	}
	
	
	/**
	 * This method writes all content the fields of the given object into the file
	 * 
	 * @param bean
	 * @throws IllegalAccessException If one of the fields of the given bean is not accessible
	 */
	public void addValuesBean(final Object bean) throws IllegalAccessException
	{
		values.clear();
		
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			values.add(String.valueOf(field.get(bean)));
		}
		persistRecord();
	}
	
	
	/**
	 * set the csv header (first row of the file)
	 * as soon a header is set, it will be written to the file
	 * 
	 * @param header note: value count has to match header count
	 */
	public void setHeader(final String... header)
	{
		this.header.clear();
		for (final String string : header)
		{
			this.header.add(string);
		}
		writeHeader = true;
		headerSize = this.header.size();
	}
	
	
	/**
	 * This method uses reflection to get the available field names and fills a header with them
	 * 
	 * @param bean
	 */
	public void setHeaderBean(final Object bean)
	{
		header.clear();
		
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field field : fields)
		{
			header.add(field.getName());
		}
		
		writeHeader = true;
		headerSize = header.size();
	}
	
	
	/**
	 * The additional info will be printed above the header.
	 * Header must be set
	 * 
	 * @param info
	 */
	public void setAdditionalInfo(final String info)
	{
		additionalInfo = info;
	}
	
	
	/**
	 * do the actual persisting stuff
	 */
	private void persistRecord()
	{
		try
		{
			if (file == null)
			{
				File dir = new File(fileName).getParentFile();
				if (!dir.exists())
				{
					boolean created = dir.mkdirs();
					if (!created)
					{
						log.warn("Could not create export dir: " + dir.getAbsolutePath());
					}
				}
				int counter = 0;
				if (autoIncrement)
				{
					while ((file = new File(fileName + counter + ".csv")).exists())
					{
						counter++;
					}
				} else
				{
					file = new File(fileName + ".csv");
				}
				
				fileWriter = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file, append), "UTF-8"));
				
				if (writeHeader)
				{
					fileWriter.write("#Sumatra CSVExporter\n");
					fileWriter.write("#" + new Date().toString() + "\n");
					
					if (!additionalInfo.isEmpty())
					{
						fileWriter.write("#" + additionalInfo + "\n");
					}
					
					fileWriter.write(header.poll());
					for (final String s : header)
					{
						fileWriter.write(delimiter + s);
					}
					fileWriter.write("\n");
					fileWriter.flush();
				}
				
			}
			if (writeHeader && (headerSize != values.size()))
			{
				throw new CSVExporterException("object count on values must match header", null);
			}
			fileWriter.write(values.poll());
			for (final String s : values)
			{
				fileWriter.write(delimiter + s);
			}
			fileWriter.write("\n");
			fileWriter.flush();
			
		} catch (final FileNotFoundException err)
		{
			throw new CSVExporterException("file not found", err);
		} catch (final IOException err)
		{
			throw new CSVExporterException("io error", err);
		}
		
	}
	
	
	/**
	 * @return
	 */
	public String getAbsoluteFileName()
	{
		return new File(fileName + ".csv").getAbsolutePath();
	}
	
	
	/**
	 * closes the file stream.
	 * do not forget to call this method when you are done
	 */
	public void close()
	{
		if (fileWriter != null)
		{
			try
			{
				fileWriter.close();
				log.debug("Saved csv file to " + file.getAbsolutePath());
			} catch (final IOException err)
			{
				throw new CSVExporterException("io error while closing the file", err);
			}
		}
		isClosed = true;
	}
	
	
	/**
	 * @return
	 */
	public boolean isClosed()
	{
		return isClosed;
	}
	
	
	/**
	 * @return
	 */
	public boolean isEmpty()
	{
		return values.isEmpty();
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
