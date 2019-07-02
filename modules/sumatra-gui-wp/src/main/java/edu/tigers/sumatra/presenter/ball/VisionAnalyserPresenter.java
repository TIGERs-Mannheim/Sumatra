/*
 * Copyright (c) 2009 - 2018, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.presenter.ball;

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

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.tigers.sumatra.data.collector.ITimeSeriesDataCollectorObserver;
import edu.tigers.sumatra.data.collector.TimeSeriesDataCollector;
import edu.tigers.sumatra.matlab.MatlabConnection;
import edu.tigers.sumatra.view.ball.IBallAnalyserPanelObserver;
import edu.tigers.sumatra.view.ball.VisionAnalyserPanel;
import edu.tigers.sumatra.views.ASumatraViewPresenter;
import edu.tigers.sumatra.views.ISumatraView;
import edu.tigers.sumatra.wp.util.TimeSeriesDataCollectorFactory;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class VisionAnalyserPresenter extends ASumatraViewPresenter
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(VisionAnalyserPresenter.class.getName());
	
	private final VisionAnalyserPanel panel = new VisionAnalyserPanel();
	private TimeSeriesDataCollector dataCollector = null;
	
	
	/**
	 * Default Constructor
	 */
	public VisionAnalyserPresenter()
	{
		panel.addObserver(new PanelObserver());
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
	
	
	private class PanelObserver implements IBallAnalyserPanelObserver, ClipboardOwner
	{
		private static final String ERROR_EVALUATING_MATLAB_FUNCTION = "Error evaluating matlab function: ";
		
		private static final String DESCRIPTION = "description";
		private static final String LEARNING = "addpath('learning')";
		
		
		@Override
		public void onSave(final String filename)
		{
			File infoFile = new File(getBaseDir(filename) + "/info.json");
			if (infoFile.exists())
			{
				JSONParser jp = new JSONParser();
				try (FileReader fr = new FileReader(infoFile))
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String, Object>) jp.parse(fr);
					map.put(DESCRIPTION, panel.getDescription());
					JSONObject jo = new JSONObject(map);
					Files.write(Paths.get(infoFile.getAbsolutePath()), jo.toJSONString().getBytes());
				} catch (IOException | ParseException err)
				{
					log.error("Could not parse JSON.", err);
				} catch (ClassCastException err)
				{
					log.error("Could not cast a type. Wrong format?!", err);
				}
			}
			panel.markDirty(false);
		}
		
		
		@Override
		public void onRecord(final boolean record, final boolean stopAutomatically)
		{
			if (record)
			{
				if (dataCollector != null)
				{
					log.warn("Last ball watcher was not removed?!");
					dataCollector.stopExport();
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
				String fileName = "manual/" + sdf.format(new Date());
				dataCollector = TimeSeriesDataCollectorFactory.createFullCollector(fileName);
				dataCollector.addObserver(new TimeSeriesDataCollectorObserver());
				dataCollector.setStopAutomatically(stopAutomatically);
				boolean started = dataCollector.start();
				panel.setRecording(started);
				if (!started)
				{
					dataCollector = null;
				}
			} else
			{
				dataCollector.stopExport();
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
					processYesDeleteAnswerForFile(filename);
				}
				panel.updateFiles();
			}
		}
		
		
		private void processYesDeleteAnswerForFile(final String filename)
		{
			File file = new File(filename);
			if (file.isDirectory())
			{
				File[] files = file.listFiles();
				deleteFiles(file, files);
			}
			if (!file.delete())
			{
				log.error("Could not delete file: " + filename);
			}
		}
		
		
		private void deleteFiles(final File originalFile, final File[] files)
		{
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
						log.warn("Could not delete file: " + originalFile.getAbsolutePath());
					}
				}
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
				callMatlab("plotVisionData", basePath);
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
				log.error(ERROR_EVALUATING_MATLAB_FUNCTION + err.getMessage(), err);
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
				try (FileReader fr = new FileReader(infoFile))
				{
					@SuppressWarnings("unchecked")
					Map<String, Object> jo = (Map<String, Object>) jp.parse(fr);
					String description = (String) jo.get(DESCRIPTION);
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
					
					processJsonbjects(jo);
				} catch (IOException | ParseException err)
				{
					log.error("Could not parse JSON.", err);
				} catch (ClassCastException err)
				{
					log.error("Could not cast a type. Wrong format?!", err);
				}
			} else
			{
				panel.setValidFileSelected(false);
				panel.setDescription("");
				panel.setNumSamples(0);
				panel.clearKeyValue();
			}
		}
		
		
		void processJsonbjects(final Map<String, Object> jsonObjects)
		{
			for (Map.Entry<String, Object> entry : jsonObjects.entrySet())
			{
				if (DESCRIPTION.equals(entry.getKey()) || "numSamples".equals(entry.getKey()))
				{
					continue;
				}
				panel.setKeyValue(entry.getKey(), String.valueOf(entry.getValue()));
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
			// nothing to do here
		}
		
		
		@Override
		public void onPlotCrookedKick(final List<String> filenames)
		{
			MatlabProxy mp;
			try
			{
				mp = MatlabConnection.getMatlabProxy();
				mp.eval(LEARNING);
				mp.returningFeval("crookedKick", 1,
						filenames.toArray(new Object[filenames.size()]));
			} catch (MatlabConnectionException err)
			{
				log.error(err.getMessage(), err);
			} catch (MatlabInvocationException err)
			{
				log.error(ERROR_EVALUATING_MATLAB_FUNCTION + err.getMessage(), err);
			}
			
		}
	}
	
	private class TimeSeriesDataCollectorObserver implements ITimeSeriesDataCollectorObserver
	{
		@Override
		public void postProcessing(final String fileName)
		{
			panel.updateFiles();
			panel.setRecording(false);
			dataCollector = null;
		}
	}
}
