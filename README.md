# Java OpenCV Based Ballot Paper Counting System
This is a balot paper counting system for Standard Sri Lankan ballot papers implemented using openCV API for Java. 

## Requirements
* Java 7+
* Maven 3 for building
* openCV library files for your host system

## Installation
1. Clone this repository or download it
2. Build the project using maven (use `mvn clean install` command on the root directory of the project)
3. Once build is complee the application will be packaged into a jar file inside the target folder
4. Run the following command on the root of the project directory
```
java -jar target\ballot-counter-1.0-SNAPSHOT.jar -Djava.library.path="<path to your openCV libs>"
```
## Directory Structure
The application makes use of standard set of folders & files that are expected to be present at the current directory the app is run on. They are as follows:
* `balotpapers` folder - this folder holds the image files of the ballot papers that needs to be counted
* `config_images` folder - this folder holds the file `pref_vote_template.jpg` which is the unmarked template for preference votes
* `config_images\parties` folder - this folder holds template images for each party's symbol. 
