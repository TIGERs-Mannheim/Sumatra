/*
 * *********************************************************
 * Copyright (c) 2009 - 2015, DHBW Mannheim - Tigers Mannheim
 * Project: TIGERS - Sumatra
 * Date: Apr 29, 2015
 * Author(s): Nicolai Ommer <nicolai.ommer@gmail.com>
 * *********************************************************
 */
package edu.tigers.sumatra.augm;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import edu.dhbw.mannheim.tigers.sumatra.proto.AugmWrapperProtos.AugmWrapper;
import edu.tigers.sumatra.ids.ETeamColor;
import edu.tigers.sumatra.model.SumatraModel;
import edu.tigers.sumatra.persistance.RecordBerkeleyPersistence;


/**
 * @author Nicolai Ommer <nicolai.ommer@gmail.com>
 */
public class BerkeleyAugmWrapperExporter
{
	@SuppressWarnings("unused")
	private static final Logger log;
	
	
	static
	{
		SumatraModel.changeLogLevel(Level.INFO);
		log = Logger.getLogger(BerkeleyAugmWrapperExporter.class.getName());
	}
	
	private final String	workingDir;
	
	private final String	fileEnding				= "MTS";
	@SuppressWarnings("unused") // FIXME
	private String			geometry					= "Lab.xml";
	private final String	outputVideoFilename	= "out";
	@SuppressWarnings("unused") // FIXME
	private ETeamColor	ourTeamColor			= ETeamColor.BLUE;
	private long			timestampOffset		= 6000;
	
	
	/**
	 * @param workingDir
	 */
	public BerkeleyAugmWrapperExporter(final String workingDir)
	{
		this.workingDir = workingDir;
	}
	
	
	private int findNextGab(final int idxStart, final AiDataBuffer aiData)
	{
		int idxPre = idxStart;
		for (int i = idxStart; i < aiData.size(); i++)
		{
			if ((aiData.get(i).getTimestamp() - aiData.get(idxPre).getTimestamp()) > 1000)
			{
				break;
			}
			idxPre = i;
		}
		return idxPre;
	}
	
	
	private int skipForward(final int idxStart, final AiDataBuffer aiData, final long timestamp)
	{
		for (int i = idxStart; i < aiData.size(); i++)
		{
			if (aiData.get(i).getTimestamp() >= timestamp)
			{
				return i;
			}
		}
		return aiData.size() - 1;
	}
	
	
	/**
	 * @param videoFiles
	 * @param parallel
	 */
	public void cutVideos(final List<VideoCut> videoFiles, final boolean parallel)
	{
		Stream<VideoCut> stream;
		if (parallel)
		{
			stream = videoFiles.parallelStream();
		} else
		{
			stream = videoFiles.stream();
		}
		stream.forEach(cut -> cutVideo(cut.inName, cut.cutStart, cut.cutDuration, cut.outName));
	}
	
	
	private void cutVideo(final String filename, final double tStart, final double duration, final String outFilename)
	{
		ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", filename, "-ss", String.valueOf(tStart), "-t",
				String.valueOf(duration), "-c", "copy", outFilename);
		executeCommand(pb);
		log.info("ffmpeg copy done.");
	}
	
	
	/**
	 * @param videoCuts
	 */
	public void mergeVideos(final List<VideoCut> videoCuts)
	{
		StringBuilder sb = new StringBuilder();
		for (VideoCut cut : videoCuts)
		{
			sb.append("file ");
			sb.append(cut.outName);
			sb.append('\n');
		}
		try
		{
			Files.write(Paths.get(workingDir, "outFiles.txt"), sb.toString().getBytes());
			log.info("Starting merging videos");
			ProcessBuilder pb = new ProcessBuilder("ffmpeg",
					"-f", "concat",
					"-i", workingDir + "/outFiles.txt",
					"-c", "copy",
					workingDir + "/" + outputVideoFilename + "." + fileEnding);
			executeCommand(pb);
			log.info("ffmpeg concat done.");
		} catch (IOException err)
		{
			log.error("Could not write outFiles.txt", err);
		}
	}
	
	
	/**
	 * Length of video file
	 * 
	 * @param filename
	 * @return [s]
	 */
	private double getDuration(final String filename)
	{
		ProcessBuilder pb = new ProcessBuilder("exiftool", "-duration", "-n", "-s3", filename);
		String out = executeCommand(pb, false);
		return Double.valueOf(out);
	}
	
	
	/**
	 * timestamp in video file
	 * 
	 * @param filename
	 * @return [s]
	 */
	private long getStarttime(final String filename)
	{
		ProcessBuilder pb = new ProcessBuilder("exiftool", "-DateTimeOriginal", "-d", "'%s'", "-s3", filename);
		String out = executeCommand(pb, false);
		return Long.valueOf(out.replaceAll("'", "").trim()) * 1000L;
	}
	
	
	/**
	 * @param fileName
	 * @return
	 */
	private double getFps(final String fileName)
	{
		ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-i", fileName);
		String out = executeCommand(pb, true);
		Pattern pattern = Pattern.compile(".*, ([0-9]*) fps.*");
		Matcher matcher = pattern.matcher(out.replaceAll("\n", " "));
		if (matcher.matches())
		{
			String strFps = matcher.group(1);
			return Double.valueOf(strFps);
		}
		log.error("Could not read fps in output String: " + out);
		return 0;
	}
	
	
	private String readInputStream(final InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		
		StringBuilder sb = new StringBuilder();
		try
		{
			String line;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
				sb.append('\n');
			}
		} catch (IOException err)
		{
			log.error("Could not read command output");
		}
		return sb.toString();
	}
	
	
	private String executeCommand(final ProcessBuilder pb, final boolean expectError)
	{
		Process child;
		try
		{
			log.info("Executing os command: " + StringUtils.join(pb.command(), " "));
			child = pb.start();
			child.waitFor();
			
			String errorOut = readInputStream(child.getErrorStream());
			if (!expectError && (child.exitValue() != 0))
			{
				log.error("Error during command execution:\n" + errorOut);
			}
			return readInputStream(child.getInputStream()) + errorOut;
		} catch (IOException err1)
		{
			log.error("Could not execute: " + pb.toString(), err1);
		} catch (InterruptedException err)
		{
			log.error("Interrupted while waiting for command");
		}
		return "";
	}
	
	
	private void executeCommand(final ProcessBuilder pb)
	{
		Process child;
		try
		{
			log.info("Executing os command: " + StringUtils.join(pb.command(), " "));
			pb.inheritIO();
			child = pb.start();
			child.waitFor();
		} catch (IOException err1)
		{
			log.error("Could not execute: " + pb.toString(), err1);
		} catch (InterruptedException err)
		{
			log.error("Interrupted while waiting for command");
		}
	}
	
	
	private VideoFile readFile(final String filename)
	{
		log.info("Read " + filename);
		VideoFile vf = new VideoFile();
		vf.filename = filename;
		vf.duration = getDuration(filename);
		vf.starttime = getStarttime(filename) + timestampOffset;
		vf.endtime = vf.starttime + (long) (vf.duration * 1000);
		vf.fps = getFps(filename);
		log.info(vf);
		return vf;
	}
	
	
	private List<VideoFile> readVideoFiles(final String videoDir)
	{
		try
		{
			return Files.list(Paths.get(videoDir))
					.filter(p -> p.toFile().getName().endsWith("." + fileEnding))
					.filter(p -> !p.toFile().getName().equals(outputVideoFilename + "." + fileEnding))
					.sorted()
					.map(p -> readFile(p.toFile().getAbsolutePath()))
					.collect(Collectors.toList());
		} catch (IOException err)
		{
			log.error("Could not read video files", err);
		}
		return Collections.emptyList();
	}
	
	
	private List<VideoCut> collectVideoCutInformation(final VideoFile videoFile, final AiDataBuffer aiData)
	{
		List<VideoCut> cuts = new ArrayList<>();
		if (aiData.isEmpty())
		{
			return cuts;
		}
		log.info("Processing " + videoFile.filename);
		long tStart = Math.max(videoFile.starttime, aiData.get(0).getTimestamp());
		long tMax = Math.min(videoFile.endtime, aiData.get(aiData.size() - 1).getTimestamp());
		int tsIdx = skipForward(0, aiData, tStart);
		while ((tStart < tMax) && (tsIdx < (aiData.size() - 1)))
		{
			tStart = aiData.get(tsIdx).getTimestamp();
			tsIdx = findNextGab(tsIdx, aiData);
			long tEnd = Math.min(tMax, aiData.get(tsIdx).getTimestamp());
			long dur = tEnd - tStart;
			if ((dur > 1000))
			{
				cuts.add(createVideoCut(videoFile, tStart, dur));
			}
			tsIdx++;
		}
		return cuts;
	}
	
	
	private VideoCut createVideoCut(final VideoFile videoFile, final long tStart, final long dur)
	{
		VideoCut cut = new VideoCut();
		cut.cutStarttime = tStart;
		cut.cutStart = ((tStart - videoFile.starttime) / 1000.0);
		cut.cutDuration = dur / 1000.0;
		File f = new File(videoFile.filename);
		cut.inName = videoFile.filename;
		cut.outName = String.format("%s/t%d_%s", workingDir, tStart, f.getName());
		log.info(cut);
		return cut;
	}
	
	
	/**
	 * @param recordDbPath
	 * @param outDir
	 * @return
	 * @throws IOException
	 */
	public AiDataBuffer generateAiData(final String recordDbPath, final String outDir) throws IOException
	{
		RecordBerkeleyPersistence db = new RecordBerkeleyPersistence(recordDbPath, true);
		// AugmentedDataTransformer augmTransformer = new AugmentedDataTransformer();
		AiDataBuffer aiData = new AiDataBuffer(outDir);
		// int dbSize = db.size();
		// for (int posLower = 0; posLower < dbSize; posLower += BUFFER_SIZE)
		// {
		// List<RecordFrame> frames = db.load(posLower, BUFFER_SIZE);
		// for (RecordFrame recFrame : frames)
		// {
		// VisualizationFrame frame = recFrame.getVisFrame(ourTeamColor);
		// if (frame.getTeamColor() == ourTeamColor)
		// {
		// AugmWrapper wrapper = augmTransformer.createAugmWrapper(frame);
		// aiData.add(wrapper);
		// }
		// }
		// }
		aiData.flush();
		log.info("Created AI data for " + aiData.size() + " frames");
		db.close();
		return aiData;
	}
	
	
	/**
	 * @param videoFiles
	 * @param aiData
	 * @return
	 */
	public List<VideoCut> collectCutInformation(final List<VideoFile> videoFiles, final AiDataBuffer aiData)
	{
		List<VideoCut> cuts = new ArrayList<>();
		for (VideoFile vf : videoFiles)
		{
			cuts.addAll(collectVideoCutInformation(vf, aiData));
		}
		
		double totalDuration = cuts.stream().mapToDouble(cut -> cut.cutDuration).sum();
		double totalDurationOrig = videoFiles.stream().mapToDouble(f -> f.duration).sum();
		log.info("Collected " + cuts.size() + " cuts withwind a total length of " + totalDuration + "s from "
				+ videoFiles.size() + " files with a total length of " + totalDurationOrig);
		return cuts;
	}
	
	
	/**
	 * Get all ai data that is included in the video cuts
	 * 
	 * @param cuts
	 * @param aiDataIn
	 * @param aiDirOut
	 * @return
	 * @throws IOException
	 */
	public AiDataBuffer getOverlappingAiData(final List<VideoCut> cuts, final AiDataBuffer aiDataIn,
			final String aiDirOut) throws IOException
	{
		Collections.sort(cuts);
		AiDataBuffer aiDataOut = new AiDataBuffer(aiDirOut);
		aiDataOut.clear();
		
		int aiDataIdx = 0;
		long endTime = 0;
		for (VideoCut cut : cuts)
		{
			long startTime = cut.cutStarttime;
			if (startTime < endTime)
			{
				log.warn("Overlapping detected!");
			}
			endTime = startTime + (long) (cut.cutDuration * 1000);
			for (; aiDataIdx < aiDataIn.size(); aiDataIdx++)
			{
				AugmWrapper augm = aiDataIn.get(aiDataIdx);
				long aiStarttime = augm.getTimestamp();
				if (aiStarttime > startTime)
				{
					if (aiStarttime < endTime)
					{
						aiDataOut.add(augm);
					} else
					{
						break;
					}
				}
			}
		}
		aiDataOut.flush();
		
		log.info("Found " + aiDataOut.size() + " overlapping aiData frames within the " + aiDataIn.size()
				+ " original frames.");
		log.info("Should last for " + (aiDataOut.size() / 60.0) + "s with 60ps");
		
		return aiDataOut;
	}
	
	
	/**
	 * @param aiData
	 * @param fps
	 * @param outDir
	 * @return
	 * @throws IOException
	 */
	public AiDataBuffer reduceAiData(final AiDataBuffer aiData, final double fps, final String outDir)
			throws IOException
	{
		AiDataBuffer aiDataOut = new AiDataBuffer(outDir);
		aiDataOut.clear();
		int spf = (int) (1000f / fps);
		long firstTimestamp = -1;
		int frameId = 0;
		for (AugmWrapper w : aiData)
		{
			long timestamp = w.getTimestamp();
			if (firstTimestamp == -1)
			{
				firstTimestamp = timestamp;
			}
			if ((spf * frameId) <= (timestamp - firstTimestamp))
			{
				aiDataOut.add(w);
				frameId++;
			}
		}
		aiDataOut.flush();
		log.info("Reduced ai data from " + aiData.size() + " to " + aiDataOut.size());
		return aiDataOut;
	}
	
	private static class VideoFile implements Comparable<VideoFile>
	{
		String	filename;
		/** unix timestamp [ms] */
		long		starttime;
		long		endtime;
		double	duration;
		double	fps;
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("VideoFile [filename=");
			builder.append(filename);
			builder.append(", starttime=");
			builder.append(starttime);
			builder.append(", endtime=");
			builder.append(endtime);
			builder.append(", duration=");
			builder.append(duration);
			builder.append(", fps=");
			builder.append(fps);
			builder.append("]");
			return builder.toString();
		}
		
		
		@Override
		public int compareTo(final VideoFile o)
		{
			return filename.compareTo(o.filename);
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(duration);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			result = (prime * result) + (int) (endtime ^ (endtime >>> 32));
			result = (prime * result) + ((filename == null) ? 0 : filename.hashCode());
			temp = Double.doubleToLongBits(fps);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			result = (prime * result) + (int) (starttime ^ (starttime >>> 32));
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			VideoFile other = (VideoFile) obj;
			if (Double.doubleToLongBits(duration) != Double.doubleToLongBits(other.duration))
			{
				return false;
			}
			if (endtime != other.endtime)
			{
				return false;
			}
			if (filename == null)
			{
				if (other.filename != null)
				{
					return false;
				}
			} else if (!filename.equals(other.filename))
			{
				return false;
			}
			if (Double.doubleToLongBits(fps) != Double.doubleToLongBits(other.fps))
			{
				return false;
			}
			if (starttime != other.starttime)
			{
				return false;
			}
			return true;
		}
	}
	
	private static class VideoCut implements Comparable<VideoCut>
	{
		String	inName;
		String	outName;
		long		cutStarttime;
		double	cutStart;
		double	cutDuration;
		
		
		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("VideoCut [outName=");
			builder.append(outName);
			builder.append(", inName=");
			builder.append(inName);
			builder.append(", cutStarttime=");
			builder.append(cutStarttime);
			builder.append(", cutStart=");
			builder.append(cutStart);
			builder.append(", cutDuration=");
			builder.append(cutDuration);
			builder.append("]");
			return builder.toString();
		}
		
		
		@Override
		public int compareTo(final VideoCut o)
		{
			return Double.compare(cutStarttime, o.cutStarttime);
		}
		
		
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(cutDuration);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(cutStart);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(cutStarttime);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			result = (prime * result) + ((inName == null) ? 0 : inName.hashCode());
			result = (prime * result) + ((outName == null) ? 0 : outName.hashCode());
			return result;
		}
		
		
		@Override
		public boolean equals(final Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			VideoCut other = (VideoCut) obj;
			if (Double.doubleToLongBits(cutDuration) != Double.doubleToLongBits(other.cutDuration))
			{
				return false;
			}
			if (Double.doubleToLongBits(cutStart) != Double.doubleToLongBits(other.cutStart))
			{
				return false;
			}
			if (Double.doubleToLongBits(cutStarttime) != Double.doubleToLongBits(other.cutStarttime))
			{
				return false;
			}
			if (inName == null)
			{
				if (other.inName != null)
				{
					return false;
				}
			} else if (!inName.equals(other.inName))
			{
				return false;
			}
			if (outName == null)
			{
				if (other.outName != null)
				{
					return false;
				}
			} else if (!outName.equals(other.outName))
			{
				return false;
			}
			return true;
		}
	}
	
	
	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException
	{
		String videoDir = System.getProperty("user.home") + "/ownCloud/public/Videos/2015-04 Erlangen/ER-Force 5on5";
		String recordDbDir = "2015-04-26_10-59_ER-Force_vs_TIGERS_Mannheim";
		String workingDir = System.getProperty("user.home", "/tmp") + "/" + recordDbDir;
		String aiDir = workingDir + "/aiDataAll";
		String aiDirOverlapping = workingDir + "/aiDataTmp";
		String aiDirFiltered = workingDir + "/aiData";
		
		boolean reduceAiData = false;
		boolean cutVideos = false;
		
		BerkeleyAugmWrapperExporter exporter = new BerkeleyAugmWrapperExporter(workingDir);
		// may want to change some fields before init
		exporter.geometry = "Lab.xml";
		exporter.ourTeamColor = ETeamColor.BLUE;
		exporter.timestampOffset = 6000;
		
		AiDataBuffer aiData;
		if (!new File(aiDir).isDirectory())
		{
			aiData = exporter.generateAiData(recordDbDir, aiDir);
		} else
		{
			aiData = new AiDataBuffer(aiDir);
		}
		
		List<VideoFile> videoFiles = exporter.readVideoFiles(videoDir);
		List<VideoCut> videoCuts = exporter.collectCutInformation(videoFiles, aiData);
		AiDataBuffer overlappingAiData = exporter.getOverlappingAiData(videoCuts, aiData, aiDirOverlapping);
		
		if (reduceAiData)
		{
			double fps = videoFiles.get(0).fps;
			exporter.reduceAiData(overlappingAiData, fps, aiDirFiltered);
		}
		
		// do the cut and merge
		if (cutVideos)
		{
			exporter.cutVideos(videoCuts, true);
			exporter.mergeVideos(videoCuts);
		}
	}
}
