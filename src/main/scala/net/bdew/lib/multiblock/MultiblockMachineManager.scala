package net.bdew.lib.multiblock

import net.bdew.lib.block.HasTE
import net.bdew.lib.managers.BlockManager
import net.bdew.lib.multiblock.block.{BlockController, BlockModule}
import net.bdew.lib.multiblock.item.{ControllerBlockItem, ModuleBlockItem}
import net.bdew.lib.multiblock.tile.{TileController, TileModule}
import net.bdew.lib.{BdLib, Misc}
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.tileentity.{TileEntity, TileEntityType}
import net.minecraftforge.fml.RegistryObject

abstract class MultiblockMachineManager(blocks: BlockManager) {
  def resources: ResourceProvider

  class ControllerDef[B <: Block, E <: TileEntity, I <: BlockItem, C <: MultiblockMachineConfig](val block: RegistryObject[B with HasTE[E]], val teType: RegistryObject[TileEntityType[E]], val item: RegistryObject[I], val cfg: C)

  class ModuleDef[B <: Block, E <: TileEntity, I <: BlockItem](val block: RegistryObject[B with HasTE[E]], val teType: RegistryObject[TileEntityType[E]], val item: RegistryObject[I])

  var controllers = Map.empty[String, ControllerDef[_ <: Block, _ <: TileEntity, _ <: BlockItem, _ <: MultiblockMachineConfig]]

  def registerController[E <: TileController, B <: BlockController[E], C <: MultiblockMachineConfig](id: String, blockFactory: () => B, tileFactory: TileEntityType[_] => E, cfg: C)
                                                                                                    (implicit q: B <:< B with HasTE[E]): ControllerDef[B, E, ControllerBlockItem, C] = {
    val res = blocks.define(id, blockFactory)
      .withTE(tileFactory)
      .withItem(new ControllerBlockItem(_, blocks.defaultItemProps, this, cfg))
      .registerEx(new ControllerDef(_, _, _, cfg))

    controllers += id -> res
    res
  }

  def registerModule[E <: TileModule, B <: BlockModule[E]](id: String, blockFactory: () => B, tileFactory: TileEntityType[_] => E)
                                                          (implicit q: B <:< B with HasTE[E]): ModuleDef[B, E, ModuleBlockItem] =
    blocks.define(id, blockFactory)
      .withTE(tileFactory)
      .withItem(new ModuleBlockItem(_, blocks.defaultItemProps, this))
      .registerEx(new ModuleDef(_, _, _))

  def registerModule[E <: TileModule, B <: BlockModule[E], I <: ModuleBlockItem](id: String, blockFactory: () => B, tileFactory: TileEntityType[_] => E, itemFactory: B => I)
                                                                                (implicit q: B <:< B with HasTE[E]): ModuleDef[B, E, I] =
    blocks.define(id, blockFactory)
      .withTE(tileFactory)
      .withItem(itemFactory)
      .registerEx(new ModuleDef(_, _, _))


  def getMachinesForBlock(b: BlockModule[_]): Map[String, (Int, Int)] = {
    for ((id, machine) <- controllers; max <- machine.cfg.modules().get(b.kind)) yield {
      id -> (machine.cfg.required().getOrElse(b.kind, 0), max)
    }
  }

  def init(): Unit = {
    BdLib.logInfo(s"Initialized multiblock manager for ${Misc.getActiveModId}")
  }
}
