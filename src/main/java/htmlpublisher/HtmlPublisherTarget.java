package htmlpublisher;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.AbstractItem;
import hudson.model.AbstractProject;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Action;
import hudson.model.DirectoryBrowserSupport;
import hudson.model.ProminentProjectAction;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Descriptor;
import hudson.util.ListBoxModel;
import hudson.Extension;


import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * A representation of an HTML directory to archive and publish.
 *
 * @author Mike Rooney
 *
 */
public class HtmlPublisherTarget extends AbstractDescribableImpl<HtmlPublisherTarget> {
    /**
     * The name of the report to display for the build/project, such as "Code Coverage"
     */
    private final String reportName;

    /**
     * The path to the HTML report directory relative to the workspace.
     */
    private final String reportDir;

    /**
     * The file[s] to provide links inside the report directory.
     */
    private final String reportFiles;

    /**
     * If true, archive reports for all successful builds, otherwise only the most recent.
     */
    private final boolean keepAll;

    /**
     * The result of the build if this plugin encounters a failure (e.g. a missing report)
     */
    private String result = Result.FAILURE.toString();
    /**
     * The name of the file which will be used as the wrapper index.
     */
    private final String wrapperName = "htmlpublisher-wrapper.html";

    @DataBoundConstructor
    public HtmlPublisherTarget(String reportName, String reportDir, String reportFiles, boolean keepAll, String result) {
        this.reportName = reportName;
        this.reportDir = reportDir;
        this.reportFiles = reportFiles;
        this.keepAll = keepAll;
        this.result = result;
    }

    public String getReportName() {
        return this.reportName;
    }

    public String getReportDir() {
        return this.reportDir;
    }

    public String getReportFiles() {
        return this.reportFiles;
    }

    public boolean getKeepAll() {
        return this.keepAll;
    }
    
    public Result getResult() {
    	return Result.fromString(this.result);
    }

    public String getSanitizedName() {
        String safeName = this.reportName;
        safeName = safeName.replace(" ", "_");
        return safeName;
    }

    public String getWrapperName() {
        return this.wrapperName;
    }
    
    public ListBoxModel doFillResultItems() {
        System.out.println("Test 1a");
        Logger logger = Logger.getLogger("Foo");
        logger.info("Test 1b");
        ListBoxModel m = new ListBoxModel();
        m.add(Result.FAILURE.toString());
        m.add(Result.NOT_BUILT.toString());
        m.add(Result.SUCCESS.toString());
        m.add(Result.UNSTABLE.toString());
        m.add(Result.ABORTED.toString());
        m.get(0).selected = true;
        return m;
      }

    public FilePath getArchiveTarget(AbstractBuild build) {
        return new FilePath(this.keepAll ? getBuildArchiveDir(build) : getProjectArchiveDir(build.getProject()));
    }

    /**
     * Gets the directory where the HTML report is stored for the given project.
     */
    private File getProjectArchiveDir(AbstractItem project) {
        return new File(new File(project.getRootDir(), "htmlreports"), this.getSanitizedName());
    }
    /**
     * Gets the directory where the HTML report is stored for the given build.
     */
    private File getBuildArchiveDir(Run run) {
        return new File(new File(run.getRootDir(), "htmlreports"), this.getSanitizedName());
    }

    protected abstract class BaseHTMLAction implements Action {
        private HtmlPublisherTarget actualHtmlPublisherTarget;

        public BaseHTMLAction(HtmlPublisherTarget actualHtmlPublisherTarget) {
            this.actualHtmlPublisherTarget = actualHtmlPublisherTarget;
        }

        public String getUrlName() {
            return actualHtmlPublisherTarget.getSanitizedName();
        }

        public String getDisplayName() {
            String action = actualHtmlPublisherTarget.reportName;
            return dir().exists() ? action : null;
        }

        public String getIconFileName() {
            return dir().exists() ? "graph.gif" : null;
        }

        /**
         * Serves HTML reports.
         */
        public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
            DirectoryBrowserSupport dbs = new DirectoryBrowserSupport(this, new FilePath(this.dir()), this.getTitle(), "graph.gif", false);
            dbs.setIndexFileName(HtmlPublisherTarget.this.wrapperName); // Hudson >= 1.312
            dbs.generateResponse(req, rsp, this);
        }

        protected abstract String getTitle();

        protected abstract File dir();
        
        public ListBoxModel doFillResultItems() {
            System.out.println("Test 1a");
            Logger logger = Logger.getLogger("Foo");
            logger.info("Test 1b");
            ListBoxModel m = new ListBoxModel();
            m.add(Result.FAILURE.toString());
            m.add(Result.NOT_BUILT.toString());
            m.add(Result.SUCCESS.toString());
            m.add(Result.UNSTABLE.toString());
            m.add(Result.ABORTED.toString());
            m.get(0).selected = true;
            return m;
          }
    }

    public class HTMLAction extends BaseHTMLAction implements ProminentProjectAction {
        private final AbstractItem project;

        public HTMLAction(AbstractItem project, HtmlPublisherTarget actualHtmlPublisherTarget) {
            super(actualHtmlPublisherTarget);
            this.project = project;
        }

        @Override
        protected File dir() {
            if (this.project instanceof AbstractProject) {
                AbstractProject abstractProject = (AbstractProject) this.project;

                Run run = abstractProject.getLastSuccessfulBuild();
                if (run != null) {
                    File javadocDir = getBuildArchiveDir(run);

                    if (javadocDir.exists()) {
                        return javadocDir;
                    }
                }
            }

            return getProjectArchiveDir(this.project);
        }

        @Override
        protected String getTitle() {
            return this.project.getDisplayName() + " html2";
        }
        
        public ListBoxModel doFillResultItems() {
            System.out.println("Test 1a");
            Logger logger = Logger.getLogger("Foo");
            logger.info("Test 1b");
            ListBoxModel m = new ListBoxModel();
            m.add(Result.FAILURE.toString());
            m.add(Result.NOT_BUILT.toString());
            m.add(Result.SUCCESS.toString());
            m.add(Result.UNSTABLE.toString());
            m.add(Result.ABORTED.toString());
            m.get(0).selected = true;
            return m;
          }
    }

    public class HTMLBuildAction extends BaseHTMLAction {
        private final AbstractBuild<?, ?> build;

        public HTMLBuildAction(AbstractBuild<?, ?> build, HtmlPublisherTarget actualHtmlPublisherTarget) {
            super(actualHtmlPublisherTarget);
            this.build = build;
        }

        @Override
        protected String getTitle() {
            return this.build.getDisplayName() + " html3";
        }

        @Override
        protected File dir() {
            return getBuildArchiveDir(this.build);
        }
        
        public ListBoxModel doFillResultItems() {
            System.out.println("Test 1a");
            Logger logger = Logger.getLogger("Foo");
            logger.info("Test 1b");
            ListBoxModel m = new ListBoxModel();
            m.add(Result.FAILURE.toString());
            m.add(Result.NOT_BUILT.toString());
            m.add(Result.SUCCESS.toString());
            m.add(Result.UNSTABLE.toString());
            m.add(Result.ABORTED.toString());
            m.get(0).selected = true;
            return m;
          }
    }

    public void handleAction(AbstractBuild<?, ?> build) {
        // Add build action, if coverage is recorded for each build
        if (this.keepAll) {
            build.addAction(new HTMLBuildAction(build, this));
        }
    }

    public Action getProjectAction(AbstractProject project) {
        return new HTMLAction(project, this);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<HtmlPublisherTarget> {
        public String getDisplayName() { return ""; }
        
        
        public ListBoxModel doFillResultItems() {
            System.out.println("Test 1a");
            Logger logger = Logger.getLogger("Foo");
            logger.info("Test 1b");
            ListBoxModel m = new ListBoxModel();
            m.add(Result.FAILURE.toString());
            m.add(Result.NOT_BUILT.toString());
            m.add(Result.SUCCESS.toString());
            m.add(Result.UNSTABLE.toString());
            m.add(Result.ABORTED.toString());
            m.get(0).selected = true;
            return m;
          }
    }
}