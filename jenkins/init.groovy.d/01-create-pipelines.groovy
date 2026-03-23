import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.HudsonPrivateSecurityRealm
import jenkins.model.Jenkins
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition
import org.jenkinsci.plugins.workflow.job.WorkflowJob

def jenkins = Jenkins.instance

def adminUser = System.getenv('JENKINS_ADMIN_ID') ?: 'admin'
def adminPassword = System.getenv('JENKINS_ADMIN_PASSWORD') ?: 'admin'

if (!(jenkins.securityRealm instanceof HudsonPrivateSecurityRealm)) {
    def realm = new HudsonPrivateSecurityRealm(false)
    if (realm.getUser(adminUser) == null) {
        realm.createAccount(adminUser, adminPassword)
    }
    jenkins.setSecurityRealm(realm)
    def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
    strategy.setAllowAnonymousRead(false)
    jenkins.setAuthorizationStrategy(strategy)
}

// Local demo setup: disable CSRF crumbs to simplify API-triggered builds.
jenkins.setCrumbIssuer(null)

def repoPath = '/var/jenkins_home/repo'

def jobs = [
    '01-build-maven-builder': "${repoPath}/jenkins/pipelines/01-build-maven-builder.Jenkinsfile",
    '02-build-jdk-runtime'  : "${repoPath}/jenkins/pipelines/02-build-jdk-runtime.Jenkinsfile",
    '03-build-hello-world'  : "${repoPath}/jenkins/pipelines/03-build-hello-world.Jenkinsfile",
    '00-bootstrap-all'      : "${repoPath}/jenkins/pipelines/00-bootstrap-all.Jenkinsfile"
]

jobs.each { jobName, pipelinePath ->
    File jenkinsfile = new File(pipelinePath)
    if (!jenkinsfile.exists()) {
        println("[init] Pipeline file not found for ${jobName}: ${pipelinePath}")
        return
    }

    WorkflowJob job = jenkins.getItem(jobName)
    if (job == null) {
        job = jenkins.createProject(WorkflowJob, jobName)
        println("[init] Created job: ${jobName}")
    }

    job.setDefinition(new CpsFlowDefinition(jenkinsfile.text, true))
    job.save()
}

def bootstrap = jenkins.getItem('00-bootstrap-all') as WorkflowJob
if (bootstrap != null && (bootstrap.getLastBuild() == null || bootstrap.getLastBuild().getResult()?.toString() == 'FAILURE')) {
    bootstrap.scheduleBuild2(5)
    println('[init] Triggered first bootstrap build')
}

jenkins.save()