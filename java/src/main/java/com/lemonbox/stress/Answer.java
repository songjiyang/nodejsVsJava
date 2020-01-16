package com.lemonbox.stress;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;


@Document(collection = "answers")
public class Answer {

    @Id
    private String _id;

    @Field("user_id")
    @JsonProperty("user_id")
    private String userId;

    @Field("suggestion_id")
    @JsonProperty("suggestion_id")
    private String suggestionId;

    @Field("openid")
    @JsonProperty("openid")
    private String openId;
}
