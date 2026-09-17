package com.dtorrez.gym.security.services;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.ldap.InitialLdapContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

import org.springframework.stereotype.Component;

import com.micrium.bd.access.enuns.Parametro;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dtorrez.gym.common.exceptions.LdapContextException;
import com.dtorrez.gym.services.ParametroService;

/**
 *
 * @author alepaco.maton
 */
@Component
public class ActiveDirectoryService implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public static final int EXIT_USER = 1;
    public static final int NOT_EXIT_USER = 2;
    public static final int ERROR = 3;

    private transient ParametroService parametroService;    // 35479 coverity
    
    private static final Logger log = LogManager.getLogger(ActiveDirectoryService.class);
    
    public ActiveDirectoryService(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    private Hashtable<String, String> cargarContext(String usuario, String password) {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", (String) parametroService.getParamVal(Parametro.LDAP.INITIAL_CONTEXT_FACTORY.name()));
        env.put("java.naming.provider.url", (String) parametroService.getParamVal(Parametro.LDAP.PROVIDER_URL.name()));
        env.put("java.naming.security.authentication", (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_AUTHENTICACION.name()));
        env.put("java.naming.security.principal", (String) parametroService.getParamVal(Parametro.LDAP.DOMINIO.name()) + usuario);
        env.put("java.naming.security.credentials", password);

        return env;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean validarUsuario(String usuario, String password) throws LdapContextException {
        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put("java.naming.factory.initial", (String) parametroService.getParamVal(Parametro.LDAP.INITIAL_CONTEXT_FACTORY.name()));
        env.put("java.naming.provider.url", (String) parametroService.getParamVal(Parametro.LDAP.PROVIDER_URL.name()));
        env.put("java.naming.security.authentication", (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_AUTHENTICACION.name()));
        //env.put("java.naming.security.principal", (String) parametroService.getParamVal(ParametroID.SECURITY_PRINCIPAL) + (String)parametroService.getParamVal(ParametroID.SECURITY_USER));
        //env.put("java.naming.security.credentials", (String) parametroService.getParamVal(ParametroID.SECURITY_CREDENTIALS));

        env.put("java.naming.security.principal", (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_PRINCIPAL.name()) + usuario);
        env.put("java.naming.security.credentials", password);
        try {
            InitialDirContext ctx = new InitialDirContext(env);
            ctx.close();
            return true;
        } catch (Exception e) {
            //log.warn("usuario: " + usuario + ", No pudo iniciar session " + e.getMessage(), e);
            throw (new LdapContextException(e.getMessage(), e));
        }
        //return true;
    }

    @SuppressWarnings("rawtypes")
    public boolean validarGrupo(String grupo) throws LdapContextException {
        InitialDirContext dirC = null;
        NamingEnumeration answer = null;
        NamingEnumeration ae = null;

        Hashtable<String, String> env = new Hashtable<String, String>();

        env.put("java.naming.factory.initial", (String) parametroService.getParamVal(Parametro.LDAP.INITIAL_CONTEXT_FACTORY.name()));
        env.put("java.naming.provider.url", (String) parametroService.getParamVal(Parametro.LDAP.PROVIDER_URL.name()));
        env.put("java.naming.security.authentication", (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_AUTHENTICACION.name()));
        env.put("java.naming.security.principal", (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_PRINCIPAL.name()) + (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_USER.name()));
        env.put("java.naming.security.credentials", (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_CREDENTIALS.name()));

        try {
            /*dirC = new InitialDirContext(cargarContext((String) parametroService.getParamVal(ParametroID.SECURITY_USER),
                    (String) parametroService.getParamVal(ParametroID.SECURITY_CREDENTIALS)));*/
            dirC = new InitialDirContext(env);
        } catch (Exception e) {
            log.error("[validarGrupo] grupo: " + grupo + ", Fallo al validar el grupo, " + e.getMessage(), e);
            throw (new LdapContextException(e.getMessage(), e));
        }
        try {
            if (dirC != null) {
                String searchBase = "DC=tigo,DC=net,DC=bo";
                SearchControls searchCtls = new SearchControls();
                searchCtls.setSearchScope(2);
                String searchFilter = "(objectclass=group)";
                String[] returnAtts = {"cn"};
                searchCtls.setReturningAttributes(returnAtts);
                answer = dirC.search(searchBase, searchFilter, searchCtls);

                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    Attributes attrs = sr.getAttributes();
                    if (attrs != null) {
                        for (ae = attrs.getAll(); ae.hasMore();) {
                            Attribute attr = (Attribute) ae.next();
                            log.error("GRUPO********** " + attr.get().toString());
                            if (attr.get().toString().equals(grupo)) {
                                return true;
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("[validarGrupo] grupo: " + grupo + ", Fallo al validar el grupo", e);
        } finally {
            try {
                if (dirC != null) {
                    dirC.close();
                }
            } catch (Exception e) {
                log.warn("[validarGrupo] grupo: " + grupo + ", Fallo al cerrar el contexto LDAP", e);
            }
        }
        return false;
    }

    @SuppressWarnings({"rawtypes", "unchecked", "unused"})
    public List<String> getListaGrupos(String usuario) throws LdapContextException {
        List<String> lGrupos = new ArrayList<>();

        InitialLdapContext ctx = null;
        try {
            ctx = new InitialLdapContext(cargarContext((String) parametroService.getParamVal(Parametro.LDAP.SECURITY_USER.name()),
                    (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_CREDENTIALS.name())), null);
            String searchBase = "DC=tigo,DC=net,DC=bo";
            SearchControls searchCtls = new SearchControls();
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String returnAtts[] = {"memberOf"};
            String searchFilter = "(&(objectCategory=person)(objectClass=user)(mailNickname=" + usuario + "))";

            searchCtls.setReturningAttributes(returnAtts);
            log.error("PASO 1");
            NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
            log.error("PASO 2");
            int totalResults = 0;

            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    for (NamingEnumeration ne = attrs.getAll(); ne.hasMore();) {
                        Attribute attr = (Attribute) ne.next();
                        String grupo;
                        for (NamingEnumeration e = attr.getAll(); e.hasMore(); totalResults++) {
                            grupo = e.next().toString().trim();
                            grupo = grupo.substring(3, grupo.indexOf(",")).trim();
                            lGrupos.add(grupo);
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("PASO 3");
            log.error("[getListaGrupos] usuario: " + usuario + ", Error al obtener el listado de grupos", e);
            throw (new LdapContextException(e.getMessage(), e));
        } finally {
            try {
                ctx.close();
            } catch (Exception e2) {
                log.warn("[getListaGrupos] usuario: " + usuario + ", Fallo al cerrar el InitialLdapContext", e2);
            }
        }
        return lGrupos;
    }

     @SuppressWarnings("rawtypes")
    public String getNombreCompleto(String usuario) throws LdapContextException {
        try {
            String[] returnAtts = {"cn", "sAMAccountName", "mail", "sn", "givenName", "initials"};
            Map mapa = obtenerDatos(usuario, returnAtts);
            if (mapa.isEmpty()) {
                return "";
            }

            return String.valueOf(mapa.get("givenName") + " " + (String) mapa.get("sn")).trim();

        } catch (Exception e) {
            log.error("[getNombreCompleto] usuario: " + usuario + ", Fallo al obtener el nombre completo del usuario, " + e.getMessage(), e);
            throw (new LdapContextException(e.getMessage(), e));
        }
    }

    /**
     * perfil completo String[] returnAtts = { "cn", "sAMAccountName", "mail",
     * "sn", "givenName", "initials", "telephoneNumber", "dn", "description",
     * "physicalDeliveryOfficeName" };
     *
     * @param usuarioE
     * @param datosReturn
     * @return
     * @throws Exception
     */
    @SuppressWarnings("rawtypes")
    public Map<String, String> obtenerDatos(String usuarioE, String[] datosReturn) throws Exception {
        Map<String, String> mapa = new HashMap<>();

        InitialLdapContext ctx = null;

        try {
            ctx = new InitialLdapContext(cargarContext((String) parametroService.getParamVal(Parametro.LDAP.SECURITY_USER.name()),
                    (String) parametroService.getParamVal(Parametro.LDAP.SECURITY_CREDENTIALS.name())), null);

            String searchBase = "DC=tigo,DC=net,DC=bo";

            SearchControls searchCtls = new SearchControls();

            searchCtls.setSearchScope(2);

            String searchFilter = ("(&(objectCategory=person)(objectClass=user)(mailNickname=" + usuarioE + "))");

            searchCtls.setReturningAttributes(datosReturn);

            NamingEnumeration answer = ctx.search(searchBase, searchFilter, searchCtls);
            NamingEnumeration ae;
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                Attributes attrs = sr.getAttributes();
                if (attrs != null) {
                    for (ae = attrs.getAll(); ae.hasMore();) {
                        Attribute attr = (Attribute) ae.next();
                        String llave = attr.getID();
                        mapa.put(llave, attr.get().toString());
                    }

                }

            }

            return mapa;
        } catch (NamingException ne) {
            throw ne;
        } catch (Exception e) {
            throw e;
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }
}
