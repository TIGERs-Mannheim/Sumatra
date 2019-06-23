/*
 * Copyright (c) 2009 - 2017, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.math;

import java.math.BigInteger;
import java.util.Arrays;


/**
 * <p>
 * This class encapsulates the generation of an array of permutations
 * </p>
 * <p>
 * <strong>WARNING:</strong> Don't make n too large. Recall that the number of permutations is n! which can be very
 * large, even when n is as small as 20 -- 20! = 2,432,902,008,176,640,000 and 21! is too big to fit into a Java long,
 * which is why we use BigInteger instead.
 * </p>
 * <p>
 * <i>(original found on the <a href="http://www.merriampark.com/perm.htm">web</a>)</i><br/>
 * Only slightly modified: Returns a <strong>copy</strong> of internal buffer {@link #a} now.
 * </p>
 * 
 * @author Gero
 */
public class PermutationGenerator
{
	/** Internal buffer for permutation-values */
	private final int[]			a;
	private BigInteger			numLeft;
	private final BigInteger	total;
	
	
	/**
	 * @param n
	 */
	public PermutationGenerator(final int n)
	{
		if (n < 1)
		{
			throw new IllegalArgumentException("Min 1");
		}
		a = new int[n];
		total = getFactorial(n);
		reset();
	}
	
	
	/**
	 * Reset the generator
	 */
	public final void reset()
	{
		for (int i = 0; i < a.length; i++)
		{
			a[i] = i;
		}
		numLeft = new BigInteger(total.toString());
	}
	
	
	/**
	 * @return Number of permutations not yet generated
	 */
	public BigInteger getNumLeft()
	{
		return numLeft;
	}
	
	
	/**
	 * @return Total number of permutations
	 */
	public BigInteger getTotal()
	{
		return total;
	}
	
	
	/**
	 * @return Are there more permutations?
	 */
	public boolean hasMore()
	{
		return numLeft.compareTo(BigInteger.ZERO) > 0;
	}
	
	
	/**
	 * @return Compute factorial
	 */
	private static BigInteger getFactorial(final int n)
	{
		BigInteger fact = BigInteger.ONE;
		for (int i = n; i > 1; i--)
		{
			fact = fact.multiply(new BigInteger(Integer.toString(i)));
		}
		return fact;
	}
	
	
	/**
	 * Generate next permutation (algorithm from Rosen p. 284)
	 * 
	 * @return Values of the permutation in an <code>int[]</code>
	 */
	public int[] getNext()
	{
		if (numLeft.equals(total))
		{
			numLeft = numLeft.subtract(BigInteger.ONE);
			return Arrays.copyOf(a, a.length);
		}
		
		int temp;
		
		// Find largest index j with a[j] < a[j+1]
		
		int j = a.length - 2;
		while (a[j] > a[j + 1])
		{
			j--;
		}
		
		// Find index k such that a[k] is smallest integer
		// greater than a[j] to the right of a[j]
		
		int k = a.length - 1;
		while (a[j] > a[k])
		{
			k--;
		}
		
		// Interchange a[j] and a[k]
		
		temp = a[k];
		a[k] = a[j];
		a[j] = temp;
		
		// Put tail end of permutation after jth position in increasing order
		
		int r = a.length - 1;
		int s = j + 1;
		
		while (r > s)
		{
			temp = a[s];
			a[s] = a[r];
			a[r] = temp;
			r--;
			s++;
		}
		
		numLeft = numLeft.subtract(BigInteger.ONE);
		return Arrays.copyOf(a, a.length);
	}
}
