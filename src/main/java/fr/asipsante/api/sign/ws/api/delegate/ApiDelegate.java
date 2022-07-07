/**
 * (c) Copyright 1998-2021, ANS. All rights reserved.
 */

package fr.asipsante.api.sign.ws.api.delegate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.asipsante.api.sign.utils.AsipSignClientException;
import fr.asipsante.api.sign.ws.model.OpenidToken;

/**
 * The Class ApiDelegate.
 */
public class ApiDelegate {

	public static final String XOPENID_HEADER_NAME ="xopenidtoken";
	/**
	 * The log.
	 */
	Logger logger = LoggerFactory.getLogger(ApiDelegate.class);

	/**
	 * Gets the request.
	 *
	 * @return the request
	 */
	public Optional<NativeWebRequest> getRequest() {
		Optional<NativeWebRequest> request = Optional.empty();
		
		final ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder
				.currentRequestAttributes();
		if (attrs != null) {
			request = Optional.of(new ServletWebRequest(attrs.getRequest()));
		}
		System.out.println(attrs.getAttributeNames(0));

		return request;
	}

	/**
	 * Gets the accept header.
	 *
	 * @return the accept header
	 */
	public Optional<String> getAcceptHeader() {
		return getRequest().map(r -> r.getHeader("Accept"));
	}

	/**
	 * Gets the OpenidToken header.
	 *
	 * @return the OpenidToken header
	 * @throws AsipSignClientException
	 */
	public List<OpenidToken> parseOpenIdTokenHeader() throws AsipSignClientException {
		ObjectMapper objectMapper = new ObjectMapper();
		Optional<NativeWebRequest> tmp = getRequest();
		NativeWebRequest tmp1 = tmp.get();
		Iterator<String> liste = tmp1.getHeaderNames();
		while (liste.hasNext()) {
			System.out.println("name: "+ liste.next());
		}
		
		// XOPENID_HEADER_NAME ="xopenidtoken";   X-OpenidToken
	//	Optional<String[]> arrayValues = getRequest().map(r -> r.getHeaderValues(XOPENID_HEADER_NAME));
		Optional<String[]> arrayValues = getRequest().map(r -> r.getHeaderValues("X-OpenidToken"));
	//	List<String> xopenids = (List<String>) getRequest().getHeaders("X-OpenidToken");
//		if (xopenids != null) {
//			
//		}
		
		
		List<OpenidToken> openidTokens = new ArrayList<OpenidToken>();
		if (arrayValues.isPresent()) {

			for (String b64string : arrayValues.get()) {
				try {
					String[] singleValues = b64string.split(",");
					for (String b64value : singleValues) {
						byte[] decodedBytes = Base64.getDecoder().decode(b64value);
						OpenidToken oid = objectMapper.readValue(new String(decodedBytes), OpenidToken.class);
						openidTokens.add(oid);
					}
				} catch (Exception e) {
					logger.error("Error lors du mapping du token", e);
					throw new AsipSignClientException("Tokens Openid non conformes.");
				}
			}
		}

		logger.error("openidTokens size :" + openidTokens.size());
		if (openidTokens.size()> 0) {
		logger.error("openidtoken[0] transmis reçu:");
		logger.error("\t userinfo (base64): {} ", openidTokens.get(0).getUserInfo());
		logger.error("\t accessToken: {} ", openidTokens.get(0).getAccessToken());
		logger.error("\t PSCResponse: {} ", openidTokens.get(0).getIntrospectionResponse());
		}
		return openidTokens;
	}
}
