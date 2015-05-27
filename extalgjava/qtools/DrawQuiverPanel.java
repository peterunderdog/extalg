//Title:       DrawQuiverPanel
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;
import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.event.*;
import java.awt.*;
import quiver.*;
import java.lang.StringBuffer;

public class DrawQuiverPanel extends Panel {
  protected Quiver quiver;
  protected QTool currentTool;
  protected Dimension size=new Dimension();
  protected Image imgbuf;
  protected boolean dragging=false;

  public static final int gridsize=20;
  public static final int gridSnapTol=8;
  
  public DrawQuiverPanel(Quiver quiver) {
    this.quiver=quiver;
    setBackground(Color.white);
    setForeground(Color.black);
  }

  public Quiver quiver()
  {
    return quiver;
  }

  /**
   * undo
   */
  public void undo()
  {
    quiver.undo();
    repaint();
  }

  /**
   * redo
   */
  public void redo()
  {
    quiver.redo();
    repaint();
  }

  public void setTool(QTool tool)
  {
    if (currentTool !=null)
    {
      removeMouseMotionListener(currentTool);
      removeMouseListener(currentTool);
      removeKeyListener(currentTool);
    }
    currentTool=tool;
    addMouseMotionListener(currentTool);
    addMouseListener(currentTool);
    addKeyListener(currentTool);
    tool.start();
  }

  /**
   * sets drag mode
   */
  public void setDragging(boolean state)
  {
    dragging=state;
  }

  /**
   * get drag mode
   */
  public boolean dragging()
  {
    return dragging;
  }

  /**
   * adjusts point to lie on grid if
   * close enough
   */
  public Point snapGridPoint(Point pt)
  {
    int roundx=(int) Math.round(pt.getX()/gridsize)*gridsize;
    int roundy=(int) Math.round(pt.getY()/gridsize)*gridsize;
    if (Math.abs(pt.x-roundx) < gridSnapTol)
      pt.setLocation(roundx, pt.y);
    if (Math.abs(pt.y-roundy) < gridSnapTol)
      pt.setLocation(pt.x, roundy);
    return pt;
  }

  public void deleteSelection()
  {
    quiver.deleteSelection();
    repaint();
  }

  public void ok()
  {
    // override me in derived classes..thank you
  }

  /**
   * draw grid within rectangle
   */
  protected void drawGrid(Graphics g2)
  {
    g2.setPaintMode();
    g2.setColor(Color.lightGray);
    getSize(size);
    int x=gridsize;
    int y=gridsize;

    while (x < size.width || y < size.height)
    {
      if (x < size.width)
      {
        g2.drawLine(x, 0, x, size.height);
        x += gridsize;
      }
      if (y < size.height)
      {
        g2.drawLine(0, y, size.width, y);
        y += gridsize;
      }
    }
  }

  /**
   * repaint using double buffer
   */
  public void dbredraw(Graphics g)
  {
    getSize(size);
    if (imgbuf==null || imgbuf.getWidth(this) != size.width || imgbuf.getHeight(this)!=size.height)
      imgbuf = createImage(size.width, size.height);
    Graphics gr=imgbuf.getGraphics();
    gr.setColor(getBackground());
    gr.fillRect(0, 0, size.width, size.height);
    paint(gr);
    g.drawImage(imgbuf, 0, 0, this);
  }

  /**
   * update
   */
  public void update(Graphics g)
  {
    if (dragging)
      currentTool.drawDrag(g, getBackground());
    else
      dbredraw(g);
  }

  public void paint(Graphics g)
  {
    drawGrid(g);
    quiver.draw(g);
  }
}
