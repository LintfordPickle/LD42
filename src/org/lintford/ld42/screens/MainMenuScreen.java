package org.lintford.ld42.screens;

import net.lintford.library.screenmanager.MenuEntry;
import net.lintford.library.screenmanager.MenuEntry.BUTTON_SIZE;
import net.lintford.library.screenmanager.MenuScreen;
import net.lintford.library.screenmanager.ScreenManager;
import net.lintford.library.screenmanager.layouts.ListLayout;

public class MainMenuScreen extends MenuScreen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	private static final int BUTTON_START = 0;
	private static final int BUTTON_EXIT = 1;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private MenuEntry mStartButton;
	private MenuEntry mExitButton;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MainMenuScreen(ScreenManager pScreenManager) {
		super(pScreenManager, "");

		ListLayout lMenuButtons = new ListLayout(this);
		lMenuButtons.alignment(ALIGNMENT.left);
		lMenuButtons.w = 250;
		lMenuButtons.setPadding(0, 20, 0, 0);
		mChildAlignment = ALIGNMENT.left;
		mChildPositionOffsetX = 50;

		mStartButton = new MenuEntry(mScreenManager, this, "Start Game");
		mStartButton.buttonSize(BUTTON_SIZE.narrow);
		mExitButton = new MenuEntry(mScreenManager, this, "Exit");
		mExitButton.buttonSize(BUTTON_SIZE.normal);

		mStartButton.registerClickListener(this, BUTTON_START);
		mExitButton.registerClickListener(this, BUTTON_EXIT);

		lMenuButtons.menuEntries().add(mStartButton);
		lMenuButtons.menuEntries().add(mExitButton);

		layouts().add(lMenuButtons);

		mESCBackEnabled = false;

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	protected void handleOnClick() {
		switch (mClickAction.consume()) {
		case BUTTON_START:
			// mScreenManager.addScreen(new MenuOpeningScreen(mScreenManager));
			CustomLoadingScreen.load(mScreenManager, true, new GameWorldScreen(mScreenManager));
			break;

		case BUTTON_EXIT:
			mScreenManager.exitGame();
			break;

		}
	}

}
