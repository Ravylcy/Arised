package com.sololeveling.necromancy.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.sololeveling.necromancy.client.renderer.ShadowRenderHandler;
import com.sololeveling.necromancy.client.renderer.ShadowVertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> {

    // --- NEW: We need access to the entity's model to render the glint. ---
    @Shadow
    protected EntityModel<T> model;

    private static final ThreadLocal<LivingEntity> currentEntity = new ThreadLocal<>();

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void captureEntity(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        currentEntity.set(entity);
    }

    // This inject now serves two purposes: cleaning up the ThreadLocal AND rendering the glint.
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("RETURN"))
    private void renderGlintAndCleanup(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo ci) {
        // --- THE FIX: Render the glint layer after everything else has been drawn. ---
        if (ShadowRenderHandler.isEntityShadow(entity)) {
            // Get the vertex consumer specifically for the enchanted glint effect.
            VertexConsumer glintConsumer = bufferSource.getBuffer(RenderType.entityGlint());
            // Render the model again, but this time to the glint buffer.
            // This layers the shimmering effect on top of the dark silhouette.
            this.model.renderToBuffer(poseStack, glintConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        // Cleanup the thread-local variable
        currentEntity.remove();
    }

    @ModifyArg(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"),
            index = 1
    )
    private VertexConsumer applyShadowEffect(VertexConsumer originalConsumer) {
        LivingEntity entity = currentEntity.get();
        if (entity != null && ShadowRenderHandler.isEntityShadow(entity)) {
            return new ShadowVertexConsumer(originalConsumer);
        }
        return originalConsumer;
    }
}