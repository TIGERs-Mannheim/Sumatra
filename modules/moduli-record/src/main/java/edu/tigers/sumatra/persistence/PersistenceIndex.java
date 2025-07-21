/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;


/**
 * The index file stores the information where in the database file data corresponding to a specific key is stored.
 * The file consists of a series of long value pairs (long key, long address),
 * with each pair corresponding to one entry, sorted by ascending address order.
 * Multiple entries are permitted per key (resolution has to be done by the PersistenceTable.IEntry.merge method).
 */
@Log4j2
public class PersistenceIndex
{

	private final Path path;
	private final DataOutputStream appendStream;

	// Lazy loaded as it can get large over time (~11 MiB one simulation halftime measured)
	private NavigableMap<Long, List<Range>> map = null;

	private final FileChannel db;


	PersistenceIndex(Path path, FileChannel db)
			throws IOException
	{
		this.path = path;
		this.db = db;
		this.appendStream = new DataOutputStream(
				Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
		);
	}


	public NavigableMap<Long, List<Range>> get()
	{
		if (map != null)
		{
			return map;
		}

		NavigableMap<Long, List<Range>> index = new TreeMap<>();

		try (DataInputStream stream = new DataInputStream(new BufferedInputStream(
				Files.newInputStream(path, StandardOpenOption.READ)
		)))
		{
			long key = 0;
			long address = -1;

			// repeated FileInputStream.available() is too slow
			for (long available = stream.available(); available > 0; available -= 2 * Long.BYTES)
			{
				long nextKey = stream.readLong();
				long nextAddress = stream.readLong();

				if (address != -1)
				{
					index.computeIfAbsent(
							key, k -> new ArrayList<>(1)
					).add(new Range(address, (int) (nextAddress - address)));
				}

				key = nextKey;
				address = nextAddress;
			}

			if (address != -1)
			{
				index.computeIfAbsent(
						key, k -> new ArrayList<>(1)
				).add(new Range(address, (int) (db.size() - address)));
			}
		} catch (IOException e)
		{
			log.error("Could not read index", e);
		}

		this.map = index;
		return map;
	}


	public void append(long key, long address) throws IOException
	{
		this.map = null; // Invalidate index

		appendStream.writeLong(key);
		appendStream.writeLong(address);
	}


	public void close()
	{
		try
		{
			appendStream.close();
		} catch (IOException e)
		{
			log.error("Could not close db", e);
		}
	}


	public record Range(long address, int size)
	{
	}
}
