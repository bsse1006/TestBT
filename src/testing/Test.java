package testing;

import com.github.javaparser.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import parser.*;
import profiles.Bug;
import profiles.Developer;
import profiles.FreshGraduate;
import profiles.NewDeveloper;

import javax.print.Doc;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

public class Test
{
    private LocalDate testingDate;
    private List<Developer> experiencedDevelopers = new ArrayList<>();
    private List<NewDeveloper> newExperiencedDevelopers = new ArrayList<>();
    private List<FreshGraduate> freshGraduates = new ArrayList<>();
    private Map<String, Bug> mapOfBugs;
    private Map<String, Developer> mapOfDevelopers;
    private List<String> listOfSourceCodeLibraryImports;
    //private Map<String, String> mapOfDevelopersWithGithubURLs = new HashMap<>();
    private List<Bug> testBugs = new ArrayList<>();

    private List<String> eds = new ArrayList<>();
    private List<String> neds = new ArrayList<>();
    private List<String> fgs = new ArrayList<>();

    private int numberOfED;
    private int numberOfNED;
    private int numberOfFG;
    private int numberOfTestingComponents;
    private int numberOfTestingProducts;
    private int numberOfExistingComponents;
    private int numberOfExistingProducts;
    private int numberOfTrainingBugs;
    private int numberOfTestingBugs;

    private String bugReportsFilePath;
    private String bugReportSolversFilePath;
    private String sourceCodeDirectory;
    private String githubReposDirectory;
    private int teamSizePerDeveloper;
    private int numberOfBugSolutionNeededToUpgrade;

    private int iteration = 1;

    private Bug currentBug;

    private List<Result> teamResults = new ArrayList<>();
    private double MRR;
    private double recall;
    private double efficiency;
    private double match;

    public Test(int teamSizePerDeveloper, int numberOfBugSolutionNeededToUpgrade, LocalDate testingDate, String bugReportsFilePath, String bugReportSolversFilePath, String sourceCodeDirectory, String githubReposDirectory) throws Exception {
        this.teamSizePerDeveloper = teamSizePerDeveloper;
        this.numberOfBugSolutionNeededToUpgrade = numberOfBugSolutionNeededToUpgrade;
        this.testingDate = testingDate;
        this.bugReportsFilePath = bugReportsFilePath;
        this.bugReportSolversFilePath = bugReportSolversFilePath;
        this.sourceCodeDirectory = sourceCodeDirectory;
        this.githubReposDirectory = githubReposDirectory;
        testing();
    }

    public void createFreshGraduate (Developer developer)
    {
        List<String> list = new ArrayList<>();

        for(String bugID: developer.getListOfBugIds())
        {
            list.addAll(mapOfBugs.get(bugID).getListOfKeywords());
        }

        Collections.shuffle(list);

        list = list.subList(0, list.size()/2);

        FreshGraduate fg = new FreshGraduate(developer);
        fg.getListOfKeyWords().addAll(list);

        freshGraduates.add(fg);
    }

    public void chooseNewDeveloperOrFreshGraduate (Developer developer) throws Exception
    {
        Boolean isNED = new File(githubReposDirectory + "\\" + developer.getName()).exists();

        if(isNED)
        {
            RepoParser rp = new RepoParser(githubReposDirectory + "\\" + developer.getName());
            if(rp.getListOfRepositoryKeywords().size()==0&&rp.getListOfLibraryImports().size()==0)
            {
                createFreshGraduate(developer);
            }
            else
            {
                newExperiencedDevelopers.add(new NewDeveloper(developer,rp.getListOfRepositoryKeywords(),rp.getListOfLibraryImports()));
            }
        }
        else
        {
            createFreshGraduate(developer);
        }
    }

    public void testing () throws Exception {
        XMLParser parser = new XMLParser(bugReportsFilePath, bugReportSolversFilePath);
        parser.parsing();
        this.mapOfBugs = parser.getMapOfBugs();
        this.mapOfDevelopers = parser.getMapOfDevelopers();

        for(Developer developer: parser.getMapOfDevelopers().values())
        {
            if(developer.getStartDate().compareTo(testingDate)<0)
            {
                experiencedDevelopers.add(developer);
            }
            else
            {
                chooseNewDeveloperOrFreshGraduate(developer);
            }
        }

        numberOfED = experiencedDevelopers.size();
        numberOfNED = newExperiencedDevelopers.size();
        numberOfFG = freshGraduates.size();

        numberOfExistingProducts = existingProductCounter();
        numberOfExistingComponents = existingComponentCounter();
        numberOfTestingProducts = testingProductCounter();
        numberOfTestingComponents = testingComponentCounter();

        for (Developer developer: experiencedDevelopers)
        {
            List<String> temp = new ArrayList<>();
            for(String bugID: developer.getListOfBugIds())
            {
                Bug bug = mapOfBugs.get(bugID);
                if (bug.getSolutionDate().compareTo(testingDate) > 0)
                {
                    temp.add(bugID);
                }
            }
            developer.getListOfBugIds().removeAll(temp);
            temp.clear();
        }

        for (NewDeveloper developer: newExperiencedDevelopers)
        {
            List<String> temp = new ArrayList<>();
            for(String bugID: developer.getDeveloperCore().getListOfBugIds())
            {
                Bug bug = mapOfBugs.get(bugID);
                if (bug.getSolutionDate().compareTo(testingDate) > 0)
                {
                    temp.add(bugID);
                }
            }
            developer.getDeveloperCore().getListOfBugIds().removeAll(temp);
            temp.clear();
        }

        for (FreshGraduate developer: freshGraduates)
        {
            List<String> temp = new ArrayList<>();
            for(String bugID: developer.getDeveloperCore().getListOfBugIds())
            {
                Bug bug = mapOfBugs.get(bugID);
                if (bug.getSolutionDate().compareTo(testingDate) > 0)
                {
                    temp.add(bugID);
                }
            }
            developer.getDeveloperCore().getListOfBugIds().removeAll(temp);
            temp.clear();
        }

        for(Map.Entry b: mapOfBugs.entrySet())
        {
            Bug bug = (Bug) b.getValue();

            if(bug.getSolutionDate().compareTo(testingDate) > 0)
            {
                testBugs.add(bug);
            }
        }

        numberOfTrainingBugs = mapOfBugs.size()-testBugs.size();

        Collections.sort(testBugs, (o1, o2) -> o1.getSolutionDate().compareTo(o2.getSolutionDate()));

        System.out.println("cp3");

        SourceCodeParser scp = new SourceCodeParser(sourceCodeDirectory);

        listOfSourceCodeLibraryImports = scp.getListOfLibraryImports();

        indexing ();

        teamResult();

        outputResult();
    }

    private void indexing() throws IOException {
        edIndexing();
        System.out.println("ed indexed");
        nedIndexing();
        System.out.println("ned indexed");
        fgIndexing();
        System.out.println("fg indexed");
    }

    private void updateED(String s) throws IOException {
        String indexPath = "src/files/edIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        for(Developer developer: experiencedDevelopers)
        {
            if(developer.getName().equals(s))
            {
                Document document = new Document();

                String content = "";

                developer.getListOfBugIds().add(currentBug.getId());

                for(String bugID: developer.getListOfBugIds())
                {
                    Bug bug = mapOfBugs.get(bugID);
                    content = content + " " + convertListToString(bug.getListOfKeywords());
                    content = content + " " + bug.getProduct();
                    content = content + " " + bug.getComponent();
                }

                document.add(new TextField("content", content, Field.Store.NO));
                document.add(new StringField("name", developer.getName(), Field.Store.YES));
                document.add(new StringField("startingDate", developer.getStartDate().toString(), Field.Store.YES));

                indexWriter.deleteDocuments(new Term("name", s));
                indexWriter.addDocument(document);

                break;
            }
        }

        indexWriter.close();
    }

    private void updateNED(String s) throws IOException {

        NewDeveloper currentDev = null;

        for(NewDeveloper developer: newExperiencedDevelopers)
        {
            if(developer.getDeveloperCore().getName().equals(s))
            {
                developer.getDeveloperCore().getListOfBugIds().add(currentBug.getId());

                if (developer.getDeveloperCore().getListOfBugIds().size()==numberOfBugSolutionNeededToUpgrade)
                {
                    String indexPath = "src/files/nedIndex";

                    Directory dir = FSDirectory.open(Paths.get(indexPath));

                    Analyzer analyzer = new StandardAnalyzer();

                    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

                    IndexWriter indexWriter = new IndexWriter(dir, iwc);

                    indexWriter.deleteDocuments(new Term("name", s));
                    currentDev = developer;
                    experiencedDevelopers.add(developer.getDeveloperCore());

                    indexWriter.close();

                    updateED(developer.getDeveloperCore().getName());
                }

                break;
            }
        }

        if (currentDev!=null)
        {
            newExperiencedDevelopers.remove(currentDev);
        }
    }

    private void updateFG(String s) throws IOException {

        FreshGraduate currentDev = null;

        for(FreshGraduate developer: freshGraduates)
        {
            if(developer.getDeveloperCore().getName().equals(s))
            {
                developer.getDeveloperCore().getListOfBugIds().add(currentBug.getId());

                if (developer.getDeveloperCore().getListOfBugIds().size()==numberOfBugSolutionNeededToUpgrade)
                {
                    String indexPath = "src/files/fgIndex";

                    Directory dir = FSDirectory.open(Paths.get(indexPath));

                    Analyzer analyzer = new StandardAnalyzer();

                    IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

                    IndexWriter indexWriter = new IndexWriter(dir, iwc);

                    indexWriter.deleteDocuments(new Term("name", s));
                    currentDev = developer;
                    experiencedDevelopers.add(developer.getDeveloperCore());

                    indexWriter.close();

                    updateED(developer.getDeveloperCore().getName());
                }

                break;
            }
        }

        if (currentDev!=null)
        {
            freshGraduates.remove(currentDev);
        }
    }

    private void teamResult () throws IOException, ParseException {
        List<Bug> poorBugs = new ArrayList<>();
        System.out.println(testBugs.size());
        for (Bug testBug: testBugs)
        {
            if (experiencedDevelopers.size()>0)
            {
                readEdIndex(testBug);
            }
            if (newExperiencedDevelopers.size()>0)
            {
                readNedIndex(testBug);
            }
            if (freshGraduates.size()>0)
            {
                readFgIndex(testBug);
            }

            if(eds.size()+neds.size()+fgs.size()<teamSizePerDeveloper*3)
            {
                eds.clear();
                neds.clear();
                fgs.clear();
                poorBugs.add(testBug);
                continue;
            }

            Result result = new Result(eds,neds,fgs,testBug, teamSizePerDeveloper);

            teamResults.add(result);

            System.out.println("Iteration: " + iteration);

            System.out.println("----" + result.getRecall() + "----" + result.getMatch() + "----" + result.getRank());

            harmonicAverageResult();
            System.out.println("Recall: " + recall + "--" + "Match: " + match + "--" + "Efficiency: " + efficiency + "--"
                    + "MRR: " + MRR);

            currentBug = testBug;

            for(String dev: result.getSortedDevs())
            {
                if(eds.contains(dev))
                {
                    updateED(dev);
                }
                if(neds.contains(dev))
                {
                    updateNED(dev);
                }
                if(fgs.contains(dev))
                {
                    updateFG(dev);
                }
            }

            eds.clear();
            neds.clear();
            fgs.clear();

            iteration++;
        }

        testBugs.removeAll(poorBugs);

        System.out.println(testBugs.size());

        numberOfTestingBugs = testBugs.size();

        harmonicAverageResult();
    }

    private void harmonicAverageResult()
    {
        double avgRecall = 0;
        double avgMatch = 0;
        double avgEfficiency = 0;
        double avgMRR = 0;

        for(Result result: teamResults)
        {
            avgRecall = avgRecall + result.getRecall();
            avgMatch = avgMatch + result.getMatch();
            avgEfficiency = avgEfficiency + (1.0/result.getRank());

            if (!Double.isNaN(result.getRank())&&!(result.getRank()==0.0))
            {
                avgMRR = avgMRR + (1.0/result.getRank());
            }
        }

        recall = avgRecall/teamResults.size();
        match = avgMatch/teamResults.size();
        efficiency = teamResults.size()/avgEfficiency;
        MRR = avgMRR/teamResults.size();
    }

    private void averageResult()
    {
        double avgRecall = 0;
        double avgMatch = 0;
        double avgEfficiency = 0;
        double avgMRR = 0;

        for(Result result: teamResults)
        {
            avgRecall = avgRecall + result.getRecall();
            avgMatch = avgMatch + result.getMatch();
            avgEfficiency = avgEfficiency + result.getRank();

            if (!Double.isNaN(result.getRank())&&!(result.getRank()==0.0))
            {
                avgMRR = avgMRR + (1.0/result.getRank());
            }
        }

        recall = avgRecall/teamResults.size();
        match = avgMatch/teamResults.size();
        efficiency = avgEfficiency/teamResults.size();
        MRR = avgMRR/teamResults.size();
    }

    public void outputResult () throws IOException {
        String content = "No. of ED: " + experiencedDevelopers.size() + "\n"
                + "No. of NED: " + newExperiencedDevelopers.size() + "\n"
                + "No. of FG: " + freshGraduates.size() + "\n"
                + "No. of existing bug reports: " + (mapOfBugs.size()-testBugs.size()) + "\n"
                + "No. of existing bug report product: " + existingProductCounter() + "\n"
                + "No. of existing bug report component: " + existingComponentCounter() + "\n"
                + "No. of testing bug reports: " + testBugs.size() + "\n"
                + "No. of testing bug report product: " + testingProductCounter() + "\n"
                + "No. of testing bug report component: " + testingComponentCounter() + "\n"
                + "\n" + "\n"
                + "Average Recall: " + recall + "\n"
                + "\n" + "\n"
                + "Average Match: " + match +  "\n"
                + "\n" + "\n"
                + "Average Effectiveness: " + efficiency + "\n"
                + "\n" + "\n"
                + "Mean Reciprocal Rank: " + MRR + "\n";

        File file = new File("src/files/output");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(content);
        bw.close();

        System.out.println("Done");
    }

    public int existingProductCounter ()
    {
        Set<String> products = new HashSet<>();

        for(Map.Entry m: mapOfBugs.entrySet())
        {
            Bug bug = (Bug) m.getValue();

            if(testBugs.contains(bug))
            {
                continue;
            }
            else
            {
                products.add(bug.getProduct());
            }
        }

        return products.size();
    }

    public int existingComponentCounter ()
    {
        Set<String> components = new HashSet<>();

        for(Map.Entry m: mapOfBugs.entrySet())
        {
            Bug bug = (Bug) m.getValue();

            if(testBugs.contains(bug))
            {
                continue;
            }
            else
            {
                components.add(bug.getComponent());
            }
        }

        return components.size();
    }

    public int testingProductCounter ()
    {
        Set<String> products = new HashSet<>();

        for(Bug bug: testBugs)
        {
            products.add(bug.getProduct());
        }

        return products.size();
    }

    public int testingComponentCounter ()
    {
        Set<String> components = new HashSet<>();

        for(Bug bug: testBugs)
        {
            components.add(bug.getComponent());
        }

        return components.size();
    }

    public void readEdIndex (Bug testBug) throws IOException, ParseException {
        String indexPath = "src/files/edIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        DirectoryReader directoryReader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher (directoryReader);

        BooleanQuery.setMaxClauseCount(16384);

        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String s: testBug.getListOfKeywords())
        {
            bq.add(new BooleanClause(new TermQuery(new Term("content", s)), BooleanClause.Occur.SHOULD));
        }

        TopDocs results = searcher.search(bq.build(), experiencedDevelopers.size());

        for(ScoreDoc scoreDoc: results.scoreDocs)
        {
            Document document = searcher.doc(scoreDoc.doc);
            eds.add(document.get("name"));
        }

        directoryReader.close();
    }

    private void readNedIndex (Bug testBug) throws IOException, ParseException {
        String indexPath = "src/files/nedIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        DirectoryReader directoryReader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher (directoryReader);

        BooleanQuery.setMaxClauseCount(16384);

        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String s: testBug.getListOfKeywords())
        {
            bq.add(new BooleanClause(new TermQuery(new Term("content", s)), BooleanClause.Occur.SHOULD));
        }

        for (String s: listOfSourceCodeLibraryImports)
        {
            bq.add(new BooleanClause(new TermQuery(new Term("content", s)), BooleanClause.Occur.SHOULD));
        }

        TopDocs results = searcher.search(bq.build(), newExperiencedDevelopers.size());

        for(ScoreDoc scoreDoc: results.scoreDocs)
        {
            Document document = searcher.doc(scoreDoc.doc);
            neds.add(document.get("name"));
        }

        directoryReader.close();
    }

    private void readFgIndex (Bug testBug) throws IOException, ParseException {
        String indexPath = "src/files/fgIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        DirectoryReader directoryReader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher (directoryReader);

        BooleanQuery.setMaxClauseCount(16384);

        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String s: testBug.getListOfKeywords())
        {
            bq.add(new BooleanClause(new TermQuery(new Term("content", s)), BooleanClause.Occur.SHOULD));
        }

        TopDocs results = searcher.search(bq.build(), freshGraduates.size());

        for(ScoreDoc scoreDoc: results.scoreDocs)
        {
            Document document = searcher.doc(scoreDoc.doc);
            fgs.add(document.get("name"));
        }

        directoryReader.close();
    }

    private String convertListToString (List<String> list)
    {
        String listString = "";

        for(String s: list)
        {
            listString = listString + s + " ";
        }

        return listString;
    }

    public void deleteIndex (String path)
    {
        File file = new File (path);

        for(File subFile: file.listFiles())
        {
            subFile.delete();
        }
    }

    public void edIndexing() throws IOException {
        String indexPath = "src/files/edIndex";

        deleteIndex(indexPath);

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        for(Developer developer: experiencedDevelopers)
        {
            Document document = new Document();

            String content = "";

            for(String bugID: developer.getListOfBugIds())
            {
                Bug bug = mapOfBugs.get(bugID);
                if (bug.getSolutionDate().compareTo(testingDate) < 0)
                {
                    content = content + " " + convertListToString(bug.getListOfKeywords());
                    content = content + " " + bug.getProduct();
                    content = content + " " + bug.getComponent();
                }
            }

            document.add(new TextField("content", content, Field.Store.NO));
            document.add(new StringField("name", developer.getName(), Field.Store.YES));
            document.add(new StringField("startingDate", developer.getStartDate().toString(), Field.Store.YES));

            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    private void nedIndexing() throws IOException {
        String indexPath = "src/files/nedIndex";

        deleteIndex(indexPath);

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        for(NewDeveloper developer: newExperiencedDevelopers)
        {
            Document document = new Document();

            String content = "";

            content = content + " " + convertListToString(developer.getListOfLibraryImports());
            content = content + " " + convertListToString(developer.getListOfRepositoryKeywords());


            document.add(new TextField("content", content, Field.Store.NO));
            document.add(new StringField("name", developer.getDeveloperCore().getName(), Field.Store.YES));
            document.add(new StringField("startingDate", developer.getDeveloperCore().getStartDate().toString(), Field.Store.YES));

            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    private void fgIndexing() throws IOException {
        String indexPath = "src/files/fgIndex";

        deleteIndex(indexPath);

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        for(FreshGraduate developer: freshGraduates)
        {
            Document document = new Document();

            String content = "";

            content = content + " " + convertListToString(developer.getListOfKeyWords());


            document.add(new TextField("content", content, Field.Store.NO));
            document.add(new StringField("name", developer.getDeveloperCore().getName(), Field.Store.YES));
            document.add(new StringField("startingDate", developer.getDeveloperCore().getStartDate().toString(), Field.Store.YES));

            indexWriter.addDocument(document);
        }

        indexWriter.close();
    }

    public int getNumberOfED() {
        return numberOfED;
    }

    public int getNumberOfNED() {
        return numberOfNED;
    }

    public int getNumberOfFG() {
        return numberOfFG;
    }

    public int getNumberOfTestingComponents() {
        return numberOfTestingComponents;
    }

    public int getNumberOfTestingProducts() {
        return numberOfTestingProducts;
    }

    public int getNumberOfExistingComponents() {
        return numberOfExistingComponents;
    }

    public int getNumberOfExistingProducts() {
        return numberOfExistingProducts;
    }

    public int getNumberOfTrainingBugs() {
        return numberOfTrainingBugs;
    }

    public LocalDate getTestingDate() {
        return testingDate;
    }

    public Map<String, Bug> getMapOfBugs() {
        return mapOfBugs;
    }

    public Map<String, Developer> getMapOfDevelopers() {
        return mapOfDevelopers;
    }

    public List<String> getListOfSourceCodeLibraryImports() {
        return listOfSourceCodeLibraryImports;
    }

    public String getBugReportsFilePath() {
        return bugReportsFilePath;
    }

    public String getBugReportSolversFilePath() {
        return bugReportSolversFilePath;
    }

    public String getSourceCodeDirectory() {
        return sourceCodeDirectory;
    }

    public String getGithubReposDirectory() {
        return githubReposDirectory;
    }

    public int getTeamSizePerDeveloper() {
        return teamSizePerDeveloper;
    }

    public int getNumberOfBugSolutionNeededToUpgrade() {
        return numberOfBugSolutionNeededToUpgrade;
    }

    public double getMRR() {
        return MRR;
    }

    public double getRecall() {
        return recall;
    }

    public double getEfficiency() {
        return efficiency;
    }

    public double getMatch() {
        return match;
    }

    public int getNumberOfTestingBugs() {
        return numberOfTestingBugs;
    }
}
