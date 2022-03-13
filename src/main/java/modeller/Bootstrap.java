package modeller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import modeller.daos.FInput;
import modeller.daos.FOutput;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Order(1)
@Slf4j
@RequiredArgsConstructor
@Component
public class Bootstrap implements CommandLineRunner, ApplicationContextAware {

    final TopicDistributionService topicDistributionService;
    ApplicationContext context;
    Reader inputReader;
    FileWriter outputWriter;
    ObjectMapper om = new ObjectMapper();

    @Value("${input:input.csv}")
    private String input = "input.csv";

    @Value("${output:output.json}")
    private String output = "output.json";


    @Value("${skip:-1}")
    private int skip = -1;
    @Value("${title:0}")
    private int title = 0;
    @Value("${date:1}")
    private int date = 1;
    @Value("${post:2}")
    private int post = 2;

    @Override
    public void run(String... args) throws Exception {

Files.deleteIfExists(Path.of(output));
Files.createFile(Path.of(output));

        inputReader = Files.newBufferedReader(Paths.get(input));
        outputWriter = new FileWriter(new File(output), true);
        read();

    }

    public void read() throws Exception {
        CSVReader csvReader = new CSVReader(inputReader);

        String[] line;
        int i = 0;
        boolean set = false;
        while ((line = csvReader.readNext()) != null) {
            if (i <= skip) {

                set = true;
                i++;
                continue;
            }
            FInput b = new FInput(line[title], line[date], line[post]);
            FOutput output = topicDistributionService.run(b);
            writer(om.writeValueAsString(output), set ? 0 :  i);
            i++;
            set = false;
        }
        writer("]",-1);
        inputReader.close();
        csvReader.close();
        outputWriter.close();
        shutdownContext();

    }

    public void writer(String str, int i) throws IOException {
        try  {
            if(i != -1){
                if (i == 0) outputWriter.append("[");else outputWriter.append(",");
            }
            outputWriter.append(str);

            //more code
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

    }


    @PostMapping("/shutdownContext")
    public void shutdownContext() {
        ((ConfigurableApplicationContext) context).close();
    }


    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        this.context = ctx;
    }
}

