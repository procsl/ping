package cn.procsl.ping.demo.domain.entity;

import cn.procsl.ping.boot.domain.annotation.CreateRepository;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author procsl
 * @date 2020/05/18
 */
@Entity
@Table
@CreateRepository
public class User {

    @Id
    String id;
}
