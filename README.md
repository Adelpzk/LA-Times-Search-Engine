# LA Times Search Engine

Welcome to the LA Times Search Engine project by Adel Pazoki Toroudi. This search engine is designed to retrieve information from LA Times documents spanning the years 1989 and 1990. The engine incorporates query-biased snippets and showcases the top 10 search results.

# Video Demo: 

[![IMAGE ALT TEXT HERE](https://img.youtube.com/vi/3NtOeLSpI-w/0.jpg)](https://www.youtube.com/watch?v=3NtOeLSpI-w)

## Indexing LA Times Documents

### IndexEngine Program

To initiate the indexing process, follow these steps:

1. In the root directory, compile the IndexEngine program:

  ```bash
  javac -d out -cp src src/IndexEngine/*.java src/utilities/*.java
  ```

2. Run the IndexEngine program:

  ```bash
  java -Xmx8g -cp "out:out/resources" IndexEngine.IndexEngine <'Path to the zipped file'> <'Path to the directory to save the file'>
  ```
Alternatively, use the provided script: ./build.sh


## Running the LA Times Search Engine

### UserSearch Program

To execute the UserSearch program:

1. In the root directory, compile the necessary files:

  ```bash
  javac -d out src/UserSearch/.java src/utilities/.java src/GetDoc/.java src/BM25/.java
  ```

2. Run the UserSearch program:

  ```bash
  java -cp "out:out/resources" UserSearch.UserSearch
  ```

### Search Engine Features

- **Query-Biased Snippets:** The search results include query-biased snippets for enhanced content overview.

- **Top 10 Results:** The engine displays the top 10 search results, providing a concise and relevant output.


Feel free to explore the LA Times Search Engine and delve into the rich content of documents from 1989 and 1990!

