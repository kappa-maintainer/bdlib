package net.bdew.lib.recipes

import com.google.gson.{JsonObject, JsonSyntaxException}
import net.bdew.lib.JSResLoc
import net.bdew.lib.resource.{FluidResource, ItemResource, Resource, ResourceKind}
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.network.PacketBuffer
import net.minecraftforge.registries.ForgeRegistries

trait ResourceOutput {
  def resource(amount: Double): Resource = Resource(resourceKind, amount)
  def resourceKind: ResourceKind
  def toPacket(pkt: PacketBuffer): Unit
}

object ResourceOutput {
  case class ItemOutput(item: Item) extends ResourceOutput {
    override def resourceKind: ResourceKind = ItemResource(item)
    override def toPacket(pkt: PacketBuffer): Unit = {
      pkt.writeUtf("item")
      pkt.writeRegistryId(item)
    }
  }

  case class FluidOutput(fluid: Fluid) extends ResourceOutput {
    override def resourceKind: ResourceKind = FluidResource(fluid)
    override def toPacket(pkt: PacketBuffer): Unit = {
      pkt.writeUtf("fluid")
      pkt.writeRegistryId(fluid)
    }
  }

  def apply(v: Item): ResourceOutput = ItemOutput(v)
  def apply(v: Fluid): ResourceOutput = FluidOutput(v)

  def fromPacket(pkt: PacketBuffer): ResourceOutput = {
    pkt.readUtf() match {
      case "item" => ItemOutput(pkt.readRegistryId())
      case "fluid" => FluidOutput(pkt.readRegistryId())
      case x => throw new RuntimeException(s"Unknown output type $x")
    }
  }

  def fromJson(js: JsonObject): ResourceOutput = {
    js.get("item") match {
      case JSResLoc(key) if ForgeRegistries.ITEMS.containsKey(key) =>
        ItemOutput(ForgeRegistries.ITEMS.getValue(key))
      case _ =>
        js.get("fluid") match {
          case JSResLoc(key) if ForgeRegistries.FLUIDS.containsKey(key) =>
            FluidOutput(ForgeRegistries.FLUIDS.getValue(key))
          case _ => throw new JsonSyntaxException("Invalid fluid or item output")
        }
    }
  }
}