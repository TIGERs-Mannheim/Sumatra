/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: May 21, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.dhbw.mannheim.tigers.sumatra.presenter.ball;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.dhbw.mannheim.tigers.moduli.exceptions.ModuleNotFoundException;
import edu.dhbw.mannheim.tigers.moduli.listenerVariables.ModulesState;
import edu.dhbw.mannheim.tigers.sumatra.model.SumatraModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.KickerModel;
import edu.dhbw.mannheim.tigers.sumatra.model.data.airecord.IRecordFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.data.frames.WorldFrameWrapper;
import edu.dhbw.mannheim.tigers.sumatra.model.data.modules.referee.RefereeMsg;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.ai.config.AIConfig;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.MergedCamDetectionFrame;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallCorrector;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BallProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.impls.worldpredictor.kalman.oextkal.BotProcessor;
import edu.dhbw.mannheim.tigers.sumatra.model.modules.types.ABotManager;
import edu.dhbw.mannheim.tigers.sumatra.util.MatlabConnection;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.BallWatcher;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.ExportDataContainer;
import edu.dhbw.mannheim.tigers.sumatra.util.csvexporter.IBallWatcherObserver;
import edu.dhbw.mannheim.tigers.sumatra.view.ball.BallAnalyserPanel;
import edu.dhbw.mannheim.tigers.sumatra.view.ball.IBallAnalyserPanelObserver;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraView;
import edu.dhbw.mannheim.tigers.sumatra.views.ISumatraViewPresenter;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BallAnalyserPresenter implements ISumatraViewPresenter
{
	@SuppressWarnings("unused")
	private static final Logger		log			= Logger.getLogger(BallAnalyserPresenter.class.getName());
	
	private final BallAnalyserPanel	panel			= new BallAnalyserPanel();
	private BallWatcher					ballWatcher	= null;
	
	
	/**
	 * 
	 */
	public BallAnalyserPresenter()
	{
		panel.addObserver(new PanelObserver());
	}
	
	
	@Override
	public void onModuliStateChanged(final ModulesState state)
	{
	}
	
	
	@Override
	public void onNewAIInfoFrame(final IRecordFrame lastAIInfoframe)
	{
	}
	
	
	@Override
	public void onAIException(final Throwable ex, final IRecordFrame frame, final IRecordFrame prevFrame)
	{
	}
	
	
	@Override
	public void onNewWorldFrame(final WorldFrameWrapper wfWrapper)
	{
	}
	
	
	@Override
	public void onNewRefereeMsg(final RefereeMsg refMsg)
	{
	}
	
	
	@Override
	public Component getComponent()
	{
		return panel;
	}
	
	
	@Override
	public ISumatraView getSumatraView()
	{
		return panel;
	}
	
	
	@Override
	public void onEmergencyStop()
	{
	}
	
	private class PanelObserver implements IBallAnalyserPanelObserver, ClipboardOwner
	{
		
		@Override
		public void onSave(final String filename)
		{
			File infoFile = new File(getBaseDir(filename) + "/info.json");
			if (infoFile.exists())
			{
				JSONParser jp = new JSONParser();
				FileReader fr = null;
				try
				{
					fr = new FileReader(infoFile);
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) jp.parse(fr);
					map.put("description", panel.getDescription());
					JSONObject jo = new JSONObject(map);
					Files.write(Paths.get(infoFile.getAbsolutePath()), jo.toJSONString().getBytes());
				} catch (IOException | ParseException err)
				{
					log.error("Could not parse JSON.", err);
				} catch (ClassCastException err)
				{
					log.error("Could not cast a type. Wrong format?!", err);
				} finally
				
				{
					if (fr != null)
					{
						try
						{
							fr.close();
						} catch (IOException err)
						{
							log.error("Could not close file reader.", err);
						}
					}
				}
			}
			panel.markDirty(false);
		}
		
		
		@Override
		public void onRecord(final boolean record, final boolean stopAutomatically)
		{
			if (record)
			{
				if (ballWatcher != null)
				{
					log.warn("Last ball watcher was not removed?!");
					ballWatcher.stopExport();
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
				String fileName = "ballManual/" + sdf.format(new Date());
				ballWatcher = new BallWatcher(fileName);
				ballWatcher.addObserver(new BallWatcherObserver());
				ballWatcher.setStopAutomatically(stopAutomatically);
				boolean started = ballWatcher.start();
				panel.setRecording(started);
				if (!started)
				{
					ballWatcher = null;
				}
			} else
			{
				ballWatcher.stopExport();
			}
		}
		
		
		@Override
		public void onDelete(final List<String> filenames)
		{
			StringBuilder sb = new StringBuilder();
			for (String s : filenames)
			{
				sb.append(s);
				sb.append('\n');
			}
			
			int answer = JOptionPane.showConfirmDialog(panel,
					"Do you really want to delete following files? \n" + sb.toString(), "Delete?",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (answer == JOptionPane.YES_OPTION)
			{
				for (String filename : filenames)
				{
					File file = new File(filename);
					if (file.isDirectory())
					{
						File[] files = file.listFiles();
						if (files != null)
						{
							for (File f : files)
							{
								if (f.isDirectory())
								{
									log.warn("Recursive deletion not supported for safety reasons! Skip " + f.getAbsolutePath());
									continue;
								}
								if (!f.delete())
								{
									log.warn("Could not delete file: " + file.getAbsolutePath());
								}
							}
						}
					}
					if (!file.delete())
					{
						log.error("Could not delete file: " + filename);
					}
				}
				panel.updateFiles();
			}
		}
		
		
		@Override
		public void onPlot(final List<String> filenames)
		{
			Thread t = new Thread(() -> plotBallData(filenames));
			t.start();
		}
		
		
		private void plotBallData(final List<String> filenames)
		{
			for (String filename : filenames)
			{
				String basePath = getBaseDir(filename);
				File f = new File(basePath);
				if (!f.exists())
				{
					continue;
				}
				callMatlab("plotBallData", basePath);
			}
		}
		
		
		private void callMatlab(final String function, final Object... params)
		{
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.feval(function, params);
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function: " + err.getMessage(), err);
			}
		}
		
		
		@Override
		public void onCreateBallModel(final List<String> filenames)
		{
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.eval("addpath('learning')");
				Object[] values = mp.returningFeval("learnBallModel", 1,
						filenames.toArray(new Object[filenames.size()]));
				double[] coeffs = (double[]) values[0];
				float[] params = new float[coeffs.length];
				for (int i = 0; i < coeffs.length; i++)
				{
					params[i] = (float) coeffs[i];
				}
				AIConfig.getBallModel().applyNewParameters(params);
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function: " + err.getMessage(), err);
			}
		}
		
		
		@Override
		public void onCreateKickModel(final List<String> filenames)
		{
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.eval("addpath('learning')");
				Object[] values = mp.returningFeval("learnKickSpeeds", 1,
						filenames.toArray(new Object[filenames.size()]));
				double[] coeffs = (double[]) values[0];
				float[] params = new float[coeffs.length];
				for (int i = 0; i < coeffs.length; i++)
				{
					params[i] = (float) coeffs[i];
				}
				ABotManager botManager;
				try
				{
					botManager = (ABotManager) SumatraModel.getInstance().getModule(ABotManager.MODULE_ID);
					KickerModel.forBot(botManager.getBotType()).applyNewParameters(params);
				} catch (ModuleNotFoundException err)
				{
					log.error("Botmanager module not found. Start Sumatra first!");
				}
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error("Error evaluating matlab function: " + err.getMessage(), err);
			} catch (Exception err)
			{
				log.error("An error occurred.", err);
			}
		}
		
		
		@Override
		public void onNewSelectedFile(final List<String> filenames)
		{
			if (filenames.isEmpty())
			{
				return;
			}
			File infoFile = new File(getBaseDir(filenames.get(0)) + "/info.json");
			if (infoFile.exists())
			{
				panel.setValidFileSelected(true);
				panel.clearKeyValue();
				JSONParser jp = new JSONParser();
				FileReader fr = null;
				try
				{
					fr = new FileReader(infoFile);
					@SuppressWarnings("unchecked")
					Map<String, Object> jo = (Map<String, Object>) jp.parse(fr);
					String description = (String) jo.get("description");
					Long numSamples = (Long) jo.get("numSamples");
					if (description != null)
					{
						panel.setDescription(description);
					} else
					{
						panel.setDescription("");
					}
					if (numSamples != null)
					{
						panel.setNumSamples((int) (long) numSamples);
					} else
					{
						panel.setNumSamples(-1);
					}
					
					for (Map.Entry<String, Object> entry : jo.entrySet())
					{
						if (entry.getKey().equals("description") || entry.getKey().equals("numSamples"))
						{
							continue;
						}
						panel.setKeyValue(entry.getKey(), String.valueOf(entry.getValue()));
					}
				} catch (IOException | ParseException err)
				{
					log.error("Could not parse JSON.", err);
				} catch (ClassCastException err)
				{
					log.error("Could not cast a type. Wrong format?!", err);
				} finally
				{
					if (fr != null)
					{
						try
						{
							fr.close();
						} catch (IOException err)
						{
							log.error("Could not close file reader.", err);
						}
					}
				}
			} else
			{
				panel.setValidFileSelected(false);
				panel.setDescription("");
				panel.setNumSamples(0);
				panel.clearKeyValue();
			}
		}
		
		
		private String getBaseDir(final String filename)
		{
			File f = new File(filename);
			final String basePath;
			if (f.isDirectory())
			{
				basePath = f.getAbsolutePath();
			} else
			{
				basePath = f.getParent();
			}
			return basePath;
		}
		
		
		@Override
		public void onBallProcessor(final List<String> filenames)
		{
			for (String selectedFile : filenames)
			{
				String basePath = getBaseDir(selectedFile);
				File f = new File(basePath);
				if (!f.exists())
				{
					return;
				}
				BallProcessor.runOnData(basePath);
			}
		}
		
		
		@Override
		public void onBallCorrector(final List<String> filenames)
		{
			for (String selectedFile : filenames)
			{
				String basePath = getBaseDir(selectedFile);
				File f = new File(basePath);
				if (!f.exists())
				{
					return;
				}
				BallCorrector.runOnData(basePath);
			}
		}
		
		
		@Override
		public void onBotProcessor(final List<String> filenames)
		{
			for (String selectedFile : filenames)
			{
				String basePath = getBaseDir(selectedFile);
				File f = new File(basePath);
				if (!f.exists())
				{
					return;
				}
				BotProcessor.runOnData(basePath);
			}
		}
		
		
		@Override
		public void onCopy(final List<String> filenames)
		{
			StringBuilder sb = new StringBuilder();
			for (String s : filenames)
			{
				sb.append(s);
				sb.append('\n');
			}
			if (sb.length() > 0)
			{
				sb.deleteCharAt(sb.length() - 1);
			}
			StringSelection stringSelection = new StringSelection(sb.toString());
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, this);
		}
		
		
		@Override
		public void lostOwnership(final Clipboard clipboard, final Transferable contents)
		{
		}
	}
	
	private class BallWatcherObserver implements IBallWatcherObserver
	{
		@Override
		public void postProcessing(final String fileName)
		{
			panel.updateFiles();
			panel.setRecording(false);
			ballWatcher = null;
		}
		
		
		@Override
		public void beforeExport(final Map<String, Object> jsonMapping)
		{
		}
		
		
		@Override
		public void onAddCustomData(final ExportDataContainer container, final MergedCamDetectionFrame frame)
		{
			panel.setCurrentNumSamples(ballWatcher.getDataSize());
		}
	}
}
