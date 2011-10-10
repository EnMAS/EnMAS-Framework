package org.enmas.client.gui

abstract class GraphicsPlugin {
  val gui: ClientGUI
  def update(iteration: POMDPIteration)
}