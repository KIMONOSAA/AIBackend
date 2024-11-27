package com.kimo.utils;

import com.kimo.domain.CouZiAdditionalFileMessage;
import com.kimo.domain.CouZiCompletionFileRequest;
import com.kimo.domain.CouZiCompletionRequest;
import com.kimo.domain.GouZiAdditionalMessages;
import com.kimo.model.dto.po.AIMasterData;
import com.kimo.session.CoZeConfiguration;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.kimo.constans.CouZiConstant.COU_ZI_ZPI_HOST;
import static com.kimo.constants.CouZiConstant.BEARER;

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

    public static CouZiCompletionFileRequest CreateCouZiCompletionFileRequest(Boolean IsStream, String userId, Boolean IsHistory, String botId, ArrayList<CouZiAdditionalFileMessage> gouZiAdditionalMessages) {
        return CouZiCompletionFileRequest
                .builder()
                .stream(IsStream)
                .userId(userId)
                .chatHistory(IsHistory)
                .botId(botId)
                .AdditionalMessages(gouZiAdditionalMessages)
                .build();
    }


    public static @NotNull GouZiAdditionalMessages createGouZiAdditionalMessages(AIMasterData chartData) {
        GouZiAdditionalMessages goZeAdditionalMessages = new GouZiAdditionalMessages();
        goZeAdditionalMessages.setContent(chartData.getUserBody());
        goZeAdditionalMessages.setRole("user");
        goZeAdditionalMessages.setContent_type("text");
        return goZeAdditionalMessages;
    }


    public static void configExtracted(String token, CoZeConfiguration yuanQiConfiguration) {
        yuanQiConfiguration.setApiHost(COU_ZI_ZPI_HOST);
        yuanQiConfiguration.setApiKey(BEARER + token);
        yuanQiConfiguration.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    }






}
