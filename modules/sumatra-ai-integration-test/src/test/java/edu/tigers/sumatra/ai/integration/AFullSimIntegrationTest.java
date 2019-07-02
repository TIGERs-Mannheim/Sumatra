package edu.tigers.sumatra.ai.integration;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.TTCCLayout;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.github.g3force.configurable.ConfigRegistration;

import edu.tigers.sumatra.geometry.Geometry;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.referee.data.RefereeMsg;
import edu.tigers.sumatra.wp.IWorldFrameObserver;
import edu.tigers.sumatra.wp.data.WorldFrameWrapper;


public abstract class AFullSimIntegrationTest implements IWorldFrameObserver
{
	private static final String MODULI_CONFIG = "integration_test.xml";
	
	protected WorldFrameWrapper lastWorldFrameWrapper = null;
	protected RefereeMsg lastRefereeMsg = null;
	
	
	private static FileAppender fileAppender;
	
	
	@Before
	public void before() throws Exception
	{
		lastRefereeMsg = null;
	}
	
	
	@BeforeClass
	public static void beforeClass() throws Exception
	{
		fileAppender = new FileAppender(new TTCCLayout(), "target/integrationTest.log");
		Logger.getRootLogger().addAppender(fileAppender);
		ConfigRegistration.setDefPath("../../config/");
		SumatraModel.getInstance().setCurrentModuliConfig(MODULI_CONFIG);
		SumatraModel.getInstance().loadModulesOfConfig(MODULI_CONFIG);
		SumatraModel.getInstance().setTestMode(true);
		Geometry.setNegativeHalfTeam(ETeamColor.BLUE);
	}
	
	
	@AfterClass
	public static void afterClass()
	{
		SumatraModel.getInstance().setTestMode(false);
		
		Logger.getRootLogger().removeAppender(fileAppender);
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wFrameWrapper)
	{
		lastWorldFrameWrapper = wFrameWrapper;
		lastRefereeMsg = wFrameWrapper.getRefereeMsg();
	}
}
