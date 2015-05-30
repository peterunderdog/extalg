//Title:       QUndoRedoItem
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;
import java.util.Vector;

/**
 * class for tracking items added to/deleted from quiver, for undo/redo
 * this is used by the undo/redo mechanism
 */
public abstract class QUndoRedoItem
{
  /** the item */
  protected QItem item;

  /**
   * constructs new UndoRedoItem instance from item, flag
   */
  public QUndoRedoItem(QItem item)
  {
    this.item=item;
  }

  /**
   * returns the item
   */
  public QItem item()
  {
    return item;
  }

  /**
   * perform undo action
   * @param q quiver upon which undo is performed
   */
  public abstract void undoAction(Quiver q);

  /**
   * perform redo action
   * @param q quiver upon which redo is performed
   */
  public abstract void redoAction(Quiver q);
}
