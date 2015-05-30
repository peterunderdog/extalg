//Title:       QPoint
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.FontMetrics;

/**
 * points (vertices) in a quiver. The term 'point' is reserved for this type
 * of point. 
 */
public class QPoint extends QItem
{
  /** the physical location of the point */
  Point pt;
  /** size (radius) of dot drawn to represent a point */
  public static final int ptSize=4;
  /** the label character for the point */
  public char label;
  public static boolean CAN_DELETE=true;

  /**
   * constructor
   * @param pt the physical location of the point
   * @param label the label character
   */
  public QPoint(Point pt, char label)
  {
    setPoint(pt);
    this.label=label;
    selected=false;
  }

  /**
   * returns the location of the point
   */
  public Point point()
  {
    return pt;
  }

  /**
   * returns the label character
   */
  public char label()
  {
    return label;
  }

  /**
   * sets the label character
   * @param c the label character
   */
  public void setLabel(char c)
  {
    this.label=c;
  }

  /**
   * sets the location of the point
   * @param pt the location
   */
  public void setPoint(Point pt)
  {
    this.pt=pt;
  }

  /**
   * translates the point by (dx, dy)
   * @param dx x component of translation
   * @param dy y component of translation
   */
  public void translate(int dx, int dy)
  {
    pt.translate(dx, dy);
  }

  /**
   * x coordinate of point's location
   */
  public int X()
  {
    return (int) point().getX();
  }

  /**
   * y coordinate of point's location
   */
  public int Y()
  {
    return (int) point().getY();
  }
  
  
  /**
   * distance from coordinate, using rectangular metric
   * @param pt a coordinate
   * @return the distance from the coordinate
   */
  public double distance(Point pt)
  {
    return point().distance(pt);
  }

  /**
   * distance from another QPoint
   * @param pt a point
   * @return the distance
   */
  public double distance(QPoint pt)
  {
    return point().distance(pt.point());
  }

  /**
   * returns rectangle containing label character.
   * This is for positioning the label, and adjusting arrows so they don't collide with labels.
   * @param g a graphics context from which font metrics are obtained
   * @return the rectangle containing the label character
   */
  protected Rectangle labelRectangle(Graphics2D g)
  {
    FontMetrics fm=g.getFontMetrics();
    int width=ptSize+2+fm.charWidth(label);
    int x=X()-width;
    int height=fm.getAscent();
    int y=Y()+ptSize+5-height;
    return new Rectangle(x, y, width, height);
  }

  /**
   * offset for endpoints of arrows.
   * Determines whether (x, y) lies within label rectangle, and if so,
   * returns an appropriate offset to move it outside the rectangle.
   * @param x x coordinate of point being tested
   * @param y y coordinate of point being tested
   * @return the offest amount
   */
  public int labelOffset(Graphics2D g, int x, int y)
  {
    Rectangle rc=labelRectangle(g);
    return rc.contains(x, y) ? rc.width : 0;
  }

  /**
   * draw a point
   * @param g graphics context onto which point will be drawn
   */
  protected void drawItem(Graphics2D g)
  {
    g.fillOval(X()-ptSize, Y()-ptSize, 2*ptSize+1, 2*ptSize+1);
    // draw label
    g.setFont(QItem.labelFont);
    Rectangle rc=labelRectangle(g);
    // debug only
    // g.draw(rc);
    g.drawString(String.valueOf(label), rc.x, rc.y+rc.height-5);
  }

  /**
   * return true iff point is selected to be deleted
   */
  public boolean isDeleteItem()
  {
    return CAN_DELETE && isSelected();
  }
}
