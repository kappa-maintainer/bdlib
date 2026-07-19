/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.multiblock.interact

import net.bdew.lib.PimpVanilla._
import net.bdew.lib.block.BlockFace
import net.bdew.lib.multiblock.data._
import net.bdew.lib.multiblock.tile.{TileController, TileModule}
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextComponentTranslation

trait CIOutputFaces extends TileController {
  val maxOutputs: Int

  val outputFaces = new DataSlotBlockFaceMap("outputs", this)
  val outputConfig = new DataSlotOutputConfig("cfg", this, maxOutputs)

  def newOutput(bp: BlockPos, face: EnumFacing, cfg: OutputConfig): Int = {
    val blockFace = BlockFace(bp, face)
    outputFaces.get(blockFace) match {
      case Some(output) =>
        println("Adding already registered output??? " + blockFace)
        output
      case None =>
        val availableOutputs = outputFaces.inverted
        (0 until maxOutputs).find(output => !availableOutputs.contains(output)) match {
          case Some(output) =>
            outputFaces += blockFace -> output
            outputConfig += output -> cfg
            outputFaces.updated()
            output
          case None =>
            val player = getWorld.getClosestPlayer(blockFace.x, blockFace.y, blockFace.z, 10, false)
            if (player != null)
              player.sendStatusMessage(new TextComponentTranslation("bdlib.multiblock.toomanyoutputs"), true)
            -1
        }
    }
  }

  serverTick.listen(doOutputs)

  override def moduleRemoved(module: TileModule): Unit = {
    for ((bf, n) <- outputFaces if bf.pos == module.getPos) {
      outputFaces -= bf
      outputConfig -= n
    }
    outputFaces.updated()
    outputConfig.updated()
    super.moduleRemoved(module)
  }

  def doOutputs(): Unit = {
    for {
      (x, n) <- outputFaces
      t <- getWorld.getTileSafe[MIOutput[?]](x.pos)
    } {
      if (!outputConfig.isDefinedAt(n) || !t.outputConfigType.isInstance(outputConfig(n)))
        outputConfig(n) = t.makeCfgObject(x.face)
      t.doOutput(x.face, outputConfig(n).asInstanceOf[t.OCType])
    }
  }

  def removeOutput(bp: BlockPos, face: EnumFacing): Unit = {
    val bf = BlockFace(bp, face)
    outputConfig -= outputFaces(bf)
    outputFaces -= bf
    outputFaces.updated()
  }
}
