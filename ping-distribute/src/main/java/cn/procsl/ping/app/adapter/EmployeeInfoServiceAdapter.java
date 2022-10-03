package cn.procsl.ping.app.adapter;

import cn.procsl.ping.boot.admin.auth.login.SessionUserDetail;
import cn.procsl.ping.boot.common.error.BusinessException;
import cn.procsl.ping.boot.oa.adapter.EmployeeServiceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@RequiredArgsConstructor
public class EmployeeInfoServiceAdapter implements EmployeeServiceAdapter {

    final HttpServletRequest request;

    @Override
    public String getEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated() && authentication.getPrincipal() instanceof SessionUserDetail) {
            return ((SessionUserDetail) authentication.getPrincipal()).getId().toString();
        }
        throw new BusinessException("找不到用户");
    }
}
