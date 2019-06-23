/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): dirk
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;


/**
 * This class contains a set of knowledgeFields and
 * methods for comparing a knowledgeField with those stored here.
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
@Entity
public class KnowledgeFields
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	/** list of knowledgeFields */
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	private List<AKnowledgeField>	knowledgeFields;
	
	/** number of compared fields (for compareNext) */
	private transient int			counter;
	/** initial starting position in knowledgeFields (for compareNext) */
	private transient int			position;
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public KnowledgeFields()
	{
		counter = 0;
		position = 0;
		knowledgeFields = new ArrayList<AKnowledgeField>();
		resetCounter();
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Compare the next {@code numFieldsToCompare} fields with the given one.
	 * The best result will be returned.
	 * 
	 * @param knowledgeField
	 * @param numFieldsToCompare
	 * @return best result or 0, if no fields are left
	 */
	public IComparisonResult compareNext(AKnowledgeField knowledgeField, int numFieldsToCompare)
	{
		double bestResult = 0;
		ComparisonResult bestKnowledgeField = null;
		for (int i = 0; i < numFieldsToCompare; i++)
		{
			if (!hasNext())
			{
				break;
			}
			counter++;
			final AKnowledgeField kf = knowledgeFields.get((counter + position) % knowledgeFields.size());
			final double result = knowledgeField.compare(kf).calcResult();
			if (result > bestResult)
			{
				bestResult = result;
				if ((bestKnowledgeField == null) || (bestKnowledgeField.calcResult() < bestResult))
				{
					bestKnowledgeField = new ComparisonResult(bestResult);
				}
			}
		}
		return new ComparisonResult(bestResult);
	}
	
	
	/**
	 * Compare all knowledgeFields with the given one.
	 * 
	 * @param knowledgeField
	 * @return
	 */
	public IComparisonResult compareAll(AKnowledgeField knowledgeField)
	{
		resetCounter();
		return compareNext(knowledgeField, knowledgeFields.size());
		
	}
	
	
	/**
	 * Is there more to compare
	 * 
	 * @return
	 */
	public boolean hasNext()
	{
		return counter != knowledgeFields.size();
	}
	
	
	/**
	 * Reset the counter for compareNext.
	 * Will also reset position randomly
	 */
	public final void resetCounter()
	{
		counter = 0;
		position = (int) (Math.random() * knowledgeFields.size());
	}
	
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 * Add knowledgeField to knowledgeFields
	 * 
	 * @param knowledgeField
	 */
	public void add(AKnowledgeField knowledgeField)
	{
		if (knowledgeField == null)
		{
			throw new IllegalArgumentException("You passed a null object");
		}
		knowledgeFields.add(knowledgeField);
	}
	
	
	/**
	 * Add all kFields to this KnowledgeFields
	 * 
	 * @param kFields
	 */
	public void addAll(KnowledgeFields kFields)
	{
		knowledgeFields.addAll(kFields.knowledgeFields);
	}
	
	
	/**
	 * @return
	 */
	public int size()
	{
		return knowledgeFields.size();
	}
	
	
	/**
	 * @return the knowledgeFields
	 */
	public final List<AKnowledgeField> getKnowledgeFields()
	{
		return knowledgeFields;
	}
}
