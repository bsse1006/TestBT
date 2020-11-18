package parser;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SourceCodeParser
{
    private List<String> listOfLibraryImports = new ArrayList<>();
    private String folderPath;

    public SourceCodeParser (String folderPath) throws Exception
    {
        this.folderPath = folderPath;
        sourceCodeParser();
    }

    private void sourceCodeParser () throws Exception
    {
        importParser(folderPath);

        print();

        String data = "";
        data = new String(Files.readAllBytes(Paths.get("src/files/sourceCodeLibraryList")));

        StringTokenizer st = new StringTokenizer(data);
        while (st.hasMoreTokens())
        {
            listOfLibraryImports.add(st.nextToken());
        }
    }

    public List<String> getListOfLibraryImports() {
        return listOfLibraryImports;
    }

    public void parseJavaLibraries (String javaFIlePath) throws FileNotFoundException
    {
        try {
            File directoryPath = new File(javaFIlePath);
            Scanner sc = new Scanner(directoryPath);

            String javaFIleContent = "";

            while (sc.hasNextLine()) {
                javaFIleContent = javaFIleContent + sc.nextLine() + "\n";
            }

            CompilationUnit cu = StaticJavaParser.parse(javaFIleContent);

            NodeList<ImportDeclaration> listOfImports = cu.getImports();

            for (ImportDeclaration i : listOfImports) {
                listOfLibraryImports.add(i.getName().toString());
            }
        }
        catch (Exception ex) {
            if(!(ex instanceof ParseProblemException))
            {
                ex.printStackTrace();
            }
        }
        catch (Error e) {
                e.printStackTrace();
        }
    }

    public void importParser (String folderPath) throws FileNotFoundException
    {
        File directoryPath = new File(folderPath);

        File filesList [] = directoryPath.listFiles();

        if(filesList == null)
        {
            return;
        }

        for(File file : filesList)
        {
            if(file.getName().length()>5)
            {
                if(file.getName().substring(file.getName().length()-4, file.getName().length()).equals("java"))
                {
                    parseJavaLibraries(file.getAbsolutePath());
                }
            }

            importParser(file.getAbsolutePath());
        }
    }

    public void print () throws Exception
    {
        File file = new File("src/files/sourceCodeLibraryList");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        for (String s: listOfLibraryImports)
        {
            bw.write(s);
            bw.newLine();
        }
        bw.close();
    }
}