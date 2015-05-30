//Title:       QMovePointTool
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;
import java.awt.event.*;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Vector;
import java.util.Enumeration;
import quiver.*;

public class QMovePointTool extends QTool
{
  protected QPoint currentPoint;
  protected boolean dragging;
  protected Vector incidentPts;

  public QMovePointTool(DrawQuiverPanel panel)
  {
    super(panel);
    incidentPts=new Vector();
  }

  /**
   * start the tool...no items can be delete
   */
  public synchronized void start()
  {
    super.start();
    QPoint.CAN_DELETE=false;
    QArrow.CAN_DELETE=false;
    QZeroRelation.CAN_DELETE=false;
    QCommutativityRelation.CAN_DELETE=false;
  }

  /**
   * button label
   */
  public String buttonLabel()
  {
    return "Move";
  }

  /**
   * drag
   */
  public void mouseDragged(MouseEvent e)
  {
    currDragPoint=e.getPoint();
    if (!panel.dragging())
    {
      if (currentPoint!=null)
      {
        lastDragPoint=null;
        incidentPts=panel.quiver().getIncidentPoints(currentPoint);
        panel.setDragging(true);
      }
    }
    else
    {
      panel.repaint();
    }
    e.consume();
  }

  /**
   * draw lines from point to points in incidentPts vector
   */
  protected void drawLinesToPoints(Point p, Graphics2D g)
  {
    Enumeration e=incidentPts.elements();
    while (e.hasMoreElements())
    {
      Point q=(Point) e.nextElement();
      g.drawLine(p.x, p.y, q.x, q.y);
    }
    // draw cross at mouse point so that we can see
    // point getting dragged w/o lines
    g.drawLine(p.x-QPoint.ptSize, p.y, p.x+QPoint.ptSize, p.y);
    g.drawLine(p.x, p.y+QPoint.ptSize, p.x, p.y-QPoint.ptSize);
  }

  /**
   * draw drag
   */
  public void drawDrag(Graphics2D g, Color background)
  {
    g.setColor(Color.black);
    g.setXORMode(background);
    if (lastDragPoint!=null)
    {
      drawLinesToPoints(lastDragPoint, g);
    }
    if (currDragPoint!=null)
    {
      drawLinesToPoints(currDragPoint, g);
    }
    lastDragPoint=currDragPoint;
  }

  /**
   * press
   */
  public void mousePressed(MouseEvent e)
  {
    e.consume();
    Point p=e.getPoint();
    currentPoint=panel.quiver().selectPointFromCoord(p);
    panel.repaint();
    panel.requestFocus();
  }

  /**
   * release
   */
  public void mouseReleased(MouseEvent e)
  {
    if (panel.dragging())
    {
      // clear drag
      currDragPoint=null;
      panel.repaint();
      panel.setDragging(false);
      Point pt=e.getPoint();
      if (currentPoint != null)
      {
        pt=panel.snapGridPoint(pt);
        panel.quiver().movePoint(currentPoint, pt);
        panel.repaint();
      }
    }
  }
}
