package net.bdew.lib.multiblock.render

import net.bdew.lib.PimpVanilla._
import net.bdew.lib.block.BlockFace
import net.bdew.lib.multiblock.ResourceProvider
import net.bdew.lib.render.connected.ConnectedModelEnhancer
import net.bdew.lib.render.primitive.{Quad, Texture, Vertex}
import net.bdew.lib.render.{Cuboid, QuadBakerDefault}
import net.minecraft.block.BlockState
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.model.BakedQuad
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.{Direction, ResourceLocation}
import net.minecraftforge.client.MinecraftForgeClient
import net.minecraftforge.client.model.data.IModelData

import scala.util.Random

class MultiblockModelEnhancer(resources: ResourceProvider) extends ConnectedModelEnhancer(resources.edge) {
  override def additionalTextureLocations: List[ResourceLocation] = super.additionalTextureLocations ++ List(resources.arrow, resources.output)

  lazy val quads: Map[Direction, Quad] =
    Direction.values().map(f => f -> Cuboid.face(Vertex(-0.01f, -0.01f, -0.01f), Vertex(1.01f, 1.01f, 1.01f), f)).toMap

  override def processBlockQuads(state: BlockState, side: Direction, rand: Random, data: IModelData, textures: Map[ResourceLocation, TextureAtlasSprite], base: () => List[BakedQuad]): List[BakedQuad] = {
    var list = super.processBlockQuads(state, side, rand, data, textures, base)
    if (state != null && side != null && MinecraftForgeClient.getRenderLayer == RenderType.cutout()) {
      data.getDataOpt(OutputFaceProperty) foreach { outputs =>
        val output = Texture(textures(resources.output))
        val arrow = Texture(textures(resources.arrow))

        if (outputs.isDefinedAt(side)) list :+= QuadBakerDefault.bakeQuad(quads(side).withTexture(output, outputs(side) + 1, false))

        val neighbours = BlockFace.neighbourFaces(side)

        if (outputs.isDefinedAt(neighbours.top))
          list :+= QuadBakerDefault.bakeQuad(quads(side).withTexture(arrow, outputs(neighbours.top) + 1, false))

        if (outputs.isDefinedAt(neighbours.right))
          list :+= QuadBakerDefault.bakeQuad(quads(side).withTexture(arrow.rotate(1), outputs(neighbours.right) + 1, false))

        if (outputs.isDefinedAt(neighbours.bottom))
          list :+= QuadBakerDefault.bakeQuad(quads(side).withTexture(arrow.rotate(2), outputs(neighbours.bottom) + 1, false))

        if (outputs.isDefinedAt(neighbours.left))
          list :+= QuadBakerDefault.bakeQuad(quads(side).withTexture(arrow.rotate(3), outputs(neighbours.left) + 1, false))
      }
    }
    list
  }
}
