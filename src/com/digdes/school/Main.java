package com.digdes.school;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void print(List<Map<String, Object>> database) {
        if (database.size() == 0) {
            System.out.println("database is empty");
            return;
        }
        Map<String, Integer> columnWidths = new LinkedHashMap<>();
        for (Map<String, Object> row : database) {
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                String column = entry.getKey();
                String value = String.valueOf(entry.getValue());
                int width = Math.max(columnWidths.getOrDefault(column, 0), value.length());
                columnWidths.put(column, width);
            }
        }
        for (Map.Entry<String, Integer> entry : columnWidths.entrySet()) {
            String format = "| %-" + (entry.getValue() + 2) + "s";
            System.out.printf(format, entry.getKey());
        }
        System.out.println("|");
        for (Map.Entry<String, Integer> entry : columnWidths.entrySet()) {
            String format = "+-%-" + (entry.getValue() + 2) + "s";
            System.out.printf(format, "-".repeat(entry.getValue() + 2));
        }
        System.out.println("+");
        for (Map<String, Object> row : database) {
            for (Map.Entry<String, Integer> entry : columnWidths.entrySet()) {
                String column = entry.getKey();
                String value = String.valueOf(row.getOrDefault(column, ""));
                String format = "| %-" + (entry.getValue() + 2) + "s";
                System.out.printf(format, " " + value + " ");
            }
            System.out.println("|");
        }
        for (Map.Entry<String, Integer> entry : columnWidths.entrySet()) {
            String format = "+-%-" + (entry.getValue() + 2) + "s";
            System.out.printf(format, "-".repeat(entry.getValue() + 2));
        }
        System.out.println("+");
    }

    public static void main(String[] args) {
        JavaSchoolStarter javaSchoolStarter = new JavaSchoolStarter();
        try {
            javaSchoolStarter.execute("INSERT    VALUES 'id'=0, 'lastName'='test', 'age'=36, 'active'=false, 'cost'=3.6");
            javaSchoolStarter.execute("INSERT VALUES 'id'=10   , 'lastName'=null, 'age'=45, 'active'=true");
            javaSchoolStarter.execute("INSERT VALUES 'lastname'='Fedorov', 'age'=4,    'active'=true, 'cost'=5");
            print(javaSchoolStarter.getDatabase());
            print(javaSchoolStarter.execute("update values lastname = null where 'cost' >=5"));
            print(javaSchoolStarter.getDatabase());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}