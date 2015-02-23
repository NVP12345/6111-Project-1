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

public class Project1Main {

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        List<String> orderedQueryTerms = promptForQueryString();
        Set<String> queryTermSet = new HashSet<String>(orderedQueryTerms);
        double targetPrecision = promptForPrecision();
        AggregateDocumentData aggregateDocumentData = new AggregateDocumentData();

        while (true) {
            System.out.println("Executing query and parsing documents...");
            List<Document> currentDocuments = BingApiUtil.getBingQueryResults(orderedQueryTerms, aggregateDocumentData.getAllDocumentsById());
            promptForRelevance(currentDocuments);
            double currentPrecision = getCurrentPrecision(currentDocuments);
            if (currentPrecision >= targetPrecision) {
                System.out.format("Precision reached %f, which meets or exceeds target of %f. Current results:\n\n", currentPrecision, targetPrecision);
                for (Document document : currentDocuments) {
                    System.out.println(document);
                    System.out.print("\n");
                }
                System.out.println("Terminating.");
                System.exit(0);
            }
            if (currentPrecision == 0) {
                System.out.println("Precision is at 0. Terminating.");
                System.exit(1);
            }

            System.out.format("Precision is at %f, which does not meet target of %f. Determining terms to augment query...\n", currentPrecision, targetPrecision);
            List<String> newQueryTerms = determineAugmentedQueryTerms(queryTermSet, aggregateDocumentData);
            String newQueryTermMessage = "Adding ";
            boolean first = true;
            for (String newQueryTerm : newQueryTerms) {
                if (!first) {
                    newQueryTermMessage += "and ";
                } else {
                    first = false;
                }
                newQueryTermMessage += "\"" + newQueryTerm + "\" ";
                queryTermSet.add(newQueryTerm);
            }
            orderedQueryTerms = orderQueryTerms(queryTermSet, aggregateDocumentData.getAggregateTermWeights());
            newQueryTermMessage += "to the query. Current query is: " + QueryTermUtil.buildQueryStringFromTerms(orderedQueryTerms);
            System.out.println(newQueryTermMessage);
        }

    }

    private static List<String> promptForQueryString() {
        System.out.print("Enter your query string: ");
        String input = IN.nextLine();
        return new ArrayList<String>(Arrays.asList(input.toLowerCase().split("\\s")));
    }

    private static double promptForPrecision() {
        while (true) {
            System.out.print("Enter the target precision (must be decimal between 0 and 1): ");
            String input = IN.nextLine();
            if (DoubleValidatorUtil.isStringParsableToDouble(input)) {
                double value = Double.parseDouble(input);
                if (value >= 0 && value <= 1) {
                    return Double.parseDouble(input);
                }
            }
        }
    }

    private static void promptForRelevance(Collection<Document> documents) {
        for (Document document : documents) {
            if (document.getRelevant() == null) {
                document.setRelevant(promptForRelevance(document));
            }
        }
    }

    private static boolean promptForRelevance(Document document) {
        System.out.println(document);
        while (true) {
            System.out.print("Is this result relevant? (y or n): ");
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
