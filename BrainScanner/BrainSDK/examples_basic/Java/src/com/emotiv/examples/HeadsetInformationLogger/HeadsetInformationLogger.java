/****************************************************************************
**
** Copyright 2015 by Emotiv. All rights reserved
** Example - Headset Information Logger
** This example allows getting headset infor: contactquality, wireless strength
** battery level.  
** This example work on single connection.
****************************************************************************/

package com.emotiv.examples.HeadsetInformationLogger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import com.emotiv.Iedk.*;
import com.emotiv.Iedk.EmoState.IEE_InputChannels_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;
import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory.Default;

/** Simple example of JNA interface mapping and usage. */
public class HeadsetInformationLogger {
	public static void main(String[] args) {
		Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
		Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();
		IntByReference userID = null;
		int state = 0;
		boolean onStateChanged = false;
		boolean readytocollect = false;
		IntByReference batteryLevel = new IntByReference(0);
		IntByReference maxBatteryLevel = new IntByReference(0);

		userID = new IntByReference(0);

		if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") != 
				EdkErrorCode.EDK_OK.ToInt()) {
			System.out.println("Emotiv Engine start up failed.");
			return;
		}
		
		int x = 0;
		while (x<1000) {
			x++;
			int count= 0;
			float excitement = 0;
			float focus = 0;
			float stress = 0;
			float relaxation = 0;
			while (count < 30000) {
				state = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);
				try {
					Thread.sleep(2000);
					count = count + 3000;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// New event needs to be handled
				if (state == EdkErrorCode.EDK_OK.ToInt()) {
	
					int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);
					Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, userID);
					
					switch(eventType)
					{
						case 0x0010:
							System.out.println("User added");
							readytocollect = true;
							break;
						case 0x0020:
							System.out.println("User removed");
							readytocollect = false; 		//just single connection
							break;
						case 0x0040:
							onStateChanged = true;
							Edk.INSTANCE.IEE_EmoEngineEventGetEmoState(eEvent, eState);
							break;
						default:
							break;
					}
					
					if (readytocollect && onStateChanged)
					{
						float timestamp = EmoState.INSTANCE.IS_GetTimeFromStart(eState);
						System.out.print(timestamp + ", ");
						System.out.println("Excitement = " + EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState));
						System.out.println("Stress = " + EmoState.INSTANCE.ES_AffectivGetFrustrationScore(eState));
						System.out.println("Relaxed = " + EmoState.INSTANCE.ES_AffectivGetMeditationScore(eState));
						System.out.println("Focus = " + EmoState.INSTANCE.ES_AffectivGetEngagementBoredomScore(eState));
						System.out.println(" ");
						focus = (focus + EmoState.INSTANCE.ES_AffectivGetEngagementBoredomScore(eState));
						relaxation = (relaxation + EmoState.INSTANCE.ES_AffectivGetMeditationScore(eState));
						stress = (stress + EmoState.INSTANCE.ES_AffectivGetFrustrationScore(eState));
						excitement = (excitement + EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState));	
					}
				} else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
					System.out.println("Internal error in Emotiv Engine!");
					break;
				}
			}
		
		excitement = excitement/10;
		focus = focus/10;
		relaxation = relaxation/10;
		stress = stress/10;
			
		if (excitement < 0 || excitement > 1) {
			excitement = 1/2;
		}
		else if (stress < 0 || stress > 1) {
			stress = 1/2;
		}
		else if (relaxation < 0 || relaxation > 1) {
			relaxation = 1/2;
		}
		else if (focus < 0 || focus > 1) {
			focus = 1/2;
		}

		PrintWriter output;
		try {
			output = new PrintWriter("C:/Users/User/Desktop/Junction/YabaDABaDo-SpotifyHack/WebInterface/web-api-auth-examples-master/authorization_code/public/BrainData/CurrentBrainOutput.txt", "UTF-8");
			output.print(String.format("%.0f",excitement*100) + "," + String.format("%.0f",stress*100) + "," + String.format("%.0f",relaxation*100) + "," + String.format("%.0f",focus*100));
			output.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		System.out.println("FINAL OUTPUT");
		System.out.println("Excitement = " + excitement);
		System.out.println("Stress = " + stress);
		System.out.println("Relaxed = " + relaxation);
		System.out.println("Focus = " + focus);
		System.out.println(" ");
		}
		
		Edk.INSTANCE.IEE_EngineDisconnect();
		System.out.println("Disconnected!");
	}
}
