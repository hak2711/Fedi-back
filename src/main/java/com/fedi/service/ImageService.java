package com.fedi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fedi.domain.entity.Account;
import com.fedi.domain.entity.Image;
import com.fedi.domain.entity.Tweet;
import com.fedi.domain.repository.AccountRepository;
import com.fedi.domain.repository.ImageRepository;
import com.fedi.domain.repository.TweetRepository;
import com.fedi.web.dto.ImageRequestDto;
import com.fedi.web.dto.ModelRequestDto;
import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final AccountRepository accountRepository;
    private final ImageRepository imageRepository;
    private final TweetRepository tweetRepository;
    private final S3Service s3Service;
    

    @Transactional
    public String upload(List<MultipartFile> images, ImageRequestDto requestDto) throws IllegalArgumentException, IOException {
        if(images.get(0).isEmpty()){
            throw new IllegalArgumentException("image is null");
        }

        if(requestDto.getAccountId() == null || requestDto.getAccountName() == null || requestDto.getTweetUrl() == null){
            throw new IllegalArgumentException("required parameter is null");
        }

        if(requestDto.getAccountId().isEmpty() || requestDto.getAccountName().isEmpty() || requestDto.getTweetUrl().isEmpty()){
            throw new IllegalArgumentException("required parameter is empty");
        }

        for(MultipartFile image : images){
            Account account = Account.builder()
                    .accountId(requestDto.getAccountId())
                    .accountName(requestDto.getAccountName())
                    .build();

            accountRepository.save(account);

            Tweet tweet = Tweet.builder()
                    .account(account)
                    .tweetUrl(requestDto.getTweetUrl())
                    .reportFlag(false)
                    .build();

            tweetRepository.save(tweet);

            String imageUrl = s3Service.upload(image);

            Image entity = Image.builder()
                    .tweet(tweet)
                    .imageUrl(imageUrl)
                    .build();

            imageRepository.save(entity);
        }

        return "success";
    }
    
    @Transactional(readOnly = true)
    public String getAllJsonString() throws JsonProcessingException {
    	List<Image> images = imageRepository.findAll();
    	List<ModelRequestDto> reqDtos = images.stream().map(ModelRequestDto::new).collect(Collectors.toList());
    	System.out.println("debug");
    	return new Gson().toJson(reqDtos);
    }
}
