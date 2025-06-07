package com.sololeveling.necromancy.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sololeveling.necromancy.core.ModConfigs;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ShadowVertexConsumer implements VertexConsumer {

    private final VertexConsumer parent;
    private final float r;
    private final float g;
    private final float b;
    private final float a;

    public ShadowVertexConsumer(VertexConsumer parent) {
        this.parent = parent;
        this.r = ModConfigs.CLIENT.SHADOW_COLOR_R.get().floatValue();
        this.g = ModConfigs.CLIENT.SHADOW_COLOR_G.get().floatValue();
        this.b = ModConfigs.CLIENT.SHADOW_COLOR_B.get().floatValue();
        this.a = ModConfigs.CLIENT.SHADOW_ALPHA.get().floatValue();
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        return parent.vertex(x, y, z);
    }

    /**
     * MODIFIED: This is the core of the silhouette effect.
     * Instead of using the parameters, we pass our own dark color tint.
     * Crucially, we still use the original 'alpha' value from the texture. This ensures
     * that transparent parts of a model (like the gaps in a skeleton's ribs) remain transparent.
     */
    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        // We use our predefined dark tint, but multiply our alpha by the original alpha.
        // This preserves transparency while applying the dark effect.
        this.parent.color(this.r, this.g, this.b, (alpha / 255.0f) * this.a);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        return parent.uv(u, v);
    }

    /**
     * MODIFIED: We force NO_OVERLAY here.
     * This prevents the red "damage flash" from appearing on our shadows,
     * keeping their aesthetic consistent even when they take damage.
     */
    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        this.parent.overlayCoords(OverlayTexture.NO_OVERLAY);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        return parent.uv2(u, v);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        return parent.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        parent.endVertex();
    }

    @Override
    public void defaultColor(int red, int green, int blue, int alpha) {
        this.parent.defaultColor(red, green, blue, alpha);
    }

    @Override
    public void unsetDefaultColor() {
        this.parent.unsetDefaultColor();
    }
}