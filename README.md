# Battlecode 2021 Team "Ctrl Alt Defeat"

### Run these commands frequently
1. `git fetch`
2. `git pull origin master`

or in IntelliJ, VCS -> Update Project -> Merge the incoming changes into the current branch

3. `./gradlew update`
4. `./gradlew build`

### For a new branch
- `git checkout -b <branchname>`
- `git push origin <branchname>`

## Project Structure

- `README.md`
    This file.
- `build.gradle`
    The Gradle build file used to build and run players.
- `src/`
    Player source code.
- `test/`
    Player test code.
- `client/`
    Contains the client. The proper executable can be found in this folder (don't move this!)
- `build/`
    Contains compiled player code and other artifacts of the build process. Can be safely ignored.
- `matches/`
    The output folder for match files.
- `maps/`
    The default folder for custom maps.
- `gradlew`, `gradlew.bat`
    The Unix (OS X/Linux) and Windows versions, respectively, of the Gradle wrapper. These are nifty scripts that you can execute in a terminal to run the Gradle build tasks of this project. If you aren't planning to do command line development, these can be safely ignored.
- `gradle/`
    Contains files used by the Gradle wrapper scripts. Can be safely ignored.


### Useful Commands

- `./gradlew run`
    Runs a game with the settings in gradle.properties
- `./gradlew update`
    Update to the newest version! Run every so often
- `./gradlew build`
    Run this after updating to build the project
