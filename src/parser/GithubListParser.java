package parser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

public class GithubListParser
{
    private Map<String, String> mapOfDevelopersWithGithubURLs = new HashMap<>();

    public Map<String, String> getMapOfDevelopersWithGithubURLs() {
        return mapOfDevelopersWithGithubURLs;
    }

    private String filePath;
    private LocalDate testingDate;

    public GithubListParser(String filePath, LocalDate testingDate) throws IOException, InterruptedException {
        this.filePath = filePath;
        this.testingDate = testingDate;
        parseGithubList();
    }

    public void parseGithubList () throws IOException, InterruptedException {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(filePath)));

        StringTokenizer st = new StringTokenizer(data);

        String content = "";

        File file = new File("src/files/GitRepos");
        if (!file.exists()) {
            file.createNewFile();
        }

        while (st.hasMoreTokens())
        {
            String key = st.nextToken();
            String value = st.nextToken();
            mapOfDevelopersWithGithubURLs.put(key, value);

            if (value.equals("0"))
            {
                continue;
            }

            content = content + key + "\n";
            GithubParser gp = new GithubParser(value, testingDate);

            content = content + gp.getRepoLinks();

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            content = "";
        }
    }
}
