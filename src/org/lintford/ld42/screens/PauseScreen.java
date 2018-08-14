package org.lintford.ld42.screens;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;
import net.lintford.library.screenmanager.screens.LoadingScreen;

public class PauseScreen extends MenuScreen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final int BUTTON_RESUME = 0;
	private static final int BUTTON_EXIT = 1;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuEntry mResumeButton;
	private MenuEntry mExitButton;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public PauseScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "Paused");

		ListLayout lMenuButtons = new ListLayout(this);
		lMenuButtons.w = 400;

		mResumeButton = new MenuEntry(mScreenManager, this, "Resume");
		mExitButton = new MenuEntry(mScreenManager, this, "Main Menu");

		mResumeButton.registerClickListener(this, BUTTON_RESUME);
		mExitButton.registerClickListener(this, BUTTON_EXIT);

		lMenuButtons.menuEntries().add(mResumeButton);
		lMenuButtons.menuEntries().add(mExitButton);

		layouts().add(lMenuButtons);
		
//		mIsPopup = true;

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_RESUME:
			exitScreen();
			break;

		case BUTTON_EXIT:
			LoadingScreen.load(mScreenManager, false, new MenuBackgroundScreen(mScreenManager), new MainMenuScreen(mScreenManager));
			break;

		}
	}

}
