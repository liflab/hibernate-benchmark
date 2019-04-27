package hibernate;

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.json.JsonStringPrinter;
import ca.uqac.lif.azrael.json.JsonStringReader;
import ca.uqac.lif.azrael.size.SizePrinter;
import ca.uqac.lif.cep.hibernate.FileHibernate;
import ca.uqac.lif.cep.hibernate.Hibernate;
import ca.uqac.lif.cep.tmf.Passthrough;
import ca.uqac.lif.json.JsonFalse;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.LatexNamer;
import ca.uqac.lif.labpal.Region;
import ca.uqac.lif.labpal.TitleNamer;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.mtnp.plot.TwoDimensionalPlot.Axis;
import ca.uqac.lif.mtnp.plot.gnuplot.Scatterplot;
import ca.uqac.lif.mtnp.table.ExpandAsColumns;
import ca.uqac.lif.mtnp.table.TransformedTable;
import hibernate.source.RandomLabelSource;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentException;

public class MainLab extends Laboratory
{
  /**
   * The maximum length of the traces to generate
   */
  public static final transient int s_maxTraceLength = 1000;

  @Override
  public void setup()
  {
    // Basic metadata
    setTitle("Benchmark for the Hibernate palette");
    setAuthor("Laboratoire d'informatique formelle");
    setDoi("TODO");

    // Command line arguments
    ArgumentMap args = getCliArguments();

    Region main_r = new Region();
    main_r.add(StreamExperiment.METHOD, StreamExperiment.METHOD_HIBERNATION, StreamExperiment.METHOD_ONLINE);
    
    // Experiment factory
    StreamExperimentFactory factory = new StreamExperimentFactory(this);
    
    // Titler and nicknamer
    TitleNamer titler = new TitleNamer(); 
    LatexNamer nicker = new LatexNamer();
    
    // Slice experiments
    {
      Region slice_r = new Region(main_r);
      slice_r.add(StreamExperiment.QUERY_NAME, "Slice average");
      slice_r.add(RandomLabelSource.NUM_SLICES, 10, 10000);
      slice_r.add(RandomLabelSource.SLICE_LENGTH, 500);
      for (Region r : slice_r.all(StreamExperiment.QUERY_NAME))
      {
        for (Region r_sl : r.all(RandomLabelSource.NUM_SLICES, RandomLabelSource.SLICE_LENGTH))
        {
          ExperimentTable tab_time = new ExperimentTable(StreamExperiment.LENGTH, StreamExperiment.METHOD, StreamExperiment.TIME);
          titler.setTitle(tab_time, r_sl, "Running time ", "");
          nicker.setNickname(tab_time, r_sl, "tt", "Time");
          ExperimentTable tab_memory = new ExperimentTable(StreamExperiment.LENGTH, StreamExperiment.METHOD, StreamExperiment.MEMORY);
          titler.setTitle(tab_memory, r_sl, "Memory consumption ", "");
          nicker.setNickname(tab_memory, r_sl, "tt", "Mem");
          for (Region r_l : r_sl.all(StreamExperiment.METHOD))
          {
            StreamExperiment se = factory.createExperiment(r_l);
            add(se);
            tab_time.add(se);
            tab_memory.add(se);            
          }
          add(tab_time);
          add(tab_memory);
          TransformedTable tt_time = new TransformedTable(new ExpandAsColumns(StreamExperiment.METHOD, StreamExperiment.TIME), tab_time);
          tt_time.setShowInList(false);
          add(tt_time);
          Scatterplot time_plot = new Scatterplot(tt_time);
          time_plot.setCaption(Axis.Y, "Time (ms)");
          titler.setTitle(time_plot, r_sl, "Running time ", "");
          add(time_plot);
          TransformedTable tt_mem = new TransformedTable(new ExpandAsColumns(StreamExperiment.METHOD, StreamExperiment.MEMORY), tab_memory);
          tt_mem.setShowInList(false);
          add(tt_mem);
          Scatterplot mem_plot = new Scatterplot(tt_mem);
          mem_plot.setCaption(Axis.Y, "Memory (bytes)");
          titler.setTitle(mem_plot, r_sl, "Memory consumption ", "");
          add(mem_plot);
        }
      }
    }
  }

  public static void main(String[] args)
  {
    // Nothing else to do here
    MainLab.initialize(args, MainLab.class);
  }
  
  protected static class EmptyExperiment extends Experiment
  {

    @Override
    public void execute() throws ExperimentException, InterruptedException
    {
      // TODO Auto-generated method stub
      
    }
    
  }

}
