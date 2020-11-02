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

    List<String> eds = new ArrayList<>();
    List<String> neds = new ArrayList<>();
    List<String> fgs = new ArrayList<>();

    Bug currentBug;

    List<Result> teamResults = new ArrayList<>();
    private double MRR;
    private double recall;
    private double efficiency;
    private double match;

    public Test(LocalDate testingDate)
    {
        this.testingDate = testingDate;
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
        Boolean isNED = new File("C:\\Users\\Hp\\Desktop\\ClonedGitRepos\\" + developer.getName()).exists();

        if(isNED)
        {
            System.out.println(developer.getName());
            RepoParser rp = new RepoParser("C:\\Users\\Hp\\Desktop\\ClonedGitRepos\\" + developer.getName());
            System.out.println(rp.getListOfLibraryImports().size()+ "----" + rp.getListOfRepositoryKeywords().size());
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


        /*System.out.println("-----" + mapOfDevelopersWithGithubURLs.get(developer.getName()));*/
        /*if(mapOfDevelopersWithGithubURLs.get(developer.getName()).equals("0"))
        {
            createFreshGraduate(developer);
        }
        else
        {
            System.out.println(developer.getName());
            RepoParser rp = new RepoParser("C:\\Users\\Hp\\Desktop\\ClonedGitRepos\\" + developer.getName());

            //GithubParser gp = new GithubParser(mapOfDevelopersWithGithubURLs.get(developer.getName()),LocalDate.parse("2013-04-15"));
            System.out.println(rp.getListOfLibraryImports().size()+ "----" + rp.getListOfRepositoryKeywords().size());
            if(rp.getListOfRepositoryKeywords().size()==0&&rp.getListOfLibraryImports().size()==0)
            {
                createFreshGraduate(developer);
            }
            else
            {
                newExperiencedDevelopers.add(new NewDeveloper(developer,rp.getListOfRepositoryKeywords(),rp.getListOfLibraryImports()));
            }
        }*/
    }

    public void testing () throws Exception {
        XMLParser parser = new XMLParser();
        parser.parsing();
        this.mapOfBugs = parser.getMapOfBugs();
        this.mapOfDevelopers = parser.getMapOfDevelopers();

        System.out.println("cp");

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

        //System.out.println("--" + experiencedDevelopers.size());

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

        Collections.sort(testBugs, (o1, o2) -> o1.getSolutionDate().compareTo(o2.getSolutionDate()));

        System.out.println("cp3");

        SourceCodeParser scp = new SourceCodeParser();

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

    /*private void updateDevs () throws IOException {
        for(int i=0; i<4; i++)
        {
            updateED(eds.get(i));
            updateNED(neds.get(i));
            updateFG(fgs.get(i));
        }
    }*/

    private void updateED(String s) throws IOException {
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\edIndex";

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

                if (developer.getDeveloperCore().getListOfBugIds().size()==10)
                {
                    String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\nedIndex";

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

        /*String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\nedIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        NewDeveloper currentDev = null;

        for(NewDeveloper developer: newExperiencedDevelopers)
        {
            if(developer.getDeveloperCore().getName().equals(s))
            {
                Document document = new Document();

                String content = "";

                content = content + " " + convertListToString(developer.getListOfLibraryImports());
                content = content + " " + convertListToString(developer.getListOfRepositoryKeywords());

                developer.getDeveloperCore().getListOfBugIds().add(currentBug.getId());

                document.add(new TextField("content", content, Field.Store.NO));
                document.add(new StringField("name", developer.getDeveloperCore().getName(), Field.Store.YES));
                document.add(new StringField("startingDate", developer.getDeveloperCore().getStartDate().toString(), Field.Store.YES));

                if (developer.getDeveloperCore().getListOfBugIds().size()==10)
                {
                    indexWriter.deleteDocuments(new Term("name", s));
                    currentDev = developer;
                    experiencedDevelopers.add(developer.getDeveloperCore());
                    updateED(developer.getDeveloperCore().getName());
                }
                else
                {
                    indexWriter.deleteDocuments(new Term("name", s));
                    indexWriter.addDocument(document);
                }

                break;
            }
        }

        if (currentDev!=null)
        {
            newExperiencedDevelopers.remove(currentDev);
        }

        indexWriter.close();*/
    }

    private void updateFG(String s) throws IOException {

        FreshGraduate currentDev = null;

        for(FreshGraduate developer: freshGraduates)
        {
            if(developer.getDeveloperCore().getName().equals(s))
            {
                developer.getDeveloperCore().getListOfBugIds().add(currentBug.getId());

                if (developer.getDeveloperCore().getListOfBugIds().size()==10)
                {
                    String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\fgIndex";

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

        /*String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\fgIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        Analyzer analyzer = new StandardAnalyzer();

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(dir, iwc);

        FreshGraduate currentDev = null;

        for(FreshGraduate developer: freshGraduates)
        {
            if(developer.getDeveloperCore().getName().equals(s))
            {
                Document document = new Document();

                String content = "";

                content = content + " " + convertListToString(developer.getListOfKeyWords());

                developer.getDeveloperCore().getListOfBugIds().add(currentBug.getId());

                document.add(new TextField("content", content, Field.Store.NO));
                document.add(new StringField("name", developer.getDeveloperCore().getName(), Field.Store.YES));
                document.add(new StringField("startingDate", developer.getDeveloperCore().getStartDate().toString(), Field.Store.YES));

                if (developer.getDeveloperCore().getListOfBugIds().size()==10)
                {
                    indexWriter.deleteDocuments(new Term("name", s));
                    currentDev = developer;
                    experiencedDevelopers.add(developer.getDeveloperCore());
                    updateED(developer.getDeveloperCore().getName());
                }
                else
                {
                    //indexWriter.updateDocument(new Term("name", s), document);
                    indexWriter.deleteDocuments(new Term("name", s));
                    indexWriter.addDocument(document);
                }

                break;
            }
        }

        if (currentDev!=null)
        {
            freshGraduates.remove(currentDev);
        }

        indexWriter.close();*/
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

            if(eds.size()+neds.size()+fgs.size()<30)
            {
                eds.clear();
                neds.clear();
                fgs.clear();
                poorBugs.add(testBug);
                continue;
            }

            /*System.out.println("-----"+eds.size());
            System.out.println(neds.size());
            System.out.println(fgs.size()+"-----");*/

            Result result = new Result(eds,neds,fgs,testBug);

            teamResults.add(result);

            System.out.println("----" + result.getRecall() + "----" + result.getMatch() + "----" + result.getRank());

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

            /*System.out.println("result done" + testBug);*/
        }

        testBugs.removeAll(poorBugs);

        System.out.println(testBugs.size());

        averageresult();
    }

    private void averageresult()
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

        /*teamPrecision.add(Double.toString(avgPrecision));
        teamRecall.add(Double.toString(avgRecall));
        teamFScore.add(Double.toString(avgFScore));
        avgTopNForSortedResult.add(Double.toString(avgTopN));
        avgEfficiencyList.add(Double.toString(avgEfficiency));
        avgMRRList.add(Double.toString(avgMRR));*/
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
                /*+ "----" + "k" + "----" + "avg recall" + "----" + "avg precision" + "----" + "avg f-score" + "----" + "\n"
                + "----" + 3 + "----" + teamRecall.get(0).substring(0,5) + "----" + teamPrecision.get(0).substring(0,5) + "----" + teamFScore.get(0).substring(0,5) + "----" + "\n"
                + "----" + 6 + "----" + teamRecall.get(1).substring(0,5) + "----" + teamPrecision.get(1).substring(0,5) + "----" + teamFScore.get(1).substring(0,5) + "----" + "\n"
                + "----" + 9 + "----" + teamRecall.get(2).substring(0,5) + "----" + teamPrecision.get(2).substring(0,5) + "----" + teamFScore.get(2).substring(0,5) + "----" + "\n"
                + "----" + 12 + "----" + teamRecall.get(3).substring(0,5) + "----" + teamPrecision.get(3).substring(0,5) + "----" + teamFScore.get(3).substring(0,5) + "----" + "\n"
                + "----" + 15 + "----" + teamRecall.get(4).substring(0,5) + "----" + teamPrecision.get(4).substring(0,5) + "----" + teamFScore.get(4).substring(0,5) + "----" + "\n"
                + "\n" + "\n"
                + "No. of test reports: " + testBugs.size() + "\n"
                + "Top 1: " + avgTopNForSortedResult.get(0).substring(0,5) + "\n"
                + "Top 2: " + avgTopNForSortedResult.get(1).substring(0,5) + "\n"
                + "Top 3: " + avgTopNForSortedResult.get(2).substring(0,5) + "\n"
                + "Top 4: " + avgTopNForSortedResult.get(3).substring(0,5) + "\n"
                + "Top 5: " + avgTopNForSortedResult.get(4).substring(0,5) + "\n"
                + "\n" + "\n"
                + "Average Effectiveness: " + "\n"
                + "Top 1: " + avgEfficiencyList.get(0).substring(0,5) + "\n"
                + "Top 2: " + avgEfficiencyList.get(1).substring(0,5) + "\n"
                + "Top 3: " + avgEfficiencyList.get(2).substring(0,5) + "\n"
                + "Top 4: " + avgEfficiencyList.get(3).substring(0,5) + "\n"
                + "Top 5: " + avgEfficiencyList.get(4).substring(0,5) + "\n"
                + "\n" + "\n"
                + "Mean Reciprocal Rank: " + "\n"
                + "Top 1: " + avgMRRList.get(0).substring(0,5) + "\n"
                + "Top 2: " + avgMRRList.get(1).substring(0,5) + "\n"
                + "Top 3: " + avgMRRList.get(2).substring(0,5) + "\n"
                + "Top 4: " + avgMRRList.get(3).substring(0,5) + "\n"
                + "Top 5: " + avgMRRList.get(4).substring(0,5) + "\n";*/

        File file = new File("C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\output");
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
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\edIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        DirectoryReader directoryReader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher (directoryReader);

        BooleanQuery.setMaxClauseCount(16384);

        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String s: testBug.getListOfKeywords())
        {
            bq.add(new BooleanClause(new TermQuery(new Term("content", s)), BooleanClause.Occur.SHOULD));
        }

        //bq.add(new BooleanClause(new TermQuery(new Term("content", "*")), BooleanClause.Occur.SHOULD));

        //QueryParser qp = new QueryParser("content", new StandardAnalyzer());

        //System.out.println(convertListToQuery(testBugs.get(0).getListOfKeywords()));

        //Query query = qp.parse(convertListToQuery(testBugs.get(0).getListOfKeywords())); //syntax

        TopDocs results = searcher.search(bq.build(), experiencedDevelopers.size());

        for(ScoreDoc scoreDoc: results.scoreDocs)
        {
            Document document = searcher.doc(scoreDoc.doc);
            eds.add(document.get("name"));
            /*System.out.println(document.get("name"));
            System.out.println(scoreDoc.doc);
            System.out.println(scoreDoc.score);
            System.out.println("----------");*/
        }

        directoryReader.close();
    }

    private void readNedIndex (Bug testBug) throws IOException, ParseException {
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\nedIndex";

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

        //bq.add(new BooleanClause(new TermQuery(new Term("content", "*")), BooleanClause.Occur.SHOULD));

        TopDocs results = searcher.search(bq.build(), newExperiencedDevelopers.size());

        for(ScoreDoc scoreDoc: results.scoreDocs)
        {
            Document document = searcher.doc(scoreDoc.doc);
            neds.add(document.get("name"));
            //System.out.println("------------------" + neds.size());
            /*System.out.println(document.get("name"));
            System.out.println(scoreDoc.doc);
            System.out.println(scoreDoc.score);*/
        }

        directoryReader.close();
    }

    private void readFgIndex (Bug testBug) throws IOException, ParseException {
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\fgIndex";

        Directory dir = FSDirectory.open(Paths.get(indexPath));

        DirectoryReader directoryReader = DirectoryReader.open(dir);

        IndexSearcher searcher = new IndexSearcher (directoryReader);

        BooleanQuery.setMaxClauseCount(16384);

        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String s: testBug.getListOfKeywords())
        {
            bq.add(new BooleanClause(new TermQuery(new Term("content", s)), BooleanClause.Occur.SHOULD));
        }

        //bq.add(new BooleanClause(new TermQuery(new Term("content", "*")), BooleanClause.Occur.SHOULD));

        TopDocs results = searcher.search(bq.build(), freshGraduates.size());
        //System.out.println(results.totalHits.value);

        for(ScoreDoc scoreDoc: results.scoreDocs)
        {
            Document document = searcher.doc(scoreDoc.doc);
            //System.out.println(document);
            fgs.add(document.get("name"));
            System.out.println("read " + document.get("name"));
            //System.out.println("------------------" + fgs.size());
            /*System.out.println(document.get("name"));
            System.out.println(scoreDoc.doc);
            System.out.println(scoreDoc.score);*/
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
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\edIndex";

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
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\nedIndex";

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
        String indexPath = "C:\\Users\\Hp\\Desktop\\TestBT\\src\\files\\fgIndex";

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
}
