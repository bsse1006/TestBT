package profiles;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NewDeveloper
{
    private Developer developerCore;
    private List<String> listOfRepositoryKeywords;
    private List<String> listOfLibraryImports;

    public NewDeveloper(Developer developerCore, List<String> listOfRepositoryKeywords, List<String> listOfLibraryImports) {
        this.developerCore = developerCore;
        this.listOfRepositoryKeywords = listOfRepositoryKeywords;
        this.listOfLibraryImports = listOfLibraryImports;
    }

    public Developer getDeveloperCore() {
        return developerCore;
    }

    public List<String> getListOfRepositoryKeywords() {
        return listOfRepositoryKeywords;
    }

    public List<String> getListOfLibraryImports() {
        return listOfLibraryImports;
    }
}
