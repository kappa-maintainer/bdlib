package net.bdew.lib.multiblock.network

import net.minecraft.network.PacketBuffer

case class MsgOutputCfgSlot(output: Int, slot: String) extends MultiblockNetHandler.Message with MsgOutputCfg

object CodecOutputCfgSlot extends MultiblockNetHandler.Codec[MsgOutputCfgSlot] {
  override def encodeMsg(m: MsgOutputCfgSlot, p: PacketBuffer): Unit = {
    p.writeVarInt(m.output)
    p.writeUtf(m.slot)
  }

  override def decodeMsg(p: PacketBuffer): MsgOutputCfgSlot = {
    MsgOutputCfgSlot(p.readVarInt(), p.readUtf(100))
  }
}
