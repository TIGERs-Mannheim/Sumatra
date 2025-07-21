/*
 * Copyright (c) 2009 - 2024, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.persistence;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayDeque;
import java.util.Queue;


@Log4j2
public abstract class ABufferedPersistenceRecorder<T extends PersistenceTable.IEntry<T>> implements IPersistenceRecorder
{
	private static final int MAX_BUFFER_SIZE = 10000;
	private final Queue<T> buffer = new ArrayDeque<>();

	private final PersistenceTable<T> table;
	private final Class<T> clazz;


	protected ABufferedPersistenceRecorder(PersistenceDb db, Class<T> clazz)
	{
		this.table = db.getTable(clazz);
		this.clazz = clazz;
	}


	public void queue(T object)
	{
		synchronized (buffer)
		{
			if(buffer.size() >= MAX_BUFFER_SIZE)
			{
				log.warn("Dropped object of type {} due to full buffer.", clazz.getName());
				return;
			}

			buffer.add(object);
		}
	}

	@Override
	public void flush()
	{
		T object;
		synchronized (buffer) {
			object = buffer.poll();
		}
		while(object != null)
		{
			table.write(object);
			synchronized (buffer) {
				object = buffer.poll();
			}
		}
	}
}
