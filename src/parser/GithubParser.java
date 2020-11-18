package parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import naturalLanguageProcessor.TextProcessor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class GithubParser
{
    private String url;
    private String userName;
    private String repositoryName;
    private LocalDate testingDate;
    private List<String> listOfRepositoryKeywords = new ArrayList<>();
    private List<String> listOfLibraryImports = new ArrayList<>();
    private String unprocessedStringOfKeywords = "";
    private String repoLinks = "";

    public GithubParser(String url, LocalDate testingDate) throws InterruptedException {
        this.url = url;
        this.testingDate = testingDate;
        parseHTML();
    }

    public List<String> getListOfRepositoryKeywords() {
        return listOfRepositoryKeywords;
    }

    public List<String> getListOfLibraryImports() {
        return listOfLibraryImports;
    }

    public LocalDate parseCreationDateOfRepository (String link) throws InterruptedException
    {
        LocalDate creationDate = null;

        try
        {
            final String document = Jsoup.connect(link).header("Authorization", "token 597782c8b356a720d602bea1cdda789662131fd0").ignoreContentType(true).execute().body();

            Object obj = new JSONParser().parse(document);

            JSONObject json = (JSONObject) obj;

            creationDate = LocalDate.parse(json.get("created_at").toString().substring(0,10));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().sleep(10000);
            return parseCreationDateOfRepository(link);
        }

        Thread.currentThread().sleep(10000);

        return creationDate;
    }

    public void parseRepositories (String link) throws InterruptedException {
        try {
            final Document document = Jsoup.connect(link).get();

            for (Element element : document.select("div.col-10.col-lg-9.d-inline-block"))
            {
                for(Element span : element.select("span"))
                {
                    if(span.attr("itemprop").equals("programmingLanguage"))
                    {
                        if(span.text().equals("Java"))
                        {
                            for (Element repo: element.select("a"))
                            {
                                if(repo.attr("itemprop").equals("name codeRepository"))
                                {
                                    repositoryName = repo.text();
                                    System.out.println("https://github.com" + repo.attr("href"));
                                    LocalDate repoDate = parseCreationDateOfRepository("https://api.github.com/repos/"+url.substring(19,url.length())+"/"+repositoryName);
                                    //System.out.println(repoDate);
                                    if (repoDate.compareTo(testingDate)<0)
                                    //if (parseCreationDateOfRepositoryFromListOfCommits("https://github.com"+repo.attr("href")+"/commits"))
                                    //if(parseCreationDateOfRepositoryFromContributors("https://github.com"+repo.attr("href")+"/graphs/contributors"))
                                    {
                                        System.out.println("--");
                                        repoLinks = repoLinks + "https://github.com" + repo.attr("href") + "\n";
                                        //unprocessedStringOfKeywords = unprocessedStringOfKeywords + ' ' + repo.text();
                                        //parseFileNames("https://github.com" + repo.attr("href"));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for(Element element : document.select("a.btn.btn-outline.BtnGroup-item"))
            {
                if (element.text().equals("Next"))
                {
                    parseRepositories(element.attr("href"));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(link);
            Thread.currentThread().sleep(10000);
            repoLinks = "";
            parseRepositories(link);
        }
    }

    public String getRepoLinks() {
        return repoLinks;
    }

    public void parseHTML () throws InterruptedException {
        try {
            final Document document = Jsoup.connect(url).get();

            String repositoryLink = "";

            for (Element element : document.select("a.UnderlineNav-item "))
            {
                if(element.text().contains("Repositories"))
                {
                    repositoryLink = "https://github.com" + element.attr("href");
                    break;
                }
            }

            parseRepositories(repositoryLink);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().sleep(10000);
            repoLinks = "";
            parseHTML();
        }
    }
}
