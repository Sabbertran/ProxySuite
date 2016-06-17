package de.sabbertran.proxysuite;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.Map.Entry;

public class ProxySuiteUtils {
    public static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;
        int len1 = rhs.length() + 1;

        // the array of distances
        int[] cost = new int[len0];
        int[] newcost = new int[len0];

        // initial cost of skipping prefix in String s0
        for (int i = 0; i < len0; i++) cost[i] = i;

        // dynamically computing the array of distances

        // transformation cost for each letter in s1
        for (int j = 1; j < len1; j++) {
            // initial cost of skipping prefix in String s1
            newcost[0] = j;

            // transformation cost for each letter in s0
            for (int i = 1; i < len0; i++) {
                // matching current letters in both strings
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;

                // computing cost for each transformation
                int cost_replace = cost[i - 1] + match;
                int cost_insert = cost[i] + 1;
                int cost_delete = newcost[i - 1] + 1;

                // keep minimum cost
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }

            // swap cost/newcost arrays
            int[] swap = cost;
            cost = newcost;
            newcost = swap;
        }

        // the distance is the cost for transforming all letters in both strings
        return cost[len0 - 1];
    }

    public static void sortMapAsc(HashMap<ProxiedPlayer, Integer> unsortMap) {
        List<Entry<ProxiedPlayer, Integer>> list = new LinkedList<Entry<ProxiedPlayer, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<ProxiedPlayer, Integer>>() {
            public int compare(Entry<ProxiedPlayer, Integer> o1,
                               Entry<ProxiedPlayer, Integer> o2) {
                if (o1.getValue() > o2.getValue())
                    return 1;
                else if (o1.getValue() < o2.getValue())
                    return -1;
                else
                    return o1.getKey().getName().compareTo(o2.getKey().getName());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<ProxiedPlayer, Integer> sortedMap = new HashMap<ProxiedPlayer, Integer>();
        for (Entry<ProxiedPlayer, Integer> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());

        unsortMap = sortedMap;
    }

    public static int getOccurrenceOf(String haystack, char needle) {
        int ret = 0;
        for (char c : haystack.toCharArray())
            if (c == needle)
                ret++;
        return ret;
    }
}