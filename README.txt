COMS E6111 ADVANCED DATABASE SYSTEMS
PROJECT 1

a)TEAM:  NICOLO PIZZOFERRATO(nvp2015)
         NATASHA S KENKRE(nsk2141)

b)The following is a list of all the files that we are submitting:
> source files : FeedbackBing.java(main file)
                 conf/WeightConstants.java
                 domain/AggregateDocumentData.java
                 domain/Document.java
                 util/BingApiUtil.java
                 util/DocumentParsingUtil.java
                 util/DocumentRetrievalUtil.java
                 util/DoubleValidatorUtil.java
                 util/QueryTermUtil.java
> libraries (jars):
                 lib/commons-codec-1.10.jar
                 lib/jsoup-1.8.1.jar
                 lib/org.json-20120521.jar
> build files:
                 build.xml
> text files:
                 README.txt
                 transcript_columbia
                 transcript_gates
                 transcript_musk


c)The language used for the implementation of this code is Java. The main file that should be executed is FeedbackBing.java. 

Usage:

$ ant
$ java -cp "FeedbackBing.jar:lib/*" FeedbackBing <client-key> <precision> <'query'>

Note: In the three cases documented in the transcript, precision has been specified as 0.9 as requested.

d) Since this is a simple program, most of the code is written as static methods. The main class contains all the program flow logic
   and some of the logic for selecting new terms, but it is simply picking the best two words (if possible) from the ordering provided
   by the AggregateDocumentData class. This is the class which handles the bulk of the calculations involving term scoring as outlined
   in the text (specifically, in Chapter 6). Other utility classes handle things like requests to the Bing API, input validation, and
   parsing of documents.

e) The main algorithm for term ranking is implemented in the AggregateDocumentData class, beginning with the "refreshAggregateData" method.
   Upon adding new documents specified as relevant or not, we first compute the document frequency (# of documents containing the term) for
   each term. Then we compute the inverse document frequency (log[(total # of documents) / (term document frequency)]) for each term. Finally,
   we iterate through each document, count the terms in each one, and compute (term frequency within the document) * (IDF of the term) --
   or as we know from class and the text, TF*IDF. If the document is relevant, we add this score to the total score of the term across
   all documents; otherwise, we subtract it. We then have an aggregate score for each term. If we sort this list in descending order, the
   earlier in the list the term is, the more likely it is that the term will improve the precision of the query (as long as the score is
   non-negative). If non-negative terms exist, we add up to two and query Bing with the augmented terms.

   In addition (using the textbook section regarding zone weighting as inspiration), we added some term weighting to lessen the score of the
   titles relative to the content. The idea is that things like 'Wikipedia' (which appears in all titles coming from that domain) would not
   have as much influence as something actually included in the content body.

f)Bing Search Account Key: gBjyBpDpbFVENUIq/YsYR813f7PuEIkpcqAsqVq45eY

