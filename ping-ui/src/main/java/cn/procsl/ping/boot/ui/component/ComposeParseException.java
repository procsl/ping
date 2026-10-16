package cn.procsl.ping.boot.ui.component;

/**
 * compose.json 文件级解析异常。
 *
 * <p>当 compose.json 文件内容非法（JSON 格式错误、顶层类型不合法等）时抛出。
 * 异常信息包含文件路径、行号（如可知）和具体错误原因，便于快速定位问题。
 */
public class ComposeParseException extends ComposeCompileException {

    private final String filePath;
    private final Integer lineNumber;

    public ComposeParseException(String filePath, Integer lineNumber, String message) {
        super(String.format("[%s:%s] %s", filePath, lineNumber != null ? lineNumber : "?", message));
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public ComposeParseException(String filePath, Integer lineNumber, String message, Throwable cause) {
        super(String.format("[%s:%s] %s", filePath, lineNumber != null ? lineNumber : "?", message), cause);
        this.filePath = filePath;
        this.lineNumber = lineNumber;
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }
}
