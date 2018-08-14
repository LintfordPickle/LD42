package org.lintford.ld42.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class GameOverScreen extends MenuScreen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final int BUTTON_EXIT = 1;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuEntry mExitButton;
	private FontUnit mHUDFont;
	private int mCreditsEarned;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameOverScreen(ScreenManager pScreenManager, int pCreditsEarned) {
		super(pScreenManager, "Game Over");

		mCreditsEarned = pCreditsEarned;

		ListLayout lMenuButtons = new ListLayout(this);
		lMenuButtons.w = 400;
		lMenuButtons.y = 400;

		mExitButton = new MenuEntry(mScreenManager, this, "Back to Main Menu");

		mExitButton.registerClickListener(this, BUTTON_EXIT);

		lMenuButtons.menuEntries().add(mExitButton);

		layouts().add(lMenuButtons);

		mESCBackEnabled = false;
		mIsPopup = false;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mHUDFont = pResourceManager.fontManager().loadNewFont("HUDFont", "res//fonts//VT323-Regular.ttf", 24);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

	}

	@Override
	public void draw(LintfordCore pCore) {
		mTopMargin = 350;

		super.draw(pCore);

		final int lCredits = mCreditsEarned;
		String lIpsum = "You have been killed. You made a total of $" + lCredits + ", not that it matters much now anyway.";

		if (lCredits > 15500) {
			lIpsum += "At least you can claim to have kind of beaten the game though ...";

		} else {
			lIpsum += "\n\nEven had you not died, it would seem unlikely that you could've made enough money before the shuttle launch.";

		}

		final float lTextBlockWidth = 550;

		mHUDFont.begin(pCore.HUD());
		mHUDFont.draw(lIpsum, -lTextBlockWidth / 2, -125, -0.01f, .87f, 0.94f, .95f, 1f, 1f, lTextBlockWidth - 10);
		mHUDFont.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_EXIT:
			LoadingScreen.load(mScreenManager, false, new MenuBackgroundScreen(mScreenManager), new MainMenuScreen(mScreenManager));
			break;

		}
	}

}
