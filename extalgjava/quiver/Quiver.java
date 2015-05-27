//Title:       Quiver
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package quiver;
import java.util.Vector;
import java.util.Enumeration;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Hashtable;
import java.lang.Integer;
import java.util.StringTokenizer;
import java.lang.ArrayIndexOutOfBoundsException;
import java.awt.Rectangle;

/**
 * represents quiver, and provides operations on and access to items contained
 * in the quiver, including points (vertices), arrows, and relations
 */
public class Quiver
{
  /**
   * inner class which maps items to vectors of other items
   */
  protected class QItemMap extends Hashtable
  {
    /**
     * add a new item to the map (without associating anything to it yet)
     * @param key an item
     */
    public void add(QItem key)
    {
      put(key, new Vector());
    }

    /**
     * add a new key (item), value (item in vector associated with key) pair to the map
     * @param key an item
     * @param value an item to be added to vector associated with the key
     */
    public void add(QItem key, QItem value)
    {
      if (get(key)==null)
        add(key);
      ((Vector) get(key)).addElement(value);
    }

    /**
     * get vector associated with item
     * @param key the item
     */
    public Vector get(QItem key)
    {
      return (Vector) super.get(key);
    }

    /**
     * removes the value from the vector associated with the key
     * @param key the item representing the key
     * @param value the value to be removed from the vector
     */
    public void remove(QItem key, QItem value)
    {
      if (get(key) != null)
        get(key).removeElement(value);
    }
  }
  
  /**
   * Inner class for mapping points to vector of arrows starting/ending at point.
   * In the quiver class, the items in the quiver belong to a flat list of items
   * with no structure. This is convenient from the standpoint of drawing and
   * deleting items. The PointToArrowsMap class provides a data structure for
   * organizing items in the quiver, and is maintained in parallel with the
   * flat list of items. It is used to measure degrees of vertices, identify arrows
   * associated with points, and to maintain label characters on vertices.
   */
  protected class PointToArrowsMap
  {
    /** maps points to vector of arrows ending at the point */       
    public QItemMap in;
    /** maps points to vector of arrows starting at the point */       
    public QItemMap out;
    /** flags indicating whether a label is in use for a vertex */
    protected boolean[] labelUsed;
    /** the valid labels for a vertex */
    protected static final String labels="123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0";

    /**
     * constructs a new map
     */ 
    public PointToArrowsMap()
    {
      in=new QItemMap();
      out=new QItemMap();
      labelUsed=new boolean[128];
      for (int i=0; i<labels.length(); i++)
      {
        labelUsed[labels.charAt(i)]=false;
      }
    }

    /**
     * removes all items from the map
     */
    public void clear()
    {
      in.clear();
      out.clear();
    }

    /**
     * adds a new entry for the point
     * @param pt point to be added to the map
     */
    public void add(QPoint pt)
    {
      in.add(pt);
      out.add(pt);
      labelUsed[pt.label()]=true;
    }

    /**
     * Adds a arrow item to the map.
     * Adds the arrow to vector of in arrows, and out arrows for its endpoints.
     * @param arr the arrow
     */
    public void add(QArrow arr)
    {
      out.add(arr.start(), arr);
      in.add(arr.end(), arr);
    }

    /**
     * Sets the label character on a vertex in the map if label is not in use by another vertex.
     * @param pt the point
     * @param label the label character
     */
    public boolean setLabel(QPoint pt, char label)
    {
      if (labels.indexOf(String.valueOf(label))==-1)
        return false;
      else if (!labelUsed[label])
      {
        labelUsed[pt.label()]=false;
        labelUsed[label]=true;
        pt.setLabel(label);
        return true;
      }
      else
        return false;
    }

    /**
     * returns the points in the madp as an enumeration
     */
    public Enumeration points()
    {
      return in.keys();
    }

    /**
     * calculates the 'in degree' of a point, i.e., the number of arrows ending at the point
     * @param pt the point (vertex)
     */
    public int inDegree(QPoint pt)
    {
      return in.get(pt).size();
    }

    /**
     * calculates the 'out degree' of a point, i.e., the number of arrows starting at the point
     * @param pt the point (vertex)
     */
    public int outDegree(QPoint pt)
    {
      return out.get(pt).size();
    }

    /**
     * removes an item (point or arrow) from the map
     * @param item the item
     */
    public void remove(QItem item)
    {
      if (item instanceof QPoint)
      {
        QPoint pt=(QPoint) item;
        labelUsed[pt.label()]=false;
        in.remove(pt);
        out.remove(pt);
      }
      else if (item instanceof QArrow)
      {
        QArrow arr=(QArrow) item;
        out.remove(arr.start(), arr);
        in.remove(arr.end(), arr);
      }
    }

    /**
     * returns true iff the character is being used as a label for a vertex
     * @param label the label character
     */
    public boolean labelUsed(char label)
    {
      return labelUsed[label];
    }

    /**
     * returns an available label (if any)
     */
    public char getLabel()
    {
      for (int i=0; i<labels.length(); i++)
      {
        if (!labelUsed[labels.charAt(i)])
          return labels.charAt(i);
      }
      return 0;
    }
  }
  

  /**
   * This is another inner class which is used to organize relations based
   * on the starting arrows of the relations. Not really used at the moment, but
   * it is intended to facilitate checking consistency of relations, etc.
   */
  protected class ArrowToRelationsMap extends QItemMap
  {
    /**
     * remove entry/entries in hashtable relevant to item, if any
     * @param item an item
     */
    public void remove(QItem item)
    {
      if (item instanceof QArrow)
      {
        super.remove(item);
      }
      else if (item instanceof QZeroRelation)
      {
        super.remove(((QZeroRelation) item).firstArrow(), item);
      }
      else if (item instanceof QCommutativityRelation)
      {
        // first arrow of each path in the comm relation
        // points to the relation, so delete 'em all
        Enumeration e=((QCommutativityRelation) item).paths().elements();
        while (e.hasMoreElements())
        {
          QPath p=(QPath) e.nextElement();
          super.remove(p.firstArrow(), item);
        }
      }
    }
  
    /**
     * add zero relation
     * @param z a zero relation
     */
    public void add(QZeroRelation z)
    {
      super.add(z.firstArrow(), z);
    }
  
    /**
     * add commutativity relation to the map
     * @param r a commutativity relation
     */
    public void add(QCommutativityRelation r)
    {
      Enumeration e=r.paths().elements();
      while (e.hasMoreElements())
      {
        QPath p=(QPath) e.nextElement();
        super.add(p.firstArrow(), r);
      }
    }
  }
  
  /**
   * This final inner class keeps track of arrows based on their endpoints,
   * regardless of orientation. This is mainly to keep track of multiple arrows
   * between the same endpoints so that they may be rendered correctly. The
   * arrows are kept in a hash table of vectors where the keys in the hash are
   * uniquely determined by the endpoints of the arrows (regardless of orientation).
   * Therefore, given an arrow, one can retrieve a vector of all arrows sharing
   * the same endpoints as the given arrow.
   */
  protected class ArrowEndpointsMap extends Hashtable
  {
    /**
     * Add arrow to map. This adds the arrow to the vector associated with
     * the endpoints of the arrow.
     * @param arr an arrow
     */
    public void add(QArrow arr)
    {
      String key=arr.hashKey();
      Vector v=(Vector) get(key);
      if (v==null)
      {
        v=new Vector();
        super.put(key, v);
      }
      v.addElement(arr);
    }

    /**
     * removes an item from the map if the item happens to be an arrow
     * @param item an item 
     */
    public void remove(QItem item)
    {
      if (item instanceof QArrow)
      {
        QArrow arr=(QArrow) item;
        Vector v=(Vector) get(arr.hashKey());
        if (v != null)
        {
          v.removeElement(arr);
        }
      }
    }
  
    /**
     * Adjust the positions of the arrows in the vector.
     * The arrows are
     * assumed to have the same endpoints
     * @param v a vector containing arrows with common endpoints
     */
    public void repositionArrows(Vector v)
    {
      int size=v.size();
      if (size==0)
        return;
      Enumeration d=v.elements();
      int span=QArrow.arrowSpacing*(size-1);
      int pos=-span/2;
      while (d.hasMoreElements())
      {
        QArrow arr=(QArrow) d.nextElement();
        arr.setPos(pos);
        pos += QArrow.arrowSpacing;
      }
    }
  
    /**
     * reposition arrows with same endpoints as given arrow
     * @param arr the arrow
     */
    public void repositionArrows(QArrow arr)
    {
      Vector v=(Vector) get(arr.hashKey());
      repositionArrows(v);
    }
  
    /**
     * reposition all arrows
     */
    public void repositionArrows()
    {
      Enumeration e=keys();
      while (e.hasMoreElements())
      {
        Vector v=(Vector) super.get(e.nextElement());
        repositionArrows(v);
      }
    }
  }

  /** the items (points, arrows, relations) in the quiver */
  protected Vector items;
  /** maps points in the quiver to their associated arrows */
  protected PointToArrowsMap ptarrowmap;
  /** maps arrows to relations */
  protected ArrowToRelationsMap arrowrelmap;
  /** maps pairs of endpoints to arrows between them */
  protected ArrowEndpointsMap arrendptmap;
  /** undo/redo stack */
  protected QUndoRedoStack undoRedoStack;

  /**
   * constructor
   */
  public Quiver()
  {
    items=new Vector();
    ptarrowmap=new PointToArrowsMap();
    arrowrelmap=new ArrowToRelationsMap();
    arrendptmap=new ArrowEndpointsMap();
    undoRedoStack=new QUndoRedoStack();
  }

  /**
   * returns item nearest to coord or null if none
   * @param pt a coordinate
   * @param e an enumeration of items
   * @return item in enumeration nearest (within snap tolerance) to coordinate, or null if none
   */
  public QItem itemFromCoord(Point pt, Enumeration e)
  {
    QItem item=null;
    double mindist=2*QItem.snapTol;
    while (e.hasMoreElements())
    {
      QItem nextItem=(QItem) e.nextElement();
      double dist=nextItem.distance(pt);
      if (dist < Math.min(mindist, nextItem.snapTol()))
      {
        item=nextItem;
        mindist=dist;
      }
    }
    return (mindist < QItem.snapTol) ? item : null;
  }

  /**
   * return item in quiver nearest to coord
   * @param pt a coordinate
   * @return item in quiver nearest (within snap tolerance) to coordinate, or null if none
   */
  public QItem itemFromCoord(Point pt)
  {
    // try to pick point first
    QItem item=pointFromCoord(pt);
    if (item==null)
    {
      item=itemFromCoord(pt, items.elements());
    }
    return item;
  }
  
  /**
   * returns point nearest to coord or null if none
   * @param a coordinate
   * @return the point (within snap tolerance) closest to the coordinate, or null if none
   */
  public QPoint pointFromCoord(Point pt)
  {
    return (QPoint) itemFromCoord(pt, ptarrowmap.points());
  }

 /**
  * selects item near coordinate (unselects all other items), and returns it
  * @param pt a coordinate
  * @return the item (within snap tolerance) closest to the coordinate, or null if none
  */
  public QItem selectItemFromCoord(Point pt)
  {
    QItem item=itemFromCoord(pt);
    if (item != null)
    {
      unselectAll();
      item.select();
      return item;
    }
    else
    {
      return null;
    }
  }

  /**
   * multiselect item (i.e., other selected items are not unselected) near coordinate and return it
   * @param pt a coordinate
   * @return the selected item, or null if none
   */
  public QItem multiSelectItemFromCoord(Point pt)
  {
    QItem item=itemFromCoord(pt);
    if (item != null)
    {
      item.toggleSelect();
      return item;
    }
    else
    {
      return null;
    }
  }

  /**
   * selects point (all other items are unselected) near coordinate, and return it
   * @param pt a coordinate
   * @return point near coordinate, or null if not found
   */
  public QPoint selectPointFromCoord(Point pt)
  {
    QPoint item=pointFromCoord(pt);
    if (item != null)
    {
      unselectAll();
      item.select();
    }
    return item;
  }

  /**
   * multiselects item (i.e., other selected items are not unselected) near coordinate and return it
   * @param pt a coordinate
   * @return the point nearest to the coordinate, or null if not found
   */
  public QPoint multiSelectPointFromCoord(Point pt)
  {
    QPoint item=pointFromCoord(pt);
    if (item != null)
    {
      item.toggleSelect();
    }
    return item;
  }

  /**
   * unselect all items in the quiver
   */
  public void unselectAll()
  {
    Enumeration enum=items.elements();
    while (enum.hasMoreElements())
    {
      ((QItem) enum.nextElement()).unselect();
    }
  }

  /**
   * Delete the selected items, along with items that depend on them.
   * The dependent items are determined in derived classes of QItem by
   * the isDeleteItem method
   */
  public synchronized void deleteSelection()
  {
    undoRedoStack.beginLevel();
    for (int i=items.size()-1; i>=0; i--)
    {
      QItem item=(QItem) items.get(i);
      if (item.isDeleteItem())
      {
        removeItem(item);
        undoRedoStack.saveDeletedItem(item);
      }
    }
    arrendptmap.repositionArrows();
  }

  /**
   * the number of arrows ending at point
   * @param pt a point
   * @return the number of arrows ending at the point
   */
  public int inDegree(QPoint pt)
  {
    return ptarrowmap.inDegree(pt);
  }

  /**
   * number of arrows starting at point
   * @param pt a point
   * @return the number of arrows starting at the point
   */
  public int outDegree(QPoint pt)
  {
    return ptarrowmap.outDegree(pt);
  }

  /**
   * removes item from quiver
   */
  public void removeItem(QItem item)
  {
    unmapItem(item);
    items.remove(item);
  }

  /**
   * removes item from maps used by quiver to keep track of items
   * @param the item to be unmapped
   */
  private void unmapItem(QItem item)
  {
    ptarrowmap.remove(item);
    arrowrelmap.remove(item);
    arrendptmap.remove(item);
  }
  
  /**
   * adds item to maps used by quiver to keep track of items
   */
  private void mapItem(QItem item)
  {
    if (item instanceof QPoint)
    {
      ptarrowmap.add((QPoint) item);
    }
    else if (item instanceof QArrow)
    {
      QArrow arr=(QArrow) item;
      ptarrowmap.add(arr);
      arrowrelmap.add(arr);
      arrendptmap.add(arr);
      arrendptmap.repositionArrows(arr);
    }
    else if (item instanceof QZeroRelation)
    {
      QZeroRelation rel=(QZeroRelation) item;
      arrowrelmap.add(rel.firstArrow(), rel);
    }
    else if (item instanceof QCommutativityRelation)
    {
      QCommutativityRelation rel=(QCommutativityRelation) item;
      arrowrelmap.add(rel);
    }
  }
  
  /**
   * adds the item to the quiver, selects it, and returns it
   * @param item to be added
   * @return the item
   */
  public QItem addItem(QItem item)
  {
    items.addElement(item);
    unselectAll();
    item.select();
    mapItem(item);
    return item;
  }

  /**
   * creates a point at the given coordinate and adds it to the quiver
   * @param p a coordinate
   * @param label a label for the point
   * @return the newly added point
   */
  protected QPoint addPoint(Point p, char label)
  {
    unselectAll();
    if (label != 0)
    {
      QPoint pt=(QPoint) addItem(new QPoint(p, label));
      undoRedoStack.beginLevel();
      undoRedoStack.saveAddedItem(pt);
      return pt;
    }
    else
    {
      return null;
    }
  }

  /**
   * creates a point at given coordinate, adds it to quiver, and returns it
   * @param p a coordinate
   * @return the new point
   */
  public QPoint addPoint(Point p)
  {
    return addPoint(p, ptarrowmap.getLabel());
  }

  /**
   * move a point...saves move state in redo/undo stack
   * @param point the point to move
   * @param p coordinates where point is to be moved
   */
  public void movePoint(QPoint point, Point p)
  {
    undoRedoStack.beginLevel();
    undoRedoStack.saveMovedPoint(point, p);
    point.setPoint(p);
  }

  /**
   * set label character on a point
   * @param pt the point
   * @param label the label
   * @return true iff was successfully set (i.e., wasn't already in use)
   */
  public boolean setLabel(QPoint pt, char label)
  {
    return ptarrowmap.setLabel(pt, label);
  }

  /**
   * add an arrow with given endpoints to the quiver, select it, and return it
   * @param start the start point
   * @param end the end point
   * @return the new arrow
   */
  public QArrow addArrow(QPoint start, QPoint end)
  {
    unselectAll();
    undoRedoStack.beginLevel();
    QArrow arr=(QArrow) addItem(new QArrow(start, end));
    undoRedoStack.saveAddedItem(arr);
    return arr;
  }

  /**
   * add a zero relation determined by the path, select it, and return it
   * @param path the path determining the zero relation
   * @return the new zero relation
   */
  public QZeroRelation addZeroRelation(QPath path)
  {
    undoRedoStack.beginLevel();
    QZeroRelation rel=(QZeroRelation) addItem(new QZeroRelation(path));
    undoRedoStack.saveAddedItem(rel);
    return rel;
  }

  /**
   * Get the selected zero relations, and return them in a vector.
   * This is used for constructing commutativity relations.
   * @return vector of selected zero relations
   */
  public Vector getSelectedZeroRelations()
  {
    Vector v=new Vector();
    Enumeration e=items.elements();
    while (e.hasMoreElements())
    {
      QItem item=(QItem) e.nextElement();
      if (item instanceof QZeroRelation && item.isSelected())
      {
        v.addElement(item);
      }
    }
    return v;
  }

  /**
   * Get the paths in selected zero relations, and return them in a vector
   * @return vector of paths in selected zero relations
   */
  public Vector getSelectedPaths()
  {
    Vector paths=new Vector();
    Vector v=getSelectedZeroRelations();
    Enumeration e=v.elements();
    while (e.hasMoreElements())
    {
      paths.addElement(((QZeroRelation) e.nextElement()).path());
    }
    return paths;
  }

  /**
   * checks that paths in vector have same start/end pts
   * @return true iff start/end points match
   */
  public boolean haveSameStartEndPts(Vector paths)
  {
    QPoint start=null;
    QPoint end=null;
    Enumeration e=paths.elements();
    while (e.hasMoreElements())
    {
      QPath p=(QPath) e.nextElement();
      if (start==null && end==null)
      {
        start=p.start();
        end=p.end();
      }
      else
      {
        if (p.start()!=start || p.end()!=end)
          return false;
      }
    }
    return true;
  }
  
  /**
   * Adds commutativity relation consisting of selected zero relations.
   * The selected zero relations are then removed. The selected zero relations
   * must have the same start/end points
   * @return the new commutativity relation (if it was successfully constructed), or null
   */
  public QCommutativityRelation addCommutativityRelation()
  {
    Vector v=getSelectedPaths();
    if (v.size() > 1)
    {
      // get selected zero relations before adding teh
      // commutativity relation because selection gets
      // whacked when addItem is called (bad!)
      Vector vec=getSelectedZeroRelations();
      // new undo/redo level is created within addCommutativityRelation, so
      // we don't need to create one here (deleted zero relations will
      // be added to the current undo/redo level
      QCommutativityRelation rel=addCommutativityRelation(v);
      if (rel != null)
      {
        Enumeration e=vec.elements();
        while (e.hasMoreElements())
        {
          QZeroRelation z=(QZeroRelation) e.nextElement();
          removeItem(z);
          undoRedoStack.saveDeletedItem(z);
        }
        return rel;
      }
    }
    return null;
  }

  /**
   * Adds a commutativity relation to the quiver.
   * This is used when non-interactively reconstructing a quiver from a string parameter.
   * @param the paths determining the relation
   * @return the commutativity relation
   */
  public QCommutativityRelation addCommutativityRelation(Vector paths)
  {
    if (paths.size() > 1)
    {
      if (haveSameStartEndPts(paths))
      {
        undoRedoStack.beginLevel();
        QCommutativityRelation rel=new QCommutativityRelation(paths);
        addItem(rel);
        undoRedoStack.saveAddedItem(rel);
        return rel;
      }
    }
    return null;
  }

  /**
   * get the points incident (i.e., at opposite end of some arrow) to another point
   * @param pt the point
   * @return a vector of points incident to given point
   */
  public Vector getIncidentPoints(QPoint pt)
  {
    Vector v=new Vector();
    Enumeration e=ptarrowmap.out.get(pt).elements();
    Point p;
    while (e.hasMoreElements())
    {
      p=((QArrow) e.nextElement()).end().point();
      if (!v.contains(p))
        v.addElement(p);
    }
    e=ptarrowmap.in.get(pt).elements();
    while (e.hasMoreElements())
    {
      p=((QArrow) e.nextElement()).start().point();
      if (!v.contains(p))
        v.addElement(p);
    }
    return v;
  }

  /**
   * draws the items in the quiver by calling the draw method for each item
   * @param g a graphics context onto which the quiver is drawn
   */
  public void draw(Graphics g)
  {
    Enumeration enum=items.elements();
    while (enum.hasMoreElements())
    {
      ((QItem) enum.nextElement()).draw(g);
    }
  }

  /**
   * Encode path as a string.
   * This is used for representing relations as strings which can then be
   * used to non-interactively reconstruct a quiver from a string parameter
   * @param p the path
   * @param iindex map which associates each item in the quiver to its index in the items vector
   */
  protected String pathToString(QPath p, QItemToIntegerMap iindex)
  {
    StringBuffer buf=new StringBuffer();
    for (int i=0; i<p.length(); i++)
    {
      if (i>0)
        buf.append(",");
      buf.append(iindex.get(p.arrowAt(i)));
    }
    return buf.toString();
  }

  /**
   * Encode quiver as string.
   * This is used for representing the quiver as a string which can then be
   * used to non-interactively reconstruct the quiver from a string parameter
   */
  public String encode()
  {
    StringBuffer buf=new StringBuffer();
    QItemToIntegerMap iindex=new QItemToIntegerMap();
    for (int index=0; index < items.size(); index++)
    {
      QItem item=(QItem) items.elementAt(index);

      iindex.put(item, index);
      if (index > 0)
      {
        buf.append(":");
      }
      if (item instanceof QPoint)
      {
        QPoint pt=(QPoint) item;
        buf.append("P");
        buf.append(pt.label());
        buf.append(pt.X());
        buf.append(",");
        buf.append(pt.Y());
      }
      else if (item instanceof QArrow)
      {
        QArrow arr=(QArrow) item;
        buf.append("A");
        buf.append(iindex.get(arr.start()));
        buf.append(",");
        buf.append(iindex.get(arr.end()));
      }
      else if (item instanceof QZeroRelation)
      {
        QZeroRelation z=(QZeroRelation) item;
        buf.append("Z");
        buf.append(pathToString(z.path(), iindex));
       }
      else if (item instanceof QCommutativityRelation)
      {
        QCommutativityRelation r=(QCommutativityRelation) item;
        buf.append("C");
        Enumeration e=r.paths().elements();
        boolean firstPath=true;
        while (e.hasMoreElements())
        {
          if (!firstPath)
            buf.append("!");
          firstPath=false;
          QPath p=(QPath) e.nextElement();
          buf.append(pathToString(p, iindex));
        }
      }
    }
    return buf.toString();
  }

  /**
   * Convert comma separated list of integers to array.
   * This is used to decode a string representation of a path in a relation
   * when non-interactively reconstructing a quiver from a string parameter.
   * @param str comma separated list of integers
   * @return array of integers in the string
   */
  protected int[] arrayFromList(String str)
  {
    StringTokenizer st=new StringTokenizer(str, ",");
    int[] iarray=new int[st.countTokens()];
    int index=0;
    while (st.hasMoreTokens())
    {
      String tok=st.nextToken();
      iarray[index++]=Integer.valueOf(tok).intValue();
    }
    return iarray;
  }

  /**
   * Get path from array of indices pointing to arrows in items vector.
   * This is used to decode a string representation of a path in a relation
   * when non-interactively reconstructing a quiver from a string parameter.
   * @param a array of integers representing indices of arrows in items vector
   * @return path consisting of the arrows in the array
   */
  protected QPath pathFromArray(int[] a)
  {
    QPath path=new QPath();
    for (int index=0; index < a.length; index++)
    {
      path.add((QArrow) items.elementAt(a[index]));
    }
    return path;
  }

  /**
   * get vector of paths from string consisting of lists of indices of arrows, separated by semicolons
   * This is used to decode a string representation of paths in a commutativity relation
   * when non-interactively reconstructing a quiver from a string parameter.
   * @param str string representation of the list of paths
   * @return vector of paths represented by string
   */
  protected Vector decodePaths(String str)
  {
    StringTokenizer st=new StringTokenizer(str, "!");
    Vector v=new Vector();
    while (st.hasMoreTokens())
    {
      int[] a=arrayFromList(st.nextToken());
      v.addElement(pathFromArray(a));
    }
    return v;
  }

  /**
   * Decode string representation of quiver.
   * This is used to non-interactively reconstruct a quiver from a string parameter.
   * @param str string representation of quiver, as produced by the encode() method
   */
  public void decode(String str)
  {
    if (str.length()==0)
      return;
    
    items.clear();
    ptarrowmap.clear();
    StringTokenizer st=new StringTokenizer(str, ":");
    while (st.hasMoreTokens())
    {
      String tok=st.nextToken();
      char type=tok.charAt(0);
      tok=tok.substring(1);
      int[] a;
      switch (type)
      {
        case 'P':
          char label=tok.charAt(0);
          tok=tok.substring(1);
          a=arrayFromList(tok);
          addPoint(new Point(a[0], a[1]), label);
          break;
        case 'A':
          a=arrayFromList(tok);
          addArrow((QPoint) items.elementAt(a[0]), (QPoint) items.elementAt(a[1]));
          break;
        case 'Z':
          a=arrayFromList(tok);
          QPath path=pathFromArray(a);
          addZeroRelation(path);
          break;
        case 'C':
          Vector v=decodePaths(tok);
          addCommutativityRelation(v);
          break;
      }
    }
    unselectAll();
  }

  /**
   * get smallest rectangle containing the quiver.
   * It suffices
   * to find smallest rectangle containing the points.
   * @return rectangle containing the quiver
   */
  public Rectangle extent()
  {
    int minx=Integer.MAX_VALUE;
    int miny=Integer.MAX_VALUE;
    int maxx=Integer.MIN_VALUE;
    int maxy=Integer.MIN_VALUE;
    
    Enumeration enum=items.elements();
    while (enum.hasMoreElements())
    {
      QItem item=(QItem) enum.nextElement();
      if (item instanceof QPoint)
      {
        int x=((QPoint) item).X();
        int y=((QPoint) item).Y();
        minx=Math.min(minx, x);
        miny=Math.min(miny, y);
        maxx=Math.max(maxx, x);
        maxy=Math.max(maxy, y);
      }
    }
    return new Rectangle(minx, miny, maxx-minx, maxy-miny);
  }

  /**
   * translate the quiver by dx, dy.
   * For this, it
   * suffices to translate the points. Everything else
   * will follow
   * @param dx x distance for translation
   * @param dy y distance for translation
   */
  public void translate(int dx, int dy)
  {
    Enumeration enum=items.elements();
    while (enum.hasMoreElements())
    {
      QItem item=(QItem) enum.nextElement();
      if (item instanceof QPoint)
      {
        ((QPoint) item).translate(dx, dy);
      }
    }
  }

  /**
   * performs undo/redo operation if possible
   */
  private void undoRedo(boolean undo)
  {
    Vector v=undo ? undoRedoStack.getUndoItems() : undoRedoStack.getRedoItems();
    if (v != null)
    {
      // undo them in the order they were 'done'
      for (int i=v.size()-1; i >= 0; i--)
      {
        QUndoRedoItem undoRedoItem=(QUndoRedoItem) v.elementAt(i);
        if (undo)
        {
          undoRedoItem.undoAction(this);
        }
        else
        {
          undoRedoItem.redoAction(this);
        }
      }
      arrendptmap.repositionArrows();
    }
  }

  /**
   * performs undo operation if possible
   */
  public void undo()
  {
    Vector v=undoRedoStack.getUndoItems();
    if (v != null)
    {
      // undo them in the order they were 'done'
      for (int i=v.size()-1; i >= 0; i--)
      {
        QUndoRedoItem undoRedoItem=(QUndoRedoItem) v.elementAt(i);
        undoRedoItem.undoAction(this);
      }
      arrendptmap.repositionArrows();
    }
  }

  /**
   * performs undo operation if possible
   */
  public void redo()
  {
    Vector v=undoRedoStack.getRedoItems();
    if (v != null)
    {
      for (int i=0; i < v.size(); i++)
      {
        QUndoRedoItem undoRedoItem=(QUndoRedoItem) v.elementAt(i);
        undoRedoItem.redoAction(this);
      }
      arrendptmap.repositionArrows();
    }
  }
}
