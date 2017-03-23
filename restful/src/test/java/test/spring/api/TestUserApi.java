package test.spring.api;

import java.io.IOException;

import static org.junit.Assert.*;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/** 
 * it comes from http://springinpractice.com/2012/04/08/sending-cookies-with-resttemplate
 * 
 * @author justin.wu
 *
 */
public class TestUserApi {
	
	@Test
	public void testUserApi(){
		
		
		String url ="http://localhost:8080/vcaps3/api/v2/users/all.json";
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders requestHeaders = new HttpHeaders();
		//requestHeaders.add("Cookie", "JSESSIONID=" + session.getValue());
		requestHeaders.add("Cookie", "JSESSIONID=42DC4E4F42926111DEC71DEB3E237D97" );
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
	}

}
