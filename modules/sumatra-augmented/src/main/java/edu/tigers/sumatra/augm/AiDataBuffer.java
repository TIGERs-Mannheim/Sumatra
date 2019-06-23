/*
 * Copyright (c) 2009 - 2016, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.augm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperContainerProtos.AugmWrapperContainer;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos;
import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.AugmWrapper;


/**
 * Buffered reading and writing of AI data
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class AiDataBuffer implements List<AugmWrapper>
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(AiDataBuffer.class
			.getName());
	
	private static final int DEFAULT_BUFFER_SIZE = 5000;
	private static final int CACHE_SIZE = 5;
	private int bufferSize = 0;
	private int currentChunkIdx = 0;
	private AugmWrapperContainer.Builder currentChunk;
	private int currentStartIdx = 0;
	private int size;
	private boolean modified = false;
	private final LinkedHashMap<Integer, AugmWrapper> cache = new LinkedHashMap<>();
	
	
	private static final String FILENAME_BASE = "aiData.bin";
	private final String baseDir;
	
	private AugmWrapper firstElement = null;
	private AugmWrapper lastElement = null;
	
	
	/**
	 * @param dir
	 * @throws IOException
	 */
	public AiDataBuffer(final String dir) throws IOException
	{
		log.debug("Creating aiData buffer");
		baseDir = dir;
		File bdf = new File(baseDir);
		if (!bdf.isDirectory() && !bdf.mkdirs())
		{
			throw new IOException("Could not access base dir: " + dir);
		}
		currentChunk = loadChunk(currentChunkIdx);
		log.debug("Determining total elements");
		size = determineSize();
		log.debug("Found " + size + " elements.");
		if (bufferSize == 0)
		{
			bufferSize = DEFAULT_BUFFER_SIZE;
		}
	}
	
	
	private int determineSize() throws IOException
	{
		int numChunks = (int) Files.list(Paths.get(baseDir))
				.filter(p -> p.toFile().getName().endsWith(FILENAME_BASE))
				.count();
		if (numChunks > 0)
		{
			AugmWrapperContainer container = readWrapper(Paths.get(getChunkFilename(numChunks - 1)));
			return ((numChunks - 1) * bufferSize) + container.getWrapperCount();
		}
		return 0;
	}
	
	
	private AugmWrapperContainer.Builder loadChunk(final int chunkIdx)
	{
		saveChunk();
		File f = new File(getChunkFilename(chunkIdx));
		AugmWrapperContainer.Builder container;
		if (f.exists())
		{
			Path path = Paths.get(f.getAbsolutePath());
			container = readWrapper(path).toBuilder();
			if (chunkIdx == 0)
			{
				firstElement = container.getWrapper(0);
			}
		} else
		{
			container = AugmWrapperContainer.newBuilder();
		}
		currentChunkIdx = chunkIdx;
		return container;
	}
	
	
	private String getChunkFilename(final int chunkIdx)
	{
		return String.format("%s/%05d_%s", baseDir, chunkIdx, FILENAME_BASE);
	}
	
	
	private void saveChunk()
	{
		if (modified)
		{
			String chunkName = getChunkFilename(currentChunkIdx);
			log.debug("Saving chunk to " + chunkName);
			try
			{
				Files.write(Paths.get(chunkName), currentChunk.build().toByteArray());
				log.debug("Saved chunk to " + chunkName);
			} catch (IOException err)
			{
				log.error("Could not write chunk!", err);
			}
			modified = false;
		}
	}
	
	
	private AugmWrapperContainer readWrapper(final Path p)
	{
		try
		{
			log.debug("Loading " + p.toFile().getAbsolutePath());
			AugmWrapperContainer container = AugmWrapperContainer.parseFrom(Files.readAllBytes(p));
			log.debug("Loaded " + p.toFile().getAbsolutePath());
			if (bufferSize == 0)
			{
				bufferSize = container.getWrapperCount();
				log.debug("Setting bufferSize to " + bufferSize);
			}
			return container;
		} catch (InvalidProtocolBufferException err)
		{
			log.error("Could not parse ai data", err);
			throw new IllegalStateException(err);
		} catch (IOException err)
		{
			log.error("Could not read ai data", err);
			throw new IllegalStateException(err);
		}
	}
	
	
	private void loadChunkForIndex(final int i)
	{
		int chunkIdx = currentChunkIdx;
		while (getRelativeIndex(i) < 0)
		{
			chunkIdx--;
			currentStartIdx -= bufferSize;
			if ((currentStartIdx < 0) || (chunkIdx < 0))
			{
				currentStartIdx = 0;
				throw new IndexOutOfBoundsException();
			}
		}
		while (getRelativeIndex(i) >= bufferSize)
		{
			chunkIdx++;
			currentStartIdx += bufferSize;
		}
		if (chunkIdx != currentChunkIdx)
		{
			currentChunk = loadChunk(chunkIdx);
		}
	}
	
	
	private int getRelativeIndex(final int i)
	{
		return i - currentStartIdx;
	}
	
	
	/**
	 * Save any unwritten data
	 */
	public void flush()
	{
		saveChunk();
	}
	
	
	@Override
	public AugmWrapper get(final int i)
	{
		if ((i < 0) || (i >= size))
		{
			throw new IndexOutOfBoundsException(Integer.toString(i));
		}
		if ((i == (size - 1)) && (lastElement != null))
		{
			return lastElement;
		}
		if ((i == 0) && (firstElement != null))
		{
			return firstElement;
		}
		if (cache.containsKey(i))
		{
			return cache.get(i);
		}
		loadChunkForIndex(i);
		AugmWrapper element = currentChunk.getWrapper(getRelativeIndex(i));
		if (i == (size - 1))
		{
			lastElement = element;
		}
		cache.put(i, element);
		if (cache.size() > CACHE_SIZE)
		{
			cache.remove(cache.keySet().iterator().next());
		}
		return element;
	}
	
	
	@Override
	public boolean isEmpty()
	{
		return currentChunk.getWrapperCount() == 0;
	}
	
	
	@Override
	public int size()
	{
		return size;
	}
	
	
	@Override
	public Iterator<AugmWrapper> iterator()
	{
		return new MyIterator();
	}
	
	
	@Override
	public boolean add(final AugmWrapper e)
	{
		int nextIdx = size;
		loadChunkForIndex(nextIdx);
		currentChunk.addWrapper(e);
		size++;
		modified = true;
		if (nextIdx == 0)
		{
			firstElement = e;
		}
		lastElement = e;
		return true;
	}
	
	
	@Override
	public boolean addAll(final Collection<? extends AugmWrapper> c)
	{
		for (AugmWrapper w : c)
		{
			add(w);
		}
		return true;
	}
	
	
	@Override
	public void clear()
	{
		currentChunk = AugmWrapperContainer.newBuilder();
		size = 0;
		currentChunkIdx = 0;
		currentStartIdx = 0;
		modified = true;
		try
		{
			Files.list(Paths.get(baseDir)).filter(p -> p.toFile().isFile()).forEach(p -> {
				if (!p.toFile().delete())
				{
					log.warn("Not able to delete file: " + p.toString());
				}
			});
		} catch (IOException err)
		{
			log.error("Could not delete obsolete files", err);
		}
	}
	
	
	@Override
	public AugmWrapper remove(final int index)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean removeAll(final Collection<?> c)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean addAll(final int index, final Collection<? extends AugmWrapper> c)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean containsAll(final Collection<?> c)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public AugmWrapper set(final int index, final AugmWrapper element)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public void add(final int index, final AugmWrapper element)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean remove(final Object o)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public boolean contains(final Object o)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public Object[] toArray()
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public <T> T[] toArray(final T[] a)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public int indexOf(final Object o)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public int lastIndexOf(final Object o)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public ListIterator<AugmWrapper> listIterator()
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public ListIterator<AugmWrapper> listIterator(final int index)
	{
		throw new NotImplementedException();
	}
	
	
	@Override
	public List<AugmWrapper> subList(final int fromIndex, final int toIndex)
	{
		throw new NotImplementedException();
	}
	
	
	/**
	 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
	 */
	public class MyIterator implements Iterator<AugmWrapper>
	{
		int i = 0;
		
		
		@Override
		public boolean hasNext()
		{
			return i < (size - 1);
		}
		
		
		@Override
		public AugmWrapperProtos.AugmWrapper next()
		{
			if (i >= (size - 1))
			{
				throw new NoSuchElementException(Integer.toString(i));
			}
			i++;
			return get(i);
		}
	}
}
