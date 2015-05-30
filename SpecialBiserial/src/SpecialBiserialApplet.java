//Title:       Special Biserial
//Version:     1.0
//Copyright:   Copyright (c) pb
//Author:      pb
//Company:

import java.awt.*;
import java.applet.*;
import qtools.*;
import java.net.URL;

public class SpecialBiserialApplet extends Applet
{
  DrawQuiverPanel panel;
  DrawQuiverControls controls;

  protected class DrawSpecialBiserialQuiverPanel extends DrawQuiverPanel
  {
    public DrawSpecialBiserialQuiverPanel(SpecialBiserialQuiver quiver)
    {
      super(quiver);
    }

    /**
     * override ok for functionality specific to this applet
     */
    public void ok()
    {
      Container c=getParent();
      if (c instanceof Applet)
      {
        StringBuffer doc=new StringBuffer();
        doc.append("http://scallion.cs.umass.edu/extalg/extalgperl/extalg.pl?");
        doc.append("quiver=").append(quiver.encode());
        try
        {
          URL url=new URL(doc.toString());
          AppletContext context=((Applet) c).getAppletContext();
          if (context != null)
            context.showDocument(url);
        }
        catch (Exception e)
        {
          System.out.println(quiver.encode());
          return;
        }
      }
    }
  }

  /**
   * common applet initialization
   */
  protected void commonInit()
  {
    setLayout(new BorderLayout());
    SpecialBiserialQuiver quiver=new SpecialBiserialQuiver();
    panel = new DrawSpecialBiserialQuiverPanel(quiver);
    controls = new DrawQuiverControls(panel);
    add("Center", panel);
    add("South",controls);
  }

  /**
   * init with given quiver..called in standalone mode
   */
  protected void init(String str)
  {
    commonInit();
    panel.quiver().decode(str);
  }

  public void init()
  {
    commonInit();
    String str=getParameter("quiver");
    if (str!=null)
      panel.quiver().decode(str);
  }

  public void destroy()
  {
    remove(panel);
    remove(controls);
  }

  public static void main(String argv[])
  {
    Frame f = new Frame("quiverCAD");
    String str="";
    if (argv.length > 0)
    {
      str=argv[0];
    }
    SpecialBiserialApplet applet=new SpecialBiserialApplet();
    applet.init(str);
    applet.start();

    f.add("Center", applet);
    f.setSize(500, 400);
    f.show();
  }

  public String getAppletInfo()
  {
    return "Program for drawing quivers of special biserial algebras.";
  }
}
