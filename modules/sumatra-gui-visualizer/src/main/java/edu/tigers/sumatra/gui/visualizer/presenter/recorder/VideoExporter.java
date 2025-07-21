/*
 * Copyright (c) 2009 - 2025, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.gui.visualizer.presenter.recorder;

import edu.tigers.sumatra.math.Hysteresis;
import io.humble.video.Codec;
import io.humble.video.Coder;
import io.humble.video.ContainerFormat;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.MediaSampled;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


@Log4j2
public class VideoExporter
{
	private static final PixelFormat.Type PIXEL_FORMAT = PixelFormat.Type.PIX_FMT_YUV420P;
	private static final String VIDEO_FORMAT = "mp4";
	private static final int FPS = 24;

	private final BlockingDeque<BufferedImage> frameBuffer = new LinkedBlockingDeque<>();
	private final Hysteresis maxFramesInBufferHyst = new Hysteresis(300, 350);
	private final MediaPacket packet = MediaPacket.make();

	private final Path filePath;
	private final Muxer muxer;
	private final Encoder encoder;
	private final MediaPicture picture;

	private int index = 0;
	private boolean running = true;


	public VideoExporter(Path filePath, int width, int height)
	{
		this.filePath = filePath;
		Rational framerate = Rational.make(1, FPS);

		muxer = Muxer.make(filePath.toString(), null, VIDEO_FORMAT);
		MuxerFormat format = muxer.getFormat();
		Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());

		encoder = Encoder.make(codec);
		encoder.setWidth(width);
		encoder.setHeight(height);
		encoder.setPixelFormat(PIXEL_FORMAT);
		encoder.setTimeBase(framerate);
		encoder.setFlag(Coder.Flag.FLAG_GLOBAL_HEADER, format.getFlag(ContainerFormat.Flag.GLOBAL_HEADER));
		encoder.open(null, null);

		picture = MediaPicture.make(
				encoder.getWidth(),
				encoder.getHeight(),
				PIXEL_FORMAT
		);
		picture.setTimeBase(framerate);
	}


	public void start() throws IOException
	{
		muxer.addNewStream(encoder);

		try
		{
			muxer.open(null, null);
		} catch (InterruptedException e)
		{
			Thread.currentThread().interrupt();
			throw new IOException("Interrupted while opening muxer", e);
		}

		maxFramesInBufferHyst.setOnUpperCallback(this::notifySlowVideoProcessing);

		Thread thread = new Thread(this::process, "VideoExporter");
		thread.setUncaughtExceptionHandler((t, e) -> log.error("Uncaught exception in {} thread", t.getName(), e));
		thread.start();
	}


	public void stop()
	{
		running = false;
		log.info("Stopping video recording, still {} frames to process, closing may take a while...", frameBuffer.size());
	}


	private void notifySlowVideoProcessing()
	{
		log.warn("slow video processing, starting to drop frames. Try a lower resolution next time.");
	}


	/**
	 * Records the screen
	 */
	public void addImageToVideo(BufferedImage image)
	{
		maxFramesInBufferHyst.update(frameBuffer.size());
		if (maxFramesInBufferHyst.isLower())
		{
			frameBuffer.add(image);
		}
	}


	private void handleFrame(BufferedImage image)
	{
		if (image == null)
		{
			return;
		}
		BufferedImage screen = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
		MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(screen, picture);
		converter.toPicture(picture, screen, index);

		encode(picture);
		index++;
	}


	/**
	 * Convert a {@link BufferedImage} of any type, to {@link BufferedImage} of a
	 * specified type. If the source image is the same type as the target type,
	 * then original image is returned, otherwise new image of the correct type is
	 * created and the content of the source image is copied into the new image.
	 *
	 * @param sourceImage the image to be converted
	 * @param targetType  the desired BufferedImage type
	 * @return a BufferedImage of the specifed target type.
	 * @see BufferedImage
	 */
	private BufferedImage convertToType(BufferedImage sourceImage, int targetType)
	{
		// if the source image is already the target type, return the source image
		if (sourceImage.getType() == targetType)
		{
			return sourceImage;
		}

		BufferedImage image = new BufferedImage(sourceImage.getWidth(),
				sourceImage.getHeight(), targetType);
		image.getGraphics().drawImage(sourceImage, 0, 0, null);
		return image;
	}


	private void encode(MediaSampled mediaSampled)
	{
		do
		{
			encoder.encode(packet, mediaSampled);
			if (packet.isComplete())
			{
				muxer.write(packet, false);
			}
		} while (packet.isComplete());
	}


	private void process()
	{
		while (running)
		{
			try
			{
				handleFrame(frameBuffer.pollFirst(200, TimeUnit.MILLISECONDS));
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}

		log.info("Start finalizing video");

		encode(null);
		muxer.close();
		log.info("Finished recording video to " + filePath);
	}
}
