//Title:       QRelationsTool
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;
import java.awt.event.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Stroke;
import java.util.Vector;
import java.awt.Point;
import quiver.*;
import java.awt.geom.CubicCurve2D;

public class QRelationsTool extends QTool
{
  protected QPath currPath=null;
  protected boolean dragging=false;
  
  public QRelationsTool(DrawQuiverPanel panel)
  {
    super(panel);
  }

  /**
   * start tool..can only delete relations
   */
  public synchronized void start()
  {
    super.start();
    QPoint.CAN_DELETE=false;
    QArrow.CAN_DELETE=false;
    QZeroRelation.CAN_DELETE=true;
    QCommutativityRelation.CAN_DELETE=true;
  }

  /**
  * button label
   */
  public String buttonLabel()
  {
    return "Relations";
  }

  /**
   * mouse pressed
   */
  public void mousePressed(MouseEvent e)
  {
    e.consume();
    Point p=e.getPoint();
    QItem item=panel.quiver().itemFromCoord(p);
    if (item==null)
    {
      panel.quiver().unselectAll();
    }
    else if (item instanceof QZeroRelation || item instanceof QCommutativityRelation)
    {
      item.select();
    }
    panel.repaint();
    panel.requestFocus();
  }

  /**
   * drag
   */
  public void mouseDragged(MouseEvent e)
  {
    if (!panel.dragging())
    {
      panel.setDragging(true);
    }
    QItem item=panel.quiver().itemFromCoord(e.getPoint());
    if (item instanceof QArrow)
    {
      if (currPath==null)
      {
        currPath=new QPath();
      }
      QArrow arr=(QArrow) item;
      if (currPath.add(arr))
      {
        currPath.select();
        panel.repaint();
      }
    }
    e.consume();
  }

  /**
   * draw drag
   */
  public void drawDrag(Graphics2D g, Color background)
  {
    panel.dbredraw(g);
    if (currPath!=null)
    {
      Stroke st=g.getStroke();
      g.setStroke(QItem.dotted);
      g.draw(currPath.getCurve());
      g.setStroke(st);
    }
  }

  /**
   * release
   */
  public void mouseReleased(MouseEvent e)
  {
    if (panel.dragging())
    {
      panel.setDragging(false);
      // do some stuff, possibly repaint the panel
      panel.quiver().unselectAll();
      if (currPath!=null && currPath.length()>1)
      {
        QZeroRelation rel=panel.quiver().addZeroRelation(currPath);
        if (rel!=null)
          rel.select();
      }
      currPath=null;
      panel.repaint();
      panel.requestFocus();
    }
    e.consume();
  }

  public void keyPressed(KeyEvent e)
  {
    char c=e.getKeyChar();
    if (c=='=')
    {
      if (panel.quiver().addCommutativityRelation() != null)
      {
        panel.repaint();
      }
    }
  }
}

