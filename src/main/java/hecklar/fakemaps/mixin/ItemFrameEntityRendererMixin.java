package hecklar.fakemaps.mixin;

import hecklar.fakemaps.FakeMaps;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.MapRenderState;
import net.minecraft.client.render.MapRenderer;
import net.minecraft.client.render.entity.ItemFrameEntityRenderer;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.item.map.MapState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemFrameEntityRenderer.class)
public class ItemFrameEntityRendererMixin {

	/**
	 * Skip GPU texture upload when vanilla maps aren't being rendered.
	 * MapRenderer.update() uploads 128x128 RGBA (64KB) per map per frame.
	 * state.mapId is assigned AFTER this call, so it still gets set correctly.
	 */
	@Redirect(
			method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/MapRenderer;update(Lnet/minecraft/component/type/MapIdComponent;Lnet/minecraft/item/map/MapState;Lnet/minecraft/client/render/MapRenderState;)V"
			)
	)
	private void skipMapRendererUpdate(MapRenderer renderer, MapIdComponent mapId, MapState mapState, MapRenderState renderState) {
		if (FakeMaps.vanillaMapsEnabled) {
			renderer.update(mapId, mapState, renderState);
		}
	}

	/**
	 * Skip item model resolution when rendering red squares.
	 * Only needed when vanilla item rendering might occur.
	 */
	@Redirect(
			method = "updateRenderState(Lnet/minecraft/entity/decoration/ItemFrameEntity;Lnet/minecraft/client/render/entity/state/ItemFrameEntityRenderState;F)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/item/ItemModelManager;updateForNonLivingEntity(Lnet/minecraft/client/render/item/ItemRenderState;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ModelTransformationMode;Lnet/minecraft/entity/Entity;)V"
			)
	)
	private void skipItemModelUpdate(ItemModelManager manager, ItemRenderState renderState, ItemStack stack, ModelTransformationMode mode, Entity entity) {
		if (!FakeMaps.renderingEnabled) {
			manager.updateForNonLivingEntity(renderState, stack, mode, entity);
		}
	}
}
