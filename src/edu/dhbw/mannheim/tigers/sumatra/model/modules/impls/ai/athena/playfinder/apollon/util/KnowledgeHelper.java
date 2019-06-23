/*
 * *********************************************************
 * Copyright (c) 2009 - 2012, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Oct 28, 2012
 * Author(s): andres
 * 
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ETeam;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.ai.ballpossession.BallPossession;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.AVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.IVector3;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.shapes.vector.Vector3f;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBall;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedFoeBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.TrackedTigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotID;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMap;
import edu.dhbw.mannheim.tigers.sumatra.model.data.trackedobjects.ids.BotIDMapConst;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.control.PlayAndRoleCount;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.AKnowledgeField;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.GeneralKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.IKnowledgeBase;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgeFieldRaster;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.apollon.KnowledgePlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.athena.playfinder.stats.ESelectionReason;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.EPlay;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.pandora.plays.PlayType;


/**
 * Helper class for LearningPlayFInder Knowledge Tests
 * 
 * @author andres
 * 
 */
public final class KnowledgeHelper
{
	private static Random		random					= new Random(System.nanoTime());
	
	private static final int	SUCCESS_FIELDS			= 500;
	private static final int	FAILED_FIELDS			= 500;
	
	/**  */
	public static final int		NUM_FAILED_FIELDS		= 100;
	
	/**  */
	public static final int		NUM_SUCCESS_FIELDS	= 50;
	
	
	private KnowledgeHelper()
	{
		
	}
	
	
	/**
	 * This method counts the number of fields that are save din this GeneralKnowledgeBase. It counts for each plays all
	 * success and failed fields and then returns a sum.
	 * @param gklb
	 * @return
	 */
	public static long countKnowledgeBase(IKnowledgeBase gklb)
	{
		long fieldCount = 0;
		for (final KnowledgePlay pl : gklb.getKnowledgePlays())
		{
			fieldCount += pl.getSuccessFields().size();
			fieldCount += pl.getFailedFields().size();
		}
		return fieldCount;
	}
	
	
	protected static IKnowledgeBase getRandomKnowledgeBase(int numOfPlays)
	{
		final IKnowledgeBase gklb = new GeneralKnowledgeBase("Test");
		for (int i = 0; i < numOfPlays; i++)
		{
			KnowledgePlay pl = null;
			do
			{
				pl = new KnowledgePlay(getRandomPlayAndRoleCount());
			} while (gklb.getKnowledgePlays().contains(pl));
			gklb.addKnowledgePlay(pl);
		}
		
		for (final KnowledgePlay klp : gklb.getKnowledgePlays())
		{
			createRandomFieldsInKnowledgePlay(klp);
		}
		return gklb;
	}
	
	
	/**
	 * extends the knowledbase with the number of plays (iuf possible). and then for all existing plays creates random
	 * fields.
	 * @param kb
	 * @param numOfPlays
	 */
	public static void extendKB(IKnowledgeBase kb, int numOfPlays)
	{
		for (int i = 0; i < numOfPlays; i++)
		{
			KnowledgePlay pl = new KnowledgePlay(getRandomPlayAndRoleCount());
			createFieldsInKnowledgePlay(pl);
			kb.addKnowledgePlay(pl);
		}
	}
	
	
	protected static PlayAndRoleCount getRandomPlayAndRoleCount()
	{
		final List<EPlay> plays = PlayType.getGamePlays();
		final EPlay play = plays.get(random.nextInt(plays.size()));
		final int n = play.getMaxRoles() - play.getMinRoles();
		int numRoles;
		if (n == 0)
		{
			numRoles = 1;
		} else
		{
			numRoles = random.nextInt(n) + play.getMinRoles();
		}
		
		final PlayAndRoleCount playAndRole = new PlayAndRoleCount(play, numRoles, ESelectionReason.HELPER);
		return playAndRole;
	}
	
	
	protected static void createRandomFieldsInKnowledgePlay(KnowledgePlay klp)
	{
		for (int j = 0; j < random.nextInt(SUCCESS_FIELDS); j++)
		{
			klp.addSuccessField(getRandomField());
		}
		for (int j = 0; j < random.nextInt(FAILED_FIELDS); j++)
		{
			klp.addFailedField(getRandomField());
		}
	}
	
	
	protected static void createFieldsInKnowledgePlay(KnowledgePlay klp)
	{
		for (int j = 0; j < NUM_SUCCESS_FIELDS; j++)
		{
			klp.addSuccessField(getRandomField());
		}
		for (int j = 0; j < NUM_FAILED_FIELDS; j++)
		{
			klp.addFailedField(getRandomField());
		}
	}
	
	
	/**
	 * Creates a random KnowledgeField.
	 * @return
	 */
	public static AKnowledgeField getRandomField()
	{
		final BallPossession ballPossession = new BallPossession();
		
		final LinkedList<IVector2> pTigers1 = new LinkedList<IVector2>();
		final LinkedList<IVector2> pFoes1 = new LinkedList<IVector2>();
		
		// tigers
		for (int i = 0; i < 6; i++)
		{
			pTigers1.add(new Vector2(getRandomX(), getRandomY()));
		}
		
		// foes
		for (int i = 0; i < 6; i++)
		{
			pFoes1.add(new Vector2(getRandomX(), getRandomY()));
		}
		
		
		// ball
		final IVector3 pBall1 = new Vector3f(getRandomX(), getRandomY(), 0);
		
		final AKnowledgeField kf1 = createKnowledgeField(pTigers1, pFoes1, pBall1, ballPossession);
		
		return kf1;
	}
	
	
	protected static AKnowledgeField createKnowledgeField(List<IVector2> pTigers, List<IVector2> pFoes, IVector3 pBall,
			BallPossession ballPossession)
	{
		final BotIDMap<TrackedBot> foeBots = new BotIDMap<TrackedBot>();
		final BotIDMap<TrackedTigerBot> tigerBots = new BotIDMap<TrackedTigerBot>();
		
		int botNum = 0;
		for (final IVector2 pos : pTigers)
		{
			final TrackedTigerBot bot = createTigerBot(new BotID(botNum), pos);
			tigerBots.put(bot.getId(), bot);
			botNum++;
		}
		
		botNum = 0;
		for (final IVector2 pos : pFoes)
		{
			final TrackedBot bot = createFoeBot(new BotID(botNum, ETeam.OPPONENTS), pos);
			foeBots.put(bot.getId(), bot);
			botNum++;
		}
		
		
		final TrackedBall ball = new TrackedBall(pBall, new Vector3f(), new Vector3f(), 0f, true);
		
		return new KnowledgeFieldRaster(BotIDMapConst.unmodifiableBotIDMap(tigerBots),
				BotIDMapConst.unmodifiableBotIDMap(foeBots), ball, ballPossession);
	}
	
	
	protected static TrackedTigerBot createTigerBot(BotID botId, IVector2 pos)
	{
		return new TrackedTigerBot(botId, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0f, 0f, 0f, 0, null);
	}
	
	
	protected static TrackedBot createFoeBot(BotID botId, IVector2 pos)
	{
		return new TrackedFoeBot(botId, pos, AVector2.ZERO_VECTOR, AVector2.ZERO_VECTOR, 0, 0, 0, 0, 0);
	}
	
	
	protected static int getRandomX()
	{
		return (int) ((random.nextDouble() * AIConfig.getGeometry().getFieldLength()) - (AIConfig.getGeometry()
				.getFieldLength() / 2));
	}
	
	
	protected static int getRandomY()
	{
		return (int) ((random.nextDouble() * AIConfig.getGeometry().getFieldWidth()) - (AIConfig.getGeometry()
				.getFieldWidth() / 2));
	}
}
