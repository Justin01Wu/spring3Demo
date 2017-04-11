package test.spring.api;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedAction;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

// it comes from 
// http://stackoverflow.com/questions/21629132/httpclient-set-credentials-for-kerberos-authentication
public class KerberosTest {
	
	public static String URL_ROOT = "http://cavcdbdev02:18080/vcaps3";
	
	public static void main(String[] args) throws Exception {
		
		URL location = KerberosTest.class.getProtectionDomain().getCodeSource().getLocation();
        System.out.println(location.getFile());
		
		System.setProperty("sun.security.krb5.debug", "true");
		///C:/samples/spring3Demo/spring3Demo/restful/target/test-classes/
		
		String loginConfPath = location.getFile().toString() +"login.conf";
        System.setProperty("java.security.auth.login.config", loginConfPath);
        System.setProperty("java.security.krb5.conf", "C:/_program3/apache-tomcat-7.0.75/conf/krb5.conf");
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");
        
        String user = "justin.wu";
        String password = "";
        String loginUrl = URL_ROOT + "/SecurityServlet";

        KerberosTest kcd = new KerberosTest();

        System.out.println("Login in with user [" + user + "] password [" + password + "] ");
        kcd.test(user, password, loginUrl);
        
	}
	
	public void test(String user, String password, final String url) {
        try {

            LoginContext loginContext = new LoginContext("Krb5JustinTest", new KerberosCallBackHandler(user, password));
            loginContext.login();

            PrivilegedAction sendAction = new PrivilegedAction() {

                @Override
                public Object run() {
                    try {

                        Subject current = Subject.getSubject(AccessController.getContext());
                        System.out.println("----------------------------------------");
                        Set<Principal> principals = current.getPrincipals();
                        for (Principal next : principals) {
                            System.out.println("DOAS Principal: " + next.getName());
                        }
                        System.out.println("----------------------------------------");

                        call(url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            };

            Subject.doAs(loginContext.getSubject(), sendAction);

        } catch (LoginException le) {
            le.printStackTrace();
        }
    }

    private void call(String url) throws IOException {
        HttpClient httpclient = getHttpClient();

        try {

            HttpUriRequest request = new HttpGet(url);
            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            System.out.println("----------------------------------------");

            System.out.println("STATUS >> " + response.getStatusLine());

            if (entity != null) {
                System.out.println("RESULT >> " + EntityUtils.toString(entity));
            }

            System.out.println("----------------------------------------");

            EntityUtils.consume(entity);

        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    private  HttpClient getHttpClient() {

        Credentials use_jaas_creds = new Credentials() {
            public String getPassword() {
                return null;
            }

            public Principal getUserPrincipal() {
                return null;
            }
        };

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(null, -1, null), use_jaas_creds);
        Registry<AuthSchemeProvider> authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create().register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory(true)).build();
        CloseableHttpClient httpclient = HttpClients.custom().setDefaultAuthSchemeRegistry(authSchemeRegistry).setDefaultCredentialsProvider(credsProvider).build();

        return httpclient;
    }

    class KerberosCallBackHandler implements CallbackHandler {

        private final String user;
        private final String password;

        public KerberosCallBackHandler(String user, String password) {
            this.user = user;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

            for (Callback callback : callbacks) {

                if (callback instanceof NameCallback) {
                    NameCallback nc = (NameCallback) callback;
                    nc.setName(user);
                } else if (callback instanceof PasswordCallback) {
                    PasswordCallback pc = (PasswordCallback) callback;
                    pc.setPassword(password.toCharArray());
                } else {
                    throw new UnsupportedCallbackException(callback, "Unknown Callback");
                }

            }
        }
    }

}
