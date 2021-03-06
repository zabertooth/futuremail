package futuremail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Created by Mike on 3/11/2017.
 */
@RestController
public class FuturemailController {
    @Autowired
    MongoTemplate mongoTemplate;

    @PostMapping(value = "/request", consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String requestFuturemail(@RequestBody FuturemailMessage futuremailMessage) {
        if (!futuremailMessage.isValid())
            return "invalid";

        futuremailMessage.setStatus(FuturemailMessageStatus.NEW);
        mongoTemplate.insert(futuremailMessage);

        return "success";
    }

    @PutMapping(value = "/cancel/{id}")
    public @ResponseBody String cancelFuturemail(@PathVariable("id") String id) {
        FuturemailMessage futuremailMessage = mongoTemplate.findOne(
                new Query(Criteria.where("id").is(id)),
                FuturemailMessage.class
        );

        if (futuremailMessage == null)
            return "no such message";

        if (futuremailMessage.getStatus() != FuturemailMessageStatus.NEW)
            return "message has already been handled";

        futuremailMessage.setStatus(FuturemailMessageStatus.CANCELLED);

        mongoTemplate.updateFirst(
                new Query(Criteria.where("id").is(futuremailMessage.getId())),
                new Update().set("status", futuremailMessage.getStatus()),
                FuturemailMessage.class
        );

        return "success";
    }

    @GetMapping(value = {"/futuremails", "/futuremails/{status}"})
    public @ResponseBody List<FuturemailMessage> getFuturemails(@PathVariable("status") Optional<String> status) {
        if (status.isPresent()) {
            return mongoTemplate.find(
                    new Query(Criteria.where("status").is(status.get().toUpperCase())),
                    FuturemailMessage.class
            );
        }
        else {
            return mongoTemplate.findAll(FuturemailMessage.class);
        }
    }
}
