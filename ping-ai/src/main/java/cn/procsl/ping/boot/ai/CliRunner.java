package cn.procsl.ping.boot.ai;

import cn.procsl.ping.boot.ai.adapter.cli.CliArguments;
import cn.procsl.ping.boot.ai.adapter.cli.CliCommand;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 * 把 Spring Boot 解析好的参数交给非交互式命令。
 */
@Component
public class CliRunner {

    private final ApplicationArguments arguments;

    public CliRunner(ApplicationArguments arguments) {
        this.arguments = arguments;
    }

    /**
     * 执行一次命令行调用。
     *
     * @return 退出码
     */
    public int run() {
        CliArguments parsed = CliArguments.parse(arguments.getSourceArgs());
        return new CliCommand(System.out, System.err).execute(parsed);
    }
}
