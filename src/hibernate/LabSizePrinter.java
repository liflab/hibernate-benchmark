package hibernate;

import ca.uqac.lif.azrael.ObjectPrinter;
import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReflectionPrintHandler;
import ca.uqac.lif.azrael.size.RotePrintHandler;
import ca.uqac.lif.azrael.size.SizePrinter;
import ca.uqac.lif.azrael.size.SizeReflectionHandler;
import ca.uqac.lif.cep.hibernate.FileHibernate;
import ca.uqac.lif.cep.tmf.QueueSink;

public class LabSizePrinter extends SizePrinter
{
  public LabSizePrinter()
  {
    super();
    m_handlers.add(new RotePrintHandler("JSONParser", 2000));
    //m_handlers.add(new QueueSinkHandler(this));
  }
  
  
  protected static class QueueSinkHandler extends SizeReflectionHandler
  {

    public QueueSinkHandler(SizePrinter p)
    {
      super(p);
    }
    
    public boolean canHandle(Object o)
    {
      return o instanceof FileHibernate;
    }
    
    public Number handle(Object o) throws PrintException
    {
      int s = (Integer) super.handle(o);
      System.out.println(s);
      return s;
    }
    
  }
}
