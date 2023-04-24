package com.pablomunuera.spriteanimation;

import static com.badlogic.gdx.Input.Keys.DOWN;
import static com.badlogic.gdx.Input.Keys.LEFT;
import static com.badlogic.gdx.Input.Keys.RIGHT;
import static com.badlogic.gdx.Input.Keys.UP;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.czyzby.websocket.WebSocket;
import com.github.czyzby.websocket.WebSocketListener;
import com.github.czyzby.websocket.WebSockets;

public class SpriteAnimation extends ApplicationAdapter {
	WebSocket socket;
	String address = "localhost";
	int port = 8888;
	// Constant rows and columns of the sprite sheet
	private static final int FRAME_COLS = 8, FRAME_ROWS = 1;
	Rectangle up, down, left, right, fire;
	final int IDLE=0, UP=1, DOWN=2, LEFT=3, RIGHT=4;
	// Objects used
	Animation<TextureRegion> walkRightAnimation; // Must declare frame type (TextureRegion)
	Animation<TextureRegion> walkLeftAnimation;
	Texture walkRightSheet;
	Texture walkLeftSheet;
	SpriteBatch spriteBatch;
	OrthographicCamera camera;
	TextureRegion currentFrame;
	float posX,posY;
	// A variable for tracking elapsed time for the animation
	float stateTime;
	float lastSend=0.0f;

	@Override
	public void create() {
		if( Gdx.app.getType()== Application.ApplicationType.Android )
			// en Android el host és accessible per 10.0.2.2
			address = "10.0.2.2";
		socket = WebSockets.newSocket(WebSockets.toWebSocketUrl(address, port));
		socket.setSendGracefully(false);
		socket.addListener((WebSocketListener) new MyWSListener());
		socket.connect();
		socket.send("Enviar dades");
		up = new Rectangle(0, 480*2/3, 800, 480/3);
		down = new Rectangle(0, 0, 800, 480/3);
		left = new Rectangle(0, 0, 800/3, 480);
		right = new Rectangle(800*2/3, 0, 800/3, 480);
		// Load the sprite sheet as a Texture
		walkRightSheet = new Texture(Gdx.files.internal("scottpilgrimRight.png"));
		walkLeftSheet = new Texture(Gdx.files.internal("scottpilgrimLeft.png"));

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		// Use the split utility method to create a 2D array of TextureRegions. This is
		// possible because this sprite sheet contains frames of equal size and they are
		// all aligned.
		TextureRegion[][] tmpRight = TextureRegion.split(walkRightSheet,
				walkRightSheet.getWidth() / FRAME_COLS,
				walkRightSheet.getHeight() / FRAME_ROWS);
		TextureRegion[][] tmpLeft = TextureRegion.split(walkLeftSheet,
				walkRightSheet.getWidth() / FRAME_COLS,
				walkRightSheet.getHeight() / FRAME_ROWS);

		// Place the regions into a 1D array in the correct order, starting from the top
		// left, going across first. The Animation constructor requires a 1D array.
		TextureRegion[] walkRightFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		TextureRegion[] walkLeftFrames = new TextureRegion[FRAME_COLS * FRAME_ROWS];
		int indexRight = 0;
		int indexLeft = 0;
		for (int i = 0; i < FRAME_ROWS; i++) {
			for (int j = 0; j < FRAME_COLS; j++) {
				walkRightFrames[indexRight++] = tmpRight[i][j];
				walkLeftFrames[indexLeft++] = tmpLeft[i][j];
			}
		}

		// Initialize the Animation with the frame interval and array of frames
		walkRightAnimation = new Animation<TextureRegion>(0.075f, walkRightFrames);
		walkLeftAnimation = new Animation<TextureRegion>(0.075f, walkLeftFrames);

		// Instantiate a SpriteBatch for drawing and reset the elapsed animation
		// time to 0
		spriteBatch = new SpriteBatch();
		stateTime = 0f;

		currentFrame=walkRightAnimation.getKeyFrame(stateTime, true);

	}

	@Override
	public void render() {
		if( stateTime-lastSend > 1.0f ) {
			lastSend = stateTime;
			socket.send("Enviar dades");
		}
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // Clear screen
		stateTime += Gdx.graphics.getDeltaTime(); // Accumulate elapsed animation time

		// Get current frame of animation for the current stateTime
		int dir=virtual_joystick_control();
		if(dir==4){
			posX=posX+3;
			currentFrame = walkRightAnimation.getKeyFrame(stateTime, true);
			spriteBatch.begin();
			spriteBatch.draw(currentFrame, posX, posY,450,450);
			spriteBatch.end();

		}
		else if(dir==3){
			posX=posX-3;
			currentFrame = walkLeftAnimation.getKeyFrame(stateTime, true);
			spriteBatch.begin();
			spriteBatch.draw(currentFrame, posX, posY,450,450);
			spriteBatch.end();

		}
		else if(dir==2){
			currentFrame = walkRightAnimation.getKeyFrame(stateTime, true);
			spriteBatch.begin();
			spriteBatch.draw(currentFrame, posX, posY,450,450);
			spriteBatch.end();
		}
		else if(dir==1){
			currentFrame = walkLeftAnimation.getKeyFrame(stateTime, true);
			spriteBatch.begin();
			spriteBatch.draw(currentFrame, posX, posY,450,450);
			spriteBatch.end();
		}
		else{
			spriteBatch.begin();
			spriteBatch.draw(currentFrame, posX, posY,450,450);
			spriteBatch.end();
		}
	}

	@Override
	public void dispose() { // SpriteBatches and Textures must always be disposed
		spriteBatch.dispose();
		walkRightSheet.dispose();
		walkLeftSheet.dispose();
	}
	protected int virtual_joystick_control() {
		// iterar per multitouch
		// cada "i" és un possible "touch" d'un dit a la pantalla
		for(int i=0;i<10;i++)
			if (Gdx.input.isTouched(i)) {
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
				// traducció de coordenades reals (depen del dispositiu) a 800x480
				camera.unproject(touchPos);
				if (up.contains(touchPos.x, touchPos.y)) {
					posY=posY+3;
					return UP;
				} else if (down.contains(touchPos.x, touchPos.y)) {
					posY=posY-3;
					return DOWN;
				} else if (left.contains(touchPos.x, touchPos.y)) {
					return LEFT;
				} else if (right.contains(touchPos.x, touchPos.y)) {
					return RIGHT;
				}
			}
		return IDLE;
	}
}
class MyWSListener implements WebSocketListener {

	@Override
	public boolean onOpen(WebSocket webSocket) {
		System.out.println("Opening...");
		return false;
	}

	@Override
	public boolean onClose(WebSocket webSocket, int closeCode, String reason) {
		System.out.println("Closing...");
		return false;
	}

	@Override
	public boolean onMessage(WebSocket webSocket, String packet) {
		System.out.println("Message:");
		return false;
	}

	@Override
	public boolean onMessage(WebSocket webSocket, byte[] packet) {
		System.out.println("Message:");
		return false;
	}

	@Override
	public boolean onError(WebSocket webSocket, Throwable error) {
		System.out.println("ERROR:"+error.toString());
		return false;
	}
}