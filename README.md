# msci-541-f23-hw5-Adelpzk<br />
msci-541-f23-hw5-Adelpzk created by GitHub Classroom<br />
Adel Pazoki Toroudi's Search Engine Project<br />

To run the UserSearch program, first, we must run the IndexEngine program. The instructions to run the IndexEngine program: <br />
<br />
In the root Directory Run `javac -d out -cp src src/IndexEngine/*.java src/utilities/*.java`
<br />
To run the program: `java -Xmx8g -cp "out:out/resources" IndexEngine.IndexEngine <'Path to the zipped file'> <'Path to the directory to save the file'>`<br />
<br />
or you Can simply choose to run the script provide. In the CommandLine run: `./build.sh`
<br />
<br />

To run the UserSearch program:<br />
Then inside the root directory Run `javac -d out src/UserSearch/*.java src/utilities/*.java src/GetDoc/*.java src/BM25/*.java`<br />
To Run the the program: `java -cp "out:out/resources" UserSearch.UserSearch `<br />
