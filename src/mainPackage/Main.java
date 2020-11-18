package mainPackage;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import naturalLanguageProcessor.TextProcessor;
import org.eclipse.jgit.api.Git;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import parser.*;

import org.jdom.Attribute;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import profiles.Bug;
import profiles.Developer;
import testing.Test;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        GithubListParser glp = new GithubListParser("C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\githubURL.txt", LocalDate.parse("2020-04-15"));
        CloneGitRepos cgr = new CloneGitRepos("C:\\Users\\Hp\\Desktop\\TestClonedGitRepos");

        Test test = new Test(10, 10, LocalDate.parse("2013-04-15"), "src/files/fixedData.xml",
                "src/files/fixedDataHistory.xml", "C:\\Users\\Hp\\Desktop\\TestClonedGitRepos",
                "C:\\Users\\Hp\\Desktop\\TestClonedGitRepos");

        
        /*XMLParser parser = new XMLParser();
        parser.parsing();

        List<Map.Entry<String, Bug>> bugs = new ArrayList<>(parser.getMapOfBugs().entrySet());

        System.out.println("Sorted bugs");

        Collections.sort(bugs, (o1, o2) -> o1.getValue().getCreationDate().compareTo(o2.getValue().getCreationDate()));

        int counter = 0;
        for(Map.Entry d: bugs)
        {
            if(counter == 5)
            {
                break;
            }
            Bug bug = (Bug)d.getValue();
            System.out.println(bug.getId()+"----->"+bug.getCreationDate());
            System.out.println(bug.getListOfSolvers());
            counter++;
        }

        System.out.println();*/
    }
}
