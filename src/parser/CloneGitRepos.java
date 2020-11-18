package parser;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class CloneGitRepos
{
    private String owner;
    private String url;
    private String directory;

    public CloneGitRepos(String directory) throws IOException {
        this.directory = directory;
        gitReposCloning();
    }

    private void cloneRepo()
    {
        Git git = Git.cloneRepository()
                .setURI(url)
                .setDirectory(new File(directory + "\\" + owner + "\\" + url.substring(url.lastIndexOf('/')+1, url.length())))
                .call();
    }

    public void gitReposCloning() throws IOException
    {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get("src/files/GitRepos")));

        StringTokenizer st = new StringTokenizer(data);
        while (st.hasMoreTokens())
        {
            String s = st.nextToken();
            if (s.length()>=19)
            {
                if(s.substring(0,19).equals("https://github.com/"))
                {
                    url = s;
                    cloneRepo();
                }
                else
                {
                    owner = s;
                }
            }
            else
            {
                owner = s;
            }
        }
    }
}