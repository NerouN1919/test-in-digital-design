package com.digdes.school;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSchoolStarter {
    private List<Map<String, Object>> database = new ArrayList<>();
    private final Set<String> canHave = Set.of("id", "lastName", "cost", "age", "active");

    private void addNull() {
        for (Map<String, Object> in : database) {
            for (String setHave : canHave) {
                if (!in.containsKey(setHave)) {
                    in.put(setHave, null);
                }
            }
        }
    }

    private Map<String, Object> toNeedClasses(Map<String, Object> before) throws Exception {
        Map<String, Object> res = new LinkedHashMap<>();
        for (Map.Entry<String, Object> in : before.entrySet()) {
            checkIfStringToEqual(in.getKey());
            if (in.getValue() == null) {
                switch (in.getKey().toLowerCase()) {
                    case "id" -> res.put("id", null);
                    case "age" -> res.put("age", null);
                    case "lastname" -> res.put("lastName", null);
                    case "cost" -> res.put("cost", null);
                    case "active" -> res.put("active", null);
                    default -> throw new Exception("Bad name");
                }
            }
            switch (in.getKey().toLowerCase()) {
                case "id" -> res.put("id", Long.parseLong((String) in.getValue()));
                case "age" -> res.put("age", Long.parseLong((String) in.getValue()));
                case "lastname" -> res.put("lastName", checkIfStringToEqual(String.valueOf(in.getValue())));
                case "cost" -> res.put("cost", Double.parseDouble((String) in.getValue()));
                case "active" -> res.put("active", Boolean.parseBoolean((String) in.getValue()));
                default -> throw new Exception("Bad name");
            }
        }
        return res;
    }

    private Map<String, Object> insert(Map<String, Object> values) throws Exception {
        database.add(toNeedClasses(values));
        addNull();
        return values;
    }

    private Map<String, Object> insertParse(String command) {
        String[] values = command.split("\\s*,\\s*");
        Map<String, Object> res = new LinkedHashMap<>();
        for (String value : values) {
            Pattern valuePattern = Pattern.compile("\\s*'(.+)'\\s*=\\s*(.+)");
            Matcher valueMatcher = valuePattern.matcher(value);
            if (valueMatcher.find()) {
                String columnName = valueMatcher.group(1);
                String columnValue = valueMatcher.group(2);
                res.put(columnName, columnValue);
            }
        }
        for (Map.Entry<String, Object> in : res.entrySet()) {
            if (in.getValue().equals("null")) {
                res.put(in.getKey(), null);
            }
        }
        return res;
    }

    private List<String> parseFromCommandToWhere(String command) throws Exception {
        List<String> res = new ArrayList<>();
        Pattern selectOrDeletePattern = Pattern.compile("(select|delete)(.*?)(?:where|$)",
                Pattern.CASE_INSENSITIVE);
        Matcher selectOrDeleteMatcher = selectOrDeletePattern.matcher(command);
        if (selectOrDeleteMatcher.find()) {
            String selectOrDeleteFields = selectOrDeleteMatcher.group(2).trim();
            String[] fields = selectOrDeleteFields.split("\\s*,\\s*");
            res.addAll(Arrays.asList(fields));
        }
        if (res.size() != 0 && res.get(0).equals("")) {
            return new ArrayList<>();
        }
        for (String in : res) {
            if (canHave.stream().noneMatch(e -> e.equalsIgnoreCase(in))) {
                throw new Exception("Bad field");
            }
        }
        return res;
    }

    private Map<String, Object> parseToUpdate(String command) throws Exception {
        Map<String, Object> res = new HashMap<>();
        Pattern pattern = Pattern.compile("update\\s*values\\s*(.*?)\\s*(?:where|$)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            String foundString = matcher.group(1);
            List<String> keyValue;
            keyValue = List.of(foundString.split(",\\s*"));
            for (String in : keyValue) {
                String[] toAdded = in.split("\\s*=\\s*");
                res.put(checkIfStringToEqual(toAdded[0]), checkIfStringToEqual(toAdded[1]));
            }
        }
        for (Map.Entry<String, Object> in : res.entrySet()) {
            if (canHave.stream().noneMatch(e -> e.equalsIgnoreCase(in.getKey()))) {
                throw new Exception("Bad field");
            }
            if (in.getValue().equals("null")) {
                res.put(in.getKey(), null);
            }
        }
        return res;
    }

    private List<List<String>> parseSelectFromWhereToEnd(String command) throws Exception {
        List<List<String>> res = new ArrayList<>();
        Pattern wherePattern = Pattern.compile("where (.*)", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(command);
        if (whereMatcher.find()) {
            String whereConditions = whereMatcher.group(1);
            Pattern conditionPattern = Pattern.compile("(?i)(\\S+?)\\s*(!=|ilike|like|=|>=|<=|<|>)" +
                    "\\s*('[^']*'|\\S*(?=\\s|$))", Pattern.CASE_INSENSITIVE);
            Pattern logicalOperatorPattern = Pattern.compile("\\b(and|or)\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = conditionPattern.matcher(whereConditions);
            Matcher logicalMatcher = logicalOperatorPattern.matcher(whereConditions);
            boolean firstCondition = true;
            while (matcher.find()) {
                if (!firstCondition && logicalMatcher.find()) {
                    List<String> logicalOperator = new ArrayList<>();
                    logicalOperator.add(logicalMatcher.group(1));
                    res.add(logicalOperator);
                }
                firstCondition = false;
                List<String> temp = new ArrayList<>();
                String column = matcher.group(1);
                if (canHave.stream().noneMatch(e -> e.equalsIgnoreCase(checkIfStringToEqual(column)))) {
                    throw new Exception("Bad field");
                }
                temp.add(column);
                temp.add(matcher.group(2));
                temp.add(matcher.group(3));
                res.add(temp);
            }
        }
        return res;
    }

    public List<Map<String, Object>> execute(String command) throws Exception {
        Map<String, Object> res;
        Pattern insertPattern = Pattern.compile("insert\\s*values\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher insertMatcher = insertPattern.matcher(command);
        Pattern selectPattern = Pattern.compile("select\\s*((.+)|$)", Pattern.CASE_INSENSITIVE);
        Matcher selectMatcher = selectPattern.matcher(command);
        Pattern wherePattern = Pattern.compile("\\bwhere\\b", Pattern.CASE_INSENSITIVE);
        Matcher whereMatcher = wherePattern.matcher(command);
        Pattern deletePattern = Pattern.compile("delete\\s*(.+|$)", Pattern.CASE_INSENSITIVE);
        Matcher deleteMatcher = deletePattern.matcher(command);
        Pattern updatePattern = Pattern.compile("update\\s*values\\s*(.+)", Pattern.CASE_INSENSITIVE);
        Matcher updateMatcher = updatePattern.matcher(command);
        if (insertMatcher.find()) {
            String valuesStr = insertMatcher.group(1);
            res = insertParse(valuesStr);
            return Collections.singletonList(insert(res));
        } else if (selectMatcher.find()) {
            if (whereMatcher.find()) {
                List<String> fieldsToSelect = parseFromCommandToWhere(command);
                List<List<String>> byWhere = parseSelectFromWhereToEnd(command);
                return select(fieldsToSelect, byWhere);
            } else {
                List<String> fieldsToSelect = parseFromCommandToWhere(command);
                return selectWithoutWhere(fieldsToSelect);
            }
        } else if (deleteMatcher.find()) {
            if (whereMatcher.find()) {
                List<List<String>> byWhere = parseSelectFromWhereToEnd(command);
                return delete(byWhere);
            } else {
                return deleteWithoutWhere();
            }
        } else if (updateMatcher.find()) {
            if (whereMatcher.find()) {
                List<List<String>> byWhere = parseSelectFromWhereToEnd(command);
                Map<String, Object> toUpdate = parseToUpdate(command);
                return update(toUpdate, byWhere);
            } else {
                Map<String, Object> toUpdate = parseToUpdate(command);
                return updateWithoutWhere(toUpdate);
            }
        } else {
            throw new Exception("Bad request");
        }
    }

    private List<Map<String, Object>> update(Map<String, Object> toUpdate, List<List<String>> byWhere) throws Exception {
        List<Map<String, Object>> tempRes = executeQuery(infixToPostfix(byWhere));
        List<Map<String, Object>> returned = new ArrayList<>();
        for (Map<String, Object> in : database) {
            for (Map<String, Object> selected : tempRes) {
                if (in.equals(selected)) {
                    returned.add(deepCopy(in));
                    toUpdate = toNeedClasses(toUpdate);
                    in.putAll(toUpdate);
                }
            }
        }
        addNull();
        return returned;
    }

    private List<Map<String, Object>> deleteWithoutWhere() {
        List<Map<String, Object>> returned = deepCopy(database);
        database.clear();
        return returned;
    }

    private static List<Map<String, Object>> deepCopy(List<Map<String, Object>> original) {
        List<Map<String, Object>> copy = new ArrayList<>();
        for (Map<String, Object> map : original) {
            Map<String, Object> mapCopy = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value instanceof Map) {
                    value = deepCopy((Map<String, Object>) value);
                }
                mapCopy.put(key, value);
            }
            copy.add(mapCopy);
        }
        return copy;
    }

    private static Map<String, Object> deepCopy(Map<String, Object> original) {
        Map<String, Object> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : original.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = deepCopy((Map<String, Object>) value);
            }
            copy.put(key, value);
        }
        return copy;
    }

    private List<Map<String, Object>> updateWithoutWhere(Map<String, Object> toUpdate) throws Exception {
        List<Map<String, Object>> beforeUpdate = deepCopy(database);
        for (Map<String, Object> in : database) {
            toUpdate = toNeedClasses(toUpdate);
            in.putAll(toUpdate);
        }
        addNull();
        return beforeUpdate;
    }

    private List<Map<String, Object>> selectWithoutWhere(List<String> fieldsToSelect) {
        if (fieldsToSelect.size() == 0) {
            fieldsToSelect = new ArrayList<>(canHave);
        }
        List<Map<String, Object>> resSelect = new ArrayList<>();
        for (Map<String, Object> in : database) {
            Map<String, Object> temp = new LinkedHashMap<>();
            for (Map.Entry<String, Object> keyValue : in.entrySet()) {
                if (fieldsToSelect.stream().anyMatch(e -> e.equalsIgnoreCase(keyValue.getKey()))) {
                    temp.put(keyValue.getKey(), keyValue.getValue());
                }
            }
            resSelect.add(temp);
        }
        return resSelect;
    }

    private List<Map<String, Object>> delete(List<List<String>> byWhere) throws Exception {
        List<Map<String, Object>> tempRes = executeQuery(infixToPostfix(byWhere));
        List<Map<String, Object>> res = new ArrayList<>();
        for (Map<String, Object> in : database) {
            if (!tempRes.contains(in)) {
                res.add(in);
            }
        }
        database = res;
        return tempRes;
    }

    private List<Map<String, Object>> select(List<String> fieldsToSelect, List<List<String>> byWhere) throws Exception {
        List<Map<String, Object>> tempRes = executeQuery(infixToPostfix(byWhere));
        List<Map<String, Object>> res = new ArrayList<>();
        if (fieldsToSelect.size() == 0) {
            return tempRes;
        }
        for (Map<String, Object> in : tempRes) {
            Map<String, Object> temp = new LinkedHashMap<>();
            for (Map.Entry<String, Object> keyValue : in.entrySet()) {
                if (fieldsToSelect.stream()
                        .anyMatch(value -> value.equalsIgnoreCase(keyValue.getKey()))) {
                    temp.put(keyValue.getKey(), keyValue.getValue());
                }
            }
            res.add(temp);
        }
        return res;
    }

    private int operatorPreority(String c) {
        if (c.equalsIgnoreCase("and")) {
            return 1;
        }
        if (c.equalsIgnoreCase("or")) {
            return 2;
        }
        return Integer.MAX_VALUE;
    }

    private boolean isOperand(String c) {
        return !(c.equalsIgnoreCase("and") ||
                c.equalsIgnoreCase("or"));
    }

    private List<List<String>> infixToPostfix(List<List<String>> infix) {
        if (infix == null || infix.size() == 0) {
            return infix;
        }
        Stack<List<String>> s = new Stack<>();
        List<List<String>> postfix = new ArrayList<>();
        for (List<String> c : infix) {
            if (isOperand(c.get(0))) {
                postfix.add(c);
            } else {

                while (!s.isEmpty() && operatorPreority(c.get(0)) >= operatorPreority(s.peek().get(0))) {
                    postfix.add(s.pop());
                }
                s.add(c);
            }
        }
        while (!s.isEmpty()) {
            postfix.add(s.pop());
        }
        return postfix;
    }

    public List<Map<String, Object>> executeQuery(List<List<String>> postfix) throws Exception {
        Stack<List<Map<String, Object>>> stack = new Stack<>();
        for (List<String> tokenList : postfix) {
            if (tokenList.size() == 1 && isOperator(tokenList.get(0))) {
                List<Map<String, Object>> operand2 = stack.pop();
                List<Map<String, Object>> operand1 = stack.pop();
                stack.push(applyOperator(tokenList.get(0), operand1, operand2));
            } else {
                stack.push(applyCondition(tokenList, database));
            }
        }
        return stack.pop();
    }

    private boolean isOperator(String token) {
        return token.equalsIgnoreCase("and")
                || token.equalsIgnoreCase("or");
    }

    private List<Map<String, Object>> applyOperator(String operator, List<Map<String, Object>> operand1,
                                                    List<Map<String, Object>> operand2) {
        return operator.equalsIgnoreCase("and")
                ? intersection(operand1, operand2) : union(operand1, operand2);
    }

    private String checkIfStringToEqual(String value) {
        if (value.toCharArray()[0] == '\'' && value.toCharArray()[value.toCharArray().length - 1] == '\'') {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private List<Map<String, Object>> applyCondition(List<String> condition,
                                                     List<Map<String, Object>> database) throws Exception {
        String field = checkIfStringToEqual(condition.get(0));
        String operator = condition.get(1);
        String value = condition.get(2);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> record : database) {
            Object fieldValue = null;
            for (Map.Entry<String, Object> in : record.entrySet()) {
                if (in.getKey().equalsIgnoreCase(field)) {
                    fieldValue = in.getValue();
                }
            }
            if (((fieldValue instanceof String || fieldValue instanceof Boolean) && (operator.equals(">")
                    || operator.equals("<") || operator.equals(">=") || operator.equals("<=")))
                    || !(fieldValue instanceof String) && (operator.equals("like") || operator.equals("ilike"))) {
                throw new Exception("Unsupported equal");
            }
            if (fieldValue == null && !operator.equals("!=") && !operator.equals("=")) continue;
            boolean match = switch (operator) {
                case "=" -> equalValues(fieldValue, value) == 0;
                case "!=" -> equalValues(fieldValue, value) != 0;
                case "<" -> compareValues(fieldValue, value) < 0;
                case ">" -> compareValues(fieldValue, value) > 0;
                case "<=" -> compareValues(fieldValue, value) <= 0;
                case ">=" -> compareValues(fieldValue, value) >= 0;
                case "like" -> like(String.valueOf(fieldValue), (value.substring(1, value.length() - 1)));
                case "ilike" -> ilike(String.valueOf(fieldValue), (value.substring(1, value.length() - 1)));
                default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
            };
            if (match) result.add(record);
        }
        return result;
    }

    private boolean like(String input, String pattern) {
        String regex = pattern
                .replaceAll("%", ".*")
                .replaceAll("_", ".");
        return input.matches(regex);
    }

    private boolean ilike(String input, String pattern) {
        String regex = pattern
                .replaceAll("%", ".*")
                .replaceAll("_", ".");
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(input).matches();
    }

    private int equalValues(Object value1, String value2) throws Exception {
        if (value1 == null && (value2 == null || value2.equals("null"))) {
            return 0;
        } else if (value1 == null || value2 == null) {
            return -1;
        } else if (value1.equals("null") && value2.equals("null")) {
            return 0;
        } else if (value1.equals("null") || value2.equals("null")) {
            return -1;
        }
        if (value1 instanceof Double) {
            String tempValue2 = checkIfStringToEqual(value2);
            if (!tempValue2.equals(value2) || value2.equals("true") || value2.equals("false")) {
                throw new Exception("Bad equal");
            }
            return Double.compare((Double) value1, Double.parseDouble(value2));
        } else if (value1 instanceof String) {
            String tempValue2 = checkIfStringToEqual(value2);
            if (tempValue2.equals(value2)) {
                return -1;
            }
            return ((String) value1).compareTo(tempValue2);
        } else if (value1 instanceof Long) {
            String tempValue2 = checkIfStringToEqual(value2);
            if (Pattern.matches(".*\\..*", value2) || !tempValue2.equals(value2)
                    || value2.equals("true") || value2.equals("false")) {
                return -1;
            }
            return Long.compare((Long) value1, Long.parseLong(value2));
        } else if (value1 instanceof Boolean) {
            if (!value2.equals("true") && !value2.equals("false")) {
                return -1;
            }
            return Boolean.compare((Boolean) value1, Boolean.parseBoolean(value2));
        } else {
            throw new IllegalArgumentException("Unsupported type for comparison: " + value1.getClass());
        }
    }

    private int compareValues(Object value1, String value2) throws Exception {
        if (value1 instanceof Double) {
            String tempValue2 = checkIfStringToEqual(value2);
            if (!tempValue2.equals(value2) || value2.equals("true") || value2.equals("false")) {
                throw new Exception("Bad equal");
            }
            return Double.compare((Double) value1, Double.parseDouble(value2));
        } else if (value1 instanceof Long) {
            String tempValue2 = checkIfStringToEqual(value2);
            if (!tempValue2.equals(value2) || value2.equals("true") || value2.equals("false")) {
                throw new Exception("Bad equal");
            }
            if (Pattern.matches(".*\\..*", value2)) {
                return Double.compare((Long) value1, Double.parseDouble(value2));
            }
            return Long.compare((Long) value1, Long.parseLong(value2));
        } else {
            throw new IllegalArgumentException("Unsupported type for comparison: " + value1.getClass());
        }
    }

    private List<Map<String, Object>> intersection(List<Map<String, Object>> list1, List<Map<String, Object>> list2) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> item : list1) {
            if (list2.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    private List<Map<String, Object>> union(List<Map<String, Object>> list1, List<Map<String, Object>> list2) {
        List<Map<String, Object>> result = new ArrayList<>(list1);
        for (Map<String, Object> item : list2) {
            if (!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

}
