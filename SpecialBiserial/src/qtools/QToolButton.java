//Title:       QTool - base class for tools
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

package qtools;
import java.awt.*;

public class QToolButton extends Checkbox
{
  protected QTool tool;
  
  public QToolButton(QTool tool, boolean state, CheckboxGroup group)
  {
    super(tool.buttonLabel(), state, group);
    this.tool=tool;
  }

  public QTool tool()
  {
    return tool;
  }
}
