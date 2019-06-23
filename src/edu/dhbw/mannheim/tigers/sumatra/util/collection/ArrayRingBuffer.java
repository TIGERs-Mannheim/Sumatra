/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 28.07.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.util.collection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;


/**
 * This class is meant to implement a fast ring-buffer-fifo using a static, wrapped array as store. <code>Null</code> is
 * not permitted, and only the very basic operations necessary for a FIFO are implemented.
 * <strong>NOTE:</strong> This implementation is fast; but as well has some drawbacks not only in functionality, but
 * also in speed of some other functions. Be sure to read the doc carefully!
 * 
 * @author Gero
 */
public class ArrayRingBuffer<D> implements Deque<D>, Serializable
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final long	serialVersionUID	= -2382323998531200972L;
	

	private final D[]				store;
	
	/**
	 * The object used to synchronize access to the {@link #store}. Receives a .notifyAll() in case store has been filled
	 */
	public final Object			sync					= new Object();
	
	private final int				size;
	/** Points on the first element in the list */
	private int						head					= 0;
	/** Points on the last element in the list */
	private int						last					= 0;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	// Allowed here, obviously...
	public ArrayRingBuffer(int size)
	{
		this.store = (D[]) new Object[size];
		this.size = size;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @return New front
	 */
	private int front()
	{
		return dec(head);
	}
	

	private int dec(int pos)
	{
		return (head - 1 + size) % size;
	}
	

	/**
	 * @return New head
	 */
	private int incHead()
	{
		head = inc(head);
		return head;
	}
	

	private int inc(int pos)
	{
		return (pos + 1) % size;
	}
	

	private boolean addAtFront(D param)
	{
		synchronized (sync)
		{
			boolean result = false;
			if (param != null)
			{
				final boolean wasEmpty = store[head] == null;
				int newHead = front();
				
				if (store[newHead] == null)
				{
					store[newHead] = param;
					head = newHead;
					if (wasEmpty)
					{
						last = newHead;
					}
				} else
				{
					// Buffer was full, so we have to turn
					store[newHead] = param;
					head = newHead;
					last = front(); // Get position before new front
				}
				result = true;
				sync.notifyAll(); // In case any derived class is listening for the buffer to be filled...
			}
			
			return result;
		}
	}
	

	private D removeFirstElement()
	{
		synchronized (sync)
		{
			D result = store[head];
			if (result != null)
			{
				store[head] = null;
				
				if (head == last)
				{
					// Only one element, shift head AND last
					last = incHead();
				} else
				{
					// More then one element, shift head only
					incHead();
				}
			}
			
			return result;
		}
	}
	

	@Override
	public boolean isEmpty()
	{
		synchronized (sync)
		{
			return store[head] == null;
		}
	}
	

	@Override
	public Object[] toArray()
	{
		synchronized (sync)
		{
			Object[] result = new Object[size()];
			fillArray(result);
			return result;
		}
	}
	

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] arr)
	{
		synchronized (sync)
		{	
			if (arr.length >= size()) {
				fillArray(arr);	//System.arraycopy(store, 0, arr, 0, size());
			} else
			{
				try
				{
					arr = (T[]) Arrays.copyOf(store, store.length, arr.getClass());
					fillArray(arr);
				} catch (ClassCastException cce)
				{
					throw new ArrayStoreException();
				}
			}
			
			return arr;
		}
	}
	
	
	private void fillArray(Object[] arr)
	{
		Iterator<D> it = iterator();
		int i = 0;
		while (it.hasNext())
		{
			arr[i] = it.next();
			i++;
		}
	}
	

	@Override
	public boolean containsAll(Collection<?> paramCollection)
	{
		synchronized (sync)
		{
			boolean result = true;
			for (Object data : paramCollection)
			{
				boolean found = data.equals(store[head]);
				for (int i = inc(head); i != head; i = inc(i))
				{
					if (data.equals(store[i]))
					{
						found = true;
						break;
					}
				}
				
				if (!found)
				{
					result = false;
					break;
				}
			}
			return result;
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean addAll(Collection<? extends D> paramCollection)
	{
		throw new UnsupportedOperationException("addAll(Objects) is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean removeAll(Collection<?> paramCollection)
	{
		throw new UnsupportedOperationException("removeAll(Objects) is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean retainAll(Collection<?> paramCollection)
	{
		throw new UnsupportedOperationException("retainAll(Objects) is not supported!");
	}
	

	@Override
	public void clear()
	{
		synchronized (sync)
		{
			Iterator<D> it = iterator();
			int i = 0;
			while(it.hasNext()) {
				store[i] = null;
				it.next();
				i++;
			}
			
			head = 0;
			last = 0;
		}
	}
	

	@Override
	public void addFirst(D paramE)
	{
		synchronized (sync)
		{
			if (paramE == null)
			{
				throw new NullPointerException("Argument may not be null!");
			}
			
			addAtFront(paramE);
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public void addLast(D paramE)
	{
		throw new UnsupportedOperationException("addLast is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean offerFirst(D paramE)
	{
		throw new UnsupportedOperationException("offerFirst is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean offerLast(D paramE)
	{
		throw new UnsupportedOperationException("offerLast is not supported!");
	}
	

	@Override
	public D removeFirst()
	{
		synchronized (sync)
		{
			D result = pollFirst();
			if (result == null)
			{
				throw new NoSuchElementException("At: [" + head + "]");
			}
			
			return result;
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public D removeLast()
	{
		throw new UnsupportedOperationException("removeLast is not supported!");
	}
	

	@Override
	public D pollFirst()
	{
		synchronized (sync)
		{
			return removeFirstElement();
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public D pollLast()
	{
		throw new UnsupportedOperationException("pollLast is not supported!");
	}
	

	@Override
	public D getFirst()
	{
		synchronized (sync)
		{
			D result = store[head];
			if (result == null)
			{
				throw new NoSuchElementException("At: " + head);
			}
			return result;
		}
	}
	

	@Override
	public D getLast()
	{
		synchronized (sync)
		{
			D result = store[last];
			if (result == null)
			{
				throw new NoSuchElementException("At: " + last);
			}
			return result;
		}
	}
	

	@Override
	public D peekFirst()
	{
		synchronized (sync)
		{
			return store[head];
		}
	}
	

	@Override
	public D peekLast()
	{
		synchronized (sync)
		{
			return store[last];
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean removeFirstOccurrence(Object paramObject)
	{
		throw new UnsupportedOperationException("removeFirstOccurrence is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean removeLastOccurrence(Object paramObject)
	{
		throw new UnsupportedOperationException("removeLastOccurrence is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean add(D paramE)
	{
		throw new UnsupportedOperationException("add is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean offer(D paramE)
	{
		throw new UnsupportedOperationException("offer is not supported!");
	}
	

	@Override
	public D remove()
	{
		synchronized (sync)
		{
			return removeFirst();
		}
	}
	

	@Override
	public D poll()
	{
		synchronized (sync)
		{
			return pollFirst();
		}
	}
	

	@Override
	public D element()
	{
		synchronized (sync)
		{
			return getFirst();
		}
	}
	

	@Override
	public D peek()
	{
		synchronized (sync)
		{
			return peekFirst();
		}
	}
	

	@Override
	public void push(D paramE)
	{
		synchronized (sync)
		{
			addFirst(paramE);
		}
	}
	

	@Override
	public D pop()
	{
		synchronized (sync)
		{
			return removeFirst();
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean remove(Object paramObject)
	{
		throw new UnsupportedOperationException("remove(Object) is not supported!");
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public boolean contains(Object paramObject)
	{
		throw new UnsupportedOperationException("contains(Object) is not supported!");
	}
	

	/**
	 * NOT {@link Deque#size()}, but the buffer-size of this ring-buffer!
	 */
	@Override
	public int size()
	{
		synchronized (sync)
		{
			return size;
		}
	}
	

	@Override
	public Iterator<D> iterator()
	{
		synchronized (sync)
		{
			return new RingBufferIterator();
		}
	}
	

	/**
	 * <strong>NOT (YET) SUPPORTED!</strong>
	 */
	@Override
	public Iterator<D> descendingIterator()
	{
		throw new UnsupportedOperationException("Due to implementation details descendingIterator is not supported!");
	}
	
	
	public class RingBufferIterator implements Iterator<D>
	{
		/** The pointer to the current object */
		private int	pos			= head;
		
		/** Maximum number of accesses */
		private int	maxAccesses	= size;
		
		/** Number of accesses the iterator already has performed */
		private int	accesses		= 0;
		
		
		private int incPos()
		{
			pos = inc(pos);
			return pos;
		}
		

		@Override
		public boolean hasNext()
		{
			return accesses < maxAccesses;
		}
		

		@Override
		public D next()
		{
			D result = null;
			if (hasNext())
			{
				result = store[pos];
				incPos();
				accesses++;
			} else
			{
				throw new NoSuchElementException();
			}
			return result;
		}
		

		/**
		 * <strong>NOT (YET) SUPPORTED!</strong>
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Due to implementation details remove is not supported!");
		}
		
	}
}
