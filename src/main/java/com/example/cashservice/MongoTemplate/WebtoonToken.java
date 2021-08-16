package com.example.cashservice.MongoTemplate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

@Getter
@Setter
@NoArgsConstructor
@Document
public class WebtoonToken {

    @Id
    private ObjectId id;
    private String epino;
    private String nft;
    private String tokenId;

    @Builder
    public WebtoonToken(String epino, String nft, String tokenId){
        this.epino = epino;
        this.nft = nft;
        this.tokenId = tokenId;

    }
}
