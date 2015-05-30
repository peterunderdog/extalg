//Title:       QItemToIntegerMap
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package quiver;

import java.util.Hashtable;
import java.lang.Integer;

/** maps items to integer values */
public class QItemToIntegerMap extends Hashtable
{
  /**
   * associate integer value with item
   * @param item the item
   * @param val value associated with item
   */
  public void put(QItem item, int val)
  {
    super.put(item, new Integer(val));
  }

  /**
   * check whether item is a valid key in hash
   * @param item an item
   * @return true iff item has a value associated with it
   */
  public boolean exists(QItem item)
  {
    return super.get(item) != null;
  }

  /**
   * return value associated with item, or default
   * @param item an item
   * @param def a default value
   * @return value associated with item, or def if none exists
   */
  public int get(QItem item, int def)
  {
    Integer val=(Integer) super.get(item);
    return val != null ? val.intValue() : def;
  }

  /**
   * Return value associated with item. 
   * -1 is the
   * default default
   * @param item an item
   * @return the value associated with item, or -1 if none exists
   */
  public int get(QItem item)
  {
    return get(item, -1);
  }
}
