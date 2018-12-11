package com.example.spring_is.common.config.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.AuthoritiesExtractor;
import org.springframework.boot.autoconfigure.security.oauth2.resource.FixedAuthoritiesExtractor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.RestTemplate;

import com.example.spring_is.common.model.UserInfo;

public class AppUserInfoTokenServices implements ResourceServerTokenServices
{

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * The origin of the "sub" key is WSO2 IS 5.1.0 integration.
	 */
	private static final String[] PRINCIPAL_KEYS = new String[]
	{ "user", "username", "userid", "user_id", "login", "id", "name", "sub" };

	private final String userInfoEndpointUrl;

	private final String clientId;

	private RestTemplate restTemplate;

	private String tokenType = DefaultOAuth2AccessToken.BEARER_TYPE;

	private AuthoritiesExtractor authoritiesExtractor = new FixedAuthoritiesExtractor();

	public AppUserInfoTokenServices(String userInfoEndpointUrl, String clientId)
	{
		this.logger.info("the userInfoEndpointUrl is {} and clientId {}", userInfoEndpointUrl,clientId);
		this.userInfoEndpointUrl = userInfoEndpointUrl;
		this.clientId = clientId;
	}

	@Override
	public OAuth2Authentication loadAuthentication(String accessToken)
			throws AuthenticationException, InvalidTokenException
	{
		Map<String, Object> map = getMap(this.userInfoEndpointUrl, accessToken);
		if (map.containsKey("error"))
		{
			this.logger.info("userinfo returned error: " + map.get("error"));
			throw new InvalidTokenException(accessToken);
		}
		return extractAuthentication(map);
	}

	private OAuth2Authentication extractAuthentication(Map<String, Object> map)
	{
		Object principal = getPrincipal(map);
		List<GrantedAuthority> authorities = this.authoritiesExtractor.extractAuthorities(map);
		Set<String> set = new HashSet<String>();
		set.add("openid");
		OAuth2Request request = new OAuth2Request(null, this.clientId, authorities, true,set , null, null, null, null);
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, "N/A",authorities);
		token.setDetails(map);
		return new OAuth2Authentication(request, token);
	}

	/**
	 * Return the principal that should be used for the token. The default
	 * implementation looks for well know {@code user*} keys in the map.
	 * 
	 * @param map
	 *            the source map
	 * @return the principal or {@literal "unknown"}
	 */
	protected Object getPrincipal(Map<String, Object> map)
	{
		for (String key : PRINCIPAL_KEYS)
		{
			if (map.containsKey(key))
			{
				return map.get(key);
			}
		}
		return "unknown";
	}

	@Override
	public OAuth2AccessToken readAccessToken(String accessToken)
	{
		throw new UnsupportedOperationException("Not supported: read access token");
	}
	
	
	private Map<String, Object> getMap(String path, String accessToken)
	{
		this.logger.info("Getting user info from {} with access token {}", path, accessToken);
		try
		{
			RestTemplate restTemplate = this.restTemplate;
			if (restTemplate == null)
			{
				restTemplate = new RestTemplate();
			}
			DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
			token.setTokenType(this.tokenType);
			
			
			HttpHeaders headers = new HttpHeaders();
			headers.setBearerAuth(accessToken);
			headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
			headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
			HttpEntity<String> entity = new HttpEntity<String>(null,headers);
			logger.info("==========================================================");
			logger.info("USER INFO FETCH, using NEW access token: {}", accessToken);
			logger.info("==========================================================");
			UserInfo info = restTemplate.exchange(path, HttpMethod.GET, entity, UserInfo.class).getBody();
			
			Map<String, Object> map = new HashMap<>();
			
			map.put("username",info.getUserName());
			map.put("id",info.getId());
			List<String> authorityList = new ArrayList<>();
			info.getGroups().forEach(data->{
				authorityList.add(data.getDisplay());
			});
			map.put("authorities",authorityList);
			return map;
		} catch (Exception ex)
		{
			ex.printStackTrace();
			this.logger.info("Could not fetch user details: " + ex.getClass() + ", " + ex.getMessage());
			return Collections.<String, Object> singletonMap("error", "Could not fetch user details");
		}
	}

}