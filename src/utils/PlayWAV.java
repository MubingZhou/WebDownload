package utils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
//import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.log4j.Logger;

public class PlayWAV {
	private static Logger logger = Logger.getLogger(PlayWAV.class);
	
	public static void play(String fileName) {
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				logger.error("[Playing Sound File] File not existing!");
				return;
			}
			
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip test = AudioSystem.getClip();  

            test.open(ais);
            test.start();

            while (!test.isRunning())
                Thread.sleep(10);
            while (test.isRunning())
                Thread.sleep(10);

            test.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
