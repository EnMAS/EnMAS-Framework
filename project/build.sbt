name := "EnMAS"

version := "0.5"

unmanagedClasspath in Runtime <+= (baseDirectory) map {
  bd => Attributed.blank(bd / "config")
}
