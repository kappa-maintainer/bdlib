package net.bdew.lib.gui.widgets

import com.mojang.blaze3d.vertex.PoseStack
import net.bdew.lib.gui.{Point, Rect}
import net.minecraft.client.gui.components.Button
import net.minecraft.network.chat.Component

class ButtonWidgetPressable(clicked: WidgetButton => Unit) extends Button.OnPress {
  override def onPress(b: Button): Unit = clicked(b.asInstanceOf[WidgetButton])
}

class WidgetButton(val rect: Rect, text: Component, clicked: WidgetButton => Unit)
  extends Button(rect.x.round, rect.y.round, rect.w.round, rect.h.round, text, new ButtonWidgetPressable(clicked)) with Widget {

  override def mouseClicked(p: Point, button: Int): Boolean = {
    val pp = p + rect.origin
    if (super.mouseClicked(pp.x, pp.y, button)) {
      clicked(this)
      true
    } else false
  }

  override def draw(m: PoseStack, mouse: Point, partial: Float): Unit = {
    render(m, mouse.x.round, mouse.y.round, partial)
  }
}
