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

    public void parseGithubList () throws IOException, InterruptedException {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get("C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\githubURL.txt")));

        StringTokenizer st = new StringTokenizer(data);

        String content = "";

        File file = new File("C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\GitRepos");
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

            System.out.println("in");
            System.out.println(key + "-----" + value);
            GithubParser gp = new GithubParser(value, LocalDate.parse("2013-04-15"));
            System.out.println("out");

            content = content + gp.getRepoLinks();

            FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
            content = "";
        }
    }
}
