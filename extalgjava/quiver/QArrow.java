//Title:       QArrow
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Polygon;

/**
 * arrows in a quiver
 */
public class QArrow extends QItem
{
  /** the start point of the arrow */
  protected QPoint start;
  /** the end point of the arrow */
  protected QPoint end;
  /** the position of the arrow in relation to other arrows between the same two points */
  protected int pos;
  /** determines how far apart multiple arrows between same points are spaced */
  public static final int arrowSpacing=20;
  /** tells whether items of this type can be deleted by a tool */    public static boolean CAN_DELETE=true;

  /**
   * constructor
   * @param start the start point of the arrow
   * @param end the end point of the arrow
   */
  public QArrow(QPoint start, QPoint end)
  {
    this.start=start;
    this.end=end;
    this.pos=0;
    selected=false;
  }

  /**
   * returns the 'snap tolerance' associated with arrow items for
   * the purpose of selecting items by clicking with the mouse.
   * The snap tolerance for arrows is smaller than the usual snap
   * tolerance for items so that arrows are not selected in
   * preference to other nearby items.
   */
  public double snapTol()
  {
    return 2.0;
  }

  /**
   * allows setting the position of an arrow relative to other arrows with
   * same endpoints. The pos parameter specifies a positive or negative offset
   * in pixels from the normal position (i.e., through the endpoints)
   * which will be applied to the arrow
   * @param pos positive or negative offset from normal arrow position in pixels
   */
  public void setPos(int pos)
  {
    this.pos=pos;
  }

  /**
   * returns start point of arrow
   */
  public QPoint start()
  {
    return start;
  }

  /**
   * returns end point of arrow
   */
  public QPoint end()
  {
    return end;
  }
  
  /**
   * returns x component of vector in the direction of the arrow
   */
  public int vx()
  {
    return (int) end.X()-start.X();
  }

  /**
   * returns y component of vector in the direction of the arrow
   */
  public int vy()
  {
    return (int) end.Y()-start.Y();
  }

  /**
   * returns x component of unit vector in the direction of the arrow
   */
  public double ux()
  {
    return vx()/norm();
  }

  /**
   * returns y component of unit vector in the direction of the arrow
   */
  public double uy()
  {
    return vy()/norm();
  }

  /**
   * returns x component of unit vector perpendicular to arrow,
   * where the unit vector is normalized so that its x component is
   * non-negative
   */
  public double px()
  {
    if (ux()==0.0)
      return 1.0;
    else if (ux() > 0.0)
      return -uy();
    else
      return uy();
  }

  /**
   * returns y component of unit vector perpendicular to arrow,
   * where the unit vector is normalized so that its x component is
   * non-negative
   */
  public double py()
  {
    return ux() > 0.0 ? ux() : -ux();
  }

  /**
   * returns actual x coordinate of start of arrow, as determined by
   * pos offset
   */
  public int startPosX()
  {
    return (int) Math.ceil(pos*px()) + start.X();
  }

  /**
   * returns actual y coordinate of start of arrow, as determined by
   * pos offset
   */
  public int startPosY()
  {
    return (int) Math.ceil(pos*py()) + start.Y();
  }

  /**
   * returns actual x coordinate of end of arrow, as determined by
   * pos offset
   */
  public int endPosX()
  {
    return (int) Math.ceil(pos*px()) + end.X();
  }

  /**
   * returns actual y coordinate of start of arrow, as determined by
   * pos offset
   */
  public int endPosY()
  {
    return (int) Math.ceil(pos*py()) + end.Y();
  }

  /**
   * returns length of the arrow, which is defined to be the distance
   * between its vertices, not the distance between the actual endpoints
   * of the arrow as drawn
   */
  public double norm()
  {
    return start.distance(end);
  }
      
  /**
   * draws an arrow item. The actual endpoints of the arrow are adjusted
   * first by applying the pos offset, if any, for multiple arrows. Then
   * the endpoints are adjusted to avoid collisions with labels on the
   * vertices
   * @param g a graphics context onto which the item will be rendered
   */
  protected void drawItem(Graphics g)
  {
    // find endpoint...
    final int arrHeadLength=8;
    final int arrHeadWidth=4;
    final int arrowOffset=QPoint.ptSize+2;

    double len=norm()-arrowOffset;
    int startx=(int) Math.ceil(arrowOffset*ux()) + startPosX();
    int starty=(int) Math.ceil(arrowOffset*uy()) + startPosY();
    int endx=(int) Math.floor(len*ux()) + startPosX();
    int endy=(int) Math.floor(len*uy()) + startPosY();
    // adjust for labels
    int offset=start.labelOffset(g, startx, starty);
    if (offset > 0)
    {
      startx=(int) Math.ceil(offset*ux()) + startPosX();
      starty=(int) Math.ceil(offset*uy()) + startPosY();
    }
    offset=end.labelOffset(g, endx, endy);
    if (offset > 0)
    {
      len=norm()-offset;
      endx=(int) Math.floor(len*ux()) + startPosX();
      endy=(int) Math.floor(len*uy()) + startPosY();
    }
    g.drawLine(startx, starty, endx, endy);
    // draw arrow head
    double lengthToArrHead=len-arrHeadLength;
    int arrstartx=(int) Math.floor(lengthToArrHead*ux()) + startPosX();
    int arrstarty=(int) Math.floor(lengthToArrHead*uy()) + startPosY();
    int ax=(int) Math.round(arrHeadWidth*uy())+arrstartx;
    int ay=(int) Math.round(arrHeadWidth*-ux())+arrstarty;
    int bx=(int) Math.round(arrHeadWidth*-uy())+arrstartx;
    int by=(int) Math.round(arrHeadWidth*ux())+arrstarty;
    Polygon p=new Polygon();
    p.addPoint(endx, endy);
    p.addPoint(ax, ay);
    p.addPoint(bx, by);
    g.fillPolygon(p);
  }

  /**
   * calculates the distance of the item from a coordinate (e.g., a mouse click)
   * @param pt a coordinate
   */
  public double distance(Point pt)
  {
    double a2=pt.getX()-startPosX();
    double b2=pt.getY()-startPosY();
    double crossProd=vx()*b2-a2*vy();
    double dotProd=vx()*a2+vy()*b2;
    if (dotProd > 0 && dotProd < norm()*norm())
    {
      return Math.abs(crossProd/norm());
    }
    else
    {
      return Math.min(start.distance(pt), end.distance(pt));
    }
  }

  /**
   * returns true iff item should be deleted in the process of
   * deleting selected items from a quiver. Arrows should be
   * deleted if they are selected, or if either endpoint is selected.
   * This can be overridden by the CAN_DELETE attribute
   */
  public boolean isDeleteItem()
  {
    return CAN_DELETE && (isSelected() || start().isDeleteItem() || end().isDeleteItem());
  }

  /**
   * returns key uniquely determined by the endpoints (regardless of
   * orientation) of arrow, for putting arrows in hash
   */
  public String hashKey()
  {
    boolean stFirst=start.label() < end.label();
    char first=stFirst ? start.label() : end.label();
    char last=stFirst ? end.label() : start.label();
    return String.valueOf(first).concat(String.valueOf(last));
  }
}
