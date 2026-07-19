/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.tile.inventory

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.util.text.ITextComponent

class MultipleInventoryAdapter(val map: Map[Int, (IInventory, Int)]) extends IInventory {
  def this(inventories: Iterable[IInventory]) =
    this((inventories flatMap { inv => (0 until inv.getSizeInventory).map((inv, _)) }).zipWithIndex.map(_.swap).toMap)
  def this(inventories: IInventory*) =
    this((inventories flatMap { inv => (0 until inv.getSizeInventory).map((inv, _)) }).zipWithIndex.map(_.swap).toMap)
    
  private lazy val inventories: Set[IInventory] = map.values.map(_._1).toSet
  override lazy val getInventoryStackLimit: Int = inventories.map(_.getInventoryStackLimit).min
  override lazy val getSizeInventory: Int = map.keySet.max + 1

  private def run[T](slot: Int, f: (IInventory, Int) => T): Option[T] =
    map.get(slot).map(f.tupled)

  override def decrStackSize(slot: Int, num: Int): ItemStack =
    run(slot, (i, s) => i.decrStackSize(s, num)).getOrElse(ItemStack.EMPTY)

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean =
    run(slot, (i, s) => i.isItemValidForSlot(s, item)).getOrElse(false)

  override def setInventorySlotContents(slot: Int, stack: ItemStack): Unit =
    run(slot, (i, s) => i.setInventorySlotContents(s, stack))

  override def getStackInSlot(slot: Int): ItemStack =
    run(slot, (i, s) => i.getStackInSlot(s)).getOrElse(ItemStack.EMPTY)

  override def removeStackFromSlot(slot: Int): ItemStack =
    run(slot, (i, s) => i.removeStackFromSlot(s)).getOrElse(ItemStack.EMPTY)

  override def clear(): Unit = inventories.foreach(_.clear())
  override def isEmpty: Boolean = inventories.forall(_.isEmpty)

  override def markDirty(): Unit = inventories.foreach(_.markDirty())

  override def closeInventory(player: EntityPlayer): Unit = inventories.foreach(_.closeInventory(player))

  override def openInventory(player: EntityPlayer): Unit = inventories.foreach(_.openInventory(player))

  override def isUsableByPlayer(p: EntityPlayer): Boolean = inventories.exists(_.isUsableByPlayer(p))

  override def getFieldCount = 0
  override def getField(id: Int) = 0

  override def setField(id: Int, value: Int): Unit = {}
  override def getDisplayName: ITextComponent = null
  override def getName = ""
  override def hasCustomName = false
}
