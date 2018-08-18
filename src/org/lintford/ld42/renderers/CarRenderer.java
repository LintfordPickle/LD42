package org.lintford.ld42.renderers;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.cars.BaseCar;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.debug.Debug;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class CarRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "CarRenderer";

	public static final String CAR_TEXTURE_NAME = "CarTexture";
	public static final String CAR_TEXTURE_FILENAME = "res//textures//textureCars.png";

	public static final boolean RENDER_DEBUG_COLLIABLES = false;
	public static final boolean RENDER_DEBUG_LINES = false;

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;

	private Texture mCarTexture;
	private TextureBatch mTextureBatch;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public int ZDepth() {
		return 5;

	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public CarRenderer(RendererManager pRendererManager, World pWorld, int pGroupID) {
		super(pRendererManager, RENDERER_NAME, pGroupID);

		mTextureBatch = new TextureBatch();
		mWorld = pWorld;

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

		mCarTexture = TextureManager.textureManager().loadTexture(CAR_TEXTURE_NAME, CAR_TEXTURE_FILENAME);

		mTextureBatch.loadGLContent(pResourceManager);
	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mTextureBatch.unloadGLContent();

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore pCore) {
		mTextureBatch.begin(pCore.gameCamera());

		renderCar(pCore, mWorld.carManager().playerCar(), mTextureBatch);

		final int CAR_COUNT = mWorld.carManager().cars().size();
		for (int i = 0; i < CAR_COUNT; i++) {
			BaseCar lCar = mWorld.carManager().cars().get(i);

			renderCar(pCore, lCar, mTextureBatch);

		}

		mTextureBatch.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void renderCar(LintfordCore pCore, BaseCar pCar, TextureBatch pTextureBatch) {
		pCar.setDimensions(32, 64);

		float srcBaseX = (int) (pCar.carType % 2) * 256; // 2 columns
		float srcBaseY = (int) (pCar.carType / 2) * 64;

		srcBaseX += (32 * pCar.turnRank);
		if (pCar.turningLeft())
			srcBaseX += 3 * 32;

		float lAng = pCar.rotation + pCar.steerAngle;
		float lStrDirX = (float) Math.cos(lAng);
		float lStrDirY = (float) Math.sin(lAng);

		// Render the front wheels (back wheels are probably not visible)
		float lRad90Offset = (float) Math.toRadians(90);

		float lWheelBaseWidth = 15;
		float lRot = pCar.rotation + pCar.steerAngle + lRad90Offset;
		float lLeftXOffset = (float) Math.cos(pCar.rotation + lRad90Offset) * lWheelBaseWidth;
		float lLeftYOffset = (float) Math.sin(pCar.rotation + lRad90Offset) * lWheelBaseWidth;

		float lRightXOffset = (float) Math.cos(pCar.rotation - lRad90Offset) * lWheelBaseWidth;
		float lRightYOffset = (float) Math.sin(pCar.rotation - lRad90Offset) * lWheelBaseWidth;

		mTextureBatch.draw(mCarTexture, 0, 448, 10, 14, pCar.mFrontWheels.x - 5 + lLeftXOffset, pCar.mFrontWheels.y - 7 + lLeftYOffset, 10, 14, -0.3f, lRot, 5f, 7f, 1f, pCar.colorR, pCar.colorG, pCar.colorB, 1);
		mTextureBatch.draw(mCarTexture, 0, 448, 10, 14, pCar.mFrontWheels.x - 5 + lRightXOffset, pCar.mFrontWheels.y - 7 + lRightYOffset, 10, 14, -0.3f, lRot, 5f, 7f, 1f, pCar.colorR, pCar.colorG, pCar.colorB, 1);

		mTextureBatch.draw(mCarTexture, 0, 448, 10, 14, pCar.mRearWheels.x - 5 + lLeftXOffset, pCar.mRearWheels.y - 7 + lLeftYOffset, 10, 14, -0.3f, pCar.rotation + lRad90Offset, 5f, 7f, 1f, pCar.colorR, pCar.colorG,
				pCar.colorB, 1);
		mTextureBatch.draw(mCarTexture, 0, 448, 10, 14, pCar.mRearWheels.x - 5 + lRightXOffset, pCar.mRearWheels.y - 7 + lRightYOffset, 10, 14, -0.3f, pCar.rotation + lRad90Offset, 5f, 7f, 1f, pCar.colorR, pCar.colorG,
				pCar.colorB, 1);

		mTextureBatch.draw(mCarTexture, srcBaseX, srcBaseY, 32, 64, pCar.bounds(), -0.3f, pCar.colorR, pCar.colorG, pCar.colorB, 1);

		if (RENDER_DEBUG_COLLIABLES) {
			if (mWorld.carManager().playerCar() == pCar) {
				if (pCar.currentNode != null)
					Debug.debugManager().drawers().drawRect(pCore.HUD(), pCar.currentNode.x, pCar.currentNode.y, 10, 10, 1f, 1f, 1f);

				if (pCar.targetNode != null)
					Debug.debugManager().drawers().drawRect(pCore.HUD(), pCar.targetNode.x, pCar.targetNode.y, 10, 10, 1f, 1f, 1f);

				Debug.debugManager().drawers().startLineRenderer(pCore.gameCamera());
				Debug.debugManager().drawers().drawLine(pCar.bounds().centerX, pCar.bounds().centerY, pCar.bounds().centerX + lStrDirX * 100f, pCar.bounds().centerY + lStrDirY * 100f, 1f, 0f, 0f);

				if (pCar.targetNode != null) {
					Debug.debugManager().drawers().drawLine(pCar.x, pCar.y, pCar.targetNode.x, pCar.targetNode.y);

				}
				Debug.debugManager().drawers().endLineRenderer();

			}

			Debug.debugManager().drawers().drawPoly(pCore.gameCamera(), pCar.bounds());

		}

		if (RENDER_DEBUG_LINES) {
			Debug.debugManager().drawers().startLineRenderer(pCore.gameCamera());
			Debug.debugManager().drawers().drawLine(pCar.bounds().centerX, pCar.bounds().centerY, pCar.bounds().centerX + lStrDirX * 100f, pCar.bounds().centerY + lStrDirY * 100f, 1f, 0f, 0f);

			if (pCar.targetNode != null) {
				Debug.debugManager().drawers().drawLine(pCar.x, pCar.y, pCar.targetNode.x, pCar.targetNode.y);

			}
			Debug.debugManager().drawers().endLineRenderer();

		}

	}

}
