package org.enmas.server.logging

import org.enmas.pomdp._, org.enmas.server._

class FileLogger(logFile: String) extends Logger {

  def add(iteration: POMDPIteration) = {}

  def log: Log = List[POMDPIteration]()

}