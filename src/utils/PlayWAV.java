package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
	
	public static void play(String fileName, int playBackSpeed) {
		try {
			File file = new File(fileName);
			if(!file.exists()) {
				logger.error("[Playing Sound File] File not existing!");
				return;
			}
			//Sound s = new Sound(fileName);
			
			//int playBackSpeed = 1;  // play speed
			
			AudioInputStream ais = AudioSystem.getAudioInputStream(file);
			AudioFormat af = ais.getFormat();
			int frameSize = af.getFrameSize();
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] b = new byte[2^16];
	        int read = 1;
	        while( read>-1 ) {
	            read = ais.read(b);
	            if (read>0) {
	                baos.write(b, 0, read);
	            }
	        }
	        
	        byte[] b1 = baos.toByteArray();
	        System.out.println("b1.len=" + b1.length);
	        byte[] b2 = new byte[b1.length/playBackSpeed];
	        
	        int b2_ind = 0;
	        int b1_start = 0;
	        if(playBackSpeed == 1)
	        	b1_start = 0;
	        if(playBackSpeed == 2)
	        	b1_start = 1;
	        for(int i = b1_start; i < b1.length; i+=playBackSpeed) {
	        	b2[b2_ind] = b1[i];
	        	b2_ind++;
	        }
	        
	        ByteArrayInputStream bais = new ByteArrayInputStream(b2);
	        AudioInputStream aisAccelerated =
	            new AudioInputStream(bais, af, b2.length);
	        Clip clip = AudioSystem.getClip();
	        clip.open(aisAccelerated);
	        
            //clip.open(ais);
            clip.start();
            

            while (!clip.isRunning())
                Thread.sleep(1);
            while (clip.isRunning())
                Thread.sleep(1);

//            clip.close();
//            ais.close();
//            bais.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void play(String fileName) {
		play(fileName, 1);
	}
}
