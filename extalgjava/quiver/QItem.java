//Title:       QItem
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package quiver;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Font;

/**
 * this is a base class for all items in a quiver, including points,
 * arrows, and relations
 */
public abstract class QItem
{
  /**
   * returns the basic 'snap tolerance' for items, i.e., when selecting
   * items with the mouse by clicking on them, the mouse must be within
   * snap tolerance of the item for the selection to take effect.
   * For some items, a smaller snap tolerance is appropriate (e.g., arrows),
   * but this value is intended to apply to most items
   */
  public static final double snapTol=10.0;

  /**
   * tells whether item is selected
   */
  protected boolean selected=false;

  /**
   * this is a dotted stroke for drawing dotted lines (e.g., the curves which
   * are used to indicate paths in relations
   */
  public static BasicStroke dotted=new BasicStroke(3, BasicStroke.CAP_ROUND,
                                                BasicStroke.JOIN_ROUND, 0,
                                                new float[]{0,6,0,6}, 0);
  /**
   * a basic sans serif font for rendering labels on vertices
   */
  public static Font labelFont=new Font("SansSerif", Font.PLAIN, 18);
  
  /**
   * selects the item
   */
  public void select()
  {
    this.selected=true;
  }

  /**
   * toggles the selection state of the item
   */
  public void toggleSelect()
  {
    selected=!selected;
  }

  /**
   * unselects the item
   */
  public void unselect()
  {
    selected=false;
  }

  /**
   * returns true iff the item is selected
   */
  public boolean isSelected()
  {
    return selected;
  }

  /**
   * Returns the colors in which the item should be rendered in selected or non-selected states.
   * @param selected specifies whether or not the item is selected
   */
  public Color itemColor(boolean sel)
  {
    return sel ? Color.red : Color.black;
  }

  /**
   * Draws the item.
   * This calls the drawItem method in derived classes to render the item
   * @param g a graphics context onto which the item will be rendered
   */
  public void draw(Graphics2D g)
  {
    g.setPaintMode();
    g.setColor(itemColor(selected));
    drawItem(g);
  }

  /**
   * returns the snap tolerance associated with an item.
   * This method is provided so that we can require certain items
   * in derived classes to override with a smaller snap tolerance.
   */
  public double snapTol()
  {
    return QItem.snapTol;
  }

  /**
   * draws the item
   * @param g a graphics context onto which the item will be rendered
   */
  protected abstract void drawItem(Graphics2D g);

  /**
   * Calculates the distance of the item from a coordinate (e.g., a mouse click)
   * @param pt a coordinate
   */
  public abstract double distance(Point pt);

  /**
   * Return true if item should be deleted
   * (either
   *      because it is selected, or some item it depends on
   * is selected)
   */
  public abstract boolean isDeleteItem();
}


