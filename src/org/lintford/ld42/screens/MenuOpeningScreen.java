package org.lintford.ld42.screens;

import org.lwjgl.glfw.GLFW;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.AARectangle;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class MenuOpeningScreen extends Screen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TextureBatch mTextureBatch;
	private Texture mBackgroundTexture;

	private boolean mIsExiting;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MenuOpeningScreen(ScreenManager pScreenManager) {
		super(pScreenManager);

		mTextureBatch = new TextureBatch();
		mShowInBackground = true;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mTextureBatch.loadGLContent(pResourceManager);
		mBackgroundTexture = TextureManager.textureManager().loadTexture("BackgroundOpeningTexture", "res//textures//textureOpening.png");

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();
	}

	@Override
	public void handleInput(LintfordCore pCore, boolean pAcceptMouse, boolean pAcceptKeyboard) {
		super.handleInput(pCore, pAcceptMouse, pAcceptKeyboard);

		if (pCore.input().mouseLeftClick() || pCore.input().keyDownTimed(GLFW.GLFW_KEY_SPACE)) {
			if (!mIsExiting) {
				LoadingScreen.load(mScreenManager, true, new GameWorldScreen(mScreenManager));
				mIsExiting = true;
			}
		}

	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		AARectangle lScreenBounds = pCore.HUD().boundingRectangle();

		mTextureBatch.begin(pCore.HUD());
		mTextureBatch.draw(mBackgroundTexture, 0, 0, 640, 480, lScreenBounds.left(), lScreenBounds.top(), lScreenBounds.w, lScreenBounds.h, -0.1f, 1, 1, 1, 1);
		mTextureBatch.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

}
