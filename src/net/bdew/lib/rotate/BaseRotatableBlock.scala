/*
 * Copyright (c) bdew, 2013 - 2014
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.rotate

import java.util

import net.bdew.lib.PimpVanilla._
import net.minecraft.block.Block
import net.minecraft.block.properties.PropertyDirection
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
import net.minecraft.util.{BlockPos, EnumFacing}
import net.minecraft.world.{IBlockAccess, World}

/**
  * Basic logic for block rotation
  */
trait BaseRotatableBlock extends Block {
  val facingProperty: PropertyDirection

  /**
    * Set of valid rotations, default is all of them
    */
  def getValidFacings: util.EnumSet[EnumFacing] = {
    return util.EnumSet.allOf(classOf[EnumFacing])
  }

  /**
    * Rotation to show when it's unavailable (like rendering item in inventory)
    */
  def getDefaultFacing: EnumFacing = EnumFacing.UP

  def setFacing(world: World, pos: BlockPos, facing: EnumFacing) =
    world.changeBlockState(pos, 3) { state =>
      state.withProperty(facingProperty, facing)
    }

  def getFacing(world: IBlockAccess, pos: BlockPos): EnumFacing =
    world.getBlockState(pos).getValue(facingProperty)

  override def rotateBlock(world: World, pos: BlockPos, axis: EnumFacing): Boolean = {
    if (getValidFacings.contains(axis)) {
      setFacing(world, pos, axis)
      return true
    }
    return false
  }

  override def getValidRotations(worldObj: World, pos: BlockPos): Array[EnumFacing] = getValidFacings.toArray(Array.empty[EnumFacing])

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack) = {
    val dir = RotatedHelper.getFacingFromEntity(placer, getValidFacings, getDefaultFacing)
    setFacing(world, pos, dir)
    super.onBlockPlacedBy(world, pos, state, placer, stack)
  }
}
