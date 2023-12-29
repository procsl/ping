package cn.procsl.ping.boot.jpa.support.extension;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryFactoryBeanSupport;
import org.springframework.data.repository.core.support.RepositoryFactoryCustomizer;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;


@Slf4j
public class JpaRepositoryFactoryCustomizer implements RepositoryFactoryCustomizer, BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(@Nonnull Object bean, @Nonnull String beanName) throws BeansException {
        if (bean instanceof RepositoryFactoryBeanSupport<?, ?, ?> repoSupport) {
            boolean bool = JpaExtensionRepository.class.isAssignableFrom(repoSupport.getObjectType());
            log.debug("发现repository接口: {}", repoSupport.getObjectType());
            if (bool) {
                log.info("增强repository接口: {}", repoSupport.getObjectType());
                repoSupport.addRepositoryFactoryCustomizer(this);
            }
        }
        return bean;
    }

    @Override
    public void customize(RepositoryFactorySupport repositoryFactory) {
        repositoryFactory.addRepositoryProxyPostProcessor((factory, repositoryInformation) -> {
            factory.addAdvice(new ExtensionMethodInterceptor(repositoryFactory, repositoryInformation));
        });
    }

    @RequiredArgsConstructor
    final static class ExtensionMethodInterceptor implements MethodInterceptor {
        final RepositoryFactorySupport repositoryFactory;
        final RepositoryInformation repositoryInformation;

        @Override

        public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
            log.info("进入方法");
            return "test";
        }

    }

}
