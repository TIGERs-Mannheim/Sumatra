# Contributing to Sumatra

The development follows a **feature branch workflow**: create a feature branch from the *master* branch, 
implement the code, and then submit a merge request. 

As a consequence:
* There is one long-lived branch *master*.
* The active development takes place only on branches
* Stable releases are tagged from the *master*
* Short-lived *feature branches* are used to develop features, fix bugs and make changes.

## Issue Management

### Create a new issue
 * Give it a sound short name
 * Give as much details as you think are required so that anybody can understand what the task is
 * Assign a Milestone, usually either ASAP (is needed for other tasks), next Event or RoboCup 2050
 * Label it *Implement* if you plan to start with it yourself
 * Label it *Open Task* if someone else can work on it (you just submit a bug or feature request)
 * Label it with an appropriate category like *KI - Offensive* for Offensive Tasks or *Sumatra* for framework specific tasks
 * Mention related people using @User
 * Assign it to yourself or someone else, if you labeled it *Implement*
 
### Issue flow
Issues are managed using the Gitlab Board. There are 4 columns in our board:
 1. **Open Tasks**: The issue is not assigned and nobody plans to work on it in near future. Check out this column if you are looking for new tasks.
 1. **Implement**: Someone is currently working on this issue or plans to do so in near future
 1. **Review**: The issue was resolved, but needs review or feedback from somebody. The issue should be assigned to the reviewer.
 1. **Closed**: The issue was resolved and merged to master.
 
Issues usually move from left to right, but moving it backwards is also allowed. For example, after a review, the author of the issue may decide to move it back to Implement when there is a lot to do.
Issues that are not listed on the board can be considered to be in the *Backlog*. This means, they are currently not relevant, but may be considered later (next year). 

## Basic Workflow
The steps below give a more detailed view of the development process.

1. Create an issue in Gitlab as described above
1. Create a feature branch from the issue and commit on it (let GitLab create the feature branch for you to have consistent naming)
1. Fix the bug or implement the feature
1. Clean up your commits using rebase if you like (needs advanced GIT knowledge. Do not blindly execute a rebase!) 
1. Pull the *master* branch
1. merge your *feature branch* with it 
1. Push the commits on the *feature branch* to GitLab 
1. Create a merge request to:
 * Have CI confirm that the code builds and the tests pass
 * Sonarqube analyzes the code quality
 * Allow colleagues to review your changes. To keep the review of reasonable size, a merge request should only contain **ONE** issue (e.g., one bug fix, one feature, one refactoring step).
1. Let someone else merge the changes into *master* and delete the feature branch (let GitLab do the merge)

### Staging Process
Some tasks may require many different features to implement and intermediate code may not be suitable for the *master*.
 For this scenario, we use staging branches. All staging branches start with *staging/*. Feature development takes place as described above, but instead of creating
 merge request for the *master*, you create a merge request for a staging branch. When the task is ready for *master*, it will be merged from the staging branch. 

### Enforcement of the Workflow
To enforce the above workflow, the following rules have been setup on GitLab:
1. Protection of the *master* and *staging/** branches
 - It is not possible to directly push onto the *master* or *staging/** branches
 - To incorporate your changes, you must use merge requests
1. Merge requests require that all relevant parties sign off
 - The Jenkins build is successful and the tests pass
 - Take Sonar analysis results into consideration
 - At least one colleague is required to approve the merge request. The reviewers may pose questions in the form of discussions pertaining to the relevant code. 
   Only once all the discussions have been marked as resolved, the merge request can be accepted. Discussions should not be closed by the 
   team member who fixed the issue.

