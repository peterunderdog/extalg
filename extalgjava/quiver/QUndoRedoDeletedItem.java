//Title:       QUndoRedoDeletedItem
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;

/**
 * class for tracking items deleted from quiver
 */
public class QUndoRedoDeletedItem extends QUndoRedoItem
{
  /**
   * constructor
   */
  public QUndoRedoDeletedItem(QItem item)
  {
    super(item);
  }

  /**
   * undo action - add the item
   */
  public void undoAction(Quiver q)
  {
    q.addItem(item());
  }

  /**
   * redo action - remove the item from the quiver
   */
  public void redoAction(Quiver q)
  {
    q.removeItem(item());
  }
}
