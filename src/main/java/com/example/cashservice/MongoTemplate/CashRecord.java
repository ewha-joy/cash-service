package com.example.cashservice.MongoTemplate;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document
public class CashRecord {

    @Id
    @JsonFormat(timezone = "Asia/Seoul")
    private ObjectId id;

    @JsonFormat(timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    private String userid;
    private long change_amount;
    private String content;
    private String toonname;
    private String epiname;
    private String epino;
    private String[] owner;


    @Builder
    public CashRecord(String userid,long change_amount, String content,String toonname,String epiname, String epino, String[] owner ){
        this.id = new ObjectId();
        this.userid = userid;
        this.change_amount = change_amount;
        this.content = content;
        this.toonname = toonname;
        this.epiname = epiname;
        this.epino = epino;
        this.createdAt = LocalDateTime.now();
        this.owner = owner;
    }

    @Override
    public String toString(){
        return  "{" +
                "\"_id\":{\"$oid\":\""+id.toString()+"\"},"+
                "\"userid\":\""+userid+"\","+
                "\"change_amount\":{\"$numberLong\":\""+change_amount+"\"},"+
                "\"content\":\""+content +"\","+
                "\"createdAt\":{\"$date\":"+Timestamp.valueOf(createdAt).getTime()+"},"+
                "\"toonname\":\""+toonname+"\","+
                "\"epiname\":\""+epiname+"\","+
                "\"epino\":\""+epino+"\","+
                "\"owner\":\""+owner+"\","+
                "\"_class\":\"com.example.cashservice.MongoTemplate.CashRecord\"" +
                "}";
    }
    // owner가 제대로 저장X
}
