package org.mvel2;

import org.mvel2.util.StringAppender;

import java.util.Collections;
import java.util.List;

import static java.lang.String.copyValueOf;
import static org.mvel2.util.ParseTools.isWhitespace;
import static org.mvel2.util.ParseTools.repeatChar;

/**
 * 编译异常，即描述在解析表达式过程中或者是在运行过程中的各种错误信息
 * Standard exception thrown for all general compileShared and some runtime failures.
 */
public class CompileException extends RuntimeException {
    /** 相应的表达式 */
    private char[] expr;

    /** 当前编译时的错误下标 */
    private int cursor = 0;
    /** 出错信息时具体的错误偏移量,用于错误显示使用 */
    private int msgOffset = 0;

    /** 出错代码行,第几行 */
    private int lineNumber = 1;
    /** 出错的列,该行中第几列 */
    private int column = 0;

    /** 有哪些错误信息,错误详情列表 */
    private List<ErrorDetail> errors;

    /** 根据相应的信息,以及错误列表,编译上下文构建出相应的编译异常 */
    public CompileException(String message, List<ErrorDetail> errors, char[] expr, int cursor, ParserContext ctx) {
        super(message);
        this.expr = expr;
        this.cursor = cursor;

        //从第1个错误信息时查找相应的出错行,列等
        if(!errors.isEmpty()) {
            ErrorDetail detail = errors.iterator().next();
            this.cursor = detail.getCursor();
            this.lineNumber = detail.getLineNumber();
            this.column = detail.getColumn();
        }

        this.errors = errors;
    }

    public String toString() {
        return generateErrorMessage();
    }

    public CompileException(String message, char[] expr, int cursor, Throwable e) {
        super(message, e);
        this.expr = expr;
        this.cursor = cursor;
    }

    public CompileException(String message, char[] expr, int cursor) {
        super(message);
        this.expr = expr;
        this.cursor = cursor;
    }

    @Override
    public String getMessage() {
        return generateErrorMessage();
    }

    /** 计算出出错的行和列 */
    private void calcRowAndColumn() {
        if(lineNumber > 1 || column > 1) return;

        int row = 1;
        int col = 1;

        if((lineNumber != 0 && column != 0) || expr == null || expr.length == 0) return;

        for(int i = 0; i < cursor && i < expr.length; i++) {
            switch(expr[i]) {
                case '\r':
                    continue;
                case '\n':
                    row++;
                    col = 1;
                    break;

                default:
                    col++;
            }
        }

        this.lineNumber = row;
        this.column = col;
    }

    /** 找出最接近错误的位置字符串 */
    private CharSequence showCodeNearError(char[] expr, int cursor) {
        if(expr == null) return "Unknown";

        //从出错的前20个到后30个为一个处理段,来进行处理
        int start = cursor - 20;
        int end = (cursor + 30);

        //防止超出结束符的情况,如果结束符超长,则开始往前推30,以保证一个可以分析的代码段
        if(end > expr.length) {
            end = expr.length;
            start -= 30;
        }

        //处理开始段超长的问题
        if(start < 0) {
            start = 0;
        }

        String cs;

        int firstCr;
        int lastCr;

        cs = copyValueOf(expr, start, end - start).trim();

        int matchStart;
        int matchOffset = 0;
        String match = null;

        if(cursor < end) {
            matchStart = cursor;
            if(matchStart > 0) {
                while(matchStart > 0 && !isWhitespace(expr[matchStart - 1])) {
                    matchStart--;
                }
            }

            matchOffset = cursor - matchStart;

            match = new String(expr, matchStart, expr.length - matchStart);
            Makematch:
            for(int i = 0; i < match.length(); i++) {
                switch(match.charAt(i)) {
                    case '\n':
                    case ')':
                        match = match.substring(0, i);
                        break Makematch;
                }
            }

            if(match.length() >= 30) {
                match = match.substring(0, 30);
            }
        }

        do{
            firstCr = cs.indexOf('\n');
            lastCr = cs.lastIndexOf('\n');

            if(firstCr == -1) break;

            int matchIndex = match == null ? 0 : cs.indexOf(match);

            if(firstCr == lastCr) {
                if(firstCr > matchIndex) {
                    cs = cs.substring(0, firstCr);
                } else if(firstCr < matchIndex) {
                    cs = cs.substring(firstCr + 1, cs.length());
                }
            } else if(firstCr < matchIndex) {
                cs = cs.substring(firstCr + 1, lastCr);
            } else {
                cs = cs.substring(0, firstCr);
            }
        }
        while(true);

        String trimmed = cs.trim();

        if(match != null) {
            msgOffset = trimmed.indexOf(match) + matchOffset;
        } else {
            msgOffset = cs.length() - (cs.length() - trimmed.length());
        }

        if(msgOffset == 0 && matchOffset == 0) {
            msgOffset = cursor;
        }

        return trimmed;
    }

    public CharSequence getCodeNearError() {
        return showCodeNearError(expr, cursor);
    }

    /** 生成相应的出错语句, 通过2行来指向具体的出错的位置 */
    private String generateErrorMessage() {
        StringAppender appender = new StringAppender().append("[Error: " + super.getMessage() + "]\n");

        int offset = appender.length();

        appender.append("[Near : {... ");

        offset = appender.length() - offset;

        appender.append(showCodeNearError(expr, cursor))
                .append(" ....}]\n")
                .append(repeatChar(' ', offset));

        if(msgOffset < 0) msgOffset = 0;

        appender.append(repeatChar(' ', msgOffset)).append('^');

        calcRowAndColumn();

        if(lineNumber != -1) {
            appender.append("\n")
                    .append("[Line: " + lineNumber + ", Column: " + (column) + "]");
        }
        return appender.toString();
    }

    public char[] getExpr() {
        return expr;
    }

    public int getCursor() {
        return cursor;
    }

    public List<ErrorDetail> getErrors() {
        return errors != null ? errors : Collections.emptyList();
    }

    public void setErrors(List<ErrorDetail> errors) {
        this.errors = errors;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getCursorOffet() {
        return this.msgOffset;
    }

    public void setExpr(char[] expr) {
        this.expr = expr;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }
}
