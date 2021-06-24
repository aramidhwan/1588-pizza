
package pizza.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="store", url="${api.url.book}", fallbackFactory = StoreServiceFallbackFactory.class)
public interface StoreService {

    @RequestMapping(method= RequestMethod.GET, path="/stores/chkOpenYN")
    public boolean chkOpenYN(@RequestParam("regionNm") String regionNm);
}