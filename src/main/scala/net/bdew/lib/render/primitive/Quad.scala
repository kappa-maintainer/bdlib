/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.render.primitive

import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.util.Direction
import net.minecraft.util.math.vector.TransformationMatrix

case class Quad(vertexes: List4[Vertex], face: Direction) {
  /**
   * Applies a transformation function to the quad
   */
  def transform(f: Vertex => Vertex): Quad = copy(vertexes = vertexes map f)

  /**
   * Applies a transformation to the vertexes, leaving other data untouched
   */
  def applyTransformation(t: TransformationMatrix): Quad = copy(
    vertexes map (_.applyTransformation(t))
    , face = t.rotateTransform(face)
  )

  /**
   * Combines vertex data with texture data for the quad
   *
   * @param texture texture object
   * @param tint    tint index (if any) - used for coloring blocks via Block.colorMultiplier
   * @return
   */
  def withTexture(texture: Texture, tint: Int = -1, shading: Boolean = true, diffuseLighting: Boolean = true): TQuad =
    TQuad(vertexes.combine(texture.uv) { (vertex, uv) =>
      vertex.withTexture(texture.sprite, uv)
    }, face, texture.sprite, tint = tint, shading = shading, diffuseLighting = diffuseLighting)
}

/**
 * Quad with vertexes and texture coordinates
 *
 * @param face face to which this quad belongs
 * @param tint tint index (if any) - used for coloring blocks via Block.colorMultiplier
 */
case class TQuad(vertexes: List4[TVertex], face: Direction, sprite: TextureAtlasSprite, tint: Int = -1, shading: Boolean = true, diffuseLighting: Boolean = true) {
  /**
   * Applies a transformation function to the quad
   */
  def transform(f: TVertex => TVertex): TQuad = copy(vertexes = vertexes map f)

  /**
   * Applies a transformation to the vertexes, leaving other data untouched
   */
  def applyTransformation(t: TransformationMatrix): TQuad = copy(
    vertexes map (_.applyTransformation(t))
    , face = t.rotateTransform(face)
  )
}


