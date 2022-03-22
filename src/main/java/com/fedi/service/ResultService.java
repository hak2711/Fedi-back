package com.fedi.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fedi.domain.repository.AnalysisRepository;
import com.fedi.web.dto.ResultDto;
import com.mysql.cj.xdevapi.JsonParser;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ResultService {
	
	private final AnalysisRepository analysisRepository;
	
	@Transactional(readOnly = true)
	public List<ResultDto> searchGreatherThan(Double threshold){
		return analysisRepository.findGreaterThan(threshold).stream()
				.map(ResultDto::new)
				.collect(Collectors.toList());
	}
	
	public String flaskTest(MultipartFile file) throws ParseException {	
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		
		JSONParser jsonParser = new JSONParser();
		String url = "http://localhost:5000/model";
		
		HttpHeaders httpHeaders = new HttpHeaders(); //Header 생성
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("file", file.getResource());
		HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(map, httpHeaders);
		
		HttpEntity<String> response = restTemplate.postForEntity(url, request, String.class);
		System.out.println("success");
		JSONObject jsonObj = (JSONObject) jsonParser.parse(response.getBody().toString());
		System.out.println(jsonObj.toString());
		
		String eyes = (String) jsonObj.get("eyes");
		return eyes;
	}
}
