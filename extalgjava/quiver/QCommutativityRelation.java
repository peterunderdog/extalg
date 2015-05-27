//Title:       QCommutativityRelation
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
package quiver;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Enumeration;
import java.util.Vector;
import java.awt.Color;

/**
 * represents commutativity relations in a quiver, i.e.,
 * relations of the form p1=p2, where p1, p2 are paths
 * starting and ending at the same vertex. The class does
 * not limit the number of parallel paths that can be part
 * of the relation
 */
public class QCommutativityRelation extends QItem
{
  /**
   * a list of paths involved in the commutativity relation. The paths
   * must have the same start and end points
   */
  protected Vector paths;
  public static boolean CAN_DELETE=true;

  /**
   * constructor
   * @param paths the paths involved in the commutativity relation
   */
  public QCommutativityRelation(Vector paths)
  {
    this.paths=paths;
  }

  /**
   * returns the vector of paths in the relation
   */
  public Vector paths()
  {
    return paths;
  }

  /**
   * Returns the colors in which the item should be rendered in selected or non-selected states.
   * @param selected specifies whether or not the item is selected
   */
  public Color itemColor(boolean selected)
  {
    return selected ? Color.red : Color.magenta;
  }

  /**
   * draws the commutativity relation item
   * each path in the relation is indicated by drawing a dotted bezier curve
   * determined by the start and end arrows of the path
   * @param g a graphics context onto which the item will be drawn
   */
  protected void drawItem(Graphics g)
  {
    Enumeration e=paths.elements();
    while (e.hasMoreElements())
    {
      ((QPath) e.nextElement()).drawCurve(g, "=");
    }
  }
  
  /**
   * Calculates the distance of the item from a coordinate (e.g., a mouse click).
   * In this case, it is the minimal distance of the point from a curve representing a path
   */
  public double distance(Point pt)
  {
    Enumeration e=paths.elements();
    double mindist=2*QItem.snapTol;
    while (e.hasMoreElements())
    {
      mindist=Math.min(((QPath) e.nextElement()).distanceToCurve(pt), mindist);
    }
    return mindist;
  }

  /**
   * returns true iff the relation contains an item (point or arrow)
   * which is selected for deletion
   */
  protected boolean containsDeleteItem()
  {
    Enumeration e=paths.elements();
    while (e.hasMoreElements())
    {
      if (((QPath) e.nextElement()).containsDeleteItem())
        return true;
    }
    return false;
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
      return isSelected() || containsDeleteItem();
    else
      return false;
  }
}

