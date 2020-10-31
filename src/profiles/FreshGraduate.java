package profiles;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FreshGraduate
{
    private Developer developerCore;
    private List<String> listOfKeyWords = new ArrayList<>();

    public FreshGraduate(Developer developerCore) {
        this.developerCore = developerCore;
    }

    public Developer getDeveloperCore() {
        return developerCore;
    }

    public List<String> getListOfKeyWords() {
        return listOfKeyWords;
    }


}
