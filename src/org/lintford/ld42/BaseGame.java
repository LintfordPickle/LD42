package org.lintford.ld42;

import org.lintford.ld42.screens.GameWorldScreen;
import org.lintford.ld42.screens.MainMenuScreen;
import org.lintford.ld42.screens.MenuBackgroundScreen;
import org.lwjgl.opengl.GL11;

import net.lintford.library.GameInfo;
import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.screenmanager.IMenuAction;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.LoadingScreen;
import net.lintford.library.screenmanager.screens.TimedIntroScreen;

public class BaseGame extends LintfordCore {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String GAME_NAME = "LD42 - The Dome";

	public static final int GAME_RESOLUTION_W = 640;
	public static final int GAME_RESOLUTION_H = 480;

	public static final boolean SHOW_INTRO = false;
	public static final boolean DEBUG_STRAIGHT_TO_GAMEWORLD = false;

	// --------------------------------------
	// Entry Point
	// --------------------------------------

	public static void main(String[] pArgs) {
		GameInfo lNewGameInfo = new GameInfo() {
			@Override
			public int baseGameResolutionWidth() {
				return GAME_RESOLUTION_W;
			}

			@Override
			public int baseGameResolutionHeight() {
				return GAME_RESOLUTION_H;
			}

			@Override
			public int windowWidth() {
				return GAME_RESOLUTION_W;

			}

			@Override
			public int windowHeight() {
				return GAME_RESOLUTION_H;

			}

			// Maintain same resolution regardless of window size
			@Override
			public boolean stretchGameResolution() {
				return true;
			}

			@Override
			public boolean windowResizeable() {
				return true;
			}

			@Override
			public String windowTitle() {
				return GAME_NAME;
			}

			@Override
			public String applicationName() {
				return GAME_NAME;
			}

		};

		// Instantiate and start game window
		BaseGame lNewBaseGame = new BaseGame(lNewGameInfo, pArgs);
		lNewBaseGame.createWindow();

	}

	// --------------------------------------
	// Variables
	// --------------------------------------

	private ScreenManager mScreenManager;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public BaseGame(GameInfo pGameInfo, String[] pArgs) {
		super(pGameInfo, pArgs);

		mScreenManager = new ScreenManager(this);

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	protected void onInitialiseApp() {
		super.onInitialiseApp();

		// Reload our own CORE texture
		TextureManager.textureManager().loadTexture(TextureManager.TEXTURE_CORE_UI_NAME, "res/textures/textureCore.png", GL11.GL_NEAREST, true);

		TextureManager.textureManager().loadTexture(LoadingScreen.LOADING_BACKGROUND_TEXTURE_NAME, "res/textures/textureLoadingScreen.png", GL11.GL_NEAREST, true);

		if (SHOW_INTRO) {
			TimedIntroScreen lSplashScreen = new TimedIntroScreen(mScreenManager, "res//textures//textureSplash.png", 4f);
			lSplashScreen.setTextureSrcRectangle(0, 0, 640, 480);
			lSplashScreen.stretchBackgroundToFit(true);
			lSplashScreen.setTimerFinishedCallback(new IMenuAction() {

				@Override
				public void TimerFinished(Screen pScreen) {

					mScreenManager.addScreen(new MenuBackgroundScreen(mScreenManager));
					mScreenManager.addScreen(new MainMenuScreen(mScreenManager));

				}

			});

			mScreenManager.addScreen(lSplashScreen);

		} else {
			if (DEBUG_STRAIGHT_TO_GAMEWORLD) {
				mScreenManager.addScreen(new GameWorldScreen(mScreenManager));

			} else {
				mScreenManager.addScreen(new MenuBackgroundScreen(mScreenManager));
				mScreenManager.addScreen(new MainMenuScreen(mScreenManager));

			}

		}

		final String lGameFont = "res/fonts/VT323-Regular.ttf";
		mResourceManager.fontManager().loadNewFont(MenuScreen.MENUSCREEN_FONT_NAME, lGameFont, MenuScreen.MENUSCREEN_FONT_POINT_SIZE);
		mResourceManager.fontManager().loadNewFont(MenuScreen.MENUSCREEN_HEADER_FONT_NAME, lGameFont, MenuScreen.MENUSCREEN_HEADER_FONT_POINT_SIZE);

		mScreenManager.initialise(lGameFont);

	}

	@Override
	protected void onLoadGLContent() {
		super.onLoadGLContent();

		mScreenManager.loadGLContent(mResourceManager);

	}

	@Override
	protected void onUnloadGLContent() {
		super.onUnloadGLContent();

		mScreenManager.unloadGLContent();

	}

	@Override
	protected void onHandleInput() {
		super.onHandleInput();

		mScreenManager.handleInput(this);

	}

	@Override
	protected void onUpdate() {
		super.onUpdate();

		mScreenManager.update(this);

	}

	@Override
	protected void onDraw() {
		super.onDraw();

		mScreenManager.draw(this);

	}

}
