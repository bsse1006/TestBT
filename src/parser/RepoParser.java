package parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import naturalLanguageProcessor.TextProcessor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class RepoParser
{
    private List<String> listOfRepositoryKeywords = new ArrayList<>();
    private List<String> listOfLibraryImports = new ArrayList<>();
    private List<File> listOfDirectories = new ArrayList<>();
    private String unprocessedStringOfKeywords = "";
    private String filePath;

    public RepoParser(String filePath) throws Exception {
        this.filePath = filePath;
        sourceCodeParser(filePath);
    }

    public List<String> getListOfRepositoryKeywords() {
        return listOfRepositoryKeywords;
    }

    public List<String> getListOfLibraryImports() {
        return listOfLibraryImports;
    }

    public void parseJavaLibraries (String javaFIlePath) throws FileNotFoundException
    {
        //System.out.println(javaFIlePath);
        try {
            File directoryPath = new File(javaFIlePath);
            Scanner sc = new Scanner(directoryPath);

            String javaFIleContent = "";

            while (sc.hasNextLine()) {
                javaFIleContent = javaFIleContent + sc.nextLine() + "\n";
            }

            if(javaFIleContent.contains("import"))
            {
                try
                {
                    CompilationUnit cu = StaticJavaParser.parse(javaFIleContent);

                    NodeList<ImportDeclaration> listOfImports = cu.getImports();

                    for (ImportDeclaration i : listOfImports) {
                        listOfLibraryImports.add(i.getName().toString());
                    }
                }
                catch (StackOverflowError e)
                {
                    return;
                }
            }
        }
        catch (Exception ex) {
            if(!(ex instanceof ParseProblemException))
            {
                ex.printStackTrace();
            }
        }
    }

    public void sourceCodeParser (String folderPath) throws Exception
    {
        File directoryPath = new File(folderPath);

        while(true)
        {
            File filesList [] = directoryPath.listFiles();

            if (filesList!=null)
            {
                for(File file : filesList)
                {
                    if (file.isDirectory())
                    {
                        listOfDirectories.add(file);
                    }
                    else
                    {
                        if(file.getName().length()>5)
                        {
                            if(file.getName().substring(file.getName().length()-4, file.getName().length()).equals("java"))
                            {
                                parseJavaLibraries(file.getAbsolutePath());
                            }
                            else if(file.getName().equals("README.md")||file.getName().equals("readme.md")||file.getName().equals("readme.txt")||file.getName().equals("readme")
                                    ||file.getName().equals("README"))
                            {
                                parseReadme (file.getAbsolutePath());
                            }
                        }
                    }
                }
            }

            if(!listOfDirectories.isEmpty())
            {
                directoryPath = listOfDirectories.get(0);
                listOfDirectories.remove(listOfDirectories.get(0));
            }
            else
            {
                break;
            }
        }

        processStringOfKeywords();

        /*File filesList [] = directoryPath.listFiles();

        if(m==1)
        {
            System.out.println(folderPath);
            System.out.println(filesList);
        }

        if(filesList == null)
        {
            return;
        }
        //System.out.println("-------------------" + directoryPath.getName());

        for(File file : filesList)
        {
            if (m==1)
            {
                System.out.println(file.getName());
            }
            //System.out.println(file.getName());
            if(file.getName().length()>5)
            {
                if(file.getName().equals("DummyJavadocClass"))
                {
                    System.out.println("oh man");;
                }
                if(file.getName().substring(file.getName().length()-4, file.getName().length()).equals("java"))
                {
                    parseJavaLibraries(file.getAbsolutePath());
                    //System.out.println(file.getName());
                }
            }
            else if(file.getName().equals("README.md")||file.getName().equals("readme.md")||file.getName().equals("readme.txt")||file.getName().equals("readme")
            ||file.getName().equals("README"))
            {
                parseReadme (file.getAbsolutePath());
            }

            sourceCodeParser(file.getAbsolutePath());
        }

        processStringOfKeywords();*/
    }

    private void parseReadme(String absolutePath)
    {
        try {
            File directoryPath = new File(absolutePath);
            Scanner sc = new Scanner(directoryPath);

            while (sc.hasNextLine()) {
                unprocessedStringOfKeywords = unprocessedStringOfKeywords + ' ' + sc.nextLine();
            }
        }
        catch (Exception ex) {
            if(!(ex instanceof ParseProblemException))
            {
                ex.printStackTrace();
            }
        }
    }

    public void processStringOfKeywords() throws Exception
    {
        TextProcessor tp = new TextProcessor(unprocessedStringOfKeywords);
        listOfRepositoryKeywords = tp.getKeywords();
    }
}
