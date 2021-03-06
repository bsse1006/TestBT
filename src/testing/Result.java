package testing;

import profiles.Bug;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Result
{
    List<String> eds;
    List<String> neds;
    List<String> fgs;
    Bug testBug;
    int teamSizePerDeveloper;
    /*List<String> precision = new ArrayList<>();
    List<String> recall = new ArrayList<>();
    List<String> fScore = new ArrayList<>();
    List<String> topNResultListForSortedDevs = new ArrayList<>();*/

    List<String> sortedDevs = new ArrayList<>();

    double recall;

    double rank;

    int match = 0;

    public Result(List<String> eds, List<String> neds, List<String> fgs, Bug testBug, int teamSizePerDeveloper) {
        this.eds = eds;
        this.neds = neds;
        this.fgs = fgs;
        this.testBug = testBug;
        this.teamSizePerDeveloper = teamSizePerDeveloper;
        rank = teamSizePerDeveloper*3 + 1;
        sortDevs();
        calcRecall();
        rankSum();
    }

    private void rankSum()
    {
        for (int i=0; i<teamSizePerDeveloper*3; i++)
        {
            if(testBug.getListOfSolvers().contains(sortedDevs.get(i)))
            {
                rank = i + 1;
                match = 1;
                break;
            }
        }
    }

    private void sortDevs()
    {
        for (int i=0; ; i++)
        {
            if (eds.size()>i&&sortedDevs.size()<teamSizePerDeveloper*3)
            {
                sortedDevs.add(eds.get(i));
            }

            if (neds.size()>i&&sortedDevs.size()<teamSizePerDeveloper*3)
            {
                sortedDevs.add(neds.get(i));
            }

            if (fgs.size()>i&&sortedDevs.size()<teamSizePerDeveloper*3)
            {
                sortedDevs.add(fgs.get(i));
            }

            if (sortedDevs.size()>=teamSizePerDeveloper*3)
            {
                break;
            }
        }

        //sortedDevs = eds;
        /*if(testBug.getSeverity().equals("high"))
        {
            sortedDevs = eds;
        }
        else if(testBug.getSeverity().equals("medium"))
        {
            sortedDevs = neds;
        }
        else if(testBug.getSeverity().equals("low"))
        {
            sortedDevs = fgs;
        }*/
    }

    public double getRecall() {
        return recall;
    }

    private void calcRecall ()
    {
        double temp = 0;

        for(String dev: sortedDevs)
        {
            if(testBug.getListOfSolvers().contains(dev))
            {
                temp = temp + 1;
            }
        }

        recall = temp/testBug.getListOfSolvers().size();
    }

    public List<String> getSortedDevs() {
        return sortedDevs;
    }

    public double getRank() {
        return rank;
    }

    public int getMatch() {
        return match;
    }
}
