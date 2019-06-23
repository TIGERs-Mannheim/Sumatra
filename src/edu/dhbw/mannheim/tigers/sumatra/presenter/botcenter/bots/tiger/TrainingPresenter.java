/* 
 * *********************************************************
 * Copyright (c) 2009 - 2011, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: 20.06.2011
 * Author(s): AndreR
 *
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.botcenter.bots.tiger;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import Jama.Matrix;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.IVector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Rectanglef;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2;
import edu.dhbw.mannheim.tigers.sumatra.model.data.Vector2f;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.bots.TigerBot;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.botmanager.commands.tiger.TigerMotorMoveV2;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ASkill;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.ESkillName;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveDataCollector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveDataCollector.DataItem;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.skillsystem.skills.tiger.MoveFixedGlobalOrientation;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.observer.ISkillSystemObserver;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ASkillSystem;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.BotCenterTreeNode;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.ETreeIconType;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.training.TrainingPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.botcenter.internals.bots.tiger.training.TrainingPanel.ITrainingPanelObserver;
import edu.moduli.exceptions.ModuleNotFoundException;

/**
 * Presenter for Training panel.
 * 
 * @author AndreR
 * 
 */
public class TrainingPresenter implements ISkillSystemObserver, ITrainingPanelObserver
{
	// --------------------------------------------------------------------------
	// --- variables and constants ----------------------------------------------
	// --------------------------------------------------------------------------
	public class DataItemSolved
	{
		public double absTime;
		public Matrix realVelocity;	// 3x1
		public Matrix commandedVeolcity;	// 3x1
		public Matrix epsilon;	// 3x4
		
	}
	
	private final Logger log = Logger.getLogger(getClass());
	
	private TrainingPanel training = null;
	private TigerBot bot = null;
	private ASkillSystem skillsystem = null;
	
	private Matrix xEpsilon = null;
	private Matrix yEpsilon = null;
	
	private boolean active = false;
	
	// --------------------------------------------------------------------------
	// --- constructors ---------------------------------------------------------
	// --------------------------------------------------------------------------
	public TrainingPresenter(TigerBot bot)
	{
		training = new TrainingPanel();
		
		this.bot = bot;
		
		try
		{
			skillsystem = (ASkillSystem) SumatraModel.getInstance().getModule("skillsystem");
		} catch (ModuleNotFoundException err)
		{
			log.error("Skillsystem not found", err);
			
			return;
		}
		
		training.addObserver(this);
		skillsystem.addObserver(this);
	}
	

	// --------------------------------------------------------------------------
	// --- methods --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public void delete()
	{
		skillsystem.removeObserver(this);
		training.removeObserver(this);
	}
	
	@Override
	public void onSkillStarted(ASkill skill, int botID)
	{
	}


	@Override
	public void onSkillCompleted(ASkill skill, int botID)
	{
		if(!active)
			return;
		
		if(botID != bot.getBotId())
		{
			return;
		}
		
		if(skill.getSkillName() == ESkillName.MOVE_FIXED_GLOBAL_ORIENTATION)
		{
			// reached target, start collecting data
			log.debug("Reached target, collecting data");
			
			if(xEpsilon == null)
			{
				skillsystem.execute(bot.getBotId(), new MoveDataCollector(new Vector2(1, 0), 0));
			}
			else
			{
				skillsystem.execute(bot.getBotId(), new MoveDataCollector(new Vector2(0, 1), 0));
			}
		}
		
		if(skill.getSkillName() == ESkillName.MOVE_DATA_COLLECTOR)
		{
			// collecting done, use data
			MoveDataCollector col = (MoveDataCollector)skill;
			
			log.debug("Gathered " + col.getData().size() + " samples");
			
			if(xEpsilon == null)
			{
				xEpsilon = calcMatrix(col.getData());
				moveToStart(new Vector2(0, 1));
			}
			else
			{
				yEpsilon = calcMatrix(col.getData());
				// done
				printMatrix(xEpsilon);
				printMatrix(yEpsilon);
				
				bot.setCorrectionMatrices(xEpsilon, yEpsilon);
				
				active = false;
			}
		}
	}
	
	private void trainedMove(IVector2 dir, float w)
	{
		if(xEpsilon == null || yEpsilon == null)
		{
			return;
		}
		
		Matrix origCmd = new Matrix(3,1);
		origCmd.set(0, 0, dir.x());
		origCmd.set(1, 0, dir.y());
		origCmd.set(2, 0, w);
		
		Matrix sendCmd = TigerBot.getMotorToCmdMatrix().minus(xEpsilon.times(dir.x()).plus(yEpsilon.times(dir.y()))).times(TigerBot.getCmdToMotorMatrix().times(origCmd));
		
		Vector2 newDir = new Vector2((float)sendCmd.get(0, 0), (float)sendCmd.get(1, 0));
		
		bot.execute(new TigerMotorMoveV2(newDir, (float)sendCmd.get(2, 0)));
	}
	
	private void startTraining()
	{
		xEpsilon = null;
		yEpsilon = null;
		
		active = true;
		moveToStart(new Vector2(1, 0));
	}
	
	private void moveToStart(IVector2 dir)
	{
		Rectanglef field = AIConfig.getGeometry().getField();
		Vector2 startPos = field.topLeft().addNew(new Vector2f(700, -700));
		
		float angleToCenter = startPos.multiplyNew(-1).getAngle();
		float angleToFrontDir = (float) Math.acos(dir.scaleToNew(1).scalarProduct(new Vector2f(0, 1)));
		if(dir.x() < 0)
		{
			angleToFrontDir -= Math.PI;
		}
		
		// Move to corner and look at center
		skillsystem.execute(bot.getBotId(), new MoveFixedGlobalOrientation(startPos, angleToCenter + angleToFrontDir));		
	}
	
	private Matrix calcMatrix(List<DataItem> data)
	{
		List<DataItemSolved> solvedData = new ArrayList<DataItemSolved>();

		for(DataItem item : data)
		{
			DataItemSolved solved = new DataItemSolved();
			solved.absTime = item.absTime;
			solved.commandedVeolcity = new Matrix(3, 1);
			solved.commandedVeolcity.set(0, 0, item.commandedVeolcity.x);
			solved.commandedVeolcity.set(1, 0, item.commandedVeolcity.y);
			solved.commandedVeolcity.set(2, 0, item.commandedVeolcity.z);	// w
			solved.realVelocity = new Matrix(3, 1);
			solved.realVelocity.set(0, 0, item.realVelocity.x);
			solved.realVelocity.set(1, 0, item.realVelocity.y);
			solved.realVelocity.set(2, 0, item.realVelocity.z);	// w
			solved.epsilon = solved.realVelocity.minus(solved.commandedVeolcity).times(TigerBot.getCmdToMotorMatrix().times(solved.commandedVeolcity).inverse());
			
			solvedData.add(solved);
		}
		
		Matrix avg = new Matrix(3,4);
		
		for(DataItemSolved item : solvedData)
		{
			avg.plusEquals(item.epsilon);
		}
		
		avg.timesEquals(1/(double)solvedData.size());
		
		return avg;
	}
	
	private void printMatrix(Matrix M)
	{
			System.out.print("[ ");
			for(int r = 0; r < M.getRowDimension(); r++)
			{
				for(int c = 0; c < M.getColumnDimension(); c++)
				{
					System.out.print(M.get(r, c) + " ");
				}
				System.out.print("\n  ");
			}
			System.out.print("]\n");
	}

	// --------------------------------------------------------------------------
	// --- getter/setter --------------------------------------------------------
	// --------------------------------------------------------------------------
	public BotCenterTreeNode getNode()
	{
		return new BotCenterTreeNode("Training", ETreeIconType.LAMP, training);
	}


	@Override
	public void onTrainedMove(IVector2 dir, float rot)
	{
		trainedMove(dir, rot);
	}
	
	@Override
	public void onStartTraining()
	{
		startTraining();
	}
}
