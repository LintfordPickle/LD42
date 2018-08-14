package org.lintford.ld42.screens;

import static org.lwjgl.opengl.GL11.glClearColor;

import org.lintford.ld42.controllers.CarController;
import org.lintford.ld42.controllers.GameParticleController;
import org.lintford.ld42.controllers.GameStateController;
import org.lintford.ld42.controllers.IOWorldLoader;
import org.lintford.ld42.controllers.LevelController;
import org.lintford.ld42.controllers.MobController;
import org.lintford.ld42.data.World;
import org.lintford.ld42.renderers.CarRenderer;
import org.lintford.ld42.renderers.GameParticleRenderer;
import org.lintford.ld42.renderers.HUDRenderer;
import org.lintford.ld42.renderers.MissionRenderer;
import org.lintford.ld42.renderers.MobRenderer;
import org.lintford.ld42.renderers.WorldRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.lintford.library.controllers.camera.CameraFollowController;
import net.lintford.library.controllers.core.ControllerManager;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.AARectangle;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.BaseGameScreen;

public class GameWorldScreen extends BaseGameScreen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final int GRID_SIZE = 300;

	private final static float GAME_END_DELAY = 5000; // ms

	private final static int WIN_GAME_CREDIT_AMOUNT = 15500;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TextureBatch mTextureBatch;

	// Data
	World mWorld; // Collection of all data in world

	// Controllers
	IOWorldLoader mIOWorldLoaderController;
	CameraFollowController mCameraFollowController;
	GameStateController mGameStateController;
	CarController mCarController;
	LevelController mLevelController;
	MobController mMobController;
	GameParticleController mGameParticleController;

	// Renderers
	CarRenderer mCarRenderer;
	WorldRenderer mWorldRenderer;
	MobRenderer mMobRenderer;
	MissionRenderer mMissionRenderer;
	GameParticleRenderer mGameParticleRenderer;
	HUDRenderer mHUDRenderer;

	private boolean mGameIsLost;
	private boolean mGameIsWon;
	private float mEndingTimer;

	private boolean mIsPaused;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameWorldScreen(ScreenManager pScreenManager) {
		super(pScreenManager, null);

		mTextureBatch = new TextureBatch();

		mShowInBackground = true;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise() {
		super.initialise();

		createData();
		createControllers();
		createRenderers();

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mTextureBatch.loadGLContent(pResourceManager);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();

	}

	@Override
	public void handleInput(LintfordCore pCore, boolean pAcceptMouse, boolean pAcceptKeyboard) {

		if (pCore.input().keyDownTimed(GLFW.GLFW_KEY_ESCAPE) || pCore.input().keyDownTimed(GLFW.GLFW_KEY_P)) {
			mScreenManager.addScreen(new PauseScreen(mScreenManager));

			return;
		}

		super.handleInput(pCore, pAcceptMouse, pAcceptKeyboard);

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		if (pCoveredByOtherScreen) {
			mIsPaused = true;
			return;
		}

		mIsPaused = false;

		if (mWorld.carManager().playerCar().health <= 0) {
			mGameIsLost = true;

		}

		if (mWorld.gameState().credits() >= WIN_GAME_CREDIT_AMOUNT) {
			mGameIsWon = true;

		}

		if (mGameIsWon && mEndingTimer < GAME_END_DELAY) {
			mEndingTimer += pCore.time().elapseGameTimeMilli();

			if (mEndingTimer > GAME_END_DELAY) {
				mScreenManager.addScreen(new GameWonScreen(mScreenManager));
				return;

			}

		}

		if (mGameIsLost && mEndingTimer < GAME_END_DELAY) {
			mEndingTimer += pCore.time().elapseGameTimeMilli();

			if (mEndingTimer > GAME_END_DELAY) {
				mScreenManager.addScreen(new GameOverScreen(mScreenManager, mWorld.gameState().credits()));
				return;

			}

		}

		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

	}

	@Override
	public void draw(LintfordCore pCore) {
		glClearColor(60.0f / 255.0f, 49.0f / 255.0f, 156.0f / 255.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		super.draw(pCore);

		if (mIsPaused) {
			// If the game is paused, then darken the screen
			AARectangle lScreenRect = pCore.HUD().boundingRectangle();

			mTextureBatch.begin(pCore.HUD());
			mTextureBatch.draw(TextureManager.TEXTURE_CORE_UI, 0, 0, 32, 32, lScreenRect.left(), lScreenRect.top(), lScreenRect.w, lScreenRect.h, -0.01f, 0, 0, 0, 0.7f);
			mTextureBatch.end();

		}

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void createData() {
		mWorld = new World();
		mWorld.initialize();

	}

	
	private void createControllers() {
		ControllerManager lControllerManager = mScreenManager.core().controllerManager();

		mCameraFollowController = new CameraFollowController(lControllerManager, mScreenManager.core().gameCamera(), mWorld.carManager().playerCar(), entityGroupID);
		mCameraFollowController.trackPlayer(true);

		mIOWorldLoaderController = new IOWorldLoader(lControllerManager, mWorld, entityGroupID);
		mLevelController = new LevelController(lControllerManager, mWorld, entityGroupID);
		mCarController = new CarController(lControllerManager, mWorld, entityGroupID);
		mMobController = new MobController(lControllerManager, mWorld, entityGroupID);
		mGameStateController = new GameStateController(lControllerManager, mWorld, entityGroupID);
		mGameParticleController = new GameParticleController(lControllerManager, mWorld, entityGroupID);

		mLevelController.initialise(mScreenManager.core());
		mCarController.initialise(mScreenManager.core());
		mMobController.initialise(mScreenManager.core());
		mGameStateController.initialise(mScreenManager.core());
		mGameParticleController.initialise(mScreenManager.core());

		mIOWorldLoaderController.loadWorld("res//maps//textureWorldMap.png");

		// Set the player position to the spawn
		mWorld.carManager().playerCar().x = mWorld.level().playerSpawnPosition().x;
		mWorld.carManager().playerCar().y = mWorld.level().playerSpawnPosition().y;

	}

	private void createRenderers() {
		mWorldRenderer = new WorldRenderer(rendererManager(), mWorld, entityGroupID);
		mCarRenderer = new CarRenderer(rendererManager(), mWorld, entityGroupID);
		mMobRenderer = new MobRenderer(rendererManager(), mWorld, entityGroupID);
		mMissionRenderer = new MissionRenderer(rendererManager(), mWorld, entityGroupID);
		mGameParticleRenderer = new GameParticleRenderer(rendererManager(), mWorld, entityGroupID);
		mHUDRenderer = new HUDRenderer(rendererManager(), mWorld, entityGroupID);

	}

}
