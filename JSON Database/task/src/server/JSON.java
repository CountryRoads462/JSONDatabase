package server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSON {
    private String json;

    public JSON(String json) {
        this.json = json;
    }

    public String getJson() {
        return json;
    }

    public synchronized String getValueByKeys(String... keys) {
        int index = 0;
        int range = 0;
        for (String str :
                keys) {
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(json);
            matcher.region(range, json.length());
            matcher.find();
            index = matcher.end() + 2;
            range = index;
        }

        StringBuilder value = new StringBuilder();
        if (json.charAt(index) == '\"') {
            index++;
            for (int i = index; i < json.length(); i++) {
                if (json.charAt(i) == '\"') {
                    break;
                } else {
                    value.append(json.charAt(i));
                }
            }
        } else {
            int numberOfOpenBrackets = 0;
            for (int i = index; i < json.length(); i++) {
                if (json.charAt(i) == '{' || json.charAt(i) == '[') {
                    numberOfOpenBrackets++;
                }
                if (json.charAt(i) == '}' || json.charAt(i) == ']') {
                    numberOfOpenBrackets--;
                }
                value.append(json.charAt(i));
                if (numberOfOpenBrackets == 0) {
                    break;
                }
            }
        }
        return value.toString();
    }

    public synchronized void deleteByKeys(String... keys) {
        int index = 0;
        int range = 0;
        String key = "";
        for (String str :
                keys) {
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(json);
            matcher.region(range, json.length());
            matcher.find();
            key = matcher.group();
            index = matcher.end() + 2;
            range = index;
        }

        StringBuilder newJson = new StringBuilder(json);
        if (json.charAt(index) == '\"') {
            json = json.replaceAll("\"" + key + "\":\"[a-zA-Z0-9]+\",?", "");
            if (json.charAt(index - 4 - key.length()) == ',' && json.charAt(index - 3 - key.length()) == '}') {
                newJson = new StringBuilder(json);
                newJson = newJson.deleteCharAt(index - 4 - key.length());
                json = newJson.toString();
            }
        } else {
            int numberOfOpenBrackets = 1;
            index++;
            do {
                if (newJson.charAt(index) == '{') {
                    numberOfOpenBrackets++;
                }
                if (newJson.charAt(index) == '}') {
                    numberOfOpenBrackets--;
                }
                newJson = newJson.deleteCharAt(index);
            } while (numberOfOpenBrackets != 0);
            newJson = newJson.insert(index, '}');
            json = newJson.toString();
        }
    }

    public synchronized void setValuesByKeys(String[] keys, String... values) {
        int index = 0;
        int range = 0;
        int count = 0;
        for (String str :
                keys) {
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(json);
            matcher.region(range, json.length());
            if (matcher.find()) {
                index = matcher.end() + 2;
                range = index;
                count++;
            } else {
                break;
            }
        }

        int number = 0;
        StringBuilder newJson = new StringBuilder(json);
        index++;
        boolean valueIsExist = true;
        while (count != keys.length) {
            newJson = newJson.insert(index, "\"" + keys[count] + "\":{}");
            number++;
            index += keys[count].length() + 4;
            count++;
            valueIsExist = false;
        }

        if (valueIsExist) {
            while (true) {
                newJson = newJson.deleteCharAt(index + number - 1);
                if (newJson.charAt(index + number - 1) == '"') {
                    newJson = newJson.deleteCharAt(index + number - 1);
                    break;
                }
            }
            newJson.insert(index + number - 1, "{}");
        } else {
            if (newJson.charAt(index + number) != '}') {
                newJson.insert(index + number, ",");
            }
        }

        if (values.length == 1) {
            newJson = newJson.deleteCharAt(index);
            newJson = newJson.deleteCharAt(index - 1);
            newJson.insert(index - 1, "\"" + values[0] + "\"");
        } else {
            for (int i = 0; i < values.length; i += 2) {
                String key = values[i];
                String value = values[i + 1];
                newJson.insert(index, "\"" + key + "\":\"" + value + "\",");
                index += key.length();
                index += value.length();
                index += 6;
            }
            newJson = newJson.deleteCharAt(index - 1);
        }
        json = newJson.toString();
    }

    public synchronized void setSingleValueByKeys(String[] keys, String value) {
        int index = 0;
        int range = 0;
        int count = 0;
        for (String str :
                keys) {
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(json);
            matcher.region(range, json.length());
            if (matcher.find()) {
                index = matcher.end() + 2;
                range = index;
                count++;
            } else {
                break;
            }
        }

        int number = 0;
        StringBuilder newJson = new StringBuilder(json);
        index++;
        boolean valueIsExist = true;
        while (count != keys.length) {
            newJson = newJson.insert(index, "\"" + keys[count] + "\":{}");
            number++;
            index += keys[count].length() + 4;
            count++;
            valueIsExist = false;
        }

        if (valueIsExist) {
            while (true) {
                newJson = newJson.deleteCharAt(index + number - 1);
                if (newJson.charAt(index + number - 1) == '"') {
                    newJson = newJson.deleteCharAt(index + number - 1);
                    break;
                }
            }
            newJson.insert(index + number - 1, "{}");
        } else {
            if (newJson.charAt(index + number) != '}') {
                newJson.insert(index + number, ",");
            }
        }

        newJson = newJson.deleteCharAt(index);
        newJson = newJson.deleteCharAt(index - 1);
        newJson.insert(index - 1, value);

        json = newJson.toString();
    }

    public static synchronized String[] getValuesFromArray(String arg) {
        String array = arg;
        array = array.replaceAll("[\\[\\]\"]", "");
        return array.split(",");
    }
}
