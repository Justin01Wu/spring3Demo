package test.spring.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.jayway.restassured.path.json.JsonPath;

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
		ResponseEntity response = restTemplate.exchange(
		    url,
		    HttpMethod.GET,
		    requestEntity,
		    String.class);
		HttpStatus status = response.getStatusCode();
		
		System.out.println(status);
		
		String body = (String)response.getBody();
		
		System.out.println(body);
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			@SuppressWarnings("unused")
			JsonNode jsonNode = objectMapper.readTree(body);
		} catch (IOException e) {
			fail("response is not a json string");
		}
		
		String name = JsonPath.from(body).get("$[1].username");
		System.out.println(name);
	}
	
	//@Test
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
		
		String json2 ="{\"store\": \"qqq\", \"expensive\": 10}";
		
		String category = JsonPath.from(json2).get("store");
		System.out.println(category);
	}
	
	//@Test
	public void testJsonFromFile() throws IOException{
		
		
		String jsonFilePath = "C:\\TEMP\\allUser.json";
		
		File reultFile = new File(jsonFilePath); 
		String resultStr = FileUtils.readFileToString(reultFile);
		
		System.out.println(resultStr);
		String category = JsonPath.from(resultStr).get("store.book[0].category");
		System.out.println(category);
	}

}
