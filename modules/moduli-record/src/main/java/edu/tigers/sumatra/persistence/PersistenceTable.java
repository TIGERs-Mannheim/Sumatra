/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import edu.tigers.sumatra.persistence.serializer.GenericSerializer;
import edu.tigers.sumatra.persistence.serializer.MappedDataOutputStream;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.LongSupplier;


@Log4j2
public class PersistenceTable<T extends PersistenceTable.IEntry<T>> implements AutoCloseable
{
	@Getter
	private final EPersistenceKeyType keyType;

	private final GenericSerializer serializer;

	private final PersistenceIndex index;

	private final MappedDataOutputStream stream;
	private final FileChannel file;

	@Getter
	private final Class<T> type;


	public PersistenceTable(Class<T> type, Path dbPath, EPersistenceKeyType keyType)
			throws IOException
	{
		this.type = type;
		this.keyType = keyType;

		serializer = new GenericSerializer(dbPath.resolve(type.getSimpleName() + ".metadata"));

		Path dbFile = dbPath.resolve(type.getSimpleName() + ".db");
		this.stream = new MappedDataOutputStream(dbFile);
		this.file = FileChannel.open(dbFile, StandardOpenOption.READ);

		this.index = new PersistenceIndex(dbPath.resolve(type.getSimpleName() + ".index"), file);
	}


	public void write(final Collection<T> elements)
	{
		elements.forEach(this::write);
	}


	public void write(T element)
	{
		try
		{
			long id = element.getKey();
			long startIndex = stream.getPos();

			serializer.serialize(stream, element);
			index.append(id, startIndex);
		} catch (RuntimeException | IOException e)
		{
			log.error("Could not write to db", e);
		}
	}


	public int size()
	{
		return index.get().size();
	}


	public void forEach(Consumer<T> consumer)
	{
		for (long key : index.get().navigableKeySet())
		{
			consumer.accept(get(key));
		}
	}


	public List<T> load()
	{
		List<T> events = new ArrayList<>(size());
		forEach(events::add);
		return events;
	}


	@SuppressWarnings("unchecked")
	public synchronized T get(long key)
	{
		if (!index.get().containsKey(key))
		{
			return null;
		}

		try
		{
			T element = null;
			for (PersistenceIndex.Range range : index.get().get(key))
			{
				file.position(range.address());
				ByteBuffer buf = ByteBuffer.allocate(range.size());
				file.read(buf);
				buf.position(0);

				T entry = (T) serializer.deserialize(buf);
				if (element != null)
				{
					element.merge(entry);
				} else
				{
					element = entry;
				}
			}
			return element;
		} catch (RuntimeException | IOException e)
		{
			log.error("Could not read from db", e);
			return null;
		}
	}


	public Long getFirstKey()
	{
		return noSuchElement(index.get()::firstKey);
	}


	public Long getLastKey()
	{
		return noSuchElement(index.get()::lastKey);
	}


	public Long getPreviousKey(long key)
	{
		return index.get().lowerKey(key);
	}


	public Long getNextKey(long key)
	{
		return index.get().higherKey(key);
	}


	public Long getNearestKey(long key)
	{
		Long neighbour = index.get().floorKey(key);
		Long ceil = index.get().ceilingKey(key);
		if (ceil != null && (neighbour == null || Math.abs(ceil - key) < Math.abs(neighbour - key)))
			return ceil;

		return neighbour;
	}


	@Override
	public void close()
	{
		try
		{
			serializer.close();
			stream.close();
			file.close();
			index.close();
		} catch (IOException e)
		{
			log.error("Could not close db", e);
		}
	}


	private Long noSuchElement(LongSupplier supplier)
	{
		try
		{
			return supplier.getAsLong();
		} catch (NoSuchElementException e)
		{
			return null;
		}
	}


	public boolean isSumatraTimestampBased()
	{
		return keyType == EPersistenceKeyType.SUMATRA_TIMESTAMP;
	}


	public interface IEntry<S>
	{
		long getKey();

		default void merge(S other)
		{
			log.warn("Entry merge attempted for class {}", getClass().getName());
		}
	}
}
