package com.kimo.utils;

import com.kimo.domain.CouZiCompletionRequest;
import com.kimo.domain.GouZiAdditionalMessages;

import java.util.ArrayList;

public class YouBanUtils {

    public static CouZiCompletionRequest CreateCouZiCompletionRequest(Boolean IsStream, String userId, Boolean IsHistory, String botId, ArrayList<GouZiAdditionalMessages> gouZiAdditionalMessages) {
        return CouZiCompletionRequest
                .builder()
                .stream(IsStream)
                .userId(userId)
                .chatHistory(IsHistory)
                .botId(botId)
                .AdditionalMessages(gouZiAdditionalMessages)
                .build();
    }



}
