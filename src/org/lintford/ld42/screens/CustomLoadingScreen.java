package org.lintford.ld42.screens;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.core.time.TimeSpan;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.transitions.TransitionFadeIn;
import net.lintford.library.screenmanager.transitions.TransitionFadeOut;

public class CustomLoadingScreen extends Screen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final float BLINK_TIMER = 500;

	public static final String LOADING_BACKGROUND_TEXTURE_NAME = "LoadingScreen";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private ScreenManager mScreenManager;
	private Screen[] mScreensToLoad;
	private final boolean mLoadingIsSlow;
	private float mBlinkTimer;

	private TextureBatch mTextureBatch;
	private Texture mLoadingTexture;
	private Texture mLoadingTextTexture;

	private FontUnit mLoadingFont;

	private boolean mWaitingForInput;
	private boolean mIsStillLoading;
	private boolean mIsBlinking;
	private boolean mIsExitingScreen;

	// --------------------------------------
	// Constructors
	// --------------------------------------

	private CustomLoadingScreen(ScreenManager pScreenManager, boolean pLoadingIsSlow, Screen[] pScreensToLoad) {
		super(pScreenManager);

		mScreenManager = pScreenManager;
		mScreensToLoad = pScreensToLoad;

		mLoadingIsSlow = pLoadingIsSlow;

		mTransitionOn = new TransitionFadeIn(new TimeSpan(500));
		mTransitionOff = new TransitionFadeOut(new TimeSpan(500));

		mWaitingForInput = true;
		mIsStillLoading = true;
		mIsPopup = true;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	public static void load(ScreenManager pScreenManager, boolean pLoadingIsSlow, Screen... pScreensToLoad) {

		// transitiion off ALL current screens
		List<Screen> lScreenList = new ArrayList<>();
		lScreenList.addAll(pScreenManager.screens());

		int lScreenCount = lScreenList.size();
		for (int i = 0; i < lScreenCount; i++) {
			if (!lScreenList.get(i).isExiting())
				lScreenList.get(i).exitScreen();

		}

		lScreenList.clear();
		lScreenList = null;

		// create and activate the loading screen
		CustomLoadingScreen lLoadingScreen = new CustomLoadingScreen(pScreenManager, pLoadingIsSlow, pScreensToLoad);
		pScreenManager.addScreen(lLoadingScreen);

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mTextureBatch = new TextureBatch();
		mTextureBatch.loadGLContent(pResourceManager);

		mLoadingTexture = TextureManager.textureManager().loadTexture(LOADING_BACKGROUND_TEXTURE_NAME, "/res/textures/core/loadingScreen.png");

		mLoadingFont = pResourceManager.fontManager().loadNewFont("HUDFont", "res//fonts//VT323-Regular.ttf", 24);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		if (mTextureBatch != null) {
			mTextureBatch.unloadGLContent();

		}

	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

		final double lDeltaTime = pCore.time().elapseGameTimeMilli();

		mBlinkTimer += lDeltaTime;

		if (mBlinkTimer > BLINK_TIMER) {
			mBlinkTimer = 0;
			mIsBlinking = !mIsBlinking;
		}

		// Wait until all the other screens have exited
		if (!mIsExitingScreen && (mScreenState == ScreenState.Active) && (mScreenManager.screens().size() == 1)) {

			// And then continue loading on the main context
			int lCount = mScreensToLoad.length;
			for (int i = 0; i < lCount; i++) {
				Screen lScreen = mScreensToLoad[i];

				if (lScreen != null && !lScreen.isInitialised()) {
					lScreen.initialise();

				}

				if (lScreen != null && !lScreen.isLoaded()) {
					lScreen.loadGLContent(mScreenManager.resources());

				}

			}

			mIsStillLoading = false;
			mWaitingForInput = true;

		}

		if (!mIsExitingScreen && mWaitingForInput) {
			if (pCore.input().keyDownTimed(GLFW.GLFW_KEY_SPACE)) {

				// screens have been loaded on the other thread, so now lets add them to
				// the screen manager
				final int lScreenCount = mScreensToLoad.length;
				for (int i = 0; i < lScreenCount; i++) {
					Screen lScreen = mScreensToLoad[i];
					if (lScreen != null) {
						mScreenManager.addScreen(lScreen);

					}

				}

				mScreenManager.removeScreen(this);

				mIsExitingScreen = true;

			}

		}

	}

	@Override
	public void draw(LintfordCore pCore) {

		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		final float lInfoTextX = 320 / 4;

		final int lLoadingTextX = 4;
		final int lLoadingTextY = 278;
		final int lLoadingTextW = 146;
		final int lLoadingTextH = 40;

		final int lPressSpaceTextX = 4;
		final int lPressSpaceTextY = 235;
		final int lPressSpaceTextW = 375;
		final int lPressSpaceTextH = 39;

		if (mLoadingIsSlow) {
			final float textureWidth = mLoadingTexture.getTextureWidth();
			final float textureHeight = mLoadingTexture.getTextureHeight();

			mTextureBatch.begin(pCore.HUD());

			mTextureBatch.draw(mLoadingTexture, 0, 0, textureWidth, textureHeight, -textureWidth / 2, -textureHeight / 2, textureWidth, textureHeight, -0.1f, 1f, 1f, 1f, 1f);
			mTextureBatch.draw(TextureManager.TEXTURE_CORE_UI, lLoadingTextX, lLoadingTextY, lLoadingTextW, lLoadingTextH, lInfoTextX - lLoadingTextW / 2, 240 - 120, lLoadingTextW, 53, -0.1f, 1f, 1f, 1f, 1f);

			if (!mIsStillLoading) {
				if (mIsBlinking) {
					mTextureBatch.draw(TextureManager.TEXTURE_CORE_UI, lPressSpaceTextX, lPressSpaceTextY, lPressSpaceTextW, lPressSpaceTextH, lInfoTextX - lPressSpaceTextW / 2,
							240 - lPressSpaceTextH - lPressSpaceTextH / 2, lPressSpaceTextW, lPressSpaceTextH, -0.1f, 1f, 1f, 1f, 1f);
				}
			}

			mTextureBatch.end();

			final String mBackStory = "The year is 2073... \n\n    ... you have been sealed within Dome#273 for over 35 years now. Humanity has been slowly turning in on itsself, empathy all but lost. The people apathetic.\n\nThe upcoming call for off-world colonists is your only chance for a brighter future - if you can but just raise the remaining amount for the lottery ticket.\n\n           ... you still need $15,550";

			mLoadingFont.begin(pCore.HUD());
			mLoadingFont.draw(mBackStory, -320 + 10, -240 + 30, -.01f, 1f, 1f, 1f, 1f, 1f, 580);
			mLoadingFont.end();

		}

	}

}
