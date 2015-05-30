//Title:       QUndoRedoAddedItem
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;

/**
 * class for tracking items added to quiver
 */
public class QUndoRedoAddedItem extends QUndoRedoItem
{
  /**
   * constructor
   */
  public QUndoRedoAddedItem(QItem item)
  {
    super(item);
  }

  /**
   * undo action - delete the item
   */
  public void undoAction(Quiver q)
  {
    q.removeItem(item());
  }

  /**
   * redo action - add the item to the quiver
   */
  public void redoAction(Quiver q)
  {
    q.addItem(item());
  }
}
