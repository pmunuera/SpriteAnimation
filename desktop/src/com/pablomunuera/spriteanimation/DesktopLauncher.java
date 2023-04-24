package com.pablomunuera.spriteanimation;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.github.czyzby.websocket.CommonWebSockets;
import com.pablomunuera.spriteanimation.SpriteAnimation;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		CommonWebSockets.initiate();
		config.setTitle("SpriteAnimation");
		new Lwjgl3Application(new SpriteAnimation(), config);
	}
}
