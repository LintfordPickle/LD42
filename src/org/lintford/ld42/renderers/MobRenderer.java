package org.lintford.ld42.renderers;

import java.util.List;

import org.lintford.ld42.data.World;
import org.lintford.ld42.data.mobs.BaseMob;
import org.lintford.ld42.data.mobs.MobManager;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class MobRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "MobRenderer";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private World mWorld;
	private MobManager mMobManager;

	private TextureBatch mTextureBatch;
	private Texture mMobTexture;

	@Override
	public int ZDepth() {
		return 2;
	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public MobRenderer(RendererManager pRendererManager, World pWorld, int pGroupID) {
		super(pRendererManager, RENDERER_NAME, pGroupID);

		mTextureBatch = new TextureBatch();

		mWorld = pWorld;
		mMobManager = mWorld.mobManager();

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

		mTextureBatch.loadGLContent(pResourceManager);
		mMobTexture = TextureManager.textureManager().loadTexture("MobTexture", "res//textures//textureMobs.png");
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
		List<BaseMob> lMobs = mMobManager.mobs();

		mTextureBatch.begin(pCore.gameCamera());

		final int lMobCount = lMobs.size();
		for (int i = 0; i < lMobCount; i++) {
			BaseMob lMob = lMobs.get(i);
			if(lMob.isAlive)
				mTextureBatch.draw(mMobTexture, 0, 0, 32, 32, lMob.x - 16, lMob.y - 16, 32, 32, -0.6f, 1, 1, 1, 1);
			else
				mTextureBatch.draw(mMobTexture, 32, 0, 32, 32, lMob.x - 16, lMob.y - 16, 32, 32, -0.6f, 1, 1, 1, 1);
		}

		mTextureBatch.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

}
