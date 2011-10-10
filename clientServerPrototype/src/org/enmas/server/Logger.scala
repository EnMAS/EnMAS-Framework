package org.enmas.server

abstract class Logger {
  def add(iteration: POMDPIteration)
  def log: Log
}