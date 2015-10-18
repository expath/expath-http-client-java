package tmp.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.ProxySelectorRoutePlanner;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Florent Georges - fgeorges.org
 */
public class ApacheHttpClientTest
{
    @Test
    public void testGetMethod()
            throws ClientProtocolException, IOException
    {
        HttpGet get = new HttpGet("http://www.fgeorges.org/");
        HttpResponse response = getClient().execute(get);
        System.err.println("Status: " + response.getStatusLine().getStatusCode());
        System.err.print("Content: ");
        response.getEntity().writeTo(System.err);
        System.err.println();
    }

    /**
     * Send a POST request to the Google authentication service.
     */
    @Test
    public void testGoogleAuth()
            throws ClientProtocolException, IOException
    {
        HttpPost post = new HttpPost("https://www.google.com/accounts/ClientLogin");
        ContentProducer producer = new StringProducer(AUTH_CONTENT);
        EntityTemplate entity = new EntityTemplate(producer);
        entity.setContentType(FORM_TYPE);
        post.setEntity(entity);
        HttpResponse resp = getClient().execute(post);
        System.err.println("Status: " + resp.getStatusLine().getStatusCode());
        System.err.print("Content: ");
        resp.getEntity().writeTo(System.err);
        System.err.println();
    }

    @Test
    public void testGoogleRedirect()
            throws ClientProtocolException, IOException
    {
        HttpPost post = new HttpPost("https://www.google.com/accounts/ClientLogin");
        ContentProducer producer = new StringProducer(AUTH_CONTENT);
        EntityTemplate entity = new EntityTemplate(producer);
        entity.setContentType(FORM_TYPE);
        post.setEntity(entity);
        AbstractHttpClient client = getClient();
        HttpResponse resp = client.execute(post);
        System.err.println("POST status: " + resp.getStatusLine().getStatusCode());
        String token = null;
        for ( String s : getStringContent(resp).split("\n") ) {
            if ( s.startsWith("Auth=") ) {
                token = s.substring(5);
            }
        }
        System.err.println("Token: " + token);
        // GetMethod get = new GetMethod("https://www.google.com/calendar/feeds/default/allcalendars/full");
        HttpGet get = new HttpGet("http://www.google.com/calendar/feeds/xmlprague.cz_k0rlr8da52ivmgp6eujip041s8%40group.calendar.google.com/private/full");
        get.setHeader("GData-Version", "2");
        get.setHeader("Authorization", "GoogleLogin auth=" + token);
        // get.setFollowRedirects(false);
        resp = client.execute(get);
        System.err.println("GET status: " + resp.getStatusLine().getStatusCode());
        for ( Cookie c : client.getCookieStore().getCookies() ) {
            System.err.println("Cookie: " + c.getName() + ", " + c.getValue());
        }
        for ( String s : client.getCookieSpecs().getSpecNames() ) {
            System.err.println("Cookie spec: " + s);
        }
        if ( resp.getStatusLine().getStatusCode() == 302 ) {
            client = getClient();
            resp = client.execute(get);
            System.err.println("GET status: " + resp.getStatusLine().getStatusCode());
        }
        HttpGet get2 = new HttpGet("http://www.google.com/calendar/feeds/xmlprague.cz_k0rlr8da52ivmgp6eujip041s8%40group.calendar.google.com/private/full");
        get2.setHeader("GData-Version", "2");
        get2.setHeader("Authorization", "GoogleLogin auth=" + token);
        // get2.setFollowRedirects(false);
        client = getClient();
        resp = client.execute(get2);
        System.err.println("GET 2 status: " + resp.getStatusLine().getStatusCode());
    }

    @Ignore("Broken authentication?!?")
    @Test
    public void testGoogleAddAgenda()
            throws ClientProtocolException, IOException
    {
        System.err.println();
        System.err.println("***** [testGoogleAddAgenda]");
        String token = authenticate();
        String uri = "https://www.google.com/calendar/feeds/default/private/full";
        HttpResponse resp = testGoogleAddAgenda_1(token, uri);
        if ( resp.getStatusLine().getStatusCode() == 302 ) {
            uri = resp.getFirstHeader("Location").getValue();
            testGoogleAddAgenda_1(token, uri);
        }
        System.err.println("***** [/testGoogleAddAgenda]");
    }

    public HttpResponse testGoogleAddAgenda_1(String token, String uri)
            throws ClientProtocolException, IOException
    {
        HttpPost post = new HttpPost(uri);
        post.setHeader("GData-Version", "2");
        post.setHeader("Authorization", "GoogleLogin auth=" + token);
        EntityTemplate entity = new EntityTemplate(new StringProducer(AGENDA_ENTRY));
        entity.setContentType(ATOM_TYPE);
        post.setEntity(entity);
        HttpResponse resp = getClient().execute(post);
        System.err.println("POST status: " + resp.getStatusLine().getStatusCode());
        System.err.println("POST message: " + resp.getStatusLine().getReasonPhrase());
        System.err.print("POST response content: ");
        resp.getEntity().writeTo(System.err);
        System.err.println();
        return resp;
    }

    @Ignore("Broken authentication?!?")
    @Test
    public void testGoogleAddAgendaStd()
            throws ClientProtocolException, IOException, URISyntaxException
    {
        System.err.println();
        System.err.println("***** [testGoogleAddAgendaStd]");
        String token = authenticate();
        URI uri = new URI("https://www.google.com/calendar/feeds/default/private/full");
        HttpURLConnection conn = testGoogleAddAgendaStd_1(token, uri);
        if ( conn.getResponseCode() == 302 ) {
            String loc = conn.getHeaderField("Location");
            uri = new URI(loc);
            testGoogleAddAgendaStd_1(token, uri);
        }
        System.err.println("***** [/testGoogleAddAgendaStd]");
    }

    private HttpURLConnection testGoogleAddAgendaStd_1(String token, URI uri)
            throws ClientProtocolException, IOException, URISyntaxException
    {
        HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("GData-Version", "2");
        conn.setRequestProperty("Authorization", "GoogleLogin auth=" + token);
        conn.setRequestProperty("Content-Type", ATOM_TYPE);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        conn.getOutputStream().write(AGENDA_ENTRY.getBytes());
        conn.disconnect();
        System.err.println("POST status: " + conn.getResponseCode());
        System.err.println("POST message: " + conn.getResponseMessage());
        System.err.println("POST response content: " + conn.getContent());
        return conn;
    }

    private String authenticate()
            throws IOException
    {
        HttpPost auth = new HttpPost("https://www.google.com/accounts/ClientLogin");
        EntityTemplate entity = new EntityTemplate(new StringProducer(AUTH_CONTENT));
        entity.setContentType(FORM_TYPE);
        auth.setEntity(entity);
        HttpResponse resp = getClient().execute(auth);
        System.err.println("AUTH status: " + resp.getStatusLine().getStatusCode());
        String token = null;
        for ( String s : getStringContent(resp).split("\n") ) {
            System.err.println("AUTH content line: " + s);
            if ( s.startsWith("Auth=") ) {
                token = s.substring(5);
            }
        }
        if ( token == null ) {
            throw new RuntimeException("Token is null!");
        }
        return token;
    }

    @Test
    public void testResponseBody()
            throws ClientProtocolException, IOException, URISyntaxException
    {
        System.err.println();
        System.err.println("***** [testResponseBody]");
        HttpGet get = new HttpGet("http://www.fgeorges.org/tmp/xproc-fixed-alternative.mpr-");
        HttpResponse resp = getClient().execute(get);
        System.err.println("Status: " + resp.getStatusLine().getStatusCode());
        HttpEntity entity = resp.getEntity();
        System.err.println("Entity class: " + entity.getClass());
        System.err.println("Entity type: " + entity.getContentType());
        System.err.println("Entity encoding: " + entity.getContentEncoding());
        System.err.println("Entity is chunck: " + entity.isChunked());
        System.err.println("Entity is repeat: " + entity.isRepeatable());
        System.err.println("Entity is stream: " + entity.isStreaming());
        System.err.println("***** [/testResponseBody]");
    }

    @Ignore("Need a certificate file, see 'Unable to find the certificate file' below")
    @Test
    public void testTrustSelfSignedKeys()
            throws ClientProtocolException, IOException, URISyntaxException,
                   KeyStoreException, NoSuchAlgorithmException,
                   CertificateException, KeyManagementException, UnrecoverableKeyException
    {
        // TODO:
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        trustStore.load(null, null);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        File in_f = new File("/Users/fgeorges/tmp/fgeorges.crt");
        if ( ! in_f.exists() ) {
            in_f = new File("h:/tmp/fgeorges.org.crt");
        }
        if ( ! in_f.exists() ) {
            throw new RuntimeException("Unable to find the certificate file...");
        }
        InputStream in = new FileInputStream(in_f);
        Certificate certif = factory.generateCertificate(in);
        trustStore.setCertificateEntry("fgeorges.org", certif);
        SSLSocketFactory socketFactory = new SSLSocketFactory(trustStore);
        Scheme sch = new Scheme("https", socketFactory, 443);
        AbstractHttpClient client = getClient();
        client.getConnectionManager().getSchemeRegistry().register(sch);
        // </TODO>

        System.err.println();
        System.err.println("***** [testTrustSelfSignedKeys]");
        HttpGet get = new HttpGet("https://www.fgeorges.org/");
        //HttpGet get = new HttpGet("https://mail.google.com/");
        HttpResponse resp = client.execute(get);
        System.err.println("Status: " + resp.getStatusLine().getStatusCode());
        System.err.println("***** [/testTrustSelfSignedKeys]");
    }

    @Test
    public void testXProcPost()
            throws ClientProtocolException, IOException, URISyntaxException
    {
        System.err.println();
        System.err.println("***** [testXProcPost]");
        String uri = "http://tests.xproc.org/service/fixed-rdf";
        String content = "<content/>";
        doPost(uri, content, XML_TYPE);
        System.err.println("***** [/testXProcPost]");
    }

    @Test
    public void testFGeorgesPost()
            throws ClientProtocolException, IOException, URISyntaxException
    {
        System.err.println();
        System.err.println("***** [testFGeorgesPost]");
        String uri = "http://www.fgeorges.org/cgi-bin/display-post";
        String content = "<content/>";
        doPost(uri, content, XML_TYPE);
        System.err.println("***** [/testFGeorgesPost]");
    }

    private void doPost(String uri, String content, String type)
            throws ClientProtocolException, IOException, URISyntaxException
    {
        HttpPost post = new HttpPost(uri);
        EntityTemplate entity = new EntityTemplate(new StringProducer(content));
        entity.setContentType(type);
        post.setEntity(entity);
        AbstractHttpClient client = getClient();
        System.err.println("DEBUG: CLIENT: " + client.getClass());
        HttpResponse resp = client.execute(post);
        System.err.println("Status: " + resp.getStatusLine().getStatusCode());
        HttpEntity body = resp.getEntity();
        System.err.println("Entity class: " + body.getClass());
        System.err.println("Entity type: " + body.getContentType());
        System.err.println("Entity encoding: " + body.getContentEncoding());
        System.err.println("Entity is chunck: " + body.isChunked());
        System.err.println("Entity is repeat: " + body.isRepeatable());
        System.err.println("Entity is stream: " + body.isStreaming());
        System.err.println("Entity body: ");
        body.writeTo(System.err);
        System.err.println();
    }

    @Test
    public void testPost()
            throws ClientProtocolException, IOException, URISyntaxException
    {
        System.err.println();
        System.err.println("***** [testPost]");
        String uri = "http://www.fgeorges.org/cgi-bin/display-post";
        HttpPost post = new HttpPost(uri);
        EntityTemplate entity = new EntityTemplate(new StringProducer("<content/>"));
        entity.setContentType(XML_TYPE);
        post.setEntity(entity);
        HttpResponse resp = getClient().execute(post);
        System.err.println("Status: " + resp.getStatusLine().getStatusCode());
        System.err.println();
        System.err.println("***** [/testPost]");
    }

    private String getStringContent(HttpResponse resp)
            throws IOException
    {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        resp.getEntity().writeTo(buf);
        Header h = resp.getEntity().getContentType();
//System.err.println("Content-Type: " + h);
//for ( HeaderElement e : h.getElements() ) {
//    System.err.println("            : " + e);
//    for ( NameValuePair p : e.getParameters() ) {
//        System.err.println("                : " + p);
//    }
//}
        // TODO: Look for the encoding in the headers...
        return buf.toString(/* encoding */);
    }

    private static class StringProducer
            implements ContentProducer
    {
        public StringProducer(String content) {
            myContent = content.getBytes();
        }
        @Override
        public void writeTo(OutputStream out) throws IOException {
            out.write(myContent);
        }
        private byte[] myContent;
    }

    private static AbstractHttpClient getClient()
    {
        // FIXME: TODO: How to manage and reuse connections?  In test cases, but
        // also in production code...
        return makeNewClient();
//        return CLIENT;
    }

    private static AbstractHttpClient makeNewClient()
    {
        AbstractHttpClient client = new DefaultHttpClient();
        HttpRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
             client.getConnectionManager().getSchemeRegistry(),
             ProxySelector.getDefault());
        client.setRoutePlanner(routePlanner);
        client.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        return client;
    }

    static {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "debug");
//        System.setProperty("http.proxyHost", "proxy");
//        System.setProperty("http.proxyPort", "8080");
//        System.setProperty("https.proxyHost", "proxy");
//        System.setProperty("https.proxyPort", "8080");
    }

    private static final String FORM_TYPE = "application/x-www-form-urlencoded";
    private static final String ATOM_TYPE = "application/atom+xml";
    private static final String XML_TYPE  = "application/xml";
    private static final AbstractHttpClient CLIENT = makeNewClient();
//    private static final AbstractHttpClient CLIENT = new DefaultHttpClient();
//    static {
//        HttpRoutePlanner routePlanner = new ProxySelectorRoutePlanner(
//             CLIENT.getConnectionManager().getSchemeRegistry(),
//             ProxySelector.getDefault());
//        CLIENT.setRoutePlanner(routePlanner);
//        CLIENT.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
//    }

    private static final String AUTH_CONTENT = "Email=fgeorges.test%40gmail.com&Passwd=testtest&source=yo&service=cl&accountType=GOOGLE";
    private static final String AGENDA_ENTRY = "<atom:entry xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns:http=\"http://expath.org/ns/http-client\"><atom:category scheme=\"http://schemas.google.com/g/2005#kind\" term=\"http://schemas.google.com/g/2005#event\"/><atom:title type=\"text\">Coffee break</atom:title><atom:content type=\"text\">Brought to you by ...</atom:content><gd:transparency xmlns:gd=\"http://schemas.google.com/g/2005\" value=\"http://schemas.google.com/g/2005#event.opaque\"/><gd:eventStatus xmlns:gd=\"http://schemas.google.com/g/2005\" value=\"http://schemas.google.com/g/2005#event.confirmed\"/><gd:where xmlns:gd=\"http://schemas.google.com/g/2005\" valueString=\"Prague\"/><gd:when xmlns:gd=\"http://schemas.google.com/g/2005\" startTime=\"2009-02-20T10:45:00.000Z\" endTime=\"2009-02-20T11:00:00.000Z\"/></atom:entry>";
}
