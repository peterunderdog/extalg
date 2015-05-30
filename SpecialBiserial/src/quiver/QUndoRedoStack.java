//Title:       UndoRedoStack
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package quiver;

import java.util.Vector;
import java.awt.Point;

public class QUndoRedoStack
{
  /** the stack...contains vectors of items corresponding to each level of undo/redo */
  protected Vector stack=new Vector();
  /** pointer to current item on stack */
  protected int current=0;

  /**
   * trims top of list (items beyond current pointer)
   */
  private void trim()
  {
    if (current < stack.size())
      stack.setSize(current);
  }

  /**
   * begin new undo/redo item list
   */
  public void beginLevel()
  {
    System.out.println("beginLevel()\n");
    if (stack.size()>0 && ((Vector) stack.lastElement()).size()==0)
    {
      System.out.println("warning: no undo/redo information in previous level\n");
    }
    trim();
    stack.addElement(new Vector());
    current=stack.size();
  }

  /**
   * records added item on current undo level...assumes that beginLevel has been called first
   * @param item the item added to the quiver
   */
  public void saveAddedItem(QItem item)
  {
    ((Vector) stack.lastElement()).addElement(new QUndoRedoAddedItem(item));
  }

  /**
   * records deleted item on current undo level...assumes that beginLevel has been called first
   * @param item the item deleted from the quiver
   */
  public void saveDeletedItem(QItem item)
  {
    ((Vector) stack.lastElement()).addElement(new QUndoRedoDeletedItem(item));
  }

  /**
   * records moved point
   * @param point the point that was moved
   * @param p the coordinates to which the point was moved
   */
  public void saveMovedPoint(QPoint point, Point p)
  {
    ((Vector) stack.lastElement()).addElement(new QUndoRedoMovedPoint(point, p));
  }

  /**
   * returns vector of undo items, or null if no items, and moves the stack pointer
   */
  public Vector getUndoItems()
  {
    if (current > 0)
      return (Vector) stack.elementAt(--current);
    else
      return null;
  }

  /**
   * returns vector of redo items, or null if no items, and moves stack pointer
   */
  public Vector getRedoItems()
  {
    if (current < stack.size())
      return (Vector) stack.elementAt(current++);
    else
      return null;
  }
}
