package domain;

import conf.WeightConstants;

import java.util.HashMap;
import java.util.Map;

public class AggregateDocumentData {

    private final Map<String, Document> allDocumentsById; // Document ID -> Document
    private final Map<String, Integer> documentTermFrequencies; // Term -> # of documents with term
    private final Map<String, Double> inverseDocumentFrequencies;  // Term -> IDF for term
    private final Map<String, Double> aggregateTermWeights; // Term -> Score

    public AggregateDocumentData() {
        allDocumentsById = new HashMap<String, Document>();
        documentTermFrequencies = new HashMap<String, Integer>();
        inverseDocumentFrequencies = new HashMap<String, Double>();
        aggregateTermWeights = new HashMap<String, Double>();
    }

    public Map<String, Document> getAllDocumentsById() {
        return allDocumentsById;
    }

    public Map<String, Double> getAggregateTermWeights() {
        return aggregateTermWeights;
    }

    public void refreshAggregateData() {
        clearAggregateData();

        /*
        For 'valid' documents only, compute the document frequency for each term
        */
        int numberOfValidDocuments = 0;
        for (Document document : allDocumentsById.values()) {
            if (document.isValid()) {
                incrementDocumentTermFrequencyForTerms(document);
                ++numberOfValidDocuments;
            }
        }

        /*
        Compute IDF for each term
        */
        calculateInverseDocumentFrequencies(numberOfValidDocuments);

        /*
        Compute aggregate TF*IDF for each term
        */
        calculateTermWeights();
    }

    private void clearAggregateData() {
        documentTermFrequencies.clear();
        inverseDocumentFrequencies.clear();
        aggregateTermWeights.clear();
    }

    private void incrementDocumentTermFrequencyForTerms(Document document) {
        for (String word : document.getAllWords()) {
            if ( ! documentTermFrequencies.containsKey(word) ) {
                documentTermFrequencies.put(word, 1);
            } else {
                documentTermFrequencies.put(word, documentTermFrequencies.get(word) + 1);
            }
        }
    }

    private void calculateInverseDocumentFrequencies(int numberOfValidDocuments) {
        for (String term : documentTermFrequencies.keySet()) {
            inverseDocumentFrequencies.put(term, Math.log10(numberOfValidDocuments / (double) documentTermFrequencies.get(term)));
        }
    }

    private void calculateTermWeights() {
        for (String documentId : allDocumentsById.keySet()) {
            Document document = allDocumentsById.get(documentId);
            if (document.isValid()) {
                Map<String, Integer> titleTermFrequencies = document.getTitleTermFrequencies();
                Map<String, Integer> contentTermFrequencies = document.getContentTermFrequencies();
                for (String term : document.getAllWords()) {
                    double termWeight = 0;

                    /*
                    Compute weighted term frequency
                    */
                    if (titleTermFrequencies.containsKey(term)) {
                        termWeight += titleTermFrequencies.get(term) * WeightConstants.TITLE_WEIGHT;
                    }
                    if (contentTermFrequencies.containsKey(term)) {
                        termWeight += contentTermFrequencies.get(term) * WeightConstants.CONTENT_WEIGHT;
                    }

                    /*
                    Compute TF*IDF
                    */
                    termWeight *= inverseDocumentFrequencies.get(term);

                    /*
                    Aggregate TF*IDF for each term
                    */
                    double aggregateTermWeightValue = document.getRelevant() ? termWeight : -1 * termWeight;
                    if ( ! aggregateTermWeights.containsKey(term) ) {
                        aggregateTermWeights.put(term, aggregateTermWeightValue);
                    } else {
                        aggregateTermWeights.put(term, aggregateTermWeights.get(term) + aggregateTermWeightValue);
                    }
                }
            }
        }
    }

}
