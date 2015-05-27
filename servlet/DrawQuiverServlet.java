/**
 * Title:        DrawQuiverServlet
 * Description:  
 * Copyright:    Copyright (c) 2000<p>
 * @author pbrown
 * @version $Id: DrawQuiverServlet.java,v 1.3 2000/12/28 22:47:03 pb Exp $
 */
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.awt.Graphics2D;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import quiver.Quiver;

public class DrawQuiverServlet extends HttpServlet
{
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
  {
    final int BORDER=20;
    
    Quiver quiver=new Quiver();
    String qstring;
    qstring=request.getParameter("quiver");
    if (qstring != null)
    {
      quiver.decode(qstring);
      Rectangle rc=quiver.extent();
      quiver.translate(BORDER-rc.x, BORDER-rc.y);
      BufferedImage img=new BufferedImage(rc.width + 2*BORDER, rc.height + 2*BORDER, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2=img.createGraphics();
      g2.setBackground(Color.white);
      g2.fillRect(0, 0, rc.width + 2*BORDER, rc.height + 2*BORDER);
      quiver.draw(g2);
      ServletOutputStream  out=response.getOutputStream();
      JPEGImageEncoder jpegEncoder=JPEGCodec.createJPEGEncoder(out);
      JPEGEncodeParam param=jpegEncoder.getDefaultJPEGEncodeParam(img);
      param.setQuality(0.75f, true);
      response.setContentType("image/jpeg");
      jpegEncoder.encode(img, param);
    }
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
  {
    doGet(request, response);
  }
}
