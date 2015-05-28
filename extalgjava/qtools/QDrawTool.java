//Title:       QDrawTool
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;
import java.awt.event.*;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.Vector;
import java.awt.Point;
import quiver.*;

public class QDrawTool extends QTool
{
  protected QPoint currentPoint;
  
  public QDrawTool(DrawQuiverPanel panel)
  {
    super(panel);
  }

  /**
   * start the tool...set the items it can delete
   */
  public synchronized void start()
  {
    super.start();
    QPoint.CAN_DELETE=true;
    QArrow.CAN_DELETE=true;
    QZeroRelation.CAN_DELETE=true;
    QCommutativityRelation.CAN_DELETE=true;
  }

  /**
   * button label
   */
  public String buttonLabel()
  {
    return "Draw";
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
   * draw drag
   */
  public void drawDrag(Graphics2D g, Color background)
  {
    g.setColor(Color.black);
    g.setXORMode(background);
    if (lastDragPoint!=null)
    {
      g.drawLine(currentPoint.point().x, currentPoint.point().y, lastDragPoint.x, lastDragPoint.y);
    }
    if (currDragPoint!=null)
    {
      g.drawLine(currentPoint.point().x, currentPoint.point().y, currDragPoint.x, currDragPoint.y);
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
    QItem item=panel.quiver().multiSelectItemFromCoord(p);
    if (item != null)
    {
      if (item instanceof QPoint)
        currentPoint=(QPoint) item;
      else
        currentPoint=null;
    }
    else if (item==null)
    {
      p=panel.snapGridPoint(p);
      currentPoint=panel.quiver().addPoint(p);
    }
    panel.repaint();
    panel.requestFocus();
  }

  /**
   * release
   */
  public void mouseReleased(MouseEvent e)
  {
    boolean gotIt=false;
    if (panel.dragging())
    {
      currDragPoint=null;
      // clear the drag
      panel.repaint();
      Point p=e.getPoint();
      QPoint pt=panel.quiver().pointFromCoord(p);
      boolean newpt=false;
      if (pt==null)
      {
        p=panel.snapGridPoint(p);
        pt=panel.quiver().addPoint(p);
        newpt=true;
      }
      if (pt != currentPoint)
      {
        if (panel.quiver().addArrow(currentPoint, pt) != null)
        {
          currentPoint=pt;
          if (newpt)
            pt.select();
          panel.repaint();
        }
      }
      panel.setDragging(false);
    }
    e.consume();
  }

  public void keyPressed(KeyEvent e)
  {
    char c=e.getKeyChar();
    if (currentPoint!=null && currentPoint.isSelected())
    {
      if (panel.quiver().setLabel(currentPoint, c))
      {
        currentPoint.unselect();
        panel.repaint();
      }
    }
  }
}
