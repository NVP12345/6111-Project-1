package domain;

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
        for (Document document : allDocumentsById.values()) {
            addQueryResultItemToAggregateData(document);
        }
        calculateInverseDocumentFrequencies();
        calculateTermWeights();
    }

    private void addQueryResultItemToAggregateData(Document document) {
        for (String word : document.getTermFrequencies().keySet()) {
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
            Map<String, Double> documentTermWeights = new HashMap<String, Double>();
            Map<String, Integer> termFrequencies = document.getTermFrequencies();
            for (String term : termFrequencies.keySet()) {
                double termWeight = termFrequencies.get(term) * inverseDocumentFrequencies.get(term);
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
