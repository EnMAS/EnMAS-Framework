package org.enmas.client.gui

import scala.swing._, javax.swing.table._

class MyTableModel( var rowData: Array[Array[Any]], val columnNames: Seq[String] ) extends AbstractTableModel {
  override def getColumnName( column: Int) = columnNames(column).toString
  def getRowCount() = rowData.length
  def getColumnCount() = columnNames.length
  def getValueAt(row: Int, col: Int): AnyRef = rowData(row)(col).asInstanceOf[AnyRef]
  override def isCellEditable( row: Int, column: Int) = false
  override def setValueAt( value: Any, row: Int, col: Int) {
    rowData(row)(col) = value
  }

  def addRow( data: Array[AnyRef]) {
    rowData ++= Array(data.asInstanceOf[Array[Any]])
  }
}
