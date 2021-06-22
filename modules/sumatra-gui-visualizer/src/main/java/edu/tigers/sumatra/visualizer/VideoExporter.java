/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */

package edu.tigers.sumatra.visualizer;

import edu.tigers.sumatra.math.Hysteresis;
import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;
import lombok.extern.log4j.Log4j2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;


@Log4j2
public class VideoExporter implements Runnable
{
	private Muxer muxer;
	private Encoder encoder;
	private MediaPacket packet;
	private MediaPicture picture;
	private int index = 0;
	private boolean initialized = false;
	private String filepath;
	private BlockingDeque<BufferedImage> frameBuffer = new LinkedBlockingDeque<>();
	private final Hysteresis maxFramesInBufferHyst = new Hysteresis(300, 350);

	private static final PixelFormat.Type PIXEL_FORMAT = PixelFormat.Type.PIX_FMT_YUV420P;
	private static final String VIDEO_FORMAT = "mp4";


	public boolean open(String filename, int width, int height) throws IOException
	{
		this.filepath = filename;
		final Rational framerate = Rational.make(1, VisualizerPresenter.getVisualizationFps());

		muxer = Muxer.make(filename, null, VIDEO_FORMAT);

		final MuxerFormat format = muxer.getFormat();
		final Codec codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());

		encoder = Encoder.make(codec);

		encoder.setWidth(width);
		encoder.setHeight(height);

		encoder.setPixelFormat(PIXEL_FORMAT);
		encoder.setTimeBase(framerate);

		if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER))
		{
			encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
		}

		encoder.open(null, null);
		muxer.addNewStream(encoder);

		try
		{
			muxer.open(null, null);
		} catch (InterruptedException e)
		{
			log.error("Could not open video muxer", e);
			Thread.currentThread().interrupt();
			return false;
		}

		picture = MediaPicture.make(
				encoder.getWidth(),
				encoder.getHeight(),
				PIXEL_FORMAT);
		picture.setTimeBase(framerate);

		packet = MediaPacket.make();
		initialized = true;
		frameBuffer.clear();
		final Thread thread = new Thread(this, "VideoExporter");
		thread.start();
		return true;
	}


	public void close()
	{
		initialized = false;
		log.info(
				"Closing video recording, still " + frameBuffer.size() + " frames to process, closing may take a while...");
	}


	/**
	 * Records the screen
	 */
	public void addImageToVideo(BufferedImage image)
	{
		boolean maxReachedThisFrame = maxFramesInBufferHyst.isLower();
		maxFramesInBufferHyst.update(frameBuffer.size());
		maxReachedThisFrame = maxReachedThisFrame && maxFramesInBufferHyst.isUpper();
		if (maxFramesInBufferHyst.isUpper())
		{
			if (maxReachedThisFrame)
			{
				log.warn("slow video processing, starting to drop frames. Try a lower resolution next time.");
			}
		} else
		{
			frameBuffer.add(image);
		}
	}


	private void handleFrame(final BufferedImage image)
	{
		final BufferedImage screen = convertToType(image, BufferedImage.TYPE_3BYTE_BGR);
		MediaPictureConverter converter = MediaPictureConverterFactory.createConverter(screen, picture);
		converter.toPicture(picture, screen, index);

		do
		{
			encoder.encode(packet, picture);
			if (packet.isComplete())
			{
				muxer.write(packet, false);
			}
		} while (packet.isComplete());
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
	private static BufferedImage convertToType(BufferedImage sourceImage,
			int targetType)
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


	public boolean isInitialized()
	{
		return initialized;
	}


	@Override
	public void run()
	{
		while (initialized || !frameBuffer.isEmpty())
		{
			try
			{
				handleFrame(frameBuffer.pollFirst(200, TimeUnit.MILLISECONDS));
			} catch (InterruptedException e)
			{
				Thread.currentThread().interrupt();
			}
		}

		do
		{
			encoder.encode(packet, null);
			if (packet.isComplete())
			{
				muxer.write(packet, false);
			}
		} while (packet.isComplete());
		muxer.close();
		log.info("Finished recording video: " + filepath);
	}
}