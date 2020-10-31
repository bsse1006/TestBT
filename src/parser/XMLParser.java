package parser;

import naturalLanguageProcessor.TextProcessor;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import profiles.Bug;
import profiles.Developer;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XMLParser
{
    private Map<String, Bug> mapOfBugs = new HashMap<>();
    private Map<String, Developer> mapOfDevelopers = new HashMap<>();

    private Element extractRootElement (String filepath)
    {
        File inputFile = new File(filepath);
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            document = saxBuilder.build(inputFile);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element root = document.getRootElement();

        return root;
    }

    private void bugDescriptionParsing () throws Exception {
        Element root = extractRootElement("C:\\Users\\Hp\\Desktop\\fixedData.xml");

        List<Element> listOfBugs = root.getChildren();

        for (int iteratorForListOfBugs = 0; iteratorForListOfBugs < listOfBugs.size(); iteratorForListOfBugs++)
        {
            Element bugElement = listOfBugs.get(iteratorForListOfBugs);

            String severity = "";

            if(bugElement.getChild("bug_severity").getText().equals("blocker")
            ||bugElement.getChild("bug_severity").getText().equals("critical")
            ||bugElement.getChild("bug_severity").getText().equals("major")
            ||bugElement.getChild("bug_severity").getText().equals("S1")
            ||bugElement.getChild("bug_severity").getText().equals("s1")
            ||bugElement.getChild("bug_severity").getText().equals("S2")
            ||bugElement.getChild("bug_severity").getText().equals("s2"))
            {
                severity = "high";
            }
            else if(bugElement.getChild("bug_severity").getText().equals("normal")
            ||bugElement.getChild("bug_severity").getText().equals("minor")
            ||bugElement.getChild("bug_severity").getText().equals("S3")
            ||bugElement.getChild("bug_severity").getText().equals("s3"))
            {
                severity = "medium";
            }
            else if(bugElement.getChild("bug_severity").getText().equals("trivial")
            ||bugElement.getChild("bug_severity").getText().equals("enhancement")
            ||bugElement.getChild("bug_severity").getText().equals("S4")
            ||bugElement.getChild("bug_severity").getText().equals("s4"))
            {
                severity = "low";
            }

            Bug bugObject = new Bug(
                    bugElement.getChild("id").getText(),
                    LocalDate.parse(bugElement.getChild("creation_time").getText().substring(0,10)),
                    bugElement.getChild("product").getText(),
                    bugElement.getChild("component").getText(),
                    severity
            );

            //System.out.println(bugElement.getChild("bug_severity").getText());

            String summaryAndDescription = bugElement.getChild("short_desc").getText() + ' ' +
                    bugElement.getChild("thetext").getText();

            TextProcessor tp = new TextProcessor(summaryAndDescription);

            bugObject.setListOfKeywords(tp.getKeywords());

            mapOfBugs.put(bugObject.getId(), bugObject);
        }
    }

    private void bugSolutionParsing ()
    {
        Element root = extractRootElement("C:\\Users\\Hp\\Desktop\\fixedDataHistory.xml");

        List<Element> listOfBugs = root.getChildren();

        for (int iteratorForListOfBugs = 0; iteratorForListOfBugs < listOfBugs.size(); iteratorForListOfBugs++)
        {
            Element bugElement = listOfBugs.get(iteratorForListOfBugs);

            String bugId = bugElement.getChild("bug_id").getText();

            List<Element> listOfElements = bugElement.getChildren();

            for (int iteratorForListOfElements = 0; iteratorForListOfElements < listOfElements.size(); iteratorForListOfElements++)
            {
                Element element = listOfElements.get(iteratorForListOfElements);

                if(element.getName().equals("element"))
                {
                    if (!element.getChild("what").getText().isEmpty())
                    {
                        LocalDate bugResolutionDate = LocalDate.parse(element.getChild("when").getText().substring(0,10));
                        String developerName = element.getChild("who").getText();

                        if (mapOfDevelopers.containsKey(developerName))
                        {
                            Developer alreadyCreatedDeveloper = mapOfDevelopers.get(developerName);

                            alreadyCreatedDeveloper.getListOfBugIds().add(bugId);

                            if(alreadyCreatedDeveloper.getStartDate().compareTo(bugResolutionDate)>0)
                            {
                                alreadyCreatedDeveloper.setStartDate(bugResolutionDate);
                            }
                        }
                        else
                        {
                            Developer developer = new Developer(developerName, bugResolutionDate);

                            developer.getListOfBugIds().add(bugId);

                            mapOfDevelopers.put(developer.getName(), developer);
                        }

                        if(mapOfBugs.get(bugId).getSolutionDate()==null)
                        {
                            mapOfBugs.get(bugId).setSolutionDate(bugResolutionDate);
                        }
                        else if(mapOfBugs.get(bugId).getSolutionDate().compareTo(bugResolutionDate)<0)
                        {
                            mapOfBugs.get(bugId).setSolutionDate(bugResolutionDate);
                        }

                        mapOfBugs.get(bugId).getListOfSolvers().add(developerName);
                    }

                    /*if (element.getChild("what").getText().equals("Resolution"))
                    {
                        LocalDate bugResolutionDate = LocalDate.parse(element.getChild("when").getText().substring(0,10));
                        String developerName = element.getChild("who").getText();

                        if (mapOfDevelopers.containsKey(developerName))
                        {
                            Developer alreadyCreatedDeveloper = mapOfDevelopers.get(developerName);

                            alreadyCreatedDeveloper.getListOfBugIds().add(bugId);

                            if(alreadyCreatedDeveloper.getStartDate().compareTo(bugResolutionDate)>0)
                            {
                                alreadyCreatedDeveloper.setStartDate(bugResolutionDate);
                            }
                        }
                        else
                        {
                            Developer developer = new Developer(developerName, bugResolutionDate);

                            developer.getListOfBugIds().add(bugId);

                            mapOfDevelopers.put(developer.getName(), developer);
                        }

                        if(mapOfBugs.get(bugId).getSolutionDate()==null)
                        {
                            mapOfBugs.get(bugId).setSolutionDate(bugResolutionDate);
                        }
                        else if(mapOfBugs.get(bugId).getSolutionDate().compareTo(bugResolutionDate)<0)
                        {
                            mapOfBugs.get(bugId).setSolutionDate(bugResolutionDate);
                        }

                        mapOfBugs.get(bugId).getListOfSolvers().add(developerName);
                    }*/
                }
            }
        }
    }

    public void parsing () throws Exception {
        bugDescriptionParsing();
        bugSolutionParsing();
    }

    public Map<String, Bug> getMapOfBugs() {
        return mapOfBugs;
    }

    public Map<String, Developer> getMapOfDevelopers() {
        return mapOfDevelopers;
    }

    @Override
    public String toString() {
        return "XMLParser{" +
                "mapOfBugs=" + mapOfBugs +
                ", mapOfDevelopers=" + mapOfDevelopers +
                '}';
    }
}
