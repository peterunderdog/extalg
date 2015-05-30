//Title:       QPath
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Point;

/** paths in a quiver */
public class QPath
{
  /** path represented as a vector of consecutive arrows */
  protected Vector path;
  /** points determining curve which is drawn to represent the path */
  private Point2D.Double[] curvePts=new Point2D.Double[4];
  /** curve which is drawn to represent the path */
  private CubicCurve2D curve=new CubicCurve2D.Double();
  /** curve used for getting midpoint of curve */
  private CubicCurve2D left=new CubicCurve2D.Double();
  /** curve used for getting midpoint of curve */
  private CubicCurve2D right=new CubicCurve2D.Double();

  /**
   * constructor
   */
  public QPath()
  {
    path=new Vector();
    for (int i=0; i<4; i++)
    {
      curvePts[i]=new Point2D.Double();
    }
  }

  /**
   * empties the path
   */
  public void clear()
  {
    path.clear();
  }

  /**
   * first arrow on path
   * @return the first arrow on the path
   */
  protected QArrow firstArrow()
  {
    return path.isEmpty() ? null : (QArrow) path.firstElement();
  }
  
  /**
   * last arrow on path
   * @return the last arrow on the path
   */
  protected QArrow lastArrow()
  {
    return path.isEmpty() ? null : (QArrow) path.lastElement();
  }

  /**
   * returns the arrow at the given index
   * @param index the index
   * @return the arrow
   */
  public QArrow arrowAt(int index)
  {
    return (QArrow) path.elementAt(index);
  }
  
  /**
   * first vertex on the path
   */
  public QPoint start()
  {
    return path.isEmpty() ? null : firstArrow().start();
  }

  /**
   * last vertex on the path
   */
  public QPoint end()
  {
    return path.isEmpty() ? null : lastArrow().end();
  }

  /**
   * length of path
   * @return length of the path
   */
  public int length()
  {
    return path.size();
  }

  /**
   * returns true iff path is cycle (i.e., ends where it began)
   */
  public boolean isCycle()
  {
    return length() > 0 && start()==end();
  }
  
  /**
   * add arrow to path...doesn't allow cycles to be extended
   * @return true iff arrow can be added to path..
   */
  public boolean add(QArrow arrow)
  {
    if ((end()==null || end()==arrow.start()) && !isCycle())
    {
      path.addElement(arrow);
      return true;
    }
    else
    {
      return false;
    }
  }

  /**
   * remove last arrow on path
   */
  public boolean removeLast()
  {
    if (path.size() > 0)
    {
      path.setSize(path.size()-1);
      return true;
    }
    else
      return false;
  }
  
  /**
   * get points for drawing curve
   * @param pts the points
   * @return true iff we got the points (i.e., path was non-empty)
   */
  public boolean getCurvePoints(Point2D[] pts)
  {
    if (length()==0)
      return false;
    else
    {
      pts[0].setLocation((double) firstArrow().startPosX(), (double) firstArrow().startPosY());
      pts[1].setLocation((double) firstArrow().endPosX(), (double) firstArrow().endPosY());
      pts[2].setLocation((double) lastArrow().startPosX(), (double) lastArrow().startPosY());
      pts[3].setLocation((double) lastArrow().endPosX(), (double) lastArrow().endPosY());
      return true;
    }
  }

  /**
   * returns true iff item on path is selected to be deleted
   */
  public boolean containsDeleteItem()
  {
    if (length()==0)
      return false;
    else if (start().isDeleteItem())
      return true;
    else
    {
      Enumeration e=path.elements();
      while (e.hasMoreElements())
      {
        QArrow arr=(QArrow) e.nextElement();
        if (arr.isDeleteItem() || arr.end().isDeleteItem())
          return true;
      }
    }
    return false;
  }

  /**
   * select all items on the path
   */
  public void select()
  {
    if (start() != null)
    {
      start().select();
    }
    Enumeration e=path.elements();
    while (e.hasMoreElements())
    {
      QArrow arr=(QArrow) e.nextElement();
      arr.select();
      arr.end().select();
    }
  }
  /**
   * gets the curve used in drawing the item
   * @return the curve
   */
  public CubicCurve2D getCurve()
  {
    getCurvePoints(curvePts);
    curve.setCurve(curvePts, 0);
    return curve;
  }

  /**
   * locates the midpoint of the curve representing the path.
   * This is used for positioning a label on the curve
   * @return the midpoint
   */
  public Point2D curveMidPoint()
  {
    CubicCurve2D.subdivide(getCurve(), left, right);
    return left.getP2();
  }

  /**
   * draws a curve, with optional label, representing the path
   * @param g a graphics context onto which the curve is rendered
   * @label a label (null if you don't want one)
   */
  public void drawCurve(Graphics2D g, String label)
  {
    if (path.size()>0)
    {
      Stroke st=g.getStroke();
      g.setStroke(QItem.dotted);
      g.draw(getCurve());
      g.setStroke(st);
      Point2D pt=curveMidPoint();
      g.setFont(QItem.labelFont);
      if (label != null)
        g.drawString(label, (int) pt.getX(), (int) pt.getY());
    }
  }

  /**
   * point to curve distance
   * this doesn't really measure distance..it just gives
   * a sort of yes/no answer as to whether the point pt
   * is close enough (within snap tolerance) to the curve
   * representing the path
   *
   * todo: this doesn't work very well...seems to 'intersect'
   * even when the cursor is pretty far away
   * @param pt a coordinate
   * @return the distance
   */
  public double distanceToCurve(Point pt)
  {
    Rectangle2D rc=new Rectangle2D.Double();
    rc.setRect(pt.x-0.5*QItem.snapTol, pt.y-0.5*QItem.snapTol, QItem.snapTol, QItem.snapTol);
    
    if (getCurve().intersects(rc))
      // return value just under snap tolerance so that points/lines
      // closer to the cursor will be returned (if they exist)
      return 0.999*QItem.snapTol;
    else
      return 2*QItem.snapTol;
  }
}
