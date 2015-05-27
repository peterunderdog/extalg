//Title:       QTool - base class for tools
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;
import java.awt.event.*;
import java.awt.Rectangle;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.util.Vector;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyListener;

public abstract class QTool implements MouseListener, MouseMotionListener, KeyListener
{
  protected DrawQuiverPanel panel;
  protected Point currDragPoint;
  protected Point lastDragPoint;

  public QTool(DrawQuiverPanel panel)
  {
    this.panel=panel;
  }
  public abstract String buttonLabel();

  /**
   * default tool startup...override for different
   * behavior
   */
  public synchronized void start()
  {
    panel.quiver().unselectAll();
    panel.repaint();
  }

  public void drawDrag(Graphics g, Color background)
  {
  }
  
  public void mouseDragged(MouseEvent e)
  {
  }

  public void mouseMoved(MouseEvent e)
  {
  }

  public void mousePressed(MouseEvent e)
  {
  }

  public void mouseReleased(MouseEvent e)
  {
  }

  public void mouseEntered(MouseEvent e)
  {
  }

  public void mouseExited(MouseEvent e)
  {
  }

  public void mouseClicked(MouseEvent e)
  {
  }

  public void keyPressed(KeyEvent e)
  {
  }

  public void keyReleased(KeyEvent e)
  {
  }

  public void keyTyped(KeyEvent e)
  {
  }
}
