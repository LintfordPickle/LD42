package org.lintford.ld42.renderers;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.mission.Mission;
import org.lintford.ld42.screens.GameWorldScreen;
import org.lwjgl.opengl.GL11;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.geometry.AARectangle;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.fonts.FontManager.FontUnit;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.core.maths.MathHelper;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class HUDRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "HUDRenderer";

	// These match the dimensions within the source texture
	private static final float HUD_PANEL_Width = 640;
	private static final float HUD_PANEL_Height = 100;

	final int MINIMAP_TEXTURE_WIDTH = 128;
	final int MINIMAP_TEXTURE_HEIGHT = 128;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;
	private TextureBatch mTextureBatch;

	private Texture mHUDTexture;
	private Texture mMapTexture;

	private FontUnit mHUDFont;

	final float mMiniMapCenterX = 20;
	final float mMiniMapCenterY = 191;
	final float mlMiniMapWidth = 600;
	final float mMiniMapHeight = 600;
	float mMissionIconRotAmt;

	private int mCreditAmtLastUpdate;
	String mCreditsStr;
	private int mHealthAmtLastUpdate;
	String mVehicleHealth;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public int ZDepth() {
		return 20;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public HUDRenderer(RendererManager pRendererManager, World pWorld, int pGroupID) {
		super(pRendererManager, RENDERER_NAME, pGroupID);

		mWorld = pWorld;
		mTextureBatch = new TextureBatch();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void initialise(LintfordCore pCore) {

	}

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mHUDTexture = TextureManager.textureManager().loadTexture("HUDTexture", "res//textures//textureHUD.png");
		mMapTexture = TextureManager.textureManager().loadTexture("MiniMapTexture", "res//maps//textureWorldMapDecor.png");

		mTextureBatch.loadGLContent(pResourceManager);

		mHUDFont = pResourceManager.fontManager().loadNewFont("HUDFont", "res//fonts//VT323-Regular.ttf", 24);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

		mMissionIconRotAmt += pCore.time().elapseGameTimeMilli() * 0.001f;

		if (mCreditAmtLastUpdate != mWorld.gameState().credits() || mCreditsStr == null) {
			mCreditAmtLastUpdate = mWorld.gameState().credits();
			mCreditsStr = "$" + mCreditAmtLastUpdate;

		}

		if (mHealthAmtLastUpdate != mWorld.carManager().playerCar().health || mVehicleHealth == null) {
			mHealthAmtLastUpdate = mWorld.carManager().playerCar().health;
			mVehicleHealth = "" + mHealthAmtLastUpdate + " / " + mWorld.carManager().playerCar().maxHealth;

		}

	}

	@Override
	public void draw(LintfordCore pCore) {
		AARectangle lWindowRect = pCore.HUD().boundingRectangle();

		mTextureBatch.begin(pCore.HUD());
		mTextureBatch.draw(mHUDTexture, 0, 0, HUD_PANEL_Width, HUD_PANEL_Height, lWindowRect.left(), lWindowRect.bottom() - HUD_PANEL_Height, HUD_PANEL_Width, HUD_PANEL_Height, -0.1f, 1f, 1f, 1f, 1f);
		mTextureBatch.end();

		mHUDFont.begin(pCore.HUD());
		mHUDFont.draw(mCreditsStr, lWindowRect.left() + 30, lWindowRect.bottom() - 52, -0.1f, 0.55f, 0.96f, 0.57f, 1f, 1f, -1);
		mHUDFont.draw(mVehicleHealth, lWindowRect.right() - 125, lWindowRect.bottom() - 52, -0.1f, 0.96f, 0.42f, 0.47f, 1f, 1f, -1);
		mHUDFont.end();

		// Draw the wanted state
		{
			mTextureBatch.begin(pCore.HUD());
			if (mWorld.gameState().isWanted()) {
				mTextureBatch.draw(mHUDTexture, 92, 105, 32, 32, lWindowRect.left(), lWindowRect.bottom() - 100 - 24, 32, 32, -0.1f, 1f, 1f, 1f, 1f);

			} else {
				mTextureBatch.draw(mHUDTexture, 59, 105, 32, 32, lWindowRect.left(), lWindowRect.bottom() - 100 - 24, 32, 32, -0.1f, 1f, 1f, 1f, 1f);

			}
			mTextureBatch.end();
		}

		renderMiniMap(pCore);

		renderMiniMapMissionInfo(pCore);

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void renderMiniMap(LintfordCore pCore) {
		float lWorldPosX = mWorld.carManager().playerCar().x;
		float lWorldPosY = mWorld.carManager().playerCar().y;

		final float lMiniMapViewportWidth = 172;
		final float lMiniMapViewportHeight = 89;

		final float GRID_SIZE = GameWorldScreen.GRID_SIZE;
		float lOffsetX = MathHelper.scaleToRange(lWorldPosX, 0, MINIMAP_TEXTURE_WIDTH * GRID_SIZE, 0, mlMiniMapWidth);
		float lOffsetY = MathHelper.scaleToRange(lWorldPosY, 0, MINIMAP_TEXTURE_HEIGHT * GRID_SIZE, 0, mMiniMapHeight);

		GL11.glEnable(GL11.GL_STENCIL_TEST);

		GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF); // Set any stencil to 1
		GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE); // What should happen to stencil values
		GL11.glStencilMask(0xFF); // Write to stencil buffer

		// Make sure we are starting with a fresh stencil buffer
		GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT); // Clear the stencil buffer

		mTextureBatch.begin(pCore.HUD());
		mTextureBatch.draw(TextureManager.TEXTURE_CORE_UI, 0, 0, 128, 128, mMiniMapCenterX - lMiniMapViewportWidth / 2, mMiniMapCenterY - lMiniMapViewportHeight / 2, lMiniMapViewportWidth, lMiniMapViewportHeight, -0.1f,
				1f, 1f, 1f, 1f);
		mTextureBatch.end();

		// Start the stencil buffer test to filter out everything outside of the scroll view
		GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Pass test if stencil value is 1

		// Render the mini map
		mTextureBatch.begin(pCore.HUD());
		mTextureBatch.draw(TextureManager.TEXTURE_CORE_UI, 0, 0, 32, 32, mMiniMapCenterX - lMiniMapViewportWidth / 2, mMiniMapCenterY - lMiniMapViewportHeight / 2, lMiniMapViewportWidth, lMiniMapViewportHeight, -0.1f,
				0f, 0f, 0f, 1f);
		mTextureBatch.draw(mMapTexture, 0, 0, 512, 512, mMiniMapCenterX - lOffsetX, mMiniMapCenterY - lOffsetY, mlMiniMapWidth, mMiniMapHeight, -0.1f, 1f, 1f, 1f, 1f);

		float lRot = mWorld.carManager().playerCar().rotation + (float) Math.toRadians(90);
		float lRotX = 8;
		float lRotY = 8;

		if (mWorld.gameState().isOnMission()) {

			float lMissPosX = mWorld.gameState().currentMission.destX - lWorldPosX;
			float lMissPosY = mWorld.gameState().currentMission.destY - lWorldPosY;

			float lMissOffsetX = MathHelper.scaleToRange(lMissPosX, 0, MINIMAP_TEXTURE_WIDTH * GRID_SIZE, 0, mlMiniMapWidth);
			float lMissOffsetY = MathHelper.scaleToRange(lMissPosY, 0, MINIMAP_TEXTURE_HEIGHT * GRID_SIZE, 0, mMiniMapHeight);

			mTextureBatch.draw(mHUDTexture, 24, 111, 16, 16, mMiniMapCenterX + lMissOffsetX - 8, mMiniMapCenterY + lMissOffsetY - 8, 16, 16, -0.1f, mMissionIconRotAmt, lRotX, lRotY, 1f, 1f, 1f, 1f, 1f);

		} else {
			final int lNumAvailMission = mWorld.missionManager().availableMissions().size();
			for (int i = 0; i < lNumAvailMission; i++) {
				// Law of delimiter eh?
				Mission lAvailMission = mWorld.missionManager().availableMissions().get(i);

				float lMissPosX = lAvailMission.questGiver.x - lWorldPosX;
				float lMissPosY = lAvailMission.questGiver.y - lWorldPosY;

				float lMissOffsetX = MathHelper.scaleToRange(lMissPosX, 0, MINIMAP_TEXTURE_WIDTH * GRID_SIZE, 0, mlMiniMapWidth);
				float lMissOffsetY = MathHelper.scaleToRange(lMissPosY, 0, MINIMAP_TEXTURE_HEIGHT * GRID_SIZE, 0, mMiniMapHeight);

				// Draw the avaialble mission icon on the minimap
				mTextureBatch.draw(mHUDTexture, 43, 111, 16, 16, mMiniMapCenterX + lMissOffsetX - 8, mMiniMapCenterY + lMissOffsetY - 8, 16, 16, -0.4f, 0, lRotX, lRotY, 1f, 1f, 1f, 1f, 1f);

			}

		}

		mTextureBatch.draw(mHUDTexture, 0, 100, 16, 16, mMiniMapCenterX - 8, mMiniMapCenterY - 8, 16, 16, -0.1f, lRot, lRotX, lRotY, 1f, 1f, 1f, 1f, 1f);

		mTextureBatch.end();

		GL11.glDisable(GL11.GL_STENCIL_TEST);

	}

	private void renderMiniMapMissionInfo(LintfordCore pCore) {
		if (mWorld.gameState().isOnMission()) {

			AARectangle lWindowRect = pCore.HUD().boundingRectangle();

			mTextureBatch.begin(pCore.HUD());

			float lMisDirX = mWorld.gameState().currentMission.destX;
			float lMisDirY = mWorld.gameState().currentMission.destY;

			float lMissionDirX = lMisDirX - mWorld.carManager().playerCar().x;
			float lMissionDirY = lMisDirY - mWorld.carManager().playerCar().y;

			float lAngleToMiss = (float) Math.atan2(lMissionDirY, lMissionDirX);
			float lOffsetX = (float) Math.cos(lAngleToMiss) * 64f;
			float lOffsetY = (float) Math.sin(lAngleToMiss) * 64f;

			float lPlayerX = mWorld.carManager().playerCar().x;
			float lPlayerY = mWorld.carManager().playerCar().y;
			float lDist = (float) Math.sqrt((lMisDirX - lPlayerX) * (lMisDirX - lPlayerX) + (lMisDirY - lPlayerY) * (lMisDirY - lPlayerY)) / 10f;
			mTextureBatch.draw(mHUDTexture, 0, 115, 18, 14, lOffsetX, lOffsetY, 18, 14, -0.1f, lAngleToMiss + (float) Math.toRadians(90), 8, 8, 1f, 1f, 1f, 1f, 1f);

			// Draw the head of the passenger
			mTextureBatch.draw(mHUDTexture, 0, 134, 18, 18, lWindowRect.left() + 205, lWindowRect.bottom() - HUD_PANEL_Height + 10, 18, 18, -0.1f, 1f, 1f, 1f, 1f);

			mHUDFont.begin(pCore.HUD());
			// Render the fare amount
			mHUDFont.draw("$" + mWorld.gameState().currentMission.fee, lWindowRect.left() + 150, lWindowRect.bottom() - 67, -0.1f, 0.26f, 0.32f, 0.97f, 1f, 1f, -1);

			// Render the distance to destination
			mHUDFont.draw((int) lDist + "m", lWindowRect.left() + 150, lWindowRect.bottom() - 35, -0.1f, 0.26f, 0.32f, 0.97f, 1f, 1f, -1);

			mHUDFont.end();

			mTextureBatch.end();

		}
	}

}