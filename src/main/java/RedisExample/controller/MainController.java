package RedisExample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@RestController
@RequestMapping("/rick")
public class MainController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private final String URL_BASE = "https://rickandmortyapi.com/api/character/";

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ValueOperations<String, String> valueOp = redisTemplate.opsForValue();
            String data = valueOp.get(getKey(id.toString()));
            if(data != null && !data.isEmpty()){ //IF FOUND DATA IN REDIS BRING BACK DATA
                return new ResponseEntity<String>(data,headers,HttpStatus.OK);
            }
            ResponseEntity<String> res = restTemplate.exchange(URL_BASE.concat(id.toString()), HttpMethod.GET, null, String.class);
            if(res.getStatusCode().equals(HttpStatus.OK)){ //SAVE DATA IN REDIS FOR AFTER BRING BACK SINCE REDIS
                valueOp.set(getKey(id.toString()),res.getBody(), Duration.ofSeconds(20));
            }

            return new ResponseEntity<String>(res.getBody(),headers,HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String getKey(String id){
        return "RICK-".concat(id);
    }


}
