/*
 * Copyright 2015-2020 Jaemon(answer_ljm@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jaemon.dingtalk.dinger;

import com.alibaba.fastjson.JSON;
import com.jaemon.dingtalk.DingTalkSender;
import com.jaemon.dingtalk.dinger.annatations.Keyword;
import com.jaemon.dingtalk.entity.DingTalkResult;
import com.jaemon.dingtalk.entity.enums.MsgTypeEnum;
import com.jaemon.dingtalk.entity.message.MarkDownReq;
import com.jaemon.dingtalk.entity.message.Message;
import com.jaemon.dingtalk.entity.message.TextReq;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * DingerMessageHandler
 *
 * @author Jaemon#answer_ljm@163.com
 * @version 2.0
 */
@Slf4j
public class DingerMessageHandler implements MessageTransfer, ParamHandle, ResultHandle<DingTalkResult> {
    private static final String PREFIX_TAG = "\\$\\{";
    private static final String SUFFIX_TAG = "}";
    protected static final String KEYWORD = "DINGTALK_DINGER_METHOD_SENDER_KEYWORD";
    protected static final String CONNECTOR = "_";

    protected DingTalkSender dingTalkSender;

    @Override
    public Message transfer(DingerDefinition dingerDefinition, Map<String, Object> params) {
        MsgTypeEnum msgType = dingerDefinition.msgType();
        Message message = dingerDefinition.message();
        // bugfix #2
        if (msgType == MsgTypeEnum.TEXT) {
            TextReq textReq = copyProperties(message);;
            String text = textReq.getText().getContent();
            String content = replaceContent(text, params);
            textReq.getText().setContent(content);
            return textReq;
        } else if (msgType == MsgTypeEnum.MARKDOWN) {
            MarkDownReq markDownReq = copyProperties(message);
            String text = markDownReq.getMarkdown().getText();
            String content = replaceContent(text, params);
            markDownReq.getMarkdown().setText(content);
            return markDownReq;
        } else {
            log.warn("invalid msgType {}.", msgType);
            return null;
        }
    }

    private String replaceContent(String content, Map<String, Object> params) {
        for (String k: params.keySet()) {
            Object v = params.get(k);
            String key = PREFIX_TAG + k +SUFFIX_TAG;
            if (v instanceof CharSequence
                    || v instanceof Character
                    || v instanceof Boolean
                    || v instanceof Number) {
                content = content.replaceAll(key, v.toString());
            } else {
                content = content.replaceAll(key, JSON.toJSONString(v));
            }

        }

        return content;
    }

    /**
     * copyProperties
     *
     * @param src src
     * @param <T> T extends Message
     * @return msg
     */
    private <T extends Message> T copyProperties(Message src) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            T dest = (T) in.readObject();
            return dest;
        } catch (Exception e) {
            //
            if (log.isDebugEnabled()) {
                log.debug("copy properties error:", e);
            }
            return null;
        }
    }

    @Override
    public Object resultHandle(Class<?> resultType, DingTalkResult dingTalkResult) {
        String name = resultType.getName();
        if (String.class.getName().equals(name)) {
            return Optional.ofNullable(dingTalkResult).map(e -> e.getData()).orElse(null);
        } else if (DingTalkResult.class.getName().equals(name)) {
            return dingTalkResult;
        }
        return null;
    }

    @Override
    public Map<String, Object> paramsHandle(Parameter[] parameters, Object[] values) {
        Map<String, Object> params = new HashMap<>();
        if (parameters.length == 0) {
            return params;
        }

        int keyWordIndex = -1;
        String keywordValue = null;
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String paramName = parameter.getName();
            com.jaemon.dingtalk.dinger.annatations.Parameter[] panno =
                    parameter.getDeclaredAnnotationsByType(com.jaemon.dingtalk.dinger.annatations.Parameter.class);
            Keyword[] kanno = parameter.getDeclaredAnnotationsByType(Keyword.class);
            if (panno != null && panno.length > 0) {
                paramName = panno[0].value();
            } else if (kanno != null && kanno.length > 0) {
                keyWordIndex = i;
                keywordValue = values[i].toString();
                continue;
            }
            params.put(paramName, values[i]);
        }

        if (keyWordIndex != -1) {
            params.put(KEYWORD, keywordValue);
        }

        return params;
    }
}