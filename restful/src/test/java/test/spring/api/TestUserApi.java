package test.spring.api;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

/** 
 * it comes from http://springinpractice.com/2012/04/08/sending-cookies-with-resttemplate
 * 
 * @author justin.wu
 *
 */
public class TestUserApi {
	
	private static String getSessionId() throws HttpException, IOException{
		HttpClient httpClient = new HttpClient();
		String url = "http://localhost:8080/vcaps3/SecurityServlet?as_user=robertp";
		GetMethod getMethod = new GetMethod(url);

		httpClient.executeMethod(getMethod);
		
		int responseCode = httpClient.executeMethod(getMethod);
		assertEquals(responseCode, 200); // will automatically follow 302 status and go to redirection page 
		
		byte[] body = getMethod.getResponseBody();
		
		String bodyStr = new String(body);
		
		System.out.println("body = " + bodyStr);
		
		String sessionId = getSessionIdFromConnection(httpClient);
		
		System.out.println("SessionId = " + sessionId);
		
		return sessionId;
		

	}
	
	private static String getSessionIdFromConnection(HttpClient httpConn) {
		HttpState state = httpConn.getState();
		Cookie[] cookies = state.getCookies();
		String sessionID = null;
		for (int c = 0; c < cookies.length; c++) {
			Cookie k = cookies[c];
			if (k.getName().equalsIgnoreCase("JSESSIONID")) {
				sessionID = k.getValue();
				return sessionID;
			}
		}
		return null;
	}
	
	//@Test
	public void testUserApi() throws HttpException, IOException{
		
		
		String url ="http://localhost:8080/vcaps3/api/v2/users/all.json";
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders requestHeaders = new HttpHeaders();

		String sessionId = getSessionId();
		requestHeaders.add("Cookie", "JSESSIONID="+ sessionId );
		
		HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
		ResponseEntity<String> response = restTemplate.exchange(
		    url,
		    HttpMethod.GET,
		    requestEntity,
		    String.class);
		HttpStatus status = response.getStatusCode();
		
		System.out.println(status);
		
		String body = (String)response.getBody();
		
		System.out.println(body);
		
		Object document = Configuration.defaultConfiguration().jsonProvider().parse(body);
		
		List<String> userName = JsonPath.read(document, "$.[?(@.userId==2)].username");
		
		assertEquals(userName.size(), 1);
		assertEquals(userName.get(0), "lixin");
		
		System.out.println("userName= " + userName);
	}
	
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


}
