//Title:       QUndoRedoMovedPoint
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;

import java.awt.Point;

/**
 * class for tracking items deleted from quiver
 */
public class QUndoRedoMovedPoint extends QUndoRedoItem
{
  protected Point p0;
  protected Point p1;
  /**
   * constructor
   */
  public QUndoRedoMovedPoint(QPoint point, Point p)
  {
    super(point);
    p0=point.point();
    p1=p;
  }

  /**
   * undo action - move back to original location
   */
  public void undoAction(Quiver q)
  {
    ((QPoint) item()).setPoint(p0);
  }

  /**
   * redo action - move it back
   */
  public void redoAction(Quiver q)
  {
    ((QPoint) item()).setPoint(p1);
  }
}
