/*
    A benchmark for Pat The Miner
    Copyright (C) 2018-2019 Laboratoire d'informatique formelle

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hibernate;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.cep.Connector;
import ca.uqac.lif.cep.Processor;
import ca.uqac.lif.cep.Pullable;
import ca.uqac.lif.cep.Pushable;
import ca.uqac.lif.cep.tmf.BlackHole;
import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;
import hibernate.source.BoundedSource;

/**
 * Experiment that connects a source to a processor and measures its
 * throughput
 */
public class StreamExperiment extends Experiment
{
  /**
   * The average number of events processed per second
   */
  public static final transient String THROUGHPUT = "Throughput";

  /**
   * Cumulative running time (in ms)
   */
  public static final transient String TIME = "Running time";

  /**
   * Number of events processed
   */
  public static final transient String LENGTH = "Stream length";
  
  /**
   * Memory consumed
   */
  public static final transient String MEMORY = "Memory";

  /**
   * Whether the experiment uses multiple threads or a single one
   */
  public static final transient String MULTITHREAD = "Multi-threaded";
  
  /**
   * The name of the query being executed
   */
  public static final transient String QUERY_NAME = "Query";
  
  /**
   * The method used to evaluate the query
   */
  public static final transient String METHOD = "Method";
  
  /**
   * The name of the "online" method
   */
  public static final transient String METHOD_ONLINE = "Online";
  
  /**
   * The name of the file hibernation method
   */
  public static final transient String METHOD_HIBERNATION = "File hibernation";

  /**
   * The processor that is being monitored in this experiment
   */
  protected transient Processor m_processor;

  /**
   * The source from which the input events will originate
   */
  protected transient BoundedSource m_source;

  /**
   * The interval at which the experiment updates its data on
   * runtime and throughput
   */
  protected transient int m_eventStep = 100;
  
  /**
   * A helper object used to compute the memory footprint of a processor
   */
  protected transient LabSizePrinter m_sizePrinter;

  /**
   * Creates a new empty stream experiment
   */
  public StreamExperiment()
  {
    describe(THROUGHPUT, "The average number of events processed per second");
    describe(TIME, "Cumulative running time (in ms)");
    describe(LENGTH, "Number of events processed");
    describe(METHOD, "The method used to evaluate the query");
    describe(MEMORY, "Memory consumption (in bytes)");
    describe(QUERY_NAME, "The name of the property being computed on the stream");
    JsonList x = new JsonList();
    x.add(0);
    write(LENGTH, x);
    JsonList y = new JsonList();
    y.add(0);
    write(TIME, y);
    JsonList z = new JsonList();
    z.add(0);
    write(MEMORY, z);
    m_sizePrinter = new LabSizePrinter();
    setInput(METHOD, METHOD_ONLINE);
  }

  @Override
  public void execute() throws ExperimentException, InterruptedException 
  {
    JsonList length = (JsonList) read(LENGTH);
    JsonList time = (JsonList) read(TIME);
    JsonList memory = (JsonList) read(MEMORY);
    // Setup processor chain
    Pullable s_p = m_source.getPullableOutput();
    Pushable t_p = m_processor.getPushableInput();
    BlackHole hole = new BlackHole();
    Connector.connect(m_processor, hole);
    long start = System.currentTimeMillis();
    int event_count = 0;
    int source_length = m_source.getEventBound();
    while (s_p.hasNext())
    {
      if (event_count % m_eventStep == 0 && event_count > 0)
      {
        long lap = System.currentTimeMillis();
        length.add(event_count);
        time.add(lap - start);
        try
        {
          m_sizePrinter.reset();
          int size = (Integer) m_sizePrinter.print(m_processor);
          memory.add(size);
        }
        catch (PrintException e)
        {
          throw new ExperimentException(e);
        }
        float prog = ((float) event_count) / ((float) source_length);
        setProgression(prog);
      }
      Object o = s_p.pull();
      t_p.push(o);
      event_count++;
    }
    long end = System.currentTimeMillis();
    write(THROUGHPUT, (1000f * (float) m_source.getEventBound()) / ((float) (end - start)));
  }

  /**
   * Sets the processor that is being monitored in this experiment
   * @param p The processor
   */
  public void setProcessor(Processor p)
  {
    m_processor = p;
  }

  /**
   * Sets the source from which the input events will originate
   * @param s The source
   */
  public void setSource(BoundedSource s)
  {
    m_source = s;
  }

  /**
   * Sets the interval at which the experiment updates its data on
   * runtime and throughput
   * @param step The interval
   */
  public void setEventStep(int step)
  {
    m_eventStep = step;
  }
}