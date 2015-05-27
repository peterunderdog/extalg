//Title:       SpecialBiserialQuiver
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb

import java.util.Vector;
import java.util.Enumeration;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;
import quiver.*;
import qtools.*;

class SpecialBiserialQuiver extends Quiver
{
  /**
   * add arrow to quiver
   */
  public QArrow addArrow(QPoint start, QPoint end)
  {
    // check degrees of vertices
    if (outDegree(start) < 2 && inDegree(end) < 2)
    {
      return super.addArrow(start, end);
    }
    else
    {
      return null;
    }
  }
}
