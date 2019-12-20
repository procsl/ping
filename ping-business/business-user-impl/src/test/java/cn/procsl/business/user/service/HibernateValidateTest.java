package cn.procsl.business.user.service;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.validation.ConstraintViolationException;

/**
 * @author procsl
 * @date 2019/12/20
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/application.xml"})
public class HibernateValidateTest {

    @Autowired
    IValidate validate;

    @Test(expected = ConstraintViolationException.class)
    public void validateNull() {
        validate.testNull(null);
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateEmpty() {
        validate.testEmpty("");
    }

    @Test(expected = ConstraintViolationException.class)
    public void validateSizeMax() {
        validate.testSize(ImmutableList.of(1, 2, 3, 4, 5));
    }

}
