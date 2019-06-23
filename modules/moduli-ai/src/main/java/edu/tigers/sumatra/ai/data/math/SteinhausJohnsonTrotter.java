/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 1, 2015
 * Author(s): FelixB <bayer.fel@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.ai.data.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * TODO FelixB <bayer.fel@gmail.com>, add comment!
 * - What should this type do (in one sentence)?
 * - If not intuitive: A simple example how to use this class
 * 
 * @author FelixB <bayer.fel@gmail.com>
 * @param <T>
 */
public class SteinhausJohnsonTrotter<T> implements Iterator<List<T>>
{
	
	private SteinhausJohnsonTrotterInner	algorithm	= null;
	private List<T>								elements		= null;
	
	
	/**
	 * @param elements
	 */
	public SteinhausJohnsonTrotter(final List<T> elements)
	{
		
		this.algorithm = new SteinhausJohnsonTrotterInner(elements.size());
		this.elements = elements;
	}
	
	
	@Override
	public boolean hasNext()
	{
		
		return this.algorithm.hasNext();
	}
	
	
	@Override
	public List<T> next()
	{
		
		if (!this.hasNext())
		{
			
			throw new NoSuchElementException();
		}
		
		List<Integer> indexList = this.algorithm.next();
		List<T> returnList = new ArrayList<T>(this.getSize());
		
		for (int i = 0; i < this.getSize(); i++)
		{
			
			returnList.add(this.elements.get(indexList.get(i)));
		}
		
		return returnList;
	}
	
	
	/**
	 * TODO FelixB <bayer.fel@gmail.com>, add comment!
	 * 
	 * @return
	 */
	public int getSize()
	{
		
		return this.algorithm.getSize();
	}
	
	private class SteinhausJohnsonTrotterInner implements Iterator<List<Integer>>
	{
		
		private final int			size;
		private DirectedInteger	firstElement				= null;
		private boolean			initialSolutionOmitted	= false;
		
		
		/**
		 * @param size
		 */
		public SteinhausJohnsonTrotterInner(final int size)
		{
			
			this.size = size;
			firstElement = new DirectedInteger(size);
		}
		
		
		/**
		 * TODO FelixB <bayer.fel@gmail.com>, add comment!
		 * 
		 * @return
		 */
		public int getSize()
		{
			
			return size;
		}
		
		private class DirectedInteger
		{
			
			private int					value;
			private int					direction		= 1;
			
			private DirectedInteger	leftNeighbour	= null;
			private DirectedInteger	rightNeighbour	= null;
			
			
			public DirectedInteger(int size)
			{
				
				size = size - 1;
				value = size;
				
				if (size > 0)
				{
					
					rightNeighbour = new DirectedInteger(this, size - 1);
				}
			}
			
			
			private DirectedInteger(final DirectedInteger leftNeighbour, final int size)
			{
				
				value = size;
				this.leftNeighbour = leftNeighbour;
				
				if (size > 0)
				{
					
					rightNeighbour = new DirectedInteger(this, size - 1);
				}
			}
			
			
			public boolean isExpired()
			{
				
				if (isMobile())
				{
					return false;
				}
				
				DirectedInteger curElement = this;
				while (null != curElement.rightNeighbour)
				{
					
					curElement = curElement.rightNeighbour;
					if (curElement.isMobile())
					{
						return false;
					}
				}
				return true;
			}
			
			
			public boolean isMobile()
			{
				
				if (1 == direction)
				{
					
					return (null != rightNeighbour) && (rightNeighbour.value < value);
				}
				return (null != leftNeighbour) && (leftNeighbour.value < value);
			}
			
			
			public int getValue()
			{
				
				return value;
			}
			
			
			public void swapDirection()
			{
				
				direction = direction * -1;
			}
		}
		
		
		private List<Integer> getCurrentPermutation()
		{
			
			List<Integer> returnList = new ArrayList<Integer>(size);
			
			
			DirectedInteger curElement = firstElement;
			returnList.add(curElement.getValue());
			
			while (null != curElement.rightNeighbour)
			{
				
				returnList.add(curElement.rightNeighbour.getValue());
				curElement = curElement.rightNeighbour;
			}
			
			return returnList;
		}
		
		
		private void calculateNextAnswer()
		{
			
			DirectedInteger largestMobileInteger = getLargestMobileInteger();
			int floor = largestMobileInteger.getValue();
			swapValues(largestMobileInteger);
			updateDirections(floor);
		}
		
		
		private void updateDirections(final int floor)
		{
			
			DirectedInteger curElement = firstElement;
			if (curElement.getValue() > floor)
			{
				
				curElement.swapDirection();
			}
			
			while (null != curElement.rightNeighbour)
			{
				
				curElement = curElement.rightNeighbour;
				if (curElement.getValue() > floor)
				{
					
					curElement.swapDirection();
				}
			}
		}
		
		
		private void swapValues(final DirectedInteger integerToSwap)
		{
			
			DirectedInteger swapPartner = null;
			if (1 == integerToSwap.direction)
			{
				
				swapPartner = integerToSwap.rightNeighbour;
			} else
			{
				
				swapPartner = integerToSwap.leftNeighbour;
			}
			
			int temp;
			temp = integerToSwap.value;
			integerToSwap.value = swapPartner.value;
			swapPartner.value = temp;
			
			temp = integerToSwap.direction;
			integerToSwap.direction = swapPartner.direction;
			swapPartner.direction = temp;
		}
		
		
		private DirectedInteger getLargestMobileInteger()
		{
			
			DirectedInteger curElement = firstElement;
			DirectedInteger curLargestMobileInteger = null;
			
			if (curElement.isMobile())
			{
				
				curLargestMobileInteger = curElement;
			}
			
			while (null != curElement.rightNeighbour)
			{
				
				curElement = curElement.rightNeighbour;
				if (curElement.isMobile()
						&& ((null == curLargestMobileInteger) || (curElement.getValue() > curLargestMobileInteger
								.getValue())))
				{
					
					curLargestMobileInteger = curElement;
				}
			}
			
			return curLargestMobileInteger;
		}
		
		
		@Override
		public boolean hasNext()
		{
			
			return !firstElement.isExpired();
		}
		
		
		@Override
		public List<Integer> next()
		{
			
			if (!hasNext())
			{
				
				throw new NoSuchElementException();
			}
			
			if (!initialSolutionOmitted)
			{
				
				initialSolutionOmitted = true;
				return getCurrentPermutation();
			}
			
			calculateNextAnswer();
			return getCurrentPermutation();
		}
	}
	
}
