package cn.procsl.ping.business.domain;

import java.io.Serializable;

/**
 * 实体标记
 *
 * @author procsl
 * @date 2020/07/28
 */
public interface DomainEntity extends Serializable {
    int LONG_ID_LENGTH = 20;

    int UUID_LENGTH = 32;

    int UUID_2_LENGTH = 36;

}
