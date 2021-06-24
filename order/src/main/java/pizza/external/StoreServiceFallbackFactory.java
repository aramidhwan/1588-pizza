package pizza.external;

import org.springframework.stereotype.Component;
import feign.hystrix.FallbackFactory;

@Component
public class StoreServiceFallbackFactory implements FallbackFactory<StoreService> {

    @Override
    public StoreService create(Throwable cause) { 
        return new StoreService() {
            @Override
            public boolean chkOpenYN(String regionNm) {
                System.out.println("####### StoreServiceFallbacked ########");
                    
                // HystrixTimeoutException 일 경우 
                if ( cause instanceof com.netflix.hystrix.exception.HystrixTimeoutException ) {
                    System.out.println("####### Hystrix timeout occured ########");
                // Hystrix circuit OPEN 일 경우 
                } else {
                    System.out.println("####### " + cause.getMessage());
                }

                return false;
            }
        };
    }

}
