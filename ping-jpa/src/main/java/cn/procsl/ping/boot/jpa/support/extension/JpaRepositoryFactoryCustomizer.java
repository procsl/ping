package cn.procsl.ping.boot.jpa.support.extension;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;


@Slf4j
public class JpaRepositoryFactoryCustomizer implements RepositoryFactoryCustomizer, BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof RepositoryFactoryBeanSupport<?, ?, ?> repoSupport) {

            boolean bool = JpaExtensionRepository.class.isAssignableFrom(repoSupport.getObjectType());
            if (bool) {
                repoSupport.addRepositoryFactoryCustomizer(this);
                log.info("增强接口名称: {}", repoSupport.getObjectType());
            }
        }
        return bean;
    }

    @Override
    public void customize(RepositoryFactorySupport repositoryFactory) {
        repositoryFactory.addRepositoryProxyPostProcessor(new RepositoryProxyPostProcessor() {
            @Override
            public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
                log.info("test: ");
                Class<?>[] interfaces = factory.getProxiedInterfaces();
                for (Class<?> face : interfaces) {

                }
            }
        });
    }


}
