package org.csc.phynixx.exceptions;

import java.io.PrintWriter;
import java.io.StringWriter;



/**
 * @author christoph
 *
 */
public class ExceptionUtils {

    protected ExceptionUtils()
    {
    }

    
    /**
    * @param throwable Throwable
    * @return    stack trace
    * @since     JDK 1.2
    */
  public static String getStackTrace(Throwable throwable)
  {
    if ( throwable==null) {
        return "";
    }
    StringWriter swrt = new StringWriter();
    PrintWriter pwrt= new PrintWriter(swrt);
    throwable.printStackTrace(pwrt);
    return swrt.toString();
  }

   

   /**
    * 
    * @return    stack trace
    * @since     JDK 1.2
    */
  public static String getStackTrace()
  {
    StringWriter swrt = new StringWriter();
    PrintWriter pwrt= new PrintWriter(swrt);
    new Exception().printStackTrace(pwrt);
    return swrt.toString();
  }


}
