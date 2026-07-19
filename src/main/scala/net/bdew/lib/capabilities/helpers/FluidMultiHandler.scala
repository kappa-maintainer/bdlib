/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.capabilities.helpers

import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.{IFluidHandler, IFluidTankProperties}

/**
  * Composes multiple fluid handlers into one.
  */
class FluidMultiHandler(handlers: List[IFluidHandler]) extends IFluidHandler {
  override def getTankProperties: Array[IFluidTankProperties] =
    handlers.flatMap(_.getTankProperties).toArray

  override def drain(resource: FluidStack, doDrain: Boolean): FluidStack =
    handlers.iterator.map(_.drain(resource, doDrain)).find(_ != null).orNull

  override def drain(maxDrain: Int, doDrain: Boolean): FluidStack =
    handlers.iterator.map(_.drain(maxDrain, doDrain)).find(_ != null).orNull

  override def fill(original: FluidStack, doFill: Boolean): Int = {
    val resource = original.copy()
    val starting = resource.amount
    val iterator = handlers.iterator
    while (iterator.hasNext && resource.amount > 0) {
      val filled = iterator.next().fill(resource.copy(), doFill)
      resource.amount -= filled
    }
    if (resource.amount <= 0) starting else starting - resource.amount
  }
}

object FluidMultiHandler {
  def wrap(handlers: List[IFluidHandler]) = {
    if (handlers.length == 1)
      handlers.head
    else
      new FluidMultiHandler(handlers)
  }
}