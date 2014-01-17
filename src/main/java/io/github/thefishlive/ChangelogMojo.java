package io.github.thefishlive;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * Wraps a jar in a Windows executable.
 *
 * @goal changelog
 * @phase compile
 */
public class ChangelogMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	MavenProject project;

 	/**
 	 * @component
	 * @readonly
 	 */
	MavenProjectHelper projectHelper;
	
	/**
	 * @parameter default-value="${project.artifactId}-${project.version}-changelog.txt"
	 * @readonly
	 */
	String changelogName;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		Git git = null;
		
		try {
			git = Git.open(project.getBasedir());
		} catch (IOException e) {
			return;
		}
		
		File changelogFile = new File(project.getBasedir() + File.separator + project.getBuild().getDirectory() + File.separator + "changelog" + File.separator + changelogName);
		
		if (!changelogFile.getParentFile().exists()) {
			try {
				if (!changelogFile.getParentFile().mkdir()) {
					throw new IOException("Could not create changelog directory");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Resource resource = new Resource();
		resource.setDirectory("." + File.separator + project.getBuild().getDirectory() + File.separator + "changelog" + File.separator);
		resource.addInclude("*-changelog.txt");
		project.addResource(resource);
		
		LogCommand log = git.log();
		FileWriter writer = null;
		
		try {
			Iterable<RevCommit> commits = log.call();
			writer = new FileWriter(changelogFile);
			
			for (RevCommit commit : commits) {
				writer.write(commit.getShortMessage() + "\n");
			}
			
		} catch (Exception e) {
			throw new MojoExecutionException("Error calling log command", e);
		} finally {
			if (writer != null) {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		getLog().info("Generated changelog (" + changelogFile + ")");
	}

}
