package org.csc.phynixx.exceptions;

import java.io.PrintStream;
import java.io.PrintWriter;

public class DelegatedRuntimeException extends RuntimeException implements IDelegated{

	
	public DelegatedRuntimeException() {
		super();
	}

	public DelegatedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public DelegatedRuntimeException(String message) {
		super(message);
	}

	public DelegatedRuntimeException(Throwable cause) {
		super(cause);
	}

    /**
    * liefert die delegierte/ausloesende Exception.
    * Falls die Delegation rekursiv ist (d.h. die deklegierte Exception ist wiederrum eine
    * delegation), so wird die delegation bis zur ersten Exception ausfgeloest, die nicht das IF IDelegated implementiert.
    *
    * @return the root cause exception.
    **/
    public Throwable getRootCause() {
	    Throwable throwable= super.getCause();

	    if (throwable instanceof IDelegated) {
	      return ((IDelegated) throwable).getRootCause();
	    }
	    if( throwable == null )
	    {
	        return new Exception("");
	    }
	    else
	    {
	        return throwable;
	    }
   }

    /**
     * liefert den Decorierenden Inhalt.
     **/
    public String getDecoratingMessage()
    {
        String decoratingMessage=super.getMessage();
        if( decoratingMessage==null ) {
            return "";
        } else {
            return decoratingMessage;
        }
    }


   /** Liefert Message
    * @return String
    */
   public String getMessage() 
   {	  
	   String message= super.getMessage();
	   if( this.getCause()!=null) {
	        return  message+" :: "+ this.getCause().getMessage();	    
	   } else {
	        return super.getMessage() + " DelegatedSystemExeption :: No RootCause defined";
	    }
   }


   /**
    * Die aktuelle Verarbeitung ermittelt die ausloesende Exception und liefert deren StackTrace
    * Eine Iteration ueber die einzelnen Stacktraces muss ggfs selbst mit Hilfe der Methode
    * getCause() implementiert werden.
    * 
    * @param stream PrintStream
    **/
   public void printStackTrace(PrintStream stream) {
     if (getRootCause()!=null) {
         stream.println(this.getMessage());
         getRootCause().printStackTrace(stream);
     } else {
        this.printStackTrace(stream);
     }
   }

    /**
    * Die aktuelle Verarbeitung verweistan die Ausfuehrung der delegierte Excetion
    * Eine Iteration ueber die einzelnen Stacktraces muss ggfs selbst mit Hilfe der Methode
    * getCause() implementiert werden.
    * @param writer PrintWriter
    **/
   public void printStackTrace(PrintWriter writer) {
     if (getRootCause()!=null) {
	     writer.write(getMessage());
         getRootCause().printStackTrace(writer);
     } else {
        this.printStackTrace(writer);
     }
   }



	   

}
