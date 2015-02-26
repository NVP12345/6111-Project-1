import domain.AggregateDocumentData;
import domain.Document;
import util.BingApiUtil;
import util.DoubleValidatorUtil;
import util.QueryTermUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class FeedbackBing {

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java FeedbackBing <precision> <'query'>");
            System.exit(1);
        }

        double targetPrecision = -1;
        if (DoubleValidatorUtil.isStringParsableToDouble(args[0])) {
            double value = Double.parseDouble(args[0]);
            if (value >= 0 && value <= 1) {
                targetPrecision = value;
            }
        }
        if (targetPrecision == -1) {
            System.out.println("Precision must be a decimal between 0 and 1, inclusive" + args[0]);
            System.exit(1);
        }

        List<String> orderedQueryTerms = new ArrayList<String>(Arrays.asList(args[1].toLowerCase().split("\\s")));
        Set<String> queryTermSet = new HashSet<String>(orderedQueryTerms);
        AggregateDocumentData aggregateDocumentData = new AggregateDocumentData();

        while (true) {
            System.out.println("Parameters:");
            System.out.format("Client key\t= %s\n", BingApiUtil.BING_ACCOUNT_KEY);
            System.out.format("Query\t\t= %s\n", QueryTermUtil.buildQueryStringFromTerms(orderedQueryTerms));
            System.out.format("Precision\t= %f\n", targetPrecision);
            List<Document> currentDocuments = BingApiUtil.getBingQueryResults(orderedQueryTerms, aggregateDocumentData.getAllDocumentsById());
            System.out.println("Bing Search Results:\n======================");
            promptForRelevance(currentDocuments);
            double currentPrecision = getCurrentPrecision(currentDocuments);

            System.out.format(
                    "======================\n" +
                    "FEEDBACK SUMMARY\n" +
                    "Query %s\n" +
                    "Precision %f\n",
                    QueryTermUtil.buildQueryStringFromTerms(orderedQueryTerms), currentPrecision
            );

            if (currentPrecision >= targetPrecision) {
                System.out.format("Desired precision reached, done");
                System.exit(0);
            }
            if (currentPrecision == 0) {
                System.out.println("Precision is at 0. Terminating.");
                System.exit(1);
            }

            System.out.format("Still below the desired precision of %f.\nIndexing results...\n", targetPrecision);
            List<String> newQueryTerms = determineAugmentedQueryTerms(queryTermSet, aggregateDocumentData);
            String newQueryTermMessage = "Augmenting by  ";
            for (String newQueryTerm : newQueryTerms) {
                newQueryTermMessage += newQueryTerm + " ";
                queryTermSet.add(newQueryTerm);
            }

            if (newQueryTerms.isEmpty()) {
                System.out.println("Below desired precision, but can no longer augment the query");
                System.exit(1);
            }

            orderedQueryTerms.addAll(newQueryTerms);
            System.out.println(newQueryTermMessage);
        }

    }

    private static void promptForRelevance(Collection<Document> documents) {
        int documentNumberCounter = 1;
        for (Document document : documents) {
            System.out.println("Result " + documentNumberCounter++);
            document.setRelevant(promptForRelevance(document));
        }
    }

    private static boolean promptForRelevance(Document document) {
        System.out.println(document);
        while (true) {
            System.out.print("Relevant (Y/N)? ");
            String input = IN.nextLine().trim().toLowerCase();
            if ("y".equals(input)) {
                return true;
            }
            if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static double getCurrentPrecision(Collection<Document> documents) {
        int relevantResults = 0;
        for (Document document : documents) {
            if (document.getRelevant()) {
                ++relevantResults;
            }
        }
        return relevantResults / (double) documents.size();
    }

    private static List<String> determineAugmentedQueryTerms(Set<String> currentQueryTerms, AggregateDocumentData aggregateDocumentData) {
        aggregateDocumentData.refreshAggregateData();
        Map<String, Double> aggregateTermWeights = aggregateDocumentData.getAggregateTermWeights();
        List<String> allOrderedQueryTerms = orderQueryTerms(aggregateTermWeights.keySet(), aggregateTermWeights);

        Iterator<String> queryTermIterator = allOrderedQueryTerms.iterator();
        List<String> newTerms = new LinkedList<String>();
        boolean nonNegativeTermWeight = true;
        while (queryTermIterator.hasNext() && nonNegativeTermWeight && newTerms.size() < 2) {
            String newTerm = queryTermIterator.next();
            if ( ! currentQueryTerms.contains(newTerm) ) {
                double aggregateTermWeight = aggregateTermWeights.get(newTerm);
                if (aggregateTermWeight >= 0) {
                    newTerms.add(newTerm);
                } else {
                    nonNegativeTermWeight = false;
                }
            }
        }

        if (newTerms.size() == 0) {
            System.out.println("Could not determine additional terms to improve relevance. Terminating.");
            System.exit(1);
        }

        return newTerms;
    }

    private static List<String> orderQueryTerms(Set<String> queryTerms, final Map<String, Double> aggregateTermWeights) {
        List<String> orderedQueryTerms = new ArrayList<String>(queryTerms);
        Collections.sort(
                orderedQueryTerms,
                new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        return aggregateTermWeights.get(o2).compareTo(aggregateTermWeights.get(o1));
                    }
                }
        );
        return orderedQueryTerms;
    }
}
