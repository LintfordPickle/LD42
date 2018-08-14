package org.lintford.ld42.renderers;

import org.lintford.ld42.controllers.LevelController;
import org.lintford.ld42.data.GameParticleSystems;
import org.lintford.ld42.data.World;

import net.lintford.library.core.LintfordCore;
import net.lintford.library.core.graphics.ResourceManager;
import net.lintford.library.core.graphics.particles.Particle;
import net.lintford.library.core.graphics.particles.ParticleSystem;
import net.lintford.library.core.graphics.textures.Texture;
import net.lintford.library.core.graphics.textures.TextureManager;
import net.lintford.library.core.graphics.textures.texturebatch.TextureBatch;
import net.lintford.library.renderers.BaseRenderer;
import net.lintford.library.renderers.RendererManager;

public class GameParticleRenderer extends BaseRenderer {

	// --------------------------------------
	// Constants
	// --------------------------------------

	public static final String RENDERER_NAME = "GameParticleRenderer";

	// --------------------------------------
	// Variables
	// --------------------------------------

	private LevelController mLevelController;

	private GameParticleSystems mGameParticles;

	private TextureBatch mParticlesTextureBatch;
	private Texture mParticlesTexture;

	// --------------------------------------
	// Properties
	// --------------------------------------

	@Override
	public int ZDepth() {
		return 4;

	}

	// --------------------------------------
	// Constructor
	// --------------------------------------

	public GameParticleRenderer(RendererManager pRendererManager, World pWorld, int pGroupID) {
		super(pRendererManager, RENDERER_NAME, pGroupID);

		mGameParticles = pWorld.gameParticles();

		mParticlesTextureBatch = new TextureBatch();

	}

	// --------------------------------------
	// Core-Methods
	// --------------------------------------

	@Override
	public void loadGLContent(ResourceManager pResourceManager) {
		super.loadGLContent(pResourceManager);

		mParticlesTextureBatch.loadGLContent(pResourceManager);

		mParticlesTexture = TextureManager.textureManager().loadTexture("GameParticlesTexture", "res//textures//textureParticles.png");

		mLevelController = (LevelController) mRendererManager.core().controllerManager().getControllerByNameRequired(LevelController.CONTROLLER_NAME, mEntityID);

	}

	@Override
	public void unloadGLContent() {
		super.unloadGLContent();

		mParticlesTextureBatch.unloadGLContent();

	}

	@Override
	public void update(LintfordCore pCore) {
		super.update(pCore);

	}

	@Override
	public void draw(LintfordCore pCore) {

		mParticlesTextureBatch.begin(pCore.gameCamera());

		final int lParticleSystemCount = mGameParticles.particleSystems().size();
		for (int i = 0; i < lParticleSystemCount; i++) {
			ParticleSystem lSystem = mGameParticles.particleSystems().get(i);
			renderParticleSystem(pCore, lSystem);

		}

		mParticlesTextureBatch.end();

	}

	// --------------------------------------
	// Methods
	// --------------------------------------

	private void renderParticleSystem(LintfordCore pCore, ParticleSystem pParticleSystem) {

		final int lNumParticles = pParticleSystem.particles().size();
		for (int i = 0; i < lNumParticles; i++) {
			Particle lP = pParticleSystem.particles().get(i);
			if (lP.isFree())
				continue;
			
			final float lScale = lP.scale;
			final float lParticleHalfSize = 16 * lScale;

			mParticlesTextureBatch.draw(mParticlesTexture, lP.sx, lP.sy, lP.sw, lP.sh, lP.x - lParticleHalfSize, lP.y - lParticleHalfSize, lParticleHalfSize * 2, lParticleHalfSize * 2, -0.2f, lP.rot, lParticleHalfSize,
					lParticleHalfSize, 1f, lP.r, lP.g, lP.b, lP.a);

		}
	}

	@Override
	public void initialise(LintfordCore pCore) {
		// TODO Auto-generated method stub

	}

}
