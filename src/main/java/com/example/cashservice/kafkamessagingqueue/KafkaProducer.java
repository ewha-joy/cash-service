package com.example.cashservice.kafkamessagingqueue;
import com.example.cashservice.MongoTemplate.CashRecord;
import com.fasterxml.jackson.databind.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class KafkaProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, String> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }


    public CashRecord send(CashRecord cashRecord){
        try {
            log.info(cashRecord.toString());
            kafkaTemplate.send("test_topic_2", cashRecord.toString());
            return cashRecord;
        } catch (Exception ex) {
                ex.printStackTrace();
        }
        return null;
    }

}
