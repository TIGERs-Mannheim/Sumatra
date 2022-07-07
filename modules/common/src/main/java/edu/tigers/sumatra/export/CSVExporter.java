/*
 * Copyright (c) 2009 - 2022, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.export;

import edu.tigers.sumatra.data.collector.IExportable;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;


/**
 * With {@link CSVExporter} you can export user-defined values to csv-files on disc.
 */
public final class CSVExporter implements Closeable
{
	private static final Logger log = LogManager.getLogger(CSVExporter.class.getName());
	private static final String EXTENSION = ".csv";
	private static final String DELIMITER = ",";

	private final Path folder;
	private final String fileName;
	private final EMode mode;

	private int numHeaders = 0;
	private FileWriter fileWriter;


	public enum EMode
	{
		EXACT_FILE_NAME,
		APPEND_TO_EXISTING_FILE,
		AUTO_INCREMENT_FILE_NAME,
		APPEND_DATE,
		PREPEND_DATE,
	}


	/**
	 * @param folder       the target folder
	 * @param baseFileName subtract-dir and name of exported file without .csv ending
	 * @param mode         the write mode
	 */
	public CSVExporter(final String folder, final String baseFileName, final EMode mode)
	{
		this.folder = Paths.get(folder);
		this.mode = mode;
		this.fileName = getFileName(baseFileName);
	}


	/**
	 * @param folder       the target folder
	 * @param baseFileName subtract-dir and name of exported file without .csv ending
	 * @param mode         the write mode
	 */
	public CSVExporter(final Path folder, final String baseFileName, final EMode mode)
	{
		this.folder = folder;
		this.mode = mode;
		this.fileName = getFileName(baseFileName);
	}


	private String getFileName(final String baseFileName)
	{
		switch (mode)
		{
			case EXACT_FILE_NAME:
			case APPEND_TO_EXISTING_FILE:
				return baseFileName + EXTENSION;
			case AUTO_INCREMENT_FILE_NAME:
				int counter = 0;
				while (folder.resolve(baseFileName + counter + EXTENSION).toFile().exists())
				{
					counter++;
				}
				return baseFileName + counter + EXTENSION;
			case APPEND_DATE:
			case PREPEND_DATE:
				String dateStr = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(new Date());
				if (StringUtils.isBlank(baseFileName))
				{
					return dateStr;
				}
				if (mode == EMode.PREPEND_DATE)
				{
					return dateStr + "_" + baseFileName + EXTENSION;
				}
				return baseFileName + "_" + dateStr + EXTENSION;
		}
		throw new IllegalStateException("Unhandled mode: " + mode);
	}


	/**
	 * @param folder   the target folder
	 * @param fileName the target file name
	 * @param list     the list of data entries
	 */
	public static void exportCollection(
			final String folder,
			final String fileName,
			final Collection<? extends IExportable> list)
	{
		CSVExporter exporter = new CSVExporter(folder, fileName, EMode.EXACT_FILE_NAME);
		list.stream().findAny().map(IExportable::getHeaders).ifPresent(exporter::setHeader);
		list.stream().map(IExportable::getNumberList).forEach(exporter::addValues);
		exporter.close();
	}


	/**
	 * adds a new data set to a file.
	 *
	 * @param values list of values. note: count of values has to match the header
	 */
	public void addValues(final Collection<?> values)
	{
		try
		{
			if (fileWriter == null)
			{
				open();
			}

			if (numHeaders > 0 && numHeaders != values.size())
			{
				log.warn("Number of headers ({}) and number of values ({}) do not match. ", numHeaders, values.size());
			}
			writeLine(values);
		} catch (final IOException err)
		{
			throw new CSVExporterException("Failed to add values to CSV file", err);
		}
	}


	/**
	 * set the csv headers
	 *
	 * @param headers
	 */
	public void setHeader(Collection<String> headers)
	{
		if (fileWriter != null)
		{
			throw new IllegalStateException("CSV file already opened. Can not change the headers anymore");
		}

		try
		{
			open();
			writeLine(headers);
			numHeaders = headers.size();
		} catch (IOException e)
		{
			throw new CSVExporterException("Could not write headers to CSV file", e);
		}
	}


	private void open() throws IOException
	{
		Files.createDirectories(folder);
		File file = folder.resolve(fileName).toFile();
		fileWriter = new FileWriter(file, mode == EMode.APPEND_TO_EXISTING_FILE);
	}


	private void writeLine(final Collection<?> values) throws IOException
	{
		if (values.isEmpty())
		{
			return;
		}
		fileWriter.write(StringUtils.join(values, DELIMITER));
		fileWriter.write("\n");
		fileWriter.flush();
	}


	/**
	 * @return the absolute path to the csv file
	 */
	public String getAbsoluteFileName()
	{
		return folder.resolve(fileName).toAbsolutePath().toString();
	}


	public String getFileName()
	{
		return fileName;
	}


	@Override
	public void close()
	{
		if (fileWriter != null)
		{
			try
			{
				fileWriter.close();
				fileWriter = null;
			} catch (final IOException err)
			{
				throw new CSVExporterException("Could not close CSV file", err);
			}
		}
		log.debug("Saved csv file to {}", this::getAbsoluteFileName);
	}
}
