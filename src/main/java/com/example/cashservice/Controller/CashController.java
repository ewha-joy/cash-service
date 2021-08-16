package com.example.cashservice.Controller;
import com.example.cashservice.MongoTemplate.CashRecord;
import com.example.cashservice.MongoTemplate.WebtoonToken;
import com.example.cashservice.kafkamessagingqueue.KafkaProducer;
import com.example.cashservice.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RequestMapping("/cash-service")
@RestController
public class CashController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private KafkaProducer kafkaProducer;

/*
{
"change_amount" : "100",
"toon_name" : "",
"epi_name": "",
"epi_no": ""
"content" : "CHARGE"
}
{
    "id": {
        "timestamp": 1624290570,
        "date": "2021-06-22T00:49:30.000+09:00"
    },
    "userid": "2",
    "change_amount": 100,
    "content": "CHARGE",
    "toonname": "",
    "epiname": ""
}


 */
    // 캐시 충전
    @PostMapping(value = "/pushmongo")
    public CashRecord PushMongo(@RequestBody Map<String,String> param, HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String jwt = bearerToken.substring(7, bearerToken.length());
        String userId = tokenProvider.getUserIdFromJWT(jwt).toString();

        Long cash = Long.valueOf(Integer.parseInt(param.get("change_amount")));
        String[] ownerList= null;
        try {
            String ownerListstr = param.get("owner");
            ownerList = ownerListstr.split(",");
        }
        catch (Exception e){
            String ownerListstr = "null,null";
            ownerList = ownerListstr.split(",");
        }

        CashRecord cr = CashRecord.builder().userid(userId).change_amount(cash).content(param.get("content"))
                .toonname(param.get("toon_name")).epiname(param.get("epi_name")).epino(param.get("epi_no")).owner(ownerList).build();

        /* kafka topic insert */
        // kafkaProducer.send(cr);
        mongoTemplate.insert(cr);

        return cr;
    }
/*
[
    {
        "id": {
            "timestamp": 1624290570,
            "date": "2021-06-22T00:49:30.000+09:00"
        },
        "userid": "2",
        "change_amount": 100,
        "content": "CHARGE",
        "toonname": "",
        "epiname": ""
    },
    {
        "id": {
            "timestamp": 1624290918,
            "date": "2021-06-22T00:55:18.000+09:00"
        },
        "userid": "2",
        "change_amount": 100,
        "content": "CHARGE",
        "toonname": "",
        "epiname": ""
    },
    {
        "id": {
            "timestamp": 1624291418,
            "date": "2021-06-22T01:03:38.000+09:00"
        },
        "userid": "2",
        "change_amount": 100,
        "content": "CHARGE",
        "toonname": "",
        "epiname": ""
    }
]
 */

    // 캐시내역 조회
    @GetMapping(value = "/getmongo")
    public List<CashRecord> GetMongo(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String jwt = bearerToken.substring(7, bearerToken.length());
        String userId = tokenProvider.getUserIdFromJWT(jwt).toString();
        Criteria criteria = new Criteria("userid");
        criteria.is(userId);
        Query query = new Query(criteria);
        List<CashRecord> crs= mongoTemplate.find(query, CashRecord.class, "cashRecord");
        return crs;
    }


    // 대여권 조회
    @GetMapping(value = "/CheckRentIng")
    public List<CashRecord> CheckRentIng(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String jwt = bearerToken.substring(7, bearerToken.length());
        String userId = tokenProvider.getUserIdFromJWT(jwt).toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime now_1 = now.minusHours(24);
        Query query = Query.query(Criteria.where("userid").is(userId)
                .and("content").is("RENT_WEBTOON")
                .and("createdAt").gte(now_1).lte(now)
        );
        // userid == userId && content == RENT_WEBTOON && id.timestamp 이 24시간보다 뒤에있다.
        List<CashRecord> crs= mongoTemplate.find(query, CashRecord.class, "cashRecord");
        return crs;
    }



    // 특정 에피소드의 대여권 조회
    @GetMapping(value = "/CheckRentEpi/{epino}")
    public String CheckRentEpi(@PathVariable int epino, HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String jwt = bearerToken.substring(7, bearerToken.length());
        String userId = tokenProvider.getUserIdFromJWT(jwt).toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime now_1 = now.minusHours(24);
        String epi_no = Integer.toString(epino);
        Query query = Query.query(Criteria.where("userid").is(userId)
                .and("content").is("RENT_WEBTOON")
                .and("createdAt").gte(now_1).lte(now)
                .and("epino").is(epi_no)
        );
        // userid == userId && content == RENT_WEBTOON && id.timestamp 이 24시간보다 뒤에있다. && epino==epino
        List<CashRecord> crs= mongoTemplate.find(query, CashRecord.class, "cashRecord");
        if(crs.isEmpty()) return "false";
        return "true";
    }


///// 여기서부터 추가된 내용 !!!!!
    //webtoontoken에서 해당하는 토큰들 다 불러오기
    @GetMapping(value = "/gettoken/{epino}")
    public List<WebtoonToken> GetToken(@PathVariable int epino, HttpServletRequest request){
        String epi_no = Integer.toString(epino);
        Query query = Query.query(Criteria.where("epino").is(epi_no));
        List<WebtoonToken> wts = mongoTemplate.find(query, WebtoonToken.class, "webtoonToken");
        return wts;
    }

    //owner에 해당하는 캐시 내역 불러오기
    @PostMapping(value = "/getCashByOwner")
    public List<CashRecord> getCashByOwner(@RequestBody Map<String, String> param, HttpServletRequest request){

        String address = param.get("address");
        System.out.println(address);
        Query query = Query.query(Criteria.where("owner").regex(address,"i"));
        List<CashRecord> crs = mongoTemplate.find(query, CashRecord.class,"cashRecord");

        return crs;
    }

    //토큰 발행하고서 웹툰-토큰 push
    @PostMapping(value = "/pushwebtoontoken")
    public WebtoonToken PushToken(@RequestBody Map<String, String> param, HttpServletRequest request){
        WebtoonToken wt = WebtoonToken.builder().epino(param.get("epino")).nft(param.get("nft")).tokenId(param.get("tokenId")).build();
        mongoTemplate.insert(wt);

        return wt;
    }






}
