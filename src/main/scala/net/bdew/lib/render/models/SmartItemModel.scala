package net.bdew.lib.render.models

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType
import net.minecraft.client.renderer.block.model.{BakedQuad, ItemOverrides}
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.core.Direction
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.client.ForgeHooksClient

import java.util
import java.util.Random

/**
 * Provides a saner replacement to ISmartItemModel (RIP) via ItemOverrides
 */
trait SmartItemModel extends BakedModel {
  /**
   * Override this to provide quads for items. Normal getQuads is not called for them.
   */
  def getItemQuads(stack: ItemStack, side: Direction, transformType: TransformType, rand: Random): util.List[BakedQuad]

  final override def getOverrides: ItemOverrides = ItemOverrides

  private object ItemOverrides extends ItemOverrides() {
    override def resolve(originalModel: BakedModel, stack: ItemStack, world: ClientLevel, entity: LivingEntity, p_173469_ : Int): BakedModel = {
      new ItemModel(stack, null)
    }
  }

  private class ItemModel(stack: ItemStack, transform: TransformType) extends BakedModelProxy(this) {
    override def getQuads(state: BlockState, side: Direction, rand: Random): util.List[BakedQuad] = {
      getItemQuads(stack, side, transform, rand)
    }

    override def handlePerspective(cameraTransformType: TransformType, mat: PoseStack): BakedModel =
      ForgeHooksClient.handlePerspective(new ItemModel(stack, cameraTransformType), cameraTransformType, mat)
  }
}