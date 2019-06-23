/*
 * *********************************************************
 * Copyright (c) 2009 - 2010, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.10.2010
 * Author(s): Gero
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.util.collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer;


/**
 * This test is meant to verify the correct behavior of the class {@link ArrayRingBuffer}
 * 
 * @author Gero
 * 
 */
public class ArrayRingBufferTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	private static final int			SIZE		= 3;
	
	/** Created for every test ({@link #setUp()}) */
	private ArrayRingBuffer<String>	buffer;
	
	private final String					packet1	= "packet1";
	private final String					packet2	= "packet2";
	private final String					packet3	= "packet3";
	private final String					packet4	= "packet4";
	
	
	/**
	 * Called by JUnit before every single test
	 * 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		buffer = new ArrayRingBuffer<String>(SIZE);
	}
	

	/**
	 * Called by JUnit after every single test
	 * 
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception
	{
		buffer = null;
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#isEmpty()}.
	 */
	@Test
	public void testIsEmpty()
	{
		assertTrue(buffer.isEmpty());
		
		buffer.push(packet1);
		buffer.push(packet2);
		assertFalse(buffer.isEmpty());
		
		buffer.push(packet3);
		assertFalse(buffer.isEmpty());
		
		buffer.poll();
		assertFalse(buffer.isEmpty());
		
		buffer.poll();
		assertFalse(buffer.isEmpty());
		
		buffer.poll();
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#toArray()}.
	 */
	@Test
	public void testToArray()
	{
		buffer.push(packet1);
		buffer.push(packet2);
		buffer.push(packet3);
		buffer.push(packet4); // Haha, turn, trick!
		
		Object[] arr = buffer.toArray();
		assertTrue(arr.length == buffer.size());
		
		assertTrue(arr[0] == buffer.poll());
		assertTrue(arr[1] == buffer.poll());
		assertTrue(arr[2] == buffer.poll());
		
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#toArray(T[])}.
	 */
	@Test
	public void testToArrayTArray()
	{
		buffer.push(packet1);
		buffer.push(packet2);
		buffer.push(packet3);
		buffer.push(packet4); // Haha, turn, trick!
		
		String[] arr = buffer.toArray(new String[buffer.size()]);
		assertTrue(arr.length == buffer.size());
		
		assertTrue(arr[0].equals(buffer.poll()));
		assertTrue(arr[1].equals(buffer.poll()));
		assertTrue(arr[2].equals(buffer.poll()));
		
		assertTrue(buffer.isEmpty());
		

		buffer.push(packet1);
		buffer.push(packet2);
		buffer.push(packet3);
		buffer.push(packet4); // Haha, turn, trick!
		
		String[] arr2 = buffer.toArray(new String[0]);
		assertTrue(arr2.length == buffer.size());
		
		assertTrue(arr2[0].equals(buffer.poll()));
		assertTrue(arr2[1].equals(buffer.poll()));
		assertTrue(arr2[2].equals(buffer.poll()));
		
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#containsAll(java.util.Collection)}.
	 */
	@Test
	public void testContainsAll()
	{
		buffer.push(packet4);
		buffer.push(packet1);
		buffer.push(packet3);
		
		List<String> ls1 = new ArrayList<String>();
		ls1.add(packet1);
		ls1.add(packet2);
		assertFalse(buffer.containsAll(ls1));
		
		List<String> ls2 = new ArrayList<String>();
		ls2.add(packet1);
		assertTrue(buffer.containsAll(ls2));
		
		List<String> ls3 = new ArrayList<String>();
		ls3.add(packet3);
		ls3.add(packet4);
		assertTrue(buffer.containsAll(ls3));
		
		List<String> ls4 = new ArrayList<String>();
		ls4.add(packet1);
		ls4.add(packet4);
		ls4.add(packet3);
		assertTrue(buffer.containsAll(ls4));
		
		List<String> ls5 = new ArrayList<String>();
		ls5.add(packet4);
		ls5.add(packet1);
		ls5.add(packet3);
		ls5.add(packet3);
		ls5.add(packet4);
		assertTrue(buffer.containsAll(ls5));
		
		List<String> ls6 = new ArrayList<String>();
		ls6.add(packet4);
		ls6.add(packet1);
		ls6.add(packet3);
		ls6.add(packet3);
		ls6.add(packet4);
		ls6.add(packet2);
		assertFalse(buffer.containsAll(ls6));
		
		List<String> ls7 = new ArrayList<String>();
		ls7.add(packet2);
		assertFalse(buffer.containsAll(ls7));
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#clear()}.
	 */
	@Test
	public void testClear()
	{
		buffer.push(packet1);
		buffer.push(packet2);
		buffer.push(packet3);
		assertFalse(buffer.isEmpty());
		
		buffer.clear();
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for
	 * {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#addFirst(java.lang.Object)}.
	 */
	@Test
	public void testAddFirst()
	{
		try
		{
			buffer.addFirst(null);
			fail("Should have thrown NullPointerException!");
		} catch (NullPointerException npe)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.addFirst(packet2);
		assertFalse(packet1 == buffer.getFirst());
		assertTrue(packet2 == buffer.getFirst());
		
		buffer.removeFirst();
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.addFirst(packet2);
		assertTrue(packet2 == buffer.getFirst());
		
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.getFirst());
		
		buffer.addFirst(packet4);
		assertTrue(packet4 == buffer.getFirst());
		assertTrue(packet2 == buffer.getLast());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#removeFirst()}.
	 */
	@Test
	public void testRemoveFirst()
	{
		try
		{
			buffer.removeFirst();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.removeFirst());
		assertTrue(buffer.isEmpty());
		
		buffer.addFirst(packet1);
		buffer.addFirst(packet2);
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.removeFirst());
		assertTrue(packet2 == buffer.removeFirst());
		assertTrue(packet1 == buffer.removeFirst());
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#pollFirst()}.
	 */
	@Test
	public void testPollFirst()
	{
		assertNull(buffer.pollFirst());
		
		buffer.push(packet1);
		assertTrue(packet1 == buffer.pollFirst());
		
		buffer.push(packet1);
		buffer.push(packet2);
		buffer.push(packet3);
		assertTrue(packet3 == buffer.pollFirst());
		assertTrue(packet2 == buffer.pollFirst());
		assertTrue(packet1 == buffer.pollFirst());
		
		assertNull(buffer.pollFirst());
	}
	

	// /**
	// * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#pollLast()}.
	// */
	// @Test
	// public void testPollLast()
	// {
	// assertNull(buffer.pollLast());
	//
	// buffer.push(packet1);
	// assertTrue(packet1 == buffer.pollLast());
	//
	// buffer.push(packet1);
	// buffer.push(packet2);
	// buffer.push(packet3);
	// assertTrue(packet1 == buffer.pollLast());
	// assertTrue(packet2 == buffer.pollLast());
	// assertTrue(packet3 == buffer.pollLast());
	//
	// assertNull(buffer.pollLast());
	// }
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#getFirst()}.
	 */
	@Test
	public void testGetFirst()
	{
		try
		{
			buffer.getFirst();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.addFirst(packet2);
		assertTrue(packet2 == buffer.getFirst());
		
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.getFirst());
		
		buffer.addFirst(packet4);
		assertTrue(packet4 == buffer.getFirst());
		
		// Empty and test
		buffer.removeFirst();
		buffer.removeFirst();
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		try
		{
			buffer.getFirst();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#getLast()}.
	 */
	@Test
	public void testGetLast()
	{
		try
		{
			buffer.getLast();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.getLast());
		
		buffer.addFirst(packet2);
		assertTrue(packet1 == buffer.getLast());
		
		buffer.addFirst(packet3);
		assertTrue(packet1 == buffer.getLast());
		
		buffer.addFirst(packet4);
		assertTrue(packet2 == buffer.getLast());
		
		// Empty and test
		buffer.removeFirst();
		buffer.removeFirst();
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		try
		{
			buffer.getLast();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#peekFirst()}.
	 */
	@Test
	public void testPeekFirst()
	{
		assertNull(buffer.peekFirst());
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.peekFirst());
		
		buffer.addFirst(packet2);
		assertTrue(packet2 == buffer.peekFirst());
		
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.peekFirst());
		
		buffer.addFirst(packet4);
		assertTrue(packet4 == buffer.peekFirst());
		
		// Empty and test
		buffer.removeFirst();
		assertTrue(packet3 == buffer.peekFirst());
		buffer.removeFirst();
		assertTrue(packet2 == buffer.peekFirst());
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		assertNull(buffer.peekFirst());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#peekLast()}.
	 */
	@Test
	public void testPeekLast()
	{
		assertNull(buffer.peekLast());
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.peekLast());
		
		buffer.addFirst(packet2);
		assertTrue(packet1 == buffer.peekLast());
		
		buffer.addFirst(packet3);
		assertTrue(packet1 == buffer.peekLast());
		
		buffer.addFirst(packet4);
		assertTrue(packet2 == buffer.peekLast());
		
		// Empty and test
		buffer.removeFirst();
		assertTrue(packet3 == buffer.peekFirst());
		buffer.removeFirst();
		assertTrue(packet2 == buffer.peekFirst());
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		assertNull(buffer.peekLast());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#remove()}.
	 */
	@Test
	public void testRemove()
	{
		try
		{
			buffer.remove();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.remove());
		assertTrue(buffer.isEmpty());
		
		buffer.addFirst(packet1);
		buffer.addFirst(packet2);
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.remove());
		assertTrue(packet2 == buffer.remove());
		assertTrue(packet1 == buffer.remove());
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#poll()}.
	 */
	@Test
	public void testPoll()
	{
		assertNull(buffer.poll());
		
		buffer.push(packet1);
		assertTrue(packet1 == buffer.poll());
		
		buffer.push(packet1);
		buffer.push(packet2);
		buffer.push(packet3);
		assertTrue(packet3 == buffer.poll());
		assertTrue(packet2 == buffer.poll());
		assertTrue(packet1 == buffer.poll());
		
		assertNull(buffer.poll());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#element()}.
	 */
	@Test
	public void testElement()
	{
		try
		{
			buffer.element();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.element());
		
		buffer.addFirst(packet2);
		assertTrue(packet2 == buffer.element());
		
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.element());
		
		buffer.addFirst(packet4);
		assertTrue(packet4 == buffer.element());
		
		// Empty and test
		buffer.removeFirst();
		buffer.removeFirst();
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		try
		{
			buffer.element();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#peek()}.
	 */
	@Test
	public void testPeek()
	{
		assertNull(buffer.peek());
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.peek());
		
		buffer.addFirst(packet2);
		assertTrue(packet2 == buffer.peek());
		
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.peek());
		
		buffer.addFirst(packet4);
		assertTrue(packet4 == buffer.peek());
		
		// Empty and test
		buffer.removeFirst();
		assertTrue(packet3 == buffer.peek());
		buffer.removeFirst();
		assertTrue(packet2 == buffer.peek());
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		assertNull(buffer.peek());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#push(java.lang.Object)}.
	 */
	@Test
	public void testPush()
	{
		try
		{
			buffer.push(null);
			fail("Should have thrown NullPointerException!");
		} catch (NullPointerException npe)
		{
		}
		
		buffer.push(packet1);
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.push(packet2);
		assertFalse(packet1 == buffer.getFirst());
		assertTrue(packet2 == buffer.getFirst());
		
		buffer.removeFirst();
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.removeFirst();
		assertTrue(buffer.isEmpty());
		
		buffer.push(packet1);
		assertTrue(packet1 == buffer.getFirst());
		
		buffer.push(packet2);
		assertTrue(packet2 == buffer.getFirst());
		
		buffer.push(packet3);
		assertTrue(packet3 == buffer.getFirst());
		
		buffer.push(packet4);
		assertTrue(packet4 == buffer.getFirst());
		assertTrue(packet2 == buffer.getLast());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#pop()}.
	 */
	@Test
	public void testPop()
	{
		try
		{
			buffer.pop();
			fail("Should have thrown NoSuchElementException!");
		} catch (NoSuchElementException nse)
		{
		}
		
		buffer.addFirst(packet1);
		assertTrue(packet1 == buffer.pop());
		assertTrue(buffer.isEmpty());
		
		buffer.addFirst(packet1);
		buffer.addFirst(packet2);
		buffer.addFirst(packet3);
		assertTrue(packet3 == buffer.pop());
		assertTrue(packet2 == buffer.pop());
		assertTrue(packet1 == buffer.pop());
		assertTrue(buffer.isEmpty());
	}
	

	/**
	 * Test method for {@link edu.dhbw.mannheim.tigers.sumatra.util.collection.ArrayRingBuffer#iterator()}.
	 */
	@Test
	public void testIterator()
	{
		buffer.push(packet3);
		buffer.push(packet2);
		buffer.push(packet1);
		
		Iterator<String> it = buffer.iterator();
		assertTrue(it.hasNext());
		
		assertTrue(packet1 == it.next());
		assertTrue(packet2 == it.next());
		assertTrue(packet3 == it.next());
		
		assertFalse(it.hasNext());
		
		try
		{
			it.next();
			fail("Should have thrown NoSuchElementException");
		} catch (NoSuchElementException nse)
		{
			
		}
	}
}
