import infodynamics.measures.continuous.*;
import java.io.*;
import java.util.*;

public class Utility {
    public static double getMean(Iterable<Double> values) {
        int count = 0;
        double sum = 0.0;
        for (double value : values) {
            count++;
            sum += value;
        }
        return sum / count;
    }
    
    public static void setProperties(String resourceName, ChannelCalculatorCommon calculator) throws Exception {
        Properties properties = new Properties();
        try (InputStream in = Utility.class.getResourceAsStream(resourceName)) {
            properties.load(in);
        }
        for (String key : properties.stringPropertyNames()) {
            calculator.setProperty(key, properties.getProperty(key));
        }
    }
}
