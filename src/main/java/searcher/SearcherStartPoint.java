package searcher;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by ilan.s on 12/10/2014.
 */
public class SearcherStartPoint {
    public static void main(String[] args) {

        String charStream = "אחהוהא";
        int index = 7;
        String dirPath = "F:\\java-test\\txt_files";
        File textFilesDir = new File(dirPath);
        File[] files = textFilesDir.listFiles();

        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < files.length; i++) {
            Runnable searchFile = new SingleFilePatternSearcher(files[i].getAbsolutePath(), charStream, index);
            executor.execute(searchFile);
        }
        executor.shutdown();
        while (! executor.isShutdown()){}
        System.out.println("Finished all threads");
    }
}
