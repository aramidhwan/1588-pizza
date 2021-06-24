package pizza;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

 @RestController
 public class StoreController {
    @Autowired
    StoreRepository storeRepository;

    @RequestMapping(value = "/stores/chkOpenYN", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public boolean chkOpenYN(@RequestParam("regionNm") String regionNm) throws Exception {

        System.out.println("##### /store/chkOpenYn  called #####");
        boolean status = false;

        // Hystrix timeout 검증을 위한 임시 코드
        if ("종로구".equals(regionNm.trim())) {
            Thread.sleep(3000);
        }

        List<Store> storeList = storeRepository.findByRegionNmAndOpenYN(regionNm, Boolean.valueOf(true));
        // 주문이 들어온 regionNm에 Open된 Sotre가 한군데라도 있으면 true를 리턴
        if (storeList.size() > 0) {
            status = true ;
        }

        return status;
    }
 }
