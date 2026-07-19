/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.covers

import net.bdew.lib.Misc
import net.bdew.lib.block.{BaseBlock, HasTE}
import net.bdew.lib.items.ItemUtils
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{BlockRenderLayer, EnumFacing, EnumHand}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.property.{IExtendedBlockState, IUnlistedProperty}

trait BlockCoverable extends BaseBlock {
  self: HasTE[? <: TileCoverable] =>

  override def getUnlistedProperties: List[IUnlistedProperty[?]] = super.getUnlistedProperties :+ CoversProperty

  override def getExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState = {
    getTE(world, pos) map { tile =>
      super.getExtendedState(state, world, pos).asInstanceOf[IExtendedBlockState].withProperty(CoversProperty,
        tile.covers flatMap { case (side, cover) =>
          cover flatMap { stack =>
            if (!stack.isEmpty && stack.getItem.isInstanceOf[ItemCover])
              Some(side -> stack.getItem.asInstanceOf[ItemCover].getDisplayItem(tile, side, stack))
            else
              None
          }
        })
    } getOrElse state
  }

  override def canRenderInLayer(state: IBlockState, layer: BlockRenderLayer): Boolean =
    layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT

  private def getCoverItem(w: IBlockAccess, pos: BlockPos, side: EnumFacing): Option[(ItemCover, ItemStack, TileCoverable)] = {
    for {
      te <- getTE(w, pos)
      coverStack <- te.covers(side)
      coverItemMaybe <- Option(coverStack.getItem)
      coverItem <- Misc.asInstanceOpt(coverItemMaybe, classOf[ItemCover])
    } yield (coverItem, coverStack, te)
  }

  override def onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val te = getTE(world, pos)

    val heldItem = player.getHeldItem(hand)

    // Shift+RightClick with empty hand should remove cover
    if (heldItem.isEmpty && player.isSneaking) {
      te.covers(side).flatMap(cover =>
        Misc.asInstanceOpt(cover.getItem, classOf[ItemCover]).map(_ -> cover)
      ) match {
        case Some((coverItem, cover)) =>
          if (!world.isRemote) {
            te.covers(side).unset()
            te.onCoversChanged()
            coverChanged(world, pos, side)
            ItemUtils.dropItemToPlayer(world, player, coverItem.onRemove(cover))
          }
          return true
        case None =>
      }
    }

    // If held item is cover - try to install it
    val coverItem = Option(heldItem.getItem).flatMap(Misc.asInstanceOpt(_, classOf[ItemCover]))
      .filter(item => te.isValidCover(side, heldItem) && item.isValidTile(te, side, heldItem))
    coverItem match {
      case Some(item) =>
        if (!world.isRemote && player.isInstanceOf[EntityPlayerMP]) {
          te.covers(side).flatMap(oldCover =>
            Misc.asInstanceOpt(oldCover.getItem, classOf[ItemCover]).map(_ -> oldCover)
          ).foreach { case (oldItem, oldCover) =>
            ItemUtils.throwItemAt(world, pos.offset(side), oldItem.onRemove(oldCover))
          }

          te.covers(side).set(item.onInstall(te, side, heldItem.splitStack(1), player.asInstanceOf[EntityPlayerMP]))
          te.onCoversChanged()
          coverChanged(world, pos, side)
        }
        return true
      case None =>
    }

    getCoverItem(world, pos, side) match {
      case Some((item, stack, tile)) if item.clickCover(tile, side, stack, player) => return true
      case _ =>
    }

    super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ)
  }

  override def canProvidePower(state: IBlockState): Boolean = true

  override def getWeakPower(blockState: IBlockState, blockAccess: IBlockAccess, pos: BlockPos, side: EnumFacing): Int = {
    val opposite = side.getOpposite
    if (getCoverItem(blockAccess, pos, opposite).exists { case (item, stack, tile) =>
      item.isEmittingSignal(tile, opposite, stack)
    }) 15 else 0
  }

  def coverChanged(w: World, pos: BlockPos, side: EnumFacing): Unit =
    w.neighborChanged(pos.offset(side), this, pos)

  override def breakBlock(world: World, pos: BlockPos, state: IBlockState) = {
    if (!world.isRemote) {
      val te = getTE(world, pos)
      for {
        (dir, slot) <- te.covers
        cover <- slot
        item <- Misc.asInstanceOpt(cover.getItem, classOf[ItemCover])
      } {
        te.covers(dir).unset()
        ItemUtils.throwItemAt(world, pos, item.onRemove(cover))
        coverChanged(world, pos, dir)
      }
    }
    super.breakBlock(world, pos, state)
  }
}
