
//Title:       Special Biserial
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
//Company:
//Note:        based on DrawTest applet from jdk,
//             Copyright (c) 1997, 1998 Sun Microsystems, Inc. All Rights Reserved.

import java.awt.event.*;
import java.awt.*;
import java.applet.*;
import java.util.Enumeration;

import java.util.Vector;

public class SpecialBiserialApplet extends Applet{
  DrawQuiverPanel panel;
  DrawControls controls;

  public void init() {
    setLayout(new BorderLayout());
    panel = new DrawQuiverPanel();
    controls = new DrawControls(panel);
    add("Center", panel);
    add("South",controls);
  }

  public void destroy() {
    remove(panel);
    remove(controls);
  }

  public static void main(String args[]) {
    Frame f = new Frame("Special Biserial Applet");
    SpecialBiserialApplet drawTest = new SpecialBiserialApplet();
    drawTest.init();
    drawTest.start();

    f.add("Center", drawTest);
    f.setSize(300, 300);
    f.show();
  }
  public String getAppletInfo() {
    return "Program for drawing quivers of special biserial algebras.";
  }
}

class DrawQuiverPanel extends Panel implements MouseListener, MouseMotionListener {
  Quiver quiver_;
  QPoint currentPoint_;
  boolean dragging_;
  static final int POINT_SIZE=4;
  interface drawMode
  {
    static final int ALL=0;
    static final int SELECT=1;
  }

  public DrawQuiverPanel() {
    quiver_=new Quiver();
    dragging_=false;
    setBackground(Color.white);
    setForeground(Color.black);
    addMouseMotionListener(this);
    addMouseListener(this);
  }

  public void mouseDragged(MouseEvent e) {
    dragging_=true;
    e.consume();
    repaint();
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    e.consume();
    int x1 = e.getX();
    int y1 = e.getY();
    currentPoint_=quiver_.selectPointFromCoord(x1, y1);
    if (currentPoint_==null)
    {
      currentPoint_=quiver_.addPoint(x1, y1);
    }
    repaint();
  }

  public void mouseReleased(MouseEvent e) {
    if (dragging_)
    {
      int x1 = e.getX();
      int y1 = e.getY();
      QPoint pt=quiver_.selectPointFromCoord(x1, y1);
      if (pt!=null)
      {
        quiver_.addArrow(currentPoint_, pt);
      }
    }
    dragging_=false;
    e.consume();
    repaint();
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void paint(Graphics2D g) {
    /*
    int np = lines.size();

    g.setColor(getForeground());
    g.setPaintMode();
    for (int i=0; i < np; i++) {
      Rectangle p = (Rectangle)lines.elementAt(i);
      g.setColor((Color)colors.elementAt(i));
      if (p.width != -1) {
        g.drawLine(p.x, p.y, p.width, p.height);
      } else {
        g.drawLine(p.x, p.y, p.x, p.y);
      }
    }
    if (mode == LINES) {
      g.setXORMode(getBackground());
      if (xl != -1) {
        g.drawLine(x1, y1, xl, yl);
      }
      g.setColor(getForeground());
      g.setPaintMode();
      if (x2 != -1) {
        g.drawLine(x1, y1, x2, y2);
      }
    }
    */
    g.setPaintMode();
    quiver_.draw(g);
  }
}


class DrawControls extends Panel implements ItemListener {
  DrawQuiverPanel target;

  public DrawControls(DrawQuiverPanel target) {
    this.target = target;
    setLayout(new FlowLayout());
    setBackground(Color.lightGray);
    target.setForeground(Color.red);
    CheckboxGroup group = new CheckboxGroup();
    Checkbox b;
    add(b = new Checkbox(null, group, false));
    b.addItemListener(this);
    b.setForeground(Color.red);
    add(b = new Checkbox(null, group, false));
    b.addItemListener(this);
    b.setForeground(Color.green);
    add(b = new Checkbox(null, group, false));
    b.addItemListener(this);
    b.setForeground(Color.blue);
    add(b = new Checkbox(null, group, false));
    b.addItemListener(this);
    b.setForeground(Color.pink);
    add(b = new Checkbox(null, group, false));
    b.addItemListener(this);
    b.setForeground(Color.orange);
    add(b = new Checkbox(null, group, true));
    b.addItemListener(this);
    b.setForeground(Color.black);
    target.setForeground(b.getForeground());
    Choice shapes = new Choice();
    shapes.addItemListener(this);
    shapes.addItem("Lines");
    shapes.addItem("Points");
    shapes.setBackground(Color.lightGray);
    add(shapes);
  }

  public void paint(Graphics2D g) {
    Rectangle r = getBounds();
    g.setColor(Color.lightGray);
    g.draw3DRect(0, 0, r.width, r.height, false);

    int n = getComponentCount();
    for(int i=0; i<n; i++) {
      Component comp = getComponent(i);
      if (comp instanceof Checkbox) {
        Point loc = comp.getLocation();
        Dimension d = comp.getSize();
        g.setColor(comp.getForeground());
        g.drawRect(loc.x-1, loc.y-1, d.width+1, d.height+1);
      }
    }
  }

  public void itemStateChanged(ItemEvent e) {
    if (e.getSource() instanceof Checkbox) {
      target.setForeground(((Component)e.getSource()).getForeground());
    } else if (e.getSource() instanceof Choice) {
      String choice = (String) e.getItem();
    }
  }
}

abstract class QItem
{
  public static final int snapTol_=10;
  protected boolean selected_;
  /**
   * select item
   */
  public void select()
  {
    selected_=true;
  }

  /**
   * unselect item
   */
  public void unselect()
  {
    selected_=false;
  }

  /**
   * draw a point
   */
  public void draw(Graphics2D g)
  {
    if (selected_)
    {
      g.setColor(Color.red);
    }
    else
    {
      g.setColor(Color.black);
    }
    drawItem(g);
  }

  /**
   * draw item
   */
  protected abstract void drawItem(Graphics2D g);

  /**
   * distance from point
   */
  public abstract int distance(int x, int y);
}

class QPoint extends QItem
{
  public int x_;
  public int y_;
  static final int ptSize_=4;
  public String label_;
  public QPoint(int x, int y)
  {
    x_=x;
    y_=y;
    selected_=false;
  }

  /**
   * distance from coordinate, using rectangular metric
   */
  public int distance(int x, int y)
  {
    return Math.abs(x_-x) + Math.abs(y_-y);
  }

  /**
   * draw a point
   */
  protected void drawItem(Graphics2D g)
  {
    g.fillOval(x_-ptSize_, y_-ptSize_, 2*ptSize_, 2*ptSize_);
  }
}

class QArrow extends QItem
{
  public QPoint start_;
  public QPoint end_;
  public QArrow(QPoint start, QPoint end)
  {
    start_=start;
    end_=end;
    selected_=false;
  }

  /**
   * draw an arrow
   */
  protected void drawItem(Graphics2D g)
  {
    g.drawLine(start_.x_, start_.y_, end_.x_, end_.y_);
  }

  /**
   * distance from coordinate...todo
   */
  public int distance(int x, int y)
  {
    // compute norm of crossed product..
    return 100;
  }
}

class Quiver
{
  public Quiver()
  {
    points_=new Vector();
    arrows_=new Vector();
  }

  protected Vector points_;
  protected Vector arrows_;

  /**
   * returns item nearest to coord or null if none
   */
  public QItem itemFromCoord(int x, int y, Vector vec)
  {
    QItem item=null;
    int mindist=-1;
    Enumeration enumer=vec.elements();
    while (enumer.hasMoreElements())
    {
      QItem nextItem=(QItem) enumer.nextElement();
      int dist=nextItem.distance(x,y);
      if (mindist==-1 || dist < mindist)
      {
        item=nextItem;
        mindist=dist;
      }
    }
    return (mindist < QItem.snapTol_) ? item : null;
  }

  /**
   * returns point nearest to coord or null if none
   */
  public QPoint pointFromCoord(int x, int y)
  {
    return (QPoint) itemFromCoord(x, y, points_);
  }

  /**
   * return arrow nearest to coord or null if none
   */
  public QArrow arrowFromCoord(int x, int y)
  {
    return (QArrow) itemFromCoord(x, y, arrows_);
  }

 /**
  * selects item near coordinate
  */
  public QItem selectItemFromCoord(int x, int y, Vector vec)
  {
    QItem item=itemFromCoord(x, y, vec);
    if (item != null)
    {
      unselectAll(vec);
      item.select();
      return item;
    }
    else
    {
      return null;
    }
  }

 /**
  * selects item near coordinate
  */
  public QPoint selectPointFromCoord(int x, int y)
  {
    return (QPoint) selectItemFromCoord(x, y, points_);
  }

  /**
   * select arrow near coordinate
   */
  public QArrow selectArrowFromCoordinate(int x, int y)
  {
    return (QArrow) selectItemFromCoord(x, y, arrows_);
  }

  /**
   * unselect all items
   */
  public void unselectAll(Vector v)
  {
    Enumeration enumer=v.elements();
    while (enumer.hasMoreElements())
    {
      ((QItem) enumer.nextElement()).unselect();
    }
  }

  /**
   * add item
   */
  protected QItem addItem(QItem item, Vector vec)
  {
    vec.addElement(item);
    unselectAll(vec);
    item.select();
    return item;
  }

  /**
   * add point
   */
  public QPoint addPoint(int x, int y)
  {
    return (QPoint) addItem(new QPoint(x, y), points_);
  }

  /**
   * add arrow to quiver
   */
  public QArrow addArrow(QPoint start, QPoint end)
  {
    return (QArrow) addItem(new QArrow(start, end), arrows_);
  }

  /**
   * draw the quiver
   */
  public void draw(Graphics2D g)
  {
    Enumeration enumer=points_.elements();
    while (enumer.hasMoreElements())
    {
      ((QPoint) enumer.nextElement()).draw(g);
    }
    enumer=arrows_.elements();
    while (enumer.hasMoreElements())
    {
      ((QArrow) enumer.nextElement()).draw(g);
    }
  }
}

