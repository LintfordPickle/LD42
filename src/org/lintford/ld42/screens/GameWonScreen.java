package org.lintford.ld42.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.AARectangle;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameWonScreen extends MenuScreen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final int BUTTON_EXIT = 1;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuEntry mExitButton;
	private TextureBatch mTextureBatch;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameWonScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "You're getting off this Rock!");

		ListLayout lMenuButtons = new ListLayout(this);
		lMenuButtons.w = 400;

		mExitButton = new MenuEntry(mScreenManager, this, "NICE ONE!");

		mExitButton.registerClickListener(this, BUTTON_EXIT);

		lMenuButtons.menuEntries().add(mExitButton);

		layouts().add(lMenuButtons);

		mTextureBatch = new TextureBatch();

		mESCBackEnabled = false;
		mIsPopup = false;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

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
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_EXIT:
			LoadingScreen.load(mScreenManager, false, new MenuBackgroundScreen(mScreenManager), new MainMenuScreen(mScreenManager));
			break;

		}
	}

	@Override
	public void draw(LintfordCore pCore) {

		AARectangle lScreenRect = pCore.HUD().boundingRectangle();

		mTextureBatch.begin(pCore.HUD());
		mTextureBatch.draw(TextureManager.TEXTURE_CORE_UI, 0, 0, 32, 32, lScreenRect.left(), lScreenRect.top(), lScreenRect.w, lScreenRect.h - 100, -0.01f, 0, 0, 0, 0.7f);
		mTextureBatch.end();

		super.draw(pCore);

	}

}
