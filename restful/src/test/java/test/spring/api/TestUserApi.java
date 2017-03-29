package test.spring.api;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/** 
 * demo how to test RESTful API
 * it used two libs: httpclient and com.jayway.jsonpath 
 * @author justin.wu
 *
 */
public class TestUserApi {
	
	private CookieStore httpCookieStore;
	public static String URL_ROOT = "http://localhost:8080/vcaps3";
	
	@Before
	public void setup()  {
		httpCookieStore = new BasicCookieStore();
	}	
	
	
	public static void login(CookieStore httpCookieStore, String domainUserName) throws HttpException, IOException{
		
		String url = URL_ROOT+ "/SecurityServlet?as_user=" + domainUserName;
		System.out.println("url = " + url);
		
		HttpClient client = initClient(httpCookieStore);
		
		
		HttpGet request = new HttpGet(url);
		
		HttpResponse response = client.execute(request);
		assertEquals(response.getStatusLine().getStatusCode(), 200); // will automatically follow 302 status and go to redirection page 
		
		String bodyStr = getReturn(response);
		
		System.out.println("body = " + bodyStr);
		
		

	}
	
	public static void logout(CookieStore httpCookieStore) throws HttpException, IOException{
		String url = URL_ROOT+ "/logout" ;
		
		System.out.println("url = " + url);
		
		HttpClient client = initClient(httpCookieStore);
		
		HttpGet request = new HttpGet(url);
		
		HttpResponse response = client.execute(request);
		assertEquals(response.getStatusLine().getStatusCode(), 200);  

	}
	
	private static HttpClient initClient(CookieStore httpCookieStore){
		HttpClient http = null;
		HttpClientBuilder builder = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore);
		http = builder.build();
		return http;
	}
	
	private static String getReturn(HttpResponse response) throws HttpException, IOException{
		
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		
		return result.toString();
		
	}
	
	@Test
	public void testUserApi() throws HttpException, IOException{
		
		
		String url = URL_ROOT +"/api/v2/users/all.json";

		login(httpCookieStore, "robertp");
		
		HttpClient client = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).build();
		
		final HttpGet request = new HttpGet(url);
		 
		HttpResponse response = client.execute(request);
	    assertEquals(response.getStatusLine().getStatusCode(), 200);

		System.out.println(response.getStatusLine().getStatusCode());
		
		String body = getReturn(response);
		
		System.out.println(body);
		
		Object document = Configuration.defaultConfiguration().jsonProvider().parse(body);
		
		List<String> userName = JsonPath.read(document, "$.[?(@.userId==2)].username");
		
		assertEquals(userName.size(), 1);
		assertEquals(userName.get(0), "lixin");
		
		System.out.println("userName= " + userName);
	}
	
	// it comes from 
	// https://github.com/jayway/JsonPath
	@Test
	public void testJson(){		
		
		String json ="{store: "
				+ "{    book: ["
				+ "        {            "
				+ "category: 'reference',            "
				+ "author: 'Nigel Rees',            "
				+ "title: 'Sayings of the Century',"
				+ "            price: 8.95"
				+ "        },"
				+ "        {"
				+ "            category: 'fiction',"
				+ "            author: 'J. R. R. Tolkien',"
				+ "            title: 'The Lord of the Rings',"
				+ "            isbn: '0-395-19395-8',"
				+ "            price: 22.99"
				+ "        }    ],"
				+ "    bicycle: {"
				+ "        color: 'red',"
				+ "        price: 19.95"
				+ "    }"
				+ "},"
				+ " expensive: 10"
				+ "}";
		
		Object document = Configuration.defaultConfiguration().jsonProvider().parse(json);
		
		String author0 = JsonPath.read(document, "$.store.book[0].author");
		List<String> category = JsonPath.read(document, "store.book[?(@.author=='Nigel Rees')].category");

		System.out.println("author0= " + author0);
		System.out.println("category= " + category);
		
		assertEquals(author0, "Nigel Rees");

		assertEquals(category.size(), 1);
		assertEquals(category.get(0), "reference");

	}
	
	@After
	public void tearDown() throws Exception {
		
		// logout
		logout(httpCookieStore);
		

	}


}
