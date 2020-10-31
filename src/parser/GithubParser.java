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

    public boolean parseCreationDateOfRepositoryFromListOfCommits (String link) throws InterruptedException {
        LocalDate creationDate = null;

        try {
            final Document document = Jsoup.connect(link).get();

            for (Element element : document.select("relative-time.no-wrap"))
            {
                creationDate = LocalDate.parse(element.attr("datetime").substring(0,10));
            }



            if(creationDate.compareTo(testingDate)<0)
            {
                return true;
            }

            for(Element element : document.select("a.btn.btn-outline.BtnGroup-item"))
            {
                if (element.text().equals("Older"))
                {
                    return  parseCreationDateOfRepositoryFromListOfCommits(element.attr("href"));
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().sleep(10000);
            return parseCreationDateOfRepositoryFromListOfCommits(link);
        }

        return false;
    }

    public List<String> getListOfRepositoryKeywords() {
        return listOfRepositoryKeywords;
    }

    public List<String> getListOfLibraryImports() {
        return listOfLibraryImports;
    }

    public void parseJavaLibraries (String link) throws InterruptedException {
        try {
            final Document document = Jsoup.connect(link).get();

            //System.out.println(link);

            String javaFile = "";

            for (Element element : document.select("td.blob-code.blob-code-inner.js-file-line"))
            {
                javaFile = javaFile + element.text() + "\n";
            }

            CompilationUnit cu = StaticJavaParser.parse(javaFile);

            NodeList<ImportDeclaration> listOfImports = cu.getImports();

            for(ImportDeclaration i: listOfImports)
            {
                listOfLibraryImports.add(i.getName().toString());
            }
        }
        catch (Exception ex) {
            if(!(ex instanceof ParseProblemException))
            {
                ex.printStackTrace();
                Thread.currentThread().sleep(10000);
                parseJavaLibraries(link);
            }
        }
    }

    public void parseReadme (String link) throws InterruptedException {
        try {
            final Document document = Jsoup.connect(link).get();

            for (Element element : document.select("article.markdown-body.entry-content.container-lg"))
            {
                for(Element p: element.select("p"))
                {
                    unprocessedStringOfKeywords = unprocessedStringOfKeywords + ' ' + p.text();
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().sleep(10000);
            parseReadme(link);
        }
    }

    public LocalDate parseCreationDateOfRepository (String link) throws InterruptedException
    {
        LocalDate creationDate = null;

        try
        {
            final String document = Jsoup.connect(link).header("Authorization", "token baf7ec21cbd7d2688bce0cf3ebd2af59621e1bca").ignoreContentType(true).execute().body();

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

    public void parseFileNames (String link) throws InterruptedException {
        try {
            final Document document = Jsoup.connect(link).get();

            for (Element element : document.select("a.js-navigation-open.link-gray-dark"))
            {
                if(element.text().length()>5)
                {
                    if(element.text().substring(element.text().length()-4, element.text().length()).equals("java"))
                    {
                        //System.out.println(element.text());
                        parseJavaLibraries("https://github.com" + element.attr("href"));
                    }
                }
                if(element.text().equals("README.md"))
                {
                    parseReadme("https://github.com" + element.attr("href"));
                }
                unprocessedStringOfKeywords = unprocessedStringOfKeywords + ' ' + element.text();
                parseFileNames("https://github.com" + element.attr("href"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(link);
            Thread.currentThread().sleep(10000);
            parseFileNames(link);
        }

        Thread.currentThread().sleep(10000);
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

    private boolean parseCreationDateOfRepositoryFromContributors(String link) throws InterruptedException {
        LocalDate creationDate = null;

        String s = "MMM dd, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(s);
        Date date = null;

        try {
            final Document document = Jsoup.connect(link).get();

            //Thread.currentThread().sleep(50000);

            for (Element element : document.select("h2.js-date-range.Subhead-heading "))
            {
                date = sdf.parse(element.text().substring(0,12));
            }

            creationDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            System.out.println(creationDate);

            if(creationDate.compareTo(testingDate)<0)
            {
                return true;
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().sleep(10000);
            return parseCreationDateOfRepositoryFromContributors(link);
        }

        return false;
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

            //processStringOfKeywords();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            Thread.currentThread().sleep(10000);
            repoLinks = "";
            parseHTML();
        }
    }

    public void processStringOfKeywords() throws Exception
    {
        TextProcessor tp = new TextProcessor(unprocessedStringOfKeywords);
        listOfRepositoryKeywords = tp.getKeywords();
    }
}
