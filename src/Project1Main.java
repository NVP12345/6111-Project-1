import domain.QueryResult;
import util.BingApiUtil;
import util.DoubleValidatorUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Project1Main {

    private static final Scanner IN = new Scanner(System.in);

    public static void main(String[] args) {
        String queryString = promptForQueryString();
        double targetPrecision = promptForPrecision();

        List<QueryResult> queryResults = BingApiUtil.getBingQueryResults(queryString);
        Map<QueryResult, Boolean> queryResultsToRelevance = promptForRelevance(queryResults);
        double currentPrecision = getCurrentPrecision(queryResultsToRelevance.values());
        if (currentPrecision >= targetPrecision) {
            System.out.format("Precision reached %f, which exceeds target of %f. Terminating.", currentPrecision, targetPrecision);
            System.exit(0);
        } else {
            System.out.format("Precision reached %f, which does not meet target of %f. Terminating.", currentPrecision, targetPrecision);
            System.exit(1);
        }
    }

    private static String promptForQueryString() {
        System.out.print("Enter your query string: ");
        return IN.next();
    }

    private static double promptForPrecision() {
        while (true) {
            System.out.print("Enter the target precision (must be decimal between 0 and 1): ");
            String input = IN.next();
            if (DoubleValidatorUtil.isStringParsableToDouble(input)) {
                double value = Double.parseDouble(input);
                if (value >= 0 && value <= 1) {
                    return Double.parseDouble(input);
                }
            }
        }
    }

    private static Map<QueryResult, Boolean> promptForRelevance(List<QueryResult> queryResults) {
        Map<QueryResult, Boolean> queryResultsToRelevance = new HashMap<QueryResult, Boolean>();
        for (QueryResult queryResult : queryResults) {
            queryResultsToRelevance.put(queryResult, promptForRelevance(queryResult));
        }
        return queryResultsToRelevance;
    }

    private static boolean promptForRelevance(QueryResult queryResult) {
        System.out.println(queryResult);
        while (true) {
            System.out.print("Is this result relevant? (y or n): ");
            String input = IN.next().toLowerCase();
            if ("y".equals(input)) {
                return true;
            }
            if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static double getCurrentPrecision(Collection<Boolean> relevanceValues) {
        int relevantResults = 0;
        for (Boolean relevanceValue : relevanceValues) {
            if (relevanceValue) {
                ++relevantResults;
            }
        }
        return relevantResults / 10.0;
    }
}
