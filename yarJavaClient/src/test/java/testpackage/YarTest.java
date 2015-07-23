package testpackage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarclient.bean.request.Pageable;
import com.yarclient.bean.request.RequestGetAvailableFundList;
import com.yarclient.client.YarClient;

public class YarTest {

	public static final Logger logger = LoggerFactory.getLogger(YarTest.class);
	
	@Test
	public void testYarClient(){
		YarClient client = new YarClient();
		
		    
		/*Map map = new HashMap();
		map.put("bar", "barvalue");
		map.put("foo", "foovalue");*/
		//http://wangfei5.p2pbackend.firstp2plocal.com/service.php?service=PtpCfpService
		/*1.各个参数的含义
		2.外层map是否也需要传类标识
		3.sign是什么意思？平时怎么获取？
		*/
		Map mapWai = new HashMap();
	
		
		/*mapWai.put("service","\\NCFGroup\\Ptp\\services\\PtpCfp");
		mapWai.put("method","getCfpInfo");
		
		Map map = new HashMap();
		map.put("cfpId", 201070);
		map.put("userId", 201070);
		map.put("requestDatetime", "2015-06-05 12:12:12");
		map.put("proto", "\\NCFGroup\\Protos\\Ptp\\RequestUser");
		
		mapWai.put("args", map);		
		
		mapWai.put("client","fundgate_api");
		mapWai.put("sign","be3s4rtestyu4rfwt4e2d7ce9348cb27ed904951");*/
		
		

		Map<String, Object> pageParam = new HashMap<String, Object>();
		pageParam.put("pageNo", "1");
		pageParam.put("pageSize", "1");
		
		mapWai.put("service","Fund");
		mapWai.put("method","getAvailabeFundList");
		
		Map map = new HashMap();
		map.put("pageable", pageParam);	
		//map.put("proto", "\\NCFGroup\\Protos\\FundGate\\RequestGetAvailableFundList");		
		mapWai.put("args", map);				
		mapWai.put("client","fundgate_api");
		mapWai.put("sign","be3s4rtestyu4rfwt4e2d7ce9348cb27ed904951");
		
		
		
		
		
		Pageable pageable = new Pageable();
		pageable.setPageNo("1");
		pageable.setPageSize("1");
		pageable.setSort(1);		
		RequestGetAvailableFundList fundlist = new RequestGetAvailableFundList();
		//fundlist.setCondition(condition);
		fundlist.setPageable(pageable);
		fundlist.setRequestDatetime(new Date());
		
		Map mapWai2 = new HashMap();
		mapWai2.put("service","Fund");
		mapWai2.put("method","getAvailabeFundList");
		
		mapWai2.put("args", fundlist);		
		
		mapWai2.put("client","fundgate_api");
		mapWai2.put("sign","be3s4rtestyu4rfwt4e2d7ce9348cb27ed904951");
		
		String jsonStr;
		try {
			//jsonStr = client.clientRequestToJson("http://guweigang.firstp2plocal.com:8888", "hello", map,null);
			
			//jsonStr = client.clientRequestToJson("http://wangfei5.p2pbackend.firstp2plocal.com", "callByObject", mapWai,null);
			
			jsonStr = client.clientRequestToJson("http://guweigang.fundbackend.firstp2plocal.com/", "callByObject", mapWai,null);
			System.out.println(jsonStr);
			
			/*jsonStr = client.clientRequestToJson("http://guweigang.fundbackend.firstp2plocal.com/", "callByObject", mapWai2,null);
			System.out.println(jsonStr);*/
			
			jsonStr = client.clientRequestHaveObj("http://guweigang.fundbackend.firstp2plocal.com/", "callByObject", mapWai2,null);
			System.out.println(jsonStr);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	@Test
	public void testYarServer(){

		YarClient client = new YarClient();
		Map mapWai = new HashMap();	
		
		Map<String, Object> pageParam = new HashMap<String, Object>();
		pageParam.put("pageNo", "1");
		pageParam.put("pageSize", "1");
		
		mapWai.put("service","com.yarclient.bean.RequestUser");
		mapWai.put("method","req1");
		
		Map map = new HashMap();
		map.put("pageable", pageParam);	
		//map.put("proto", "\\NCFGroup\\Protos\\FundGate\\RequestGetAvailableFundList");		
		mapWai.put("args", map);				
		mapWai.put("client","fundgate_api");
		mapWai.put("sign","be3s4rtestyu4rfwt4e2d7ce9348cb27ed904951");
		
		
		
		
		
		Pageable pageable = new Pageable();
		pageable.setPageNo("1");
		pageable.setPageSize("1");
		pageable.setSort(1);		
		RequestGetAvailableFundList fundlist = new RequestGetAvailableFundList();
		//fundlist.setCondition(condition);
		fundlist.setPageable(pageable);
		fundlist.setRequestDatetime(new Date());
		
		Map mapWai2 = new HashMap();
		mapWai2.put("service","com.yarclient.bean.RequestUser");
		mapWai2.put("method","req3");
		
		mapWai2.put("args", fundlist);		
		
		mapWai2.put("client","fundgate_api");
		mapWai2.put("sign","be3s4rtestyu4rfwt4e2d7ce9348cb27ed904951");
		
		String jsonStr;
		try {
			//jsonStr = client.clientRequestToJson("http://guweigang.firstp2plocal.com:8888", "hello", map,null);
			
			//jsonStr = client.clientRequestToJson("http://wangfei5.p2pbackend.firstp2plocal.com", "callByObject", mapWai,null);
			
			jsonStr = client.clientRequestToJson("http://127.0.0.1:8080/yarServer/requestServer", "callByObject", mapWai,null);
			System.out.println(jsonStr);
			
			/*jsonStr = client.clientRequestToJson("http://guweigang.fundbackend.firstp2plocal.com/", "callByObject", mapWai2,null);
			System.out.println(jsonStr);*/
			
			jsonStr = client.clientRequestHaveObj("http://127.0.0.1:8080/yarServer/requestServer", "callByObject", mapWai2,null);
			System.out.println(jsonStr);
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
}
