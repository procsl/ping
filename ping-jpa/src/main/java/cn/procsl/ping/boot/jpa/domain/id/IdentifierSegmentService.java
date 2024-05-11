package cn.procsl.ping.boot.jpa.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
class IdentifierSegmentService {

    final IdentifierSegmentRepository repository;

    final TransactionTemplate transactionTemplate;

    /**
     * 获取下一个可分配的数据段
     *
     * @param segmentName 数据段名称
     * @param size        数据段大小
     * @param initValue   初始数据值
     * @param retryTimes  分配重试次数
     * @return 返回可分配的数据段起始值, 如返回1, 则可分配数据段值为 1 至 1+size
     * @throws IdentifierException 如果分配重试次数超过指定值,或其他原因
     */
    public Long nextSegmentValue(String segmentName, int size, Long initValue, int retryTimes) throws IdentifierException {

        for (int i = 0; i < retryTimes; i++) {
            try {
                return transactionTemplate.execute(status -> {

                    Optional<Long> optional = repository.incrementBy(segmentName, size);
                    return optional.orElseGet(() -> {
                        repository.save(segmentName, initValue);
                        status.flush();
                        return initValue;
                    });

                });
            } catch (RuntimeException e) {
                log.warn("获取ID段出现异常", e);
            }
        }
        throw new IdentifierException("获取ID段失败, 超过重试次数");
    }

}
