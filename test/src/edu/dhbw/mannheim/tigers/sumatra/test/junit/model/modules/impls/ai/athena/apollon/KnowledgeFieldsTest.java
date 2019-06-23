/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 6, 2012
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.athena.apollon;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.dhbw.mannheim.tigers.sumatra.Sumatra;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.Agent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.AKnowledgeField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IComparisonResult;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgeFields;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.util.KnowledgeHelper;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.config.ConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AAgent;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.AConfigManager;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.athena.apollon.util.AKnowledgeFieldHelper;
import edu.dhbw.mannheim.tigers.sumatra.test.junit.model.modules.impls.ai.athena.apollon.util.KnowledgeFieldRasterHelper;


/**
 * This class tests the knowledgeFields comparison
 * 
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 * 
 */
public class KnowledgeFieldsTest
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	
	private static KnowledgeFields			manyKnowledgeFields;
	private static AKnowledgeField			randomField;
	private static AKnowledgeFieldHelper	knowledgeFieldTest	= new KnowledgeFieldRasterHelper();
	
	
	private class KnowledgeFieldPair
	{
		public final AKnowledgeField	kf1;
		public final AKnowledgeField	kf2;
		
		
		public KnowledgeFieldPair(AKnowledgeField kf1, AKnowledgeField kf2)
		{
			this.kf1 = kf1;
			this.kf2 = kf2;
		}
	}
	
	static
	{
		// Load configuration
		Sumatra.touch();
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_AI_CONFIG, Agent.VALUE_AI_CONFIG);
		SumatraModel.getInstance().setUserProperty(AAgent.KEY_GEOMETRY_CONFIG, Agent.VALUE_GEOMETRY_CONFIG);
		AConfigManager.registerConfigClient(AIConfig.getInstance().getAiClient());
		AConfigManager.registerConfigClient(AIConfig.getInstance().getGeomClient());
		new ConfigManager(); // Loads all registered configs (accessed via singleton)
		manyKnowledgeFields = new KnowledgeFields();
		
		randomField = KnowledgeHelper.getRandomField();
	}
	
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * 
	 */
	public KnowledgeFieldsTest()
	{
	}
	
	
	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	
	/**
	 */
	@BeforeClass
	public static void prepareManyFields()
	{
		int numFields = 10000;
		long startTime = System.nanoTime();
		for (int i = 0; i < numFields; i++)
		{
			manyKnowledgeFields.add(KnowledgeHelper.getRandomField());
			if ((i % (numFields / 10)) == 0)
			{
				System.out.print(".");
			}
		}
		long timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
		System.out.println("\nTook " + timeMs + "ms to generate " + numFields + " fields");
	}
	
	
	/**
	 */
	@Test
	public void testIdenticalFields()
	{
		final BallPossession ballPossession = new BallPossession();
		
		final LinkedList<IVector2> pTigers1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pTigers2 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes2 = new LinkedList<IVector2>();
		
		pTigers1.add(new Vector2(100, 100));
		pTigers2.add(new Vector2(100, 100));
		
		final IVector3 pBall1 = new Vector3f(0, 0, 0);
		final IVector3 pBall2 = new Vector3f(0, 0, 0);
		
		
		final AKnowledgeField kf1 = knowledgeFieldTest.createKnowledgeField(pTigers1, pFoes1, pBall1, ballPossession);
		final AKnowledgeField kf2 = knowledgeFieldTest.createKnowledgeField(pTigers2, pFoes2, pBall2, ballPossession);
		
		final IComparisonResult result = kf1.compare(kf2);
		System.out.println("testIdenticalFields: " + result);
		assertTrue(result.calcResult() == 1);
	}
	
	
	/**
	 */
	@Test
	public void testEmptyFields()
	{
		final BallPossession ballPossession = new BallPossession();
		
		final LinkedList<IVector2> pTigers1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pTigers2 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes2 = new LinkedList<IVector2>();
		
		final IVector3 pBall1 = new Vector3f(0, 0, 0);
		final IVector3 pBall2 = new Vector3f(0, 0, 0);
		
		
		final AKnowledgeField kf1 = knowledgeFieldTest.createKnowledgeField(pTigers1, pFoes1, pBall1, ballPossession);
		final AKnowledgeField kf2 = knowledgeFieldTest.createKnowledgeField(pTigers2, pFoes2, pBall2, ballPossession);
		
		final IComparisonResult result = kf1.compare(kf2);
		System.out.println("testEmptyFields: " + result);
		assertTrue(result.calcResult() == 1);
	}
	
	
	/**
	 */
	@Test
	public void testEqualFields()
	{
		final KnowledgeFieldPair kfp = getEqualFields();
		
		final IComparisonResult result = kfp.kf1.compare(kfp.kf2);
		System.out.println("testEqualFields: " + result);
		assertTrue(result.calcResult() > 0.9);
	}
	
	
	/**
	 */
	@Test
	public void testNonEqualFields()
	{
		final KnowledgeFieldPair kfp = getNonEqualFields();
		
		final IComparisonResult result = kfp.kf1.compare(kfp.kf2);
		System.out.println("testNonEqualFields: " + result);
		assertTrue("" + result.calcResult(), result.calcResult() < 0.9);
	}
	
	
	/**
	 */
	@Test
	public void testManyFieldsSequential()
	{
		manyKnowledgeFields.resetCounter();
		assertTrue(manyKnowledgeFields.hasNext());
		final int CHUNK = 100;
		for (int i = 0; i < (manyKnowledgeFields.size() / CHUNK); i++)
		{
			assertTrue(manyKnowledgeFields.hasNext());
			manyKnowledgeFields.compareNext(randomField, CHUNK);
		}
		assertFalse(manyKnowledgeFields.hasNext());
	}
	
	
	/**
	 */
	@Test
	public void testManyFields()
	{
		final IComparisonResult result = manyKnowledgeFields.compareAll(randomField);
		System.out.println("testManyFields: " + result);
	}
	
	
	private KnowledgeFieldPair getEqualFields()
	{
		final BallPossession ballPossession = new BallPossession();
		
		final LinkedList<IVector2> pTigers1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pTigers2 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes2 = new LinkedList<IVector2>();
		
		// tigers
		pTigers1.add(new Vector2(-2500, 0));
		pTigers1.add(new Vector2(-1500, -500));
		pTigers1.add(new Vector2(-1500, 500));
		pTigers1.add(new Vector2(1000, -1000));
		pTigers1.add(new Vector2(1500, 1000));
		pTigers1.add(new Vector2(2000, 1500));
		
		pTigers2.add(new Vector2(-2500, 0));
		pTigers2.add(new Vector2(-1500, -500));
		pTigers2.add(new Vector2(-1500, 500));
		pTigers2.add(new Vector2(2000, -1000));
		pTigers2.add(new Vector2(1250, 1000));
		pTigers2.add(new Vector2(2000, 1250));
		
		// foes
		pFoes1.add(new Vector2(500, -500));
		pFoes1.add(new Vector2(1000, 1500));
		pFoes1.add(new Vector2(2000, 500));
		pFoes1.add(new Vector2(2000, -500));
		pFoes1.add(new Vector2(2500, 0));
		pFoes1.add(new Vector2(2500, 1000));
		
		pFoes2.add(new Vector2(1000, -1000));
		pFoes2.add(new Vector2(500, 1500));
		pFoes2.add(new Vector2(2000, 500));
		pFoes2.add(new Vector2(2000, -500));
		pFoes2.add(new Vector2(2500, 0));
		pFoes2.add(new Vector2(2500, 1000));
		
		
		// ball
		final IVector3 pBall1 = new Vector3f(2000, 1000, 0);
		final IVector3 pBall2 = new Vector3f(1750, 1000, 0);
		
		final AKnowledgeField kf1 = knowledgeFieldTest.createKnowledgeField(pTigers1, pFoes1, pBall1, ballPossession);
		final AKnowledgeField kf2 = knowledgeFieldTest.createKnowledgeField(pTigers2, pFoes2, pBall2, ballPossession);
		
		return new KnowledgeFieldPair(kf1, kf2);
	}
	
	
	private KnowledgeFieldPair getNonEqualFields()
	{
		final BallPossession ballPossession = new BallPossession();
		
		final LinkedList<IVector2> pTigers1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pTigers2 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes2 = new LinkedList<IVector2>();
		
		// tigers
		pTigers1.add(new Vector2(-2500, 0));
		pTigers1.add(new Vector2(-1500, -500));
		pTigers1.add(new Vector2(-1500, 500));
		pTigers1.add(new Vector2(1000, -1000));
		pTigers1.add(new Vector2(1500, 1000));
		pTigers1.add(new Vector2(2000, 1500));
		
		pTigers2.add(new Vector2(-3000, 0));
		pTigers2.add(new Vector2(-2000, 500));
		pTigers2.add(new Vector2(-2500, 1000));
		pTigers2.add(new Vector2(-500, -500));
		pTigers2.add(new Vector2(500, 1000));
		pTigers2.add(new Vector2(1000, -500));
		
		// foes
		pFoes1.add(new Vector2(500, -500));
		pFoes1.add(new Vector2(1000, 1500));
		pFoes1.add(new Vector2(2000, 500));
		pFoes1.add(new Vector2(2000, -500));
		pFoes1.add(new Vector2(2500, 0));
		pFoes1.add(new Vector2(2500, 1000));
		
		pFoes2.add(new Vector2(-1000, 1000));
		pFoes2.add(new Vector2(-1000, -500));
		pFoes2.add(new Vector2(-500, 1500));
		pFoes2.add(new Vector2(2000, 500));
		pFoes2.add(new Vector2(2000, -500));
		pFoes2.add(new Vector2(3000, 0));
		
		
		// ball
		final IVector3 pBall1 = new Vector3f(2000, 1000, 0);
		final IVector3 pBall2 = new Vector3f(-500, 1000, 0);
		
		final AKnowledgeField kf1 = knowledgeFieldTest.createKnowledgeField(pTigers1, pFoes1, pBall1, ballPossession);
		final AKnowledgeField kf2 = knowledgeFieldTest.createKnowledgeField(pTigers2, pFoes2, pBall2, ballPossession);
		
		return new KnowledgeFieldPair(kf1, kf2);
	}
	
	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
}
