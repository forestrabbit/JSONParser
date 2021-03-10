package com.fr;

import java.util.*;
import java.io.*;

public class JSONParser {
    private final char[] data;
    private int ptr;
    private int line = 1;

    public JSONParser(String fileName) throws IOException {
        File file = new File(fileName);
        data = new char[(int) file.length()];
        ptr = 0;

        try (var reader = new InputStreamReader(new FileInputStream(file))) {
            int x = reader.read(data);
            if (x != file.length()) {
                throw new IOException();
            }
        }
    }

    public Object parse() throws IllegalArgumentException {
        try {
            if (data[ptr] == '/' && data[ptr + 1] == '/') {
                parseSingleLineComment();
                return parse();
            } else if (data[ptr] == '/' && data[ptr + 1] == '*') {
                parseMultiLineComment();
                return parse();
            } else if (data[ptr] == '"') {
                return parseString();
            } else if (data[ptr] == 'n') {
                return parseNull();
            } else if (data[ptr] == '\r' && data[ptr + 1] == '\n') {
                ptr += 2;
                line++;
                return parse();
            } else if (data[ptr] == 't') {
                return parseTrue();
            } else if (data[ptr] == 'f') {
                return parseFalse();
            } else if (data[ptr] >= '0' && data[ptr] <= '9') {
                return parseNumber();
            } else if (data[ptr] == '[') {
                return parseArray();
            } else if (data[ptr] == '{') {
                return parseObject();
            } else if (data[ptr] == ' ' || data[ptr] == '\t') {
                ptr++;
                return parse();
            } else {
                throw new IllegalArgumentException("第 " + line + " 行出错\n");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("第 " + line + " 行出错\n");
        }
    }

    /**
     * 解析单行注释
     * 遇到换行符或文件结束时解析结束
     */
    private void parseSingleLineComment() {
        while (data[ptr] != '\r' && data[ptr + 1] != '\n' && ptr < data.length) {
            ptr++;
        }
        line++;
        ptr += 2;
    }

    /**
     * 解析多行注释
     * 遇到\*\/时解析结束
     */
    private void parseMultiLineComment() throws ArrayIndexOutOfBoundsException {
        while (true) {
            if (data[ptr] == '*' && data[ptr + 1] == '/') {
                ptr += 2;
                break;
            }
            if (data[ptr] == '\r' && data[ptr + 1] == '\n') {
                line++;
                ptr++;
            }
            ptr++;
        }
    }

    /**
     * 解析字符串
     * 遇到"时解析结束
     */
    private String parseString() throws ArrayIndexOutOfBoundsException {
        var builder = new StringBuilder();
        ptr++;
        while (data[ptr] != '"') {
            if (data[ptr] == '\r' && data[ptr + 1] == '\n') {
                ptr += 2;
                line++;
                continue;
            }
            builder.append(data[ptr]);
            ptr++;
        }
        ptr++;
        return builder.toString();
    }

    /**
     * 解析null
     */
    private String parseNull() throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (data[ptr + 1] == 'u' && data[ptr + 2] == 'l' && data[ptr + 3] == 'l') {
            ptr += 4;
            return "null";
        } else {
            throw new IllegalArgumentException("第 " + line + " 行出错\n");
        }
    }

    /**
     * 解析true
     */
    private String parseTrue() throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (data[ptr + 1] == 'r' && data[ptr + 2] == 'u' && data[ptr + 3] == 'e') {
            ptr += 4;
            return "true";
        } else {
            throw new IllegalArgumentException("第 " + line + " 行出错\n");
        }
    }

    /**
     * 解析false
     */
    private String parseFalse() throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        if (data[ptr + 1] == 'a' && data[ptr + 2] == 'l' && data[ptr + 3] == 's' && data[ptr + 4] == 'e') {
            ptr += 5;
            return "false";
        } else {
            throw new IllegalArgumentException("第 " + line + " 行出错\n");
        }
    }

    /**
     * 解析数字
     */
    private double parseNumber() {
        double ans = 0;
        while (ptr < data.length) {
            if (data[ptr] >= '0' && data[ptr] <= '9') {
                ans = ans * 10 + data[ptr] - '0';
            } else if (data[ptr] == '\r' && data[ptr + 1] == '\n') {
                line++;
                ptr++;
            } else {
                break;
            }
            ptr++;
        }
        return ans;
    }

    /**
     * 解析数组
     */
    private List<Object> parseArray() throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        List<Object> list = new ArrayList<>();
        ptr++;

        while (data[ptr] != ']') {
            if (data[ptr] == '\r' && data[ptr + 1] == '\n') {
                ptr += 2;
                line++;
                continue;
            }
            if (data[ptr] == ' ' || data[ptr] == '\t') {
                ptr++;
                continue;
            }
            list.add(parse());
            while (data[ptr] == ' ' || data[ptr] == '\t' || data[ptr] == '\r' || data[ptr] == '\n') {
                if (data[ptr] == '\n') {
                    line++;
                }
                ptr++;
            }
            if (data[ptr] == ',') {
                ptr++;
                if (data[ptr] == ']') {
                    throw new IllegalArgumentException("第 " + line + " 行出错\n");
                }
            } else if (data[ptr] == ']') {
                ptr++;
                break;
            } else {
                throw new IllegalArgumentException("第 " + line + " 行出错\n");
            }
        }
        return list;
    }

    /**
     * 解析object
     */
    private Map<String, Object> parseObject() throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
        Map<String, Object> map = new HashMap<>();
        ptr++;

        while (data[ptr] != '}') {
            if (data[ptr] == '\r' && data[ptr + 1] == '\n') {
                ptr += 2;
                line++;
                continue;
            }
            if (data[ptr] == ' ' || data[ptr] == '\t') {
                ptr++;
                continue;
            }
            String key = parseString();
            while (data[ptr] == ' ' || data[ptr] == '\t') {
                ptr++;
            }
            if (data[ptr] != ':') {
                throw new IllegalArgumentException("第 " + line + " 行出错：" + data[ptr] + "\n");
            }
            ptr++;
            while (data[ptr] == ' ' || data[ptr] == '\t') {
                ptr++;
            }
            map.put(key, parse());
            while (data[ptr] == ' ' || data[ptr] == '\t' || data[ptr] == '\r' || data[ptr] == '\n') {
                if (data[ptr] == '\n') {
                    line++;
                }
                ptr++;
            }
            if (data[ptr] == ',') {
                ptr++;
                if (data[ptr] == '}') {
                    throw new IllegalArgumentException("第 " + line + " 行出错\n");
                }
            } else if (data[ptr] == '}') {
                ptr++;
                break;
            } else {
                throw new IllegalArgumentException("第 " + line + " 行出错\n");
            }
        }
        return map;
    }
}