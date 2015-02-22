import domain.QueryResultItem;
import util.BingApiUtil;
import util.DoubleValidatorUtil;
import util.QueryTermUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Project1Main {

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        List<String> queryTerms = promptForQueryString();
        double targetPrecision = promptForPrecision();
        Map<String, QueryResultItem> allQueryResultItemsById = new HashMap<String, QueryResultItem>();

        while (true) {
            System.out.println("Executing query...");
            List<QueryResultItem> currentQueryResultItems = BingApiUtil.getBingQueryResults(queryTerms);
            mergeQueryResultItems(allQueryResultItemsById, currentQueryResultItems);
            promptForRelevance(currentQueryResultItems);
            double currentPrecision = getCurrentPrecision(currentQueryResultItems);
            if (currentPrecision >= targetPrecision) {
                System.out.format("Precision reached %f, which exceeds target of %f. Current results:\n\n", currentPrecision, targetPrecision);
                for (QueryResultItem queryResultItem : currentQueryResultItems) {
                    System.out.println(queryResultItem);
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
            List<String> newQueryTerms = determineAugmentedQueryTerms(allQueryResultItemsById.values());
            String newQueryTermMessage = "Adding ";
            boolean first = true;
            for (String newQueryTerm : newQueryTerms) {
                if (!first) {
                    newQueryTermMessage += "and ";
                } else {
                    first = false;
                }
                newQueryTermMessage += "\"" + newQueryTerm + "\" ";
                queryTerms.add(newQueryTerm);
            }
            orderQueryTerms(queryTerms, allQueryResultItemsById.values());
            newQueryTermMessage += "to the query. Current query is: " + QueryTermUtil.buildQueryStringFromTerms(queryTerms);
            System.out.println(newQueryTermMessage);
        }

    }

    private static List<String> promptForQueryString() {
        System.out.print("Enter your query string: ");
        String input = IN.nextLine();
        return new ArrayList<String>(Arrays.asList(input.split("\\s")));
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

    private static void mergeQueryResultItems(Map<String, QueryResultItem> allQueryResultItems, List<QueryResultItem> currentQueryResultItems) {
        for (QueryResultItem queryResultItem : currentQueryResultItems) {
            String id = queryResultItem.getId();
            if (allQueryResultItems.containsKey(id)) {
                queryResultItem.setRelevant(allQueryResultItems.get(id).getRelevant());
            } else {
                allQueryResultItems.put(id, queryResultItem);
            }
        }
    }

    private static void promptForRelevance(Collection<QueryResultItem> queryResultItems) {
        for (QueryResultItem queryResultItem : queryResultItems) {
            if (queryResultItem.getRelevant() == null) {
                queryResultItem.setRelevant(promptForRelevance(queryResultItem));
            }
        }
    }

    private static boolean promptForRelevance(QueryResultItem queryResultItem) {
        System.out.println(queryResultItem);
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

    private static double getCurrentPrecision(Collection<QueryResultItem> queryResultItems) {
        int relevantResults = 0;
        for (QueryResultItem queryResultItem : queryResultItems) {
            if (queryResultItem.getRelevant()) {
                ++relevantResults;
            }
        }
        return relevantResults / (double) queryResultItems.size();
    }

    private static List<String> determineAugmentedQueryTerms(Collection<QueryResultItem> currentQueryResultItems) {
        List<String> newQueryTerms = new LinkedList<String>();
        newQueryTerms.add("newTerm1");
        newQueryTerms.add("newTerm2");
        return newQueryTerms;
    }

    private static void orderQueryTerms(List<String> queryTerms, Collection<QueryResultItem> values) {

    }
}
