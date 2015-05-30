//Title:       QZeroRelation
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;

/**
 * zero relations - paths whose composition is zero
 */
public class QZeroRelation extends QItem
{
  /** the path determining the zero relation */
  protected QPath path;
  public static boolean CAN_DELETE=true;

  /**
   * constructor
   * @param path the path determining the zero relation
   */
  public QZeroRelation(QPath path)
  {
    this.path=path;
  }

  /**
   * returns the path determining the relation
   */
  public QPath path()
  {
    return path;
  }

  /**
   * returns the first arrow in the path determining the relation
   */
  public QArrow firstArrow()
  {
    return path.firstArrow();
  }

  /**
   * returns the length (i.e., number of arrows) in the relation
   */
  public int length()
  {
    return path.length();
  }

  /**
   * Returns the colors in which the item should be rendered in selected or non-selected states.
   * @param selected specifies whether or not the item is selected
   */
  public Color itemColor(boolean selected)
  {
    return selected ? Color.red : Color.blue;
  }

  /**
   * draw the item
   * @param g a graphics context onto which the item will be drawn
   */
  protected void drawItem(Graphics2D g)
  {
    path.drawCurve(g, null);
  }
  
  /**
   * distance from point
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
  public double distance(Point pt)
  {
    return path.distanceToCurve(pt);
  }

  /**
   * returns true iff item should be deleted in the process of
   * deleting selected items from a quiver. Arrows should be
   * deleted if they are selected, or if either endpoint is selected.
   * This can be overridden by the CAN_DELETE attribute
   */
  public boolean isDeleteItem()
  {
    if (CAN_DELETE)
      return isSelected() || path.containsDeleteItem();
    else
      return false;
  }
}

