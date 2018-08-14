package org.lintford.ld42.screens;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.AARectangle;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.core.maths.RandomNumbers;
import net.lintford.library.core.maths.Vector2f;
import net.lintford.library.screenmanager.Screen;
import net.lintford.library.screenmanager.ScreenManager;

public class MenuBackgroundScreen extends Screen {

	// --------------------------------------
	// Constants
	// --------------------------------------

	// --------------------------------------
	// Variables
	// --------------------------------------

	private TextureBatch mTextureBatch;
	private Texture mBackgroundTexture;
	private Texture mBackgroundOverlayTexture;
	private Texture mBackgroundLogoTexture;
	private Texture mHUDTexture;

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MenuBackgroundScreen(ScreenManager pScreenManager) {
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
		mBackgroundTexture = TextureManager.textureManager().loadTexture("BackgroundTexture", "res//textures//textureMenuBackground.png");
		mBackgroundOverlayTexture = TextureManager.textureManager().loadTexture("BackgroundOverlayTexture", "res//textures//textureMenuBackgroundOverlay.png");
		mBackgroundLogoTexture = TextureManager.textureManager().loadTexture("BackgroundLogoTexture", "res//textures//textureTitleLogo.png");
		mHUDTexture = TextureManager.textureManager().loadTexture("HUDTexture", "res//textures//textureHUD.png");

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();
	}

	@Override
	public void update(LintfordCore pCore, boolean pOtherScreenHasFocus, boolean pCoveredByOtherScreen) {
		super.update(pCore, pOtherScreenHasFocus, pCoveredByOtherScreen);

		wander(pCore);

		final float lSpeed = .005f;
		mWanderPosition.x += (float) Math.cos(mWanderOrientation) * lSpeed * pCore.time().elapseGameTimeMilli();
		mWanderPosition.y += (float) Math.sin(mWanderOrientation) * lSpeed * pCore.time().elapseGameTimeMilli();

	}

	@Override
	public void draw(LintfordCore pCore) {
		super.draw(pCore);

		AARectangle lScreenBounds = pCore.HUD().boundingRectangle();

		final float lRotAmt = (float) Math.toRadians(25f);

		final float lLeft = -mOverlayWidth + mWanderPosition.x;
		final float lTop = -mOverlayHeight + mWanderPosition.y;

		mTextureBatch.begin(pCore.HUD());
		mTextureBatch.draw(mBackgroundTexture, 0, 0, 640, 480, lScreenBounds.left(), lScreenBounds.top(), lScreenBounds.w, lScreenBounds.h, -0.1f, 1, 1, 1, 1);
		mTextureBatch.draw(mBackgroundOverlayTexture, 0, 0, mOverlayWidth, mOverlayHeight, lLeft, lTop, mOverlayWidth * 2, mOverlayHeight * 2, -0.1f, lRotAmt, mOverlayWidth, mOverlayHeight, 1f, 1, 1, 1, 0.2f);
		mTextureBatch.draw(mBackgroundLogoTexture, 0, 0, 375, 140, lScreenBounds.left(), lScreenBounds.top(), 375, 140, -0.1f, 1, 1, 1, 1);
		mTextureBatch.draw(mHUDTexture, 0, 160, 640, 480, -320, -240, 640, 480, -0.1f, 1, 1, 1, 1);
		mTextureBatch.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	float mOverlayRotAmt;
	float mOverlayWidth = 387;
	float mOverlayHeight = 387;
	Vector2f mScreenCenter = new Vector2f();
	Vector2f mWanderPosition = new Vector2f();
	Vector2f mWanderDirection = new Vector2f();
	float mWanderOrientation;
	float mOverlayVX;
	float mOverlayVY;

	private void wander(LintfordCore pCore) {
		// The wander effect is accomplished by having the character aim in a random
		// direction. Every frame, this random direction is slightly modified.
		// Finally, to keep the characters on the center of the screen, we have them
		// turn to face the screen center. The further they are from the screen
		// center, the more they will aim back towards it.

		// the first step of the wander behavior is to use the random number
		// generator to offset the current wanderDirection by some random amount.
		// .25 is a bit of a magic number, but it controls how erratic the wander
		// behavior is. Larger numbers will make the characters "wobble" more,
		// smaller numbers will make them more stable. we want just enough
		// wobbliness to be interesting without looking odd.
		mWanderDirection.x += MathHelper.lerp(-.25f, .25f, (float) RandomNumbers.RANDOM.nextFloat());
		mWanderDirection.y += MathHelper.lerp(-.25f, .25f, (float) RandomNumbers.RANDOM.nextFloat());
		mWanderDirection.nor();

		mWanderOrientation = turnToFace(mWanderPosition.x, mWanderPosition.y, mWanderPosition.x + mWanderDirection.x, mWanderPosition.y + mWanderDirection.y, mWanderOrientation, .25f);

		mScreenCenter.x = -50;//pCore.config().display().windowSize().x / 2;
		mScreenCenter.y = 50;//pCore.config().display().windowSize().y / 2;

		// Here we are creating a curve that we can apply to the turnSpeed. This
		// curve will make it so that if we are close to the center of the screen,
		// we won't turn very much. However, the further we are from the screen
		// center, the more we turn. At most, we will turn at 30% of our maximum
		// turn speed. This too is a "magic number" which works well for the sample.
		// Feel free to play around with this one as well: smaller values will make
		// the characters explore further away from the center, but they may get
		// stuck on the walls. Larger numbers will hold the characters to center of
		// the screen. If the number is too large, the characters may end up
		// "orbiting" the center.
		float distanceFromScreenCenter = Vector2f.distance(mScreenCenter, mWanderPosition);
		float MaxDistanceFromScreenCenter = Math.min(mScreenCenter.y, mScreenCenter.x);

		float normalizedDistance = distanceFromScreenCenter / MaxDistanceFromScreenCenter;

		float turnToCenterSpeed = 2.8f * normalizedDistance * normalizedDistance * .25f;

		mWanderOrientation = turnToFace(mWanderPosition.x, mWanderPosition.y, mScreenCenter.x, mScreenCenter.y, mWanderOrientation, turnToCenterSpeed);

	}

	public static float turnToFace(float pPosX, float pPosY, float pFaceThisX, float pFaceThisY, float pCurrentAngle, float pTurnSpeed) {
		float x = pFaceThisX - pPosX;
		float y = pFaceThisY - pPosY;

		float desiredAngle = (float) Math.atan2(y, x);
		float difference = wrapAngle(desiredAngle - pCurrentAngle);

		// clamp
		difference = clamp(difference, -pTurnSpeed, pTurnSpeed);

		return wrapAngle(pCurrentAngle + difference);

	}

	public static float wrapAngle(float radians) {
		while (radians < -Math.PI) {
			radians += Math.PI * 2;
		}
		while (radians > Math.PI) {
			radians -= Math.PI * 2;
		}
		return radians;
	}

	static float clamp(float v, float min, float max) {
		return Math.max(min, Math.min(max, v));
	}

}
