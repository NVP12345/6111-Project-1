package domain;

import conf.WeightConstants;

import java.util.HashMap;
import java.util.Map;

public class AggregateDocumentData {

    private final Map<String, Document> allDocumentsById;
    private final Map<String, Integer> documentTermFrequencies;
    private final Map<String, Double> inverseDocumentFrequencies;
    private final Map<String, Map<String, Double>> termWeightsByDocumentId;
    private final Map<String, Double> aggregateTermWeights;

    public AggregateDocumentData() {
        allDocumentsById = new HashMap<String, Document>();
        documentTermFrequencies = new HashMap<String, Integer>();
        inverseDocumentFrequencies = new HashMap<String, Double>();
        termWeightsByDocumentId = new HashMap<String, Map<String, Double>>();
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
        for (Document document : allDocumentsById.values()) {
            if (document.isValid()) {
                incrementDocumentTermFrequencyForTerms(document);
            }
        }
        calculateInverseDocumentFrequencies();
        calculateTermWeights();
    }

    private void clearAggregateData() {
        documentTermFrequencies.clear();
        inverseDocumentFrequencies.clear();
        termWeightsByDocumentId.clear();
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

    private void calculateInverseDocumentFrequencies() {
        int N = allDocumentsById.size();
        for (String term : documentTermFrequencies.keySet()) {
            inverseDocumentFrequencies.put(term, Math.log10(N / (double) documentTermFrequencies.get(term)));
        }
    }

    private void calculateTermWeights() {
        for (String documentId : allDocumentsById.keySet()) {
            Document document = allDocumentsById.get(documentId);
            if (document.isValid()) {
                Map<String, Double> documentTermWeights = new HashMap<String, Double>();
                Map<String, Integer> titleTermFrequencies = document.getTitleTermFrequencies();
                Map<String, Integer> contentTermFrequencies = document.getContentTermFrequencies();
                for (String term : document.getAllWords()) {
                    double termWeight = 0;
                    if (titleTermFrequencies.containsKey(term)) {
                        termWeight += titleTermFrequencies.get(term) * WeightConstants.TITLE_WEIGHT;
                    }
                    if (contentTermFrequencies.containsKey(term)) {
                        termWeight += contentTermFrequencies.get(term) * WeightConstants.CONTENT_WEIGHT;
                    }
                    termWeight *= inverseDocumentFrequencies.get(term);

                    documentTermWeights.put(term, termWeight);

                    double aggregateTermWeightValue = document.getRelevant() ? termWeight : -1 * termWeight;
                    if ( ! aggregateTermWeights.containsKey(term) ) {
                        aggregateTermWeights.put(term, aggregateTermWeightValue);
                    } else {
                        aggregateTermWeights.put(term, aggregateTermWeights.get(term) + aggregateTermWeightValue);
                    }
                }
                termWeightsByDocumentId.put(documentId, documentTermWeights);
            }
        }
    }

}
