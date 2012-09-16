/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web.filter;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.security.auth.login.Configuration;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jcifs.http.AuthenticationFilter;
import jcifs.smb.SmbException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.af.authenticaion.KerberosLoginModuleConfiguration;
import ru.runa.af.authenticaion.KerberosLoginModuleResources;
import ru.runa.af.web.KrbSupportResources;

/**
 * This class in conjunction with {@link ru.runa.af.web.action.KrbLoginAction} provides
 * Kerberos support.
 * 
 * @web.filter name="krbfilter"
 * @web.filter-mapping url-pattern = "/krblogin.do"
 */
public class KrbFilter implements Filter {
    private static final Log log = LogFactory.getLog(KrbFilter.class);

    class CustomFilterConfig implements FilterConfig {
        String filterName = "krbfilter";
        Hashtable<String, String> initParams = new Hashtable<String, String>();
        ServletContext context;

        public CustomFilterConfig(ServletContext context) {
            this.context = context;
            initParams.putAll(KrbSupportResources.getInitParameters());
        }

        public String getFilterName() {
            return filterName;
        }

        public String getInitParameter(String key) {
            return initParams.get(key);
        }

        public Enumeration getInitParameterNames() {
            return initParams.keys();
        }

        public ServletContext getServletContext() {
            return context;
        }
    }

    public void destroy() {
    }

    CustomFilterConfig filter;

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(Configuration.getConfiguration() instanceof KerberosLoginModuleConfiguration)) {
            Configuration.setConfiguration(new KerberosLoginModuleConfiguration(KerberosLoginModuleResources.webKerberosResources));
        }

        try {
            AuthenticationFilter NegFilter = new AuthenticationFilter();
            NegFilter.init(filter);
            NegFilter.doFilter(request, response, chain);
        } catch (SmbException e) {
            log.error(e.getMessage(), e);
            chain.doFilter(request, response);
        }
    }

    public void init(FilterConfig filter) throws ServletException {
        this.filter = new CustomFilterConfig(filter == null ? null : filter.getServletContext());
    }
}
