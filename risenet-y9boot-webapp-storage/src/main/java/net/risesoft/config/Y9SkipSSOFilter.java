package net.risesoft.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.risesoft.enums.platform.SexEnum;
import net.risesoft.model.platform.Person;
import net.risesoft.y9.Y9LoginUserHolder;

public class Y9SkipSSOFilter implements Filter {

    protected final Logger log = LoggerFactory.getLogger(Y9SkipSSOFilter.class);

    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        HttpSession session = request.getSession();
        try {
            String loginName = "admin";
            String name = "有生系统管理员";
            // String loginName = "shidaobang";
            // String name = "施道棒";
            String tenantID = "c425281829dc4d4496ddddf7fc0198d0";
            String tenantName = "北京有生博大软件股份有限公司";
            String tenantShortName = "risesoft";
            String personID = "4c106ab010ef47858e0acc84f98ea2a1";
            String parentID = "4868e899c5034a87a49b5cd9d345de96";
            String guidPath = "4868e899c5034a87a49b5cd9d345de96,4c106ab010ef47858e0acc84f98ea2a1";
            // String personID = "185e0a6f09354b198ecefcd2fe951e7a";
            // String parentID ="9cb5374c52df415eba663c364311de54";

            session.setAttribute("tenantName", tenantName);
            session.setAttribute("loginName", loginName);

            Y9LoginUserHolder.setTenantId(tenantID);
            Y9LoginUserHolder.setTenantName(tenantName);
            Y9LoginUserHolder.setTenantShortName(tenantShortName);

            Person person = new Person();
            person.setId(personID);
            person.setTenantId(tenantID);
            person.setGuidPath(guidPath);
            person.setLoginName(loginName);
            person.setSex(SexEnum.FEMALE);
            person.setName(name);
            person.setParentId(parentID);
            person.setOriginal(true);

            session.setAttribute("loginUser", person);
            Y9LoginUserHolder.setPerson(person);
            chain.doFilter(servletRequest, servletResponse);

        } finally {
            Y9LoginUserHolder.clear();
        }
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {}
}
