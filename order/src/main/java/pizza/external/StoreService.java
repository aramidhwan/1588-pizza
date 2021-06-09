
package pizza.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@FeignClient(name="store", url="http://store:8080")
public interface StoreService {

    @RequestMapping(method= RequestMethod.GET, path="/stores")
    public void chkOpenYn(@RequestBody Store store);

}