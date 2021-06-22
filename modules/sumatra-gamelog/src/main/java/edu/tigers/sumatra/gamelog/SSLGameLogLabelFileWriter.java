/*
 * Copyright (c) 2009 - 2020, DHBW Mannheim - TIGERs Mannheim
 */
package edu.tigers.sumatra.gamelog;

import edu.tigers.sumatra.gamelog.proto.LogLabels;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;



/**
 * Writes a Label File for the game log event labeling technical challenge
 */
public class SSLGameLogLabelFileWriter
{
	private static final Logger log = LogManager.getLogger(SSLGameLogLabelFileWriter.class.getName());

    private DataOutputStream outputStream;


    /**
     * Open a file for writing.
     * Suppress missing finally block and status code return ignorance
     *
     * @param fullName
     */
    public void openPath(final String fullName)
    {
        try
        {
            // open file
            outputStream = new DataOutputStream(new FileOutputStream(fullName, false));

        } catch (IOException e)
        {
            log.error("Exception on opening log label file", e);
        }
    }


    /**
     * @return whether a file is selected and open (output stream is set)
     */
    public boolean isOpen()
    {
        return outputStream != null;
    }


    /**
     * Close output stream
     */
    public synchronized void close()
    {
        if (outputStream != null)
        {
            try
            {
                outputStream.close();
            } catch (IOException e)
            {
                log.error("Exception on closing label file", e);
            }
            outputStream = null;
        }
    }


    /**
     * Write bytes of protobuf labels massage to file.
     *
     * @param labels
     */
    public synchronized void write(final LogLabels.Labels labels)
    {
        if (outputStream == null)
        {
            return;
        }

        try
        {
            outputStream.write(labels.toByteArray());
        }
        catch (IOException e)
        {
            log.error("Exception writing to gamelog", e);
        }

    }
}
