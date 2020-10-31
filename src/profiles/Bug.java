package profiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Bug
{
    private String id;
    private LocalDate creationDate;
    private String product;
    private String component;
    private String severity;
    private List<String> listOfKeywords;
    private LocalDate solutionDate;
    private List<String> listOfSolvers = new ArrayList<>();

    public Bug()
    {

    }

    public Bug(String id, LocalDate creationDate, String product, String component, String severity) {
        this.id = id;
        this.creationDate = creationDate;
        this.product = product;
        this.component = component;
        this.severity = severity;
        this.severity = severity;
    }

    public String getId() {
        return id;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public String getProduct() {
        return product;
    }

    public String getComponent() {
        return component;
    }

    public String getSeverity() {
        return severity;
    }

    public List<String> getListOfKeywords() {
        return listOfKeywords;
    }

    public void setListOfKeywords(List<String> listOfKeywords) {
        this.listOfKeywords = listOfKeywords;
    }

    public LocalDate getSolutionDate() {
        return solutionDate;
    }

    public void setSolutionDate(LocalDate solutionDate) {
        this.solutionDate = solutionDate;
    }

    public List<String> getListOfSolvers() {
        return listOfSolvers;
    }

    @Override
    public String toString() {
        return "Bug{" +
                "id='" + id + '\'' +
                ", creationDate='" + creationDate + '\'' +
                ", product='" + product + '\'' +
                ", component='" + component + '\'' +
                ", severity='" + severity + '\'' +
                ", listOfKeywords=" + listOfKeywords +
                ", solutionDate='" + solutionDate + '\'' +
                '}';
    }
}
