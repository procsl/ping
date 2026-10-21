package cn.procsl.ping.boot.ui.exception;

/**
 * compose.json 文件级解析异常。
 *
 * <p>当 compose.json 文件内容非法（JSON 格式错误、顶层结构不合法等）时抛出。
 * 异常信息含文件路径、行号（如可知）与具体错误原因，便于快速定位声明来源。
 */
public class ComposeParseException extends ComposeException {

    private final String filePath;
    private final Integer lineNumber;

    public ComposeParseException(String filePath, Integer lineNumber, String message) {
        super(format(filePath, lineNumber, message));
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public ComposeParseException(String filePath, Integer lineNumber, String message, Throwable cause) {
        super(format(filePath, lineNumber, message), cause);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    private static String format(String filePath, Integer lineNumber, String message) {
        return String.format("[%s:%s] %s", filePath, lineNumber != null ? lineNumber : "?", message);
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }
}
