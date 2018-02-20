import java.io.*;
import java.util.*;
import java.util.regex.*;

public class TimeSeriesEnsembleReader implements AutoCloseable {
    private static final Pattern ARGUMENT_PATTERN = Pattern.compile("^[a-z_]+ = .+$");
    private static final Pattern AGENT_PATTERN = Pattern.compile("^# AGENT (?<agentIndex>\\d+$)");
    private static final Pattern DIMENSIONS_PATTERN = Pattern.compile("^# DIMENSIONS (?<neuronCount>\\d+) (?<inputNeuronCount>\\d+) (?<outputNeuronCount>\\d+)$");
    
    private static String getExceptionMessage(String line) {
        return String.format("Unexpected line '%s'.", line);
    }
    
    private BufferedReader reader;
    
    public TimeSeriesEnsembleReader(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }
    
    public void readArguments(PrintStream out) throws IOException {
        String line = reader.readLine();
        if (!line.equals("# BEGIN ARGUMENTS")) {
            throw new IOException(getExceptionMessage(line));
        }
        while (true) {
            line = reader.readLine();
            if (line.equals("# END ARGUMENTS")) {
                break;
            }
            Matcher matcher = ARGUMENT_PATTERN.matcher(line);
            if (!matcher.matches()) {
                throw new IOException(getExceptionMessage(line));
            }
            out.printf("# %s%n", line);
        }
    }
    
    public TimeSeriesEnsemble read() throws IOException {
        TimeSeriesEnsemble ensemble = readHeader();
        if (ensemble == null) {
            return null;
        }
        while (true) {
            TimeSeries timeSeries = readTimeSeries(ensemble.getNeuronCount());
            if (timeSeries == null) {
                break;
            }
            ensemble.add(timeSeries);
        }
        return ensemble;
    }
    
    private TimeSeriesEnsemble readHeader() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        Matcher matcher = AGENT_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IOException(getExceptionMessage(line));
        }
        int agentIndex = Integer.parseInt(matcher.group("agentIndex"));
        line = reader.readLine();
        matcher = DIMENSIONS_PATTERN.matcher(line);
        if (!matcher.matches()) {
            throw new IOException(getExceptionMessage(line));
        }
        int neuronCount = Integer.parseInt(matcher.group("neuronCount"));
        int inputNeuronCount = Integer.parseInt(matcher.group("inputNeuronCount"));
        int outputNeuronCount = Integer.parseInt(matcher.group("outputNeuronCount"));
        TimeSeriesEnsemble ensemble = new TimeSeriesEnsemble(agentIndex, neuronCount, inputNeuronCount, outputNeuronCount);
        readNerves(ensemble);
        readSynapses(ensemble);
        return ensemble;
    }
    
    private void readNerves(TimeSeriesEnsemble ensemble) throws IOException {
        reader.mark(1024);
        String line = reader.readLine();
        if (!line.equals("# BEGIN NERVES")) {
            reader.reset();
            return;
        }
        int start = 0;
        while (true) {
            line = reader.readLine();
            if (line.equals("# END NERVES")) {
                break;
            }
            try (Scanner scanner = new Scanner(line)) {
                String name = scanner.next();
                int count = scanner.nextInt();
                ensemble.addNerve(new Nerve(name, Utility.getRange(start, count)));
                start += count;
            }
        }
    }
    
    private void readSynapses(TimeSeriesEnsemble ensemble) throws IOException {
        String line = reader.readLine();
        if (!line.equals("# BEGIN SYNAPSES")) {
            throw new IOException(getExceptionMessage(line));
        }
        while (true) {
            line = reader.readLine();
            if (line.equals("# END SYNAPSES")) {
                break;
            }
            try (Scanner scanner = new Scanner(line)) {
                int preNeuronIndex = scanner.nextInt();
                while (scanner.hasNext()) {
                    int postNeuronIndex = scanner.nextInt();
                    ensemble.addSynapse(new Synapse(preNeuronIndex, postNeuronIndex));
                }
            }
        }
    }
    
    private TimeSeries readTimeSeries(int dimension) throws IOException {
        String line = reader.readLine();
        if (line.equals("# BEGIN ENSEMBLE")) {
            line = reader.readLine();
        }
        if (line.equals("# END ENSEMBLE")) {
            return null;
        }
        if (!line.equals("# BEGIN TIME SERIES")) {
            throw new IOException(getExceptionMessage(line));
        }
        TimeSeries timeSeries = new TimeSeries(dimension);
        while (true) {
            line = reader.readLine();
            if (line.equals("# END TIME SERIES")) {
                return timeSeries;
            }
            double[] row = new double[dimension];
            try (Scanner scanner = new Scanner(line)) {
                for (int index = 0; index < dimension; index++) {
                    row[index] = scanner.nextDouble();
                }
                if (scanner.hasNext()) {
                    throw new IOException(getExceptionMessage(line));
                }
            }
            timeSeries.add(row);
        }
    }
    
    public void close() throws IOException {
        reader.close();
    }
}
