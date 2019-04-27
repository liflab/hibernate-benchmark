package hibernate;

import ca.uqac.lif.azrael.json.JsonStringPrinter;
import ca.uqac.lif.azrael.json.JsonStringReader;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.hibernate.FileHibernate;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.cep.tmf.Slice;
import ca.uqac.lif.cep.util.NthElement;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.ExperimentFactory;
import ca.uqac.lif.labpal.Region;
import hibernate.source.BoundedSource;
import hibernate.source.RandomLabelSource;

public class StreamExperimentFactory extends ExperimentFactory<MainLab,StreamExperiment>
{

  public StreamExperimentFactory(MainLab lab)
  {
    super(lab, StreamExperiment.class);
  }

  @Override
  protected StreamExperiment createExperiment(Region r)
  {
    StreamExperiment se = new StreamExperiment();
    String prop_name = ((JsonString) r.get(StreamExperiment.QUERY_NAME)).stringValue();
    se.setInput(StreamExperiment.QUERY_NAME, prop_name);
    Processor proc = getProcessor(prop_name, r);
    BoundedSource src = getSource(prop_name, r, se);
    if (proc == null || src == null)
    {
      return null;
    }
    String method = ((JsonString) r.get(StreamExperiment.METHOD)).stringValue();
    if (method.compareTo(StreamExperiment.METHOD_ONLINE) == 0)
    {
      se.setInput(StreamExperiment.METHOD, StreamExperiment.METHOD_ONLINE);
      se.setSource(src);
      se.setProcessor(proc);
      return se;
    }
    if (method.compareTo(StreamExperiment.METHOD_HIBERNATION) == 0)
    {
      se.setSource(src);
      se.setInput(StreamExperiment.METHOD, StreamExperiment.METHOD_HIBERNATION);
      FileHibernate fh = new FileHibernate(proc, "/tmp/exp", new JsonStringPrinter(), new JsonStringReader());
      se.setProcessor(fh);
      return se;
    }
    return null;
  }
  
  protected Processor getProcessor(String property_name, Region r)
  {
    if (property_name.compareTo("Slice average") == 0)
    {
      return getSliceAverage();
    }
    return null;
  }

  protected BoundedSource getSource(String property_name, Region r, StreamExperiment se)
  {
    if (property_name.compareTo("Slice average") == 0)
    {
      int slice_len = r.getInt(RandomLabelSource.SLICE_LENGTH);
      int num_slices = r.getInt(RandomLabelSource.NUM_SLICES);
      se.describe(RandomLabelSource.SLICE_LENGTH, "The length of each slice");
      se.describe(RandomLabelSource.NUM_SLICES, "The number of concurrent slices");
      se.setInput(RandomLabelSource.SLICE_LENGTH, slice_len);
      se.setInput(RandomLabelSource.NUM_SLICES, num_slices);
      return new RandomLabelSource(m_lab.getRandom(), MainLab.s_maxTraceLength, slice_len, num_slices);
    }
    return null;
  }
  
  protected Processor getSliceAverage()
  {
    Slice slice = new Slice(new NthElement(0), new Passthrough());
    return slice;
  }
}
