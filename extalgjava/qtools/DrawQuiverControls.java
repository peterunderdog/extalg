//Title:       DrawControls
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;

import java.awt.event.*;
import java.awt.*;

public class DrawQuiverControls extends Panel implements ActionListener, ItemListener {
  protected DrawQuiverPanel target;
  protected Button deleteButton=new Button("Delete");
  protected Button okButton=new Button("OK");
  protected Button undoButton=new Button("Undo");
  protected Button redoButton=new Button("Redo");

  public DrawQuiverControls(DrawQuiverPanel target) {
    this.target = target;
    setLayout(new FlowLayout());
    CheckboxGroup group=new CheckboxGroup();
    QDrawTool drawTool=new QDrawTool(target);
    QMovePointTool moveTool=new QMovePointTool(target);
    QRelationsTool relTool=new QRelationsTool(target);
    QToolButton toolBtn;
    add(toolBtn=new QToolButton(drawTool, true, group));
    toolBtn.addItemListener(this);
    target.setTool(drawTool);
    add(toolBtn=new QToolButton(moveTool, false, group));
    toolBtn.addItemListener(this);
    add(toolBtn=new QToolButton(relTool, false, group));
    toolBtn.addItemListener(this);
    addButton(undoButton);
    addButton(redoButton);
    addButton(deleteButton);
    addButton(okButton);
  }

  /**
   * add a button
   */
  private void addButton(Button btn)
  {
    add(btn);
    btn.addActionListener(this);
  }

  public void paint(Graphics g) {
    Rectangle r = getBounds();
    g.setColor(Color.lightGray);
    g.draw3DRect(0, 0, r.width, r.height, false);
  }

  public void itemStateChanged(ItemEvent e) {
    if (e.getSource() instanceof QToolButton) {
      target.setTool(((QToolButton)e.getSource()).tool());
    }
  }

  public void actionPerformed(ActionEvent e) 
  {
    Object src=e.getSource();
    if (src==deleteButton)
      target.deleteSelection();
    else if (src==okButton)
      target.ok();
    else if (src==undoButton)
      target.undo();
    else if (src==redoButton)
      target.redo();
  }
}
