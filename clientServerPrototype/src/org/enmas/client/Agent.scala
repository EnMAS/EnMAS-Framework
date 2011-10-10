package org.enmas.client

import org.enmas.pomdp._, org.enmas.messaging._

trait Agent extends Client {
  protected final def takeAction(action: Action) = {
    self.reply(TakeAction(action))
  }
}
