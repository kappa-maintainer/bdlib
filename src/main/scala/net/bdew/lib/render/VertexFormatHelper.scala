/*
 * Copyright (c) bdew, 2013 - 2017
 * https://github.com/bdew/bdlib
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.lib.render

import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.client.renderer.vertex.VertexFormatElement.EnumUsage

import scala.jdk.CollectionConverters._

class VertexFormatHelper(val format: VertexFormat) {
  val positionIndex = format.getElements.asScala.indexWhere(_.getUsage == EnumUsage.POSITION)
  val textureIndex = format.getElements.asScala.indexWhere(e => e.getUsage == EnumUsage.UV && e.getIndex == 0)
}

object VertexFormatHelper {
  var cache = Map.empty[VertexFormat, VertexFormatHelper]

  def get(f: VertexFormat) = {
    if (cache.isDefinedAt(f)) {
      cache(f)
    } else {
      val helper = new VertexFormatHelper(f)
      cache += f -> helper
      helper
    }
  }
}